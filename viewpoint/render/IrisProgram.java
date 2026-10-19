package viewpoint.render;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import viewpoint.platform.GlProgram;

final class IrisProgram {
   static final int UNIT_BASE = 96;
   static final int SCRATCH_UNIT = 95;
   private static final Set<String> LIBRARY_SAMPLERS = Set.of(
      "uLight", "uPlantSlots", "uFloorPages", "uSpots", "uSpotShadow", "uLampShadow", "uPackTable", "uMeshRecords"
   );
   private static final Set<String> ON_UNIT_0 = Set.of("vp_albedoTex", "uTexture");
   final String name;
   final GlProgram gl;
   final List<Integer> drawBuffers;
   final int tintOutput;
   final Map<String, Integer> units = new LinkedHashMap<>();
   final Map<String, String> samplerTypes = new LinkedHashMap<>();
   final Map<String, Integer> images = new LinkedHashMap<>();
   final Map<String, IrisProgram.Uniform> uniforms = new LinkedHashMap<>();

   IrisProgram(String var1, GlProgram var2, List<Integer> var3, int var4, Map<String, String> var5) {
      this.name = var1;
      this.gl = var2;
      this.drawBuffers = var3;
      this.tintOutput = var4;
      this.activeUniforms();
      int var6 = 96;
      int var7 = 0;
      var2.use();

      for (Entry var9 : var5.entrySet()) {
         IrisProgram.Uniform var10 = this.uniforms.get(var9.getKey());
         if (var10 != null) {
            if (((String)var9.getValue()).contains("image")) {
               this.images.put((String)var9.getKey(), var7);
               GL20.glUniform1i(var10.location(), var7++);
            } else if (((String)var9.getKey()).equals("vp_albedoTex")) {
               this.units.put((String)var9.getKey(), 0);
               GL20.glUniform1i(var10.location(), 0);
            } else if (!LIBRARY_SAMPLERS.contains(var9.getKey())) {
               this.units.put((String)var9.getKey(), var6);
               this.samplerTypes.put((String)var9.getKey(), (String)var9.getValue());
               GL20.glUniform1i(var10.location(), var6++);
            }
         }
      }

      GL20.glUseProgram(0);
   }

   private void activeUniforms() {
      int var1 = this.gl.id();
      int var2 = GL20.glGetProgrami(var1, 35718);
      IntBuffer var3 = BufferUtils.createIntBuffer(1);
      IntBuffer var4 = BufferUtils.createIntBuffer(1);

      for (int var5 = 0; var5 < var2; var5++) {
         String var6 = GL20.glGetActiveUniform(var1, var5, 256, var3, var4);
         var6 = var6.endsWith("[0]") ? var6.substring(0, var6.length() - 3) : var6;
         this.uniforms.put(var6, new IrisProgram.Uniform(GL20.glGetUniformLocation(var1, var6), var4.get(0), var3.get(0)));
      }
   }

   void checkUnits() {
      for (Entry var2 : this.uniforms.entrySet()) {
         IrisProgram.Uniform var3 = (IrisProgram.Uniform)var2.getValue();
         if (isSampler(var3.type()) && !ON_UNIT_0.contains(var2.getKey()) && GL20.glGetUniformi(this.gl.id(), var3.location()) == 0) {
            throw new IllegalStateException(this.name + ": the sampler " + (String)var2.getKey() + " has no texture unit");
         }
      }
   }

   private static boolean isSampler(int var0) {
      return var0 >= 35677 && var0 <= 35684
         || var0 >= 36288 && var0 <= 36293
         || var0 >= 36297 && var0 <= 36312
         || var0 >= 36876 && var0 <= 36879
         || var0 >= 37128 && var0 <= 37133;
   }

   void use() {
      this.gl.use();
   }

   boolean has(String var1) {
      return this.uniforms.containsKey(var1);
   }

   void set(Map<String, float[]> var1) {
      for (Entry var3 : this.uniforms.entrySet()) {
         float[] var4 = (float[])var1.get(var3.getKey());
         if (var4 != null) {
            set((IrisProgram.Uniform)var3.getValue(), var4);
         }
      }
   }

   void set(String var1, float... var2) {
      IrisProgram.Uniform var3 = this.uniforms.get(var1);
      if (var3 != null) {
         set(var3, var2);
      }
   }

   private static void set(IrisProgram.Uniform var0, float[] var1) {
      int var2 = var0.location();
      if (var0.size() > 1 && (var0.type() == 5124 || var0.type() == 5126)) {
         int[] var3 = new int[Math.min(var0.size(), var1.length)];
         float[] var4 = new float[var3.length];

         for (int var5 = 0; var5 < var3.length; var5++) {
            var3[var5] = Math.round(var1[var5]);
            var4[var5] = var1[var5];
         }

         if (var0.type() == 5124) {
            GL20.glUniform1iv(var2, var3);
         } else {
            GL20.glUniform1fv(var2, var4);
         }
      } else {
         switch (var0.type()) {
            case 5124:
            case 35670:
               GL20.glUniform1i(var2, Math.round(var1[0]));
               break;
            case 5125:
               GL30.glUniform1ui(var2, Math.round(var1[0]));
               break;
            case 5126:
               GL20.glUniform1f(var2, var1[0]);
               break;
            case 35664:
               GL20.glUniform2f(var2, var1[0], get(var1, 1));
               break;
            case 35665:
               GL20.glUniform3f(var2, var1[0], get(var1, 1), get(var1, 2));
               break;
            case 35666:
               GL20.glUniform4f(var2, var1[0], get(var1, 1), get(var1, 2), get(var1, 3));
               break;
            case 35667:
            case 35671:
               GL20.glUniform2i(var2, Math.round(var1[0]), Math.round(get(var1, 1)));
               break;
            case 35668:
            case 35672:
               GL20.glUniform3i(var2, Math.round(var1[0]), Math.round(get(var1, 1)), Math.round(get(var1, 2)));
               break;
            case 35669:
            case 35673:
               GL20.glUniform4i(var2, Math.round(var1[0]), Math.round(get(var1, 1)), Math.round(get(var1, 2)), Math.round(get(var1, 3)));
               break;
            case 35675:
               if (var1.length >= 16) {
                  GL20.glUniformMatrix3fv(var2, false, new float[]{var1[0], var1[1], var1[2], var1[4], var1[5], var1[6], var1[8], var1[9], var1[10]});
               } else if (var1.length >= 9) {
                  GL20.glUniformMatrix3fv(var2, false, var1);
               }
               break;
            case 35676:
               if (var1.length >= 16) {
                  GL20.glUniformMatrix4fv(var2, false, var1);
               }
         }
      }
   }

   private static float get(float[] var0, int var1) {
      return var1 < var0.length ? var0[var1] : 0.0F;
   }

   List<String> samplers() {
      return new ArrayList<>(this.units.keySet());
   }

   void delete() {
      this.gl.delete();
   }

   record Uniform(int location, int type, int size) {
   }
}
