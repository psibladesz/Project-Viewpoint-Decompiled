package viewpoint.packs;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import viewpoint.platform.Graphics;
import viewpoint.platform.LiveSettings;
import viewpoint.render.PackModel;
import viewpoint.render.PackModels;
import zombie.ZomboidFileSystem.PZFolder;
import zombie.gameStates.ChooseGameInfo;
import zombie.gameStates.ChooseGameInfo.Mod;
import zombie.iso.sprite.IsoSprite;

public final class ModelPacks {
   private static final String MEDIA = "media/";
   private static final int ERRORS_SHOWN = 5;
   private static final String LODS = "World/Levels of detail";
   private static final String SECTION = "World/Model packs";
   private static final LiveSettings.Number RING = LiveSettings.number(
      "lod.packRing", "Model packs' models within (chunks)", "World/Levels of detail", 0.0F, 16.0F, 1.0F, 6.0F
   );
   private static final ArrayList<ModelPacks.Pack> packs = new ArrayList<>();
   private static final HashSet<String> seen = new HashSet<>();
   private static final HashMap<String, LiveSettings.Toggle> switches = new HashMap<>();
   private static HashMap<String, PackBind> bound = new HashMap<>();
   private static int generation;

   public static boolean register(String var0, String var1) {
      if (var0 != null && var1 != null) {
         if (!seen.add(var0 + "\n" + var1)) {
            return registered(var0, var1);
         }

         File var2 = find(var0, var1);
         if (var2 == null) {
            System.out.println("[Viewpoint] model pack " + var0 + ": no " + var1 + " in its folders");
            return false;
         }

         List var3;
         try {
            var3 = Files.readAllLines(var2.toPath(), StandardCharsets.UTF_8);
         } catch (IOException var5) {
            System.out.println("[Viewpoint] model pack " + var0 + ": " + var1 + " unreadable: " + var5);
            return false;
         }

         switches.computeIfAbsent(var0, var0x -> LiveSettings.toggle("packs." + var0x, label(var0x), "World/Model packs", true));
         add(read(var0, var1, var2.getParentFile(), PackManifest.parse(var3)));
         PackModels.publish();
         return true;
      } else {
         return false;
      }
   }

   public static void frame() {
      boolean var0 = false;

      for (ModelPacks.Pack var2 : packs) {
         LiveSettings.Toggle var3 = switches.get(var2.modId);
         boolean var4 = var3 == null || var3.get();
         var0 |= var4 != var2.on;
         var2.on = var4;
      }

      if (var0) {
         rebind();
      }
   }

   public static int ring() {
      return Graphics.MODEL_PACKS.get() && RING.getInt() > 0 && !bound.isEmpty() ? RING.getInt() : -1;
   }

   public static PackBind bind(IsoSprite var0) {
      return var0.name == null ? null : bound.get(var0.name);
   }

   public static PackBind bind(String var0) {
      return bound.get(var0);
   }

   public static int generation() {
      return generation;
   }

   public static boolean any() {
      return !bound.isEmpty();
   }

   static ModelPacks.Pack read(String var0, String var1, File var2, PackManifest var3) {
      HashMap var4 = new HashMap();

      for (Entry var6 : var3.models.entrySet()) {
         File var7 = new File(var2, (String)var6.getValue()).toPath().toAbsolutePath().normalize().toFile();
         var4.put((String)var6.getKey(), PackModels.of(var7));
      }

      LinkedHashMap var9 = new LinkedHashMap();

      for (Entry var12 : var3.binds.entrySet()) {
         PackManifest.Bind var8 = (PackManifest.Bind)var12.getValue();
         var9.put((String)var12.getKey(), new PackBind((PackModel)var4.get(var8.model()), var8.degrees(), var8.x(), var8.y(), var8.z(), var8.scale()));
      }

      String var11 = var3.id != null ? var3.id : var0 + ":" + var1;
      System.out
         .println(
            "[Viewpoint] model pack "
               + var11
               + " ("
               + var0
               + ", "
               + var1
               + "): "
               + var4.size()
               + " models, "
               + var9.size()
               + " sprites"
               + (var3.errors.isEmpty() ? "" : ", " + var3.errors.size() + " lines left out")
         );

      for (int var13 = 0; var13 < Math.min(5, var3.errors.size()); var13++) {
         System.out.println("[Viewpoint] model pack " + var11 + ": " + var3.errors.get(var13));
      }

      return new ModelPacks.Pack(var0, var1, var11, var3.name != null ? var3.name : var11, var9);
   }

   static void add(ModelPacks.Pack var0) {
      packs.add(var0);
      rebind();
   }

   static void rebind() {
      HashMap var0 = new HashMap();

      for (ModelPacks.Pack var2 : packs) {
         if (var2.on) {
            var0.putAll(var2.binds);
         }
      }

      bound = var0;
      generation++;
   }

   static void clear() {
      packs.clear();
      seen.clear();
      rebind();
   }

   private static boolean registered(String var0, String var1) {
      for (ModelPacks.Pack var3 : packs) {
         if (var3.modId.equals(var0) && var3.manifest.equals(var1)) {
            return true;
         }
      }

      return false;
   }

   private static String label(String var0) {
      Mod var1 = ChooseGameInfo.getModDetails(var0);
      return var1 != null && var1.getName() != null ? var1.getName() : var0;
   }

   private static File find(String var0, String var1) {
      Mod var2 = ChooseGameInfo.getModDetails(var0);
      if (var2 == null) {
         return null;
      }

      String var3 = var1.replace('\\', '/');
      var3 = var3.startsWith("media/") ? var3.substring("media/".length()) : var3;

      for (PZFolder var7 : new PZFolder[]{var2.mediaFile.version, var2.mediaFile.common}) {
         File var8 = var7 == null ? null : var7.canonicalFile;
         File var9 = var8 == null ? null : new File(var8, var3);
         if (var9 != null && var9.isFile()) {
            return var9;
         }
      }

      return null;
   }

   private ModelPacks() {
   }

   static {
      RING.describe("How many chunks round you draw the model packs' authored models; further out, the sprites' art. 0 draws none.");
   }

   static final class Pack {
      final String modId;
      final String manifest;
      final String id;
      final String name;
      final Map<String, PackBind> binds;
      boolean on = true;

      Pack(String var1, String var2, String var3, String var4, Map<String, PackBind> var5) {
         this.modId = var1;
         this.manifest = var2;
         this.id = var3;
         this.name = var4;
         this.binds = var5;
      }
   }
}
