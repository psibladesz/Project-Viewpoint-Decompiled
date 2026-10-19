package viewpoint.platform;

import imgui.ImGui;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BooleanSupplier;
import viewpoint.iris.IrisPack;
import zombie.ZomboidFileSystem;

public final class Graphics {
   static final String TAB = "Graphics";
   static final String GENERAL = "Graphics/General";
   static final String SHADERS = "Graphics/Shaders";
   static final String OPTIONS = "Graphics/Advanced";
   public static final LiveSettings.Toggle LOW_LATENCY = LiveSettings.toggle("graphics.lowLatency", "Low latency", "Graphics/General", false);
   public static final LiveSettings.Toggle FRAME_GENERATION = LiveSettings.toggle("graphics.frameGeneration", "Frame generation", "Graphics/General", false);
   public static final LiveSettings.Toggle MODEL_PACKS = LiveSettings.toggle("graphics.modelPacks", "3D model packs", "Graphics/General", true);
   private static final String[] MODES = new String[]{"Vanilla", "Modern"};
   private static final String[] FORMER_MODES = new String[]{"Normal", "Pretty"};
   private static final int VANILLA = 0;
   private static final int MODERN = 1;
   private static final String FOLDER_NOTE = "README.txt";
   private static final String FOLDER_TEXT = "Shader packs for Viewpoint go here, then choose them in Viewpoint's settings (Delete), Graphics, Modern:\r\n- a Viewpoint pack: a folder holding its pack.properties;\r\n- a Minecraft shader pack: its .zip as downloaded, or the folder holding its shaders folder.\r\nA Minecraft pack dropped here while the game runs shows after Rescan, in the same tab; a Viewpoint pack after the game starts again.\r\n";
   private static volatile LiveSettings.Choice mode;
   private static volatile LiveSettings.Choice pack;
   private static volatile List<ShaderPack> modern = List.of();
   private static volatile List<IrisPack> iris = List.of();
   private static final Map<String, Map<String, LiveSettings.Setting>> options = new HashMap<>();
   private static boolean started;
   private static boolean replaced;
   private static volatile String leftOut = "";

   static void start() {
      if (!started) {
         File var0 = new File(ZomboidFileSystem.instance.getCacheDir(), "viewpoint-shaderpacks");
         made(var0);
         start(var0);
      }
   }

   private static void made(File var0) {
      if (!var0.isDirectory()) {
         try {
            Files.createDirectories(var0.toPath());
            Files.writeString(
               var0.toPath().resolve("README.txt"),
               "Shader packs for Viewpoint go here, then choose them in Viewpoint's settings (Delete), Graphics, Modern:\r\n- a Viewpoint pack: a folder holding its pack.properties;\r\n- a Minecraft shader pack: its .zip as downloaded, or the folder holding its shaders folder.\r\nA Minecraft pack dropped here while the game runs shows after Rescan, in the same tab; a Viewpoint pack after the game starts again.\r\n",
               StandardCharsets.UTF_8
            );
            System.out.println("[Viewpoint] shader packs: made " + var0 + " for the player's packs");
         } catch (IOException | RuntimeException var2) {
            System.out.println("[Viewpoint] shader packs: " + var0 + " could not be made: " + var2);
         }
      }
   }

   static void start(File var0) {
      started = true;
      ShaderPacks.discover(var0);
      List<ShaderPack> var1 = ShaderPacks.all();
      modern = var1.stream().filter(var0x -> !var0x.id.equals("normal") && !var0x.id.equals("library")).toList();
      GraphicsPresets.start();
      mode = LiveSettings.choice("graphics.mode", "Mode", "Graphics/General", MODES, FORMER_MODES, 1);
      mode.describe("Vanilla: the plain look, quick on any GPU. Modern: the shader pack below.");
      iris = IrisSettings.start(var0, () -> irisInUse() != null);
      declarePack();

      for (ShaderPack var3 : var1) {
         LinkedHashMap var4 = new LinkedHashMap();
         BooleanSupplier var5 = () -> inUse() == var3 && irisInUse() == null;
         PackPresets.declare(var3, "Graphics/Advanced", var5);

         for (PackOption var7 : var3.options) {
            LiveSettings.Setting var8 = var7.declare(key(var3, var7), "Graphics/Advanced");
            var8.describe(var7.tooltip);
            var8.shownWhen(var5);
            var4.put(var7.id, var8);
         }

         for (PackOption var12 : var3.options) {
            if (!var12.disabledBy.isEmpty()) {
               PackOption var13 = var3.option(var12.disabledBy);
               LiveSettings.Setting var9 = (LiveSettings.Setting)var4.get(var13.id);
               ((LiveSettings.Setting)var4.get(var12.id)).enabledWhen(() -> !var13.on(var9.text()));
            }
         }

         options.put(var3.id, var4);
      }

      StringBuilder var10 = new StringBuilder();
      ShaderPacks.leftOut().forEach((var1x, var2) -> var10.append(var1x).append(": ").append(var2).append('\n'));
      IrisPacks.leftOut().forEach((var1x, var2) -> var10.append(var1x).append(": ").append(var2).append('\n'));
      leftOut = var10.toString().strip();
      SettingsWindow.panel("Graphics/Shaders", Graphics::panel);
   }

