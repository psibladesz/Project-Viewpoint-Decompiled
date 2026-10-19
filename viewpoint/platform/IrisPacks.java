package viewpoint.platform;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLCapabilities;
import viewpoint.iris.IrisMacros;
import viewpoint.iris.IrisPack;

public final class IrisPacks {
   private static final Map<String, IrisPack> packs = new LinkedHashMap<>();
   private static final Map<String, String> broken = new LinkedHashMap<>();
   private static volatile IrisPacks.Active active;
   private static volatile IrisPacks.Active drawn;
   private static volatile IrisPacks.Failure failure;
   private static int generation;
   private static Map<String, String> standard;
   private static Set<String> features;

   static boolean discover(File var0) {
      LinkedHashMap<String, IrisPack> var1 = new LinkedHashMap<>(packs);
      packs.clear();
      broken.clear();
      File[] var2 = var0 == null ? null : var0.listFiles();
      File[] var3 = var2 == null ? new File[0] : var2;
      Arrays.sort(var3);

      for (File var7 : var3) {
         boolean var8 = var7.isFile() && var7.getName().toLowerCase(Locale.ROOT).endsWith(".zip");
         if (var8 || new File(var7, "shaders").isDirectory()) {
            IrisPack var9 = var1.values().stream().filter(var1x -> var1x.file.equals(var7)).findFirst().orElse(null);
            if (var9 != null) {
               packs.put(var9.id, var9);
            } else {
               try {
                  IrisPack var10 = IrisPack.load(var7, discoveryMacros());
                  packs.put(var10.id, var10);
               } catch (IOException | RuntimeException var11) {
                  broken.put(var7.getName(), var11.getMessage());
                  System.out.println("[Viewpoint] Minecraft shader pack " + var7.getName() + " left out: " + var11.getMessage());
               }
            }
         }
      }

      if (!packs.isEmpty()) {
         System.out.println("[Viewpoint] Minecraft shader packs: " + String.join(", ", packs.keySet()));
      }

      return !var1.keySet().equals(packs.keySet());
   }

   static List<IrisPack> all() {
      return new ArrayList<>(packs.values());
   }

   static IrisPack find(String var0) {
      return packs.get(var0);
   }

   static Map<String, String> leftOut() {
      return new LinkedHashMap<>(broken);
   }

   public static void use(IrisPack var0, Map<String, String> var1) {
      IrisPacks.Active var2 = active;
      if (var0 == null) {
         active = null;
      } else if (var2 == null || var2.pack != var0 || !var2.values.equals(var1)) {
         active = new IrisPacks.Active(var0, Map.copyOf(var1), ++generation);
      }
   }

   public static IrisPacks.Active active() {
      return active;
   }

   public static IrisPacks.Active drawn() {
      return drawn;
   }

   public static IrisPacks.Failure failure() {
      return failure;
   }

   public static void linked(IrisPacks.Active var0) {
      drawn = var0;
      failure = null;
   }

   public static void failed(IrisPacks.Active var0, String var1) {
      drawn = null;
      failure = new IrisPacks.Failure(var0.pack().id, var0.generation(), var1);
      System.out.println("[Viewpoint] Minecraft shader pack " + var0.pack().id + " cannot be drawn: " + var1);
   }

   public static Map<String, String> standard() {
      if (standard == null) {
         GLCapabilities var0 = GL.getCapabilities();
         int var1 = var0.OpenGL46
                 ? 460
                 : (
                 var0.OpenGL45
                         ? 450
                         : (var0.OpenGL44 ? 440 : (var0.OpenGL43 ? 430 : (var0.OpenGL42 ? 420 : (var0.OpenGL41 ? 410 : (var0.OpenGL40 ? 400 : 330)))))
         );
         features = features(var0);
         standard = IrisMacros.standard(var1, GL11.glGetString(7936), GL11.glGetString(7937), features, true);
      }

      return standard;
   }

   public static Set<String> features() {
      standard();
      return features;
   }

   private static Set<String> features(GLCapabilities var0) {
      LinkedHashSet<String> var1 = new LinkedHashSet<>(
              List.of("PER_BUFFER_BLENDING", "FADE_VARIABLE", "REVERSED_CULLING", "HIGHER_SHADOWCOLOR", "SEPARATE_HARDWARE_SAMPLERS")
      );
      if (var0.OpenGL43) {
         var1.addAll(List.of("COMPUTE_SHADERS", "CUSTOM_IMAGES", "SSBO"));
      }

      return var1;
   }

   private static Map<String, String> discoveryMacros() {
      return IrisMacros.standard(460, "", "", Set.of("PER_BUFFER_BLENDING", "FADE_VARIABLE", "COMPUTE_SHADERS", "CUSTOM_IMAGES", "SSBO"), true);
   }

   private IrisPacks() {
   }

   public record Active(IrisPack pack, Map<String, String> values, int generation) {
   }

   public record Failure(String packId, int generation, String why) {
   }
}