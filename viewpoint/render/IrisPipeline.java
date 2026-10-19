package viewpoint.render;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.lwjgl.opengl.GL;
import viewpoint.iris.CustomUniforms;
import viewpoint.iris.IrisPack;
import viewpoint.iris.ShadersProperties;
import viewpoint.platform.IrisPacks;
import viewpoint.platform.Pool;

final class IrisPipeline {
   private static final Pattern VEC4 = Pattern.compile("vec4\\s*\\(([^)]*)\\)");
   final IrisPack pack;
   final IrisPack.Configured configured;
   final IrisPrograms programs;
   final IrisTargets targets = new IrisTargets();
   final IrisShadowTargets shadows = new IrisShadowTargets();
   final IrisTextures textures = new IrisTextures();
   final IrisImages images = new IrisImages();
   final IrisUniforms uniforms = new IrisUniforms();
   final CustomUniforms custom;
   final IrisBindings bindings;
   final List<String> report = new ArrayList<>();
   int shadowResolution = 1024;
   float shadowDistance = 128.0F;
   boolean shadowOn;
   float eyeHalflife = 10.0F;
   float wetHalflife = 600.0F;
   float dryHalflife = 200.0F;
   boolean setupDone;

   private IrisPipeline(IrisPack var1, IrisPack.Configured var2, IrisPrograms var3) {
      this.pack = var1;
      this.configured = var2;
      this.programs = var3;
      this.custom = CustomUniforms.parse(var2.properties(), this.report);
      this.bindings = new IrisBindings(this.targets, this.shadows, this.textures, this.images);
   }

   static IrisPipeline create(IrisPack var0, Map<String, String> var1, Map<String, String> var2) {
      IrisPack.Configured var3;
      try {
         var3 = var0.configure(var1, var2);
      } catch (IOException var7) {
         throw new IllegalStateException(var0.id + ": " + var7.getMessage(), var7);
      }

      List var4 = features(var0, var3.properties());
      IrisPrograms var5 = IrisPrograms.link(var0, var3, var2);
      IrisPipeline var6 = new IrisPipeline(var0, var3, var5);
      var6.report.addAll(var4);
      var6.configure();
      var6.textures.load(var0.source, var3.properties(), var6.integer("noiseTextureResolution", 256));
      var6.report.addAll(var3.notes());
      var6.report.addAll(var5.notes);
      var6.report.addAll(var6.textures.notes);
      return var6;
   }

   private static List<String> features(IrisPack var0, ShadersProperties var1) {
      if (!GL.getCapabilities().OpenGL40) {
         throw new IllegalStateException(var0.id + ": Minecraft shader packs need OpenGL 4.0, which this GPU does not give");
      }

      Set var2 = IrisPacks.features();
      LinkedHashSet var3 = new LinkedHashSet<>(var1.features(true));
      var3.removeAll(var2);
      if (!var3.isEmpty()) {
         throw new IllegalStateException(var0.id + " requires " + String.join(", ", var3) + ", which the bridge does not give on this GPU");
      }

      ArrayList var4 = new ArrayList();
      LinkedHashSet var5 = new LinkedHashSet<>(var1.features(false));
      var5.removeAll(var2);
      if (!var5.isEmpty()) {
         var4.add("runs without " + String.join(", ", var5));
      }

      if (!var1.unknown.isEmpty()) {
         var4.add("directives the bridge does not know: " + String.join(", ", var1.unknown));
      }

      return var4;
   }

   private void configure() {
      Map var1 = this.programs.constants;

      for (int var3 : this.programs.buffers) {
         if (var3 >= 0 && var3 < 32) {
            IrisTargets.Buffer var4 = this.targets.buffers[var3];
            var4.used = true;
            String var5 = constant(var1, var3, "Format");
            if (var5 != null) {
               IrisFormats.Format var6 = IrisFormats.format(var5);
               if (var6 == null) {
                  this.report.add("colortex" + var3 + ": unknown format " + var5 + " (RGBA8)");
               } else {
                  var4.format = var6;
               }
            }

            String var12 = constant(var1, var3, "Clear");
            var4.clear = var12 == null || !var12.equals("false");
            float[] var7 = vec4(constant(var1, var3, "ClearColor"));
            if (var7 != null) {
               var4.clearColor = var7;
            }

            var4.mipmap = "true".equals(constant(var1, var3, "MipmapEnabled"));
            this.size(var4, var3);
         }
      }

      this.shadowResolution = this.integer("shadowMapResolution", 2048);
      this.shadowDistance = (float)this.number("shadowDistance", 160.0);
      this.eyeHalflife = (float)this.number("eyeBrightnessHalflife", 10.0);
      this.wetHalflife = (float)this.number("wetnessHalflife", 600.0);
      this.dryHalflife = (float)this.number("drynessHalflife", 200.0);
      this.shadowOn = this.programs.draws.containsKey(IrisPrograms.Draw.SHADOW) && this.configured.properties().flag("shadow.enabled", true);

      for (int var9 : this.programs.shadowColors) {
         if (var9 >= 0 && var9 < 8) {
            this.shadows.used[var9] = true;
            IrisFormats.Format var10 = IrisFormats.format((String)var1.get("shadowcolor" + var9 + "Format"));
            this.shadows.formats[var9] = var10 == null ? this.shadows.formats[var9] : var10;
            this.shadows.clear[var9] = !"false".equals(var1.get("shadowcolor" + var9 + "Clear"));
            float[] var11 = vec4((String)var1.get("shadowcolor" + var9 + "ClearColor"));
            this.shadows.clearColors[var9] = var11 == null ? this.shadows.clearColors[var9] : var11;
            this.shadows.mipmap[var9] = "true".equals(var1.get("shadowcolor" + var9 + "MipmapEnabled"));
         }
      }
   }