   private static void declarePack() {
      ArrayList<String> var0 = new ArrayList<>(modern.stream().map(var0x -> var0x.id).toList());
      var0.addAll(iris.stream().map(var0x -> var0x.id).toList());
      if (pack != null) {
         LiveSettings.remove(pack);
      }

      LiveSettings.Choice var1 = LiveSettings.choice("graphics.pack", "Shader pack", "Graphics/Shaders", var0.toArray(new String[0]), var0.indexOf("default"));
      var1.describe(
         "The built-in packs, and those in Zomboid/viewpoint-shaderpacks: Viewpoint's (a folder with a pack.properties) and shader packs made for Minecraft (minecraft-)."
      );
      var1.shownWhen(() -> mode.get() == 1);
      pack = var1;
   }

   static void update() {
      if (started && IrisSettings.update(irisInUse())) {
         String var0 = pack.text();
         iris = IrisPacks.all();
         declarePack();
         LiveSettings.put(pack.key, var0);
      }
   }

   static String key(ShaderPack var0, PackOption var1) {
      return "pack." + var0.id + "." + var1.id;
   }

   static ShaderPack inUse() {
      if (mode.get() == 0) {
         return ShaderPacks.find("normal");
      } else {
         return pack.get() < modern.size() ? modern.get(pack.get()) : ShaderPacks.find("library");
      }
   }

   static IrisPack irisInUse() {
      int var0 = pack.get() - modern.size();
      return mode.get() == 1 && var0 >= 0 && var0 < iris.size() ? iris.get(var0) : null;
   }

   static boolean changed() {
      boolean var0 = !replaced && replaceRemoved();
      replaced = true;
      var0 |= GraphicsPresets.follow();
      var0 |= GameTextures.follow();
      ShaderPack var1 = inUse();
      Map var2 = options.get(var1.id);
      var0 |= PackPresets.follow(var1, var2);
      ShaderPacks.use(var1, var1x -> ((LiveSettings.Setting)var2.get(var1x.id)).text());
      return var0 | IrisSettings.follow(irisInUse());
   }

   static boolean replaceRemoved() {
      String var0 = LiveSettings.saved(pack.key);
      boolean var1 = false;

      for (Entry var3 : ShaderPacks.REMOVED.entrySet()) {
         String var4 = (String)var3.getKey();
         if (ShaderPacks.find(var4) == null) {
            var1 |= LiveSettings.forget(var1x -> var1x.startsWith("pack." + var4 + ".") || var1x.equals("packPreset." + var4));
            if (var0 != null && var0.trim().equalsIgnoreCase(var4)) {
               pack.set(modern.indexOf(ShaderPacks.find((String)var3.getValue())));
               System.out.println("[Viewpoint] shader pack " + var4 + " is no longer built in: " + (String)var3.getValue() + " is used instead");
               var1 = true;
            }
         }
      }

      return var1;
   }

   private static void panel() {
      IrisPack var0 = irisInUse();
      if (var0 != null) {
         irisPanel(var0);
      } else {
         ShaderPacks.Active var1 = ShaderPacks.active();
         ShaderPacks.Failure var2 = ShaderPacks.failure();
         ShaderPack var3 = var1.pack;
         ImGui.textWrapped((var3.name + (var3.author.isEmpty() ? "" : ", by " + var3.author)).replace("%", "%%"));
         if (var2 != null && var2.generation() == var1.generation) {
            ImGui.textColored(1.0F, 0.45F, 0.35F, 1.0F, var3.name + " did not compile; drawn with " + ShaderPacks.drawn().pack.name + " meanwhile:");
            ImGui.textWrapped(var2.log());
         }

         if (!leftOut.isEmpty()) {
            ImGui.textColored(1.0F, 0.75F, 0.35F, 1.0F, "Left out:");
            ImGui.textWrapped(leftOut);
         }
      }
   }

   private static void irisPanel(IrisPack var0) {
      ImGui.textWrapped(var0.id.substring("minecraft-".length()).replace("%", "%%"));
      IrisPacks.Failure var1 = IrisPacks.failure();
      IrisPacks.Active var2 = IrisPacks.drawn();
      if (var1 != null && var1.packId().equals(var0.id)) {
         ImGui.textColored(1.0F, 0.45F, 0.35F, 1.0F, "It cannot be drawn; Viewpoint's default pack draws meanwhile:");
         ImGui.textWrapped(var1.why());
      } else if (var2 == null || var2.pack() != var0) {
         ImGui.textDisabled("Linking: a few seconds.");
      }

      if (!leftOut.isEmpty()) {
         ImGui.textColored(1.0F, 0.75F, 0.35F, 1.0F, "Left out:");
         ImGui.textWrapped(leftOut);
      }
   }

   private Graphics() {
   }
}
