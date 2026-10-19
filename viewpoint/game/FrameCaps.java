package viewpoint.game;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Properties;
import java.util.function.IntSupplier;
import org.lwjglx.opengl.Display;
import org.lwjglx.opengl.DisplayMode;
import viewpoint.platform.BuildPin;
import zombie.GameWindow;
import zombie.ZomboidFileSystem;
import zombie.core.PerformanceSettings;
import zombie.gameStates.GameLoadingState;
import zombie.gameStates.GameState;

public final class FrameCaps {
   public static final int UNCAPPED = 0;
   public static final int FOLLOW_GAME = -1;
   public static final int LEAST = 24;
   public static final int MOST = 540;
   public static final int GAME_MOST = 244;
   public static final int MENU_DEFAULT = 60;
   static final String VERSION = "2";
   public static final boolean ON = BuildPin.supported();
   static final int UNKNOWN = Integer.MIN_VALUE;
   private static final String FILE = "viewpoint-frame-caps.properties";
   private static final String OPTIONS = "options.ini";
   private static volatile FrameCaps.Caps caps = new FrameCaps.Caps(Integer.MIN_VALUE, 60, false);
   private static volatile boolean writingOptions;
   private static int saved = Integer.MIN_VALUE;
   private static File folder;
   private static IntSupplier refresh = FrameCaps::monitorRefresh;

   private FrameCaps() {
   }

   public static int lockNow(int var0) {
      int var1 = showing();
      return var1 > 0 ? var1 : var0;
   }

   public static boolean uncappedNow(boolean var0) {
      int var1 = showing();
      return var1 == Integer.MIN_VALUE ? var0 : var1 == 0;
   }

   private static int showing() {
      FrameCaps.Caps var0 = caps;
      return !writingOptions && !playing() && var0.menu() != -1 ? var0.menu() : var0.game();
   }

   private static boolean playing() {
      GameState var0 = GameWindow.states.current;
      return var0 instanceof GameLoadingState || GameWindow.isIngameState();
   }

   public static int gameCap() {
      return caps.game();
   }

   public static int menuCap() {
      return caps.menu();
   }

   public static void setGameCap(int var0) {
      int var1 = var0 <= 0 ? 0 : within(var0);
      caps = new FrameCaps.Caps(var1, caps.menu(), true);
      tellGame(var1);
      write();
   }

   public static void setMenuCap(int var0) {
      int var1 = var0 < 0 ? -1 : (var0 == 0 ? 0 : within(var0));
      caps = new FrameCaps.Caps(caps.game(), var1, caps.gameChosen());
      write();
   }

   private static int within(int var0) {
      return Math.max(24, Math.min(540, var0));
   }

   private static void tellGame(int var0) {
      PerformanceSettings.instance.setFramerateUncapped(var0 == 0);
      if (var0 > 0) {
         PerformanceSettings.setLockFPS(Math.min(var0, 244));
      }
   }

   public static void startup() {
      optionsLoading();
      optionsLoaded();
   }

   public static void optionsLoading() {
      Properties var0 = read(new File(folder(), "options.ini"));
      String var1 = var0.getProperty("frameRate");
      String var2 = var0.getProperty("uncappedFPS");
      if (Boolean.parseBoolean(var2 == null ? "" : var2.trim())) {
         saved = 0;
      } else if (var1 != null) {
         saved = number(var1, Integer.MIN_VALUE);
      }
   }

   public static void optionsLoaded() {
      Properties var0 = read(file());
      boolean var1 = "2".equals(var0.getProperty("version")) && var0.getProperty("game") != null;
      int var2 = refresh.getAsInt();
      int var3 = var2 > 0 ? within(var2) : (saved == Integer.MIN_VALUE ? Integer.MIN_VALUE : (saved <= 0 ? 0 : within(saved)));
      int var4 = var1 ? cap(var0.getProperty("game"), var3) : var3;
      int var5 = cap(var0.getProperty("menu"), 60);
      caps = new FrameCaps.Caps(var4, var5, var1);
      if (var4 != Integer.MIN_VALUE) {
         tellGame(var4);
      }
   }

   public static void optionsSaving() {
      writingOptions = true;
   }

   public static void optionsSaved() {
      writingOptions = false;
      write();
   }

   private static int cap(String var0, int var1) {
      if (var0 == null) {
         return var1;
      }

      return switch (var0.trim()) {
         case "uncapped" -> 0;
         case "game" -> -1;
         default -> {
            int var4 = number(var0, Integer.MIN_VALUE);
            yield var4 == Integer.MIN_VALUE ? var1 : within(var4);
         }
      };
   }

   private static String text(int var0) {
      return var0 == 0 ? "uncapped" : (var0 == -1 ? "game" : Integer.toString(var0));
   }

   private static void write() {
      FrameCaps.Caps var0 = caps;
      Properties var1 = new Properties();
      var1.setProperty("version", "2");
      if (var0.gameChosen()) {
         var1.setProperty("game", text(var0.game()));
      }

      var1.setProperty("menu", text(var0.menu()));

      try (BufferedWriter var2 = Files.newBufferedWriter(file().toPath(), StandardCharsets.UTF_8)) {
         var1.store(
            var2,
            "Viewpoint's frame rate caps (the game's Display options): a number, uncapped, or for the menus game, to follow the game's; no game line: the monitor's refresh rate"
         );
      } catch (IOException var7) {
         System.out.println("[Viewpoint] frame caps not saved to " + file() + ": " + var7);
      }
   }

   private static Properties read(File var0) {
      Properties var1 = new Properties();
      if (var0.isFile()) {
         try (BufferedReader var2 = Files.newBufferedReader(var0.toPath(), StandardCharsets.ISO_8859_1)) {
            var1.load(var2);
         } catch (IOException | IllegalArgumentException var7) {
            System.out.println("[Viewpoint] frame caps: " + var0 + " not read: " + var7);
         }
      }

      return var1;
   }

   private static int number(String var0, int var1) {
      try {
         return Integer.parseInt(var0.trim());
      } catch (NumberFormatException var3) {
         System.out.println("[Viewpoint] frame caps: " + var0 + " is no frame rate");
         return var1;
      }
   }

   public static int refreshRate() {
      return refresh.getAsInt();
   }

   private static int monitorRefresh() {
      DisplayMode var0 = Display.getDesktopDisplayMode();
      return var0 == null ? 0 : Math.max(0, var0.getFrequency());
   }

   private static File file() {
      return new File(folder(), "viewpoint-frame-caps.properties");
   }

   private static File folder() {
      return folder != null ? folder : new File(ZomboidFileSystem.instance.getCacheDir());
   }

   static void reset(File var0, int var1) {
      caps = new FrameCaps.Caps(Integer.MIN_VALUE, 60, false);
      writingOptions = false;
      saved = Integer.MIN_VALUE;
      folder = var0;
      refresh = () -> var1;
   }

   private record Caps(int game, int menu, boolean gameChosen) {
   }
}