   private void size(IrisTargets.Buffer var1, int var2) {
      String var3 = this.configured.properties().sizeBuffer.get("colortex" + var2);
      String[] var4 = new String[]{"gcolor", "gdepth", "gnormal", "composite", "gaux1", "gaux2", "gaux3", "gaux4"};
      if (var3 == null && var2 < var4.length) {
         var3 = this.configured.properties().sizeBuffer.get(var4[var2]);
      }

      if (var3 != null) {
         String[] var5 = var3.strip().split("\\s+");
         if (var5.length >= 2) {
            String var6 = this.configured.macros().getOrDefault(var5[0], var5[0]);
            String var7 = this.configured.macros().getOrDefault(var5[1], var5[1]);

            float var8;
            float var9;
            try {
               var8 = Float.parseFloat(var6);
               var9 = Float.parseFloat(var7);
            } catch (NumberFormatException var11) {
               throw new IllegalStateException(this.pack.id + ": size.buffer.colortex" + var2 + " is not a size: " + var3, var11);
            }

            var1.fixed = !var6.contains(".") && var8 > 1.0F;
            var1.scaleX = var8;
            var1.scaleY = var9;
         }
      }
   }

   private static String constant(Map<String, String> var0, int var1, String var2) {
      String var3 = (String)var0.get("colortex" + var1 + var2);
      String[] var4 = new String[]{"gcolor", "gdepth", "gnormal", "composite", "gaux1", "gaux2", "gaux3", "gaux4"};
      if (var3 == null && var1 < var4.length) {
         var3 = (String)var0.get(var4[var1] + var2);
      }

      if (var3 == null && var1 >= 4 && var1 < 8) {
         var3 = (String)var0.get("gaux" + (var1 - 3) + var2);
      }

      return var3;
   }

   private static float[] vec4(String var0) {
      if (var0 == null) {
         return null;
      }

      Matcher var1 = VEC4.matcher(var0);
      if (!var1.find()) {
         return null;
      }

      String[] var2 = var1.group(1).split(",");
      float[] var3 = new float[4];

      for (int var4 = 0; var4 < 4; var4++) {
         String var5 = var2[Math.min(var4, var2.length - 1)].strip().replaceAll("[fF]$", "");
         var3[var4] = Float.parseFloat(var5);
      }

      return var3;
   }

   int integer(String var1, int var2) {
      return (int)this.number(var1, var2);
   }

   double number(String var1, double var2) {
      String var4 = this.programs.constants.get(var1);
      if (var4 == null) {
         return var2;
      }

      try {
         return Double.parseDouble(var4.strip().replaceAll("[fF]$", ""));
      } catch (NumberFormatException var6) {
         return var2;
      }
   }

   void checkPool() {
      long var1 = this.targets.bytes + this.shadows.bytes + this.images.bytes + this.textures.bytes;
      Pool.IRIS.use(var1);
      if (var1 > Pool.IRIS.cap()) {
         throw new IllegalStateException(
            this.pack.id
               + " needs "
               + (var1 >> 20)
               + " MiB, past the Minecraft shaders' pool's "
               + (Pool.IRIS.cap() >> 20)
               + " MiB: screen targets "
               + (this.targets.bytes >> 20)
               + " MiB, shadow maps "
               + (this.shadows.bytes >> 20)
               + " MiB, images and buffers "
               + (this.images.bytes >> 20)
               + " MiB (lower the pack's coloured light or voxel options)"
         );
      }
   }

   void release() {
      Pool.IRIS.use(0L);
      this.programs.delete();
      this.targets.release();
      this.shadows.release();
      this.textures.release();
      this.images.release();
      this.bindings.release();
   }
}
