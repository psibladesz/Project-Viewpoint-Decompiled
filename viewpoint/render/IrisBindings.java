package viewpoint.render;

import java.util.HashSet;
import java.util.Set;
import java.util.Map.Entry;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL33;

final class IrisBindings {
   private final IrisTargets targets;
   private final IrisShadowTargets shadows;
   private final IrisTextures textures;
   private final IrisImages images;
   private final int compare;
   private final int plain;
   private final Set<String> unknown = new HashSet<>();

   IrisBindings(IrisTargets var1, IrisShadowTargets var2, IrisTextures var3, IrisImages var4) {
      this.targets = var1;
      this.shadows = var2;
      this.textures = var3;
      this.images = var4;
      this.compare = GL33.glGenSamplers();
      GL33.glSamplerParameteri(this.compare, 34892, 34894);
      GL33.glSamplerParameteri(this.compare, 34893, 515);
      GL33.glSamplerParameteri(this.compare, 10241, 9729);
      GL33.glSamplerParameteri(this.compare, 10240, 9729);
      GL33.glSamplerParameteri(this.compare, 10242, 33071);
      GL33.glSamplerParameteri(this.compare, 10243, 33071);
      this.plain = 0;
   }

   static int colortex(String var0) {
      return switch (var0) {
         case "gcolor" -> 0;
         case "gdepth" -> 1;
         case "gnormal" -> 2;
         case "composite" -> 3;
         case "gaux1" -> 4;
         case "gaux2" -> 5;
         case "gaux3" -> 6;
         case "gaux4" -> 7;
         default -> var0.matches("colortex\\d{1,2}") ? Integer.parseInt(var0.substring(8)) : -1;
      };
   }

   static int shadowcolor(String var0) {
      return var0.equals("shadowcolor") ? 0 : (var0.matches("shadowcolor\\d") ? var0.charAt(11) - 48 : -1);
   }

   void bind(IrisProgram var1, String var2, Set<Integer> var3) {
      for (Entry var5 : var1.units.entrySet()) {
         String var6 = (String)var5.getKey();
         int var7 = (Integer)var5.getValue();
         if (var7 != 0) {
            String var8 = var1.samplerTypes.getOrDefault(var6, "");
            int var9 = var8.contains("3D") ? 32879 : (var8.contains("1D") ? 3552 : 3553);
            IrisTextures.Texture var10 = this.textures.custom(var2, var6);
            int var11 = colortex(var6);
            GL13.glActiveTexture(33984 + var7);
            if (var10 != null && (var11 < 0 || !var3.contains(var11))) {
               if (var10.target() != 3553) {
                  GL11.glBindTexture(3553, this.texture(var6));
               }

               GL11.glBindTexture(var10.target(), var10.id());
               GL33.glBindSampler(var7, this.plain);
            } else {
               GL11.glBindTexture(var9, var9 == 3553 ? this.texture(var6) : this.images.sampler(var6, var9));
               GL33.glBindSampler(var7, var8.contains("Shadow") ? this.compare : this.plain);
            }
         }
      }

      this.images.bind(var1);
      GL13.glActiveTexture(33984);
   }

   void unbind(IrisProgram var1) {
      for (int var3 : var1.units.values()) {
         if (var3 != 0) {
            GL13.glActiveTexture(33984 + var3);
            GL11.glBindTexture(3553, 0);
            GL11.glBindTexture(32879, 0);
            GL33.glBindSampler(var3, 0);
         }
      }

      GL13.glActiveTexture(33984);
   }

   private int texture(String var1) {
      int var2 = colortex(var1);
      if (var2 >= 0 && var2 < 32 && this.targets.buffers[var2].used) {
         return this.targets.buffers[var2].current();
      }

      int var3 = shadowcolor(var1);
      if (var3 >= 0 && var3 < 8 && this.shadows.colors[var3] != 0) {
         return this.shadows.colors[var3];
      }

      return switch (var1) {
         case "depthtex0", "gdepthtex" -> this.targets.depth;
         case "depthtex1" -> this.targets.depthOpaque;
         case "depthtex2" -> this.targets.depthNoHand;
         case "dhDepthTex", "dhDepthTex0" -> this.targets.farDepth;
         case "dhDepthTex1" -> this.targets.farDepthOpaque;
         case "shadowtex0", "shadow", "watershadow", "shadowtex0HW" -> this.shadows.depth;
         case "shadowtex1", "shadowtex1HW" -> this.shadows.depthOpaque;
         case "noisetex" -> this.textures.noise.id();
         case "normals" -> this.textures.normals.id();
         case "specular" -> this.textures.specular.id();
         case "lightmap" -> this.textures.lightmap.id();
         case "vp_tintTex" -> this.targets.tint;
         default -> {
            int var6 = this.images.sampler(var1, 3553);
            if (var6 != 0) {
               yield var6;
            } else {
               this.unknown.add(var1);
               yield this.textures.white.id();
            }
         }
      };
   }

   Set<String> unknown() {
      return this.unknown;
   }

   void release() {
      GL33.glDeleteSamplers(this.compare);
   }
}
