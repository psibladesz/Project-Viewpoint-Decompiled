package viewpoint.render;

import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL42;
import org.lwjgl.opengl.GL43;
import viewpoint.iris.CustomUniforms;
import viewpoint.iris.ProgramSet;
import viewpoint.iris.ShadersProperties;
import viewpoint.platform.GlDebug;

final class IrisFrame {
   static IrisFrame.Watcher watcher;
   private final IrisGeometry geometry = new IrisGeometry();
   private final Map<String, float[]> values = new HashMap<>();
   private final float[] ortho = new float[16];
   private final float[] identity = new float[16];
   private final float[] textureIdentities = new float[48];
   private final int[] area = new int[4];
   private int quadVao;
   private int quadVbo;
   private IrisPipeline linkedFor;
   private ModelPass models;
   private FarPass far;
   private final IrisFar distant = new IrisFar();
   private FlamePass flames;
   private BloodPass blood;
   private final Matrix4f pickInverse = new Matrix4f();

   void init() {
      this.geometry.init();
      this.flames = new FlamePass(true);
      this.blood = new BloodPass(true);
      FloatBuffer var1 = BufferUtils.createFloatBuffer(8).put(new float[]{0.0F, 0.0F, 1.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F});
      var1.flip();
      this.quadVao = GL30.glGenVertexArrays();
      this.quadVbo = GL15.glGenBuffers();
      GL30.glBindVertexArray(this.quadVao);
      GL15.glBindBuffer(34962, this.quadVbo);
      GL15.glBufferData(34962, var1, 35044);
      GL20.glVertexAttribPointer(0, 2, 5126, false, 8, 0L);
      GL20.glEnableVertexAttribArray(0);
      GL30.glBindVertexArray(0);
      GL15.glBindBuffer(34962, 0);
      new Matrix4f().setOrtho(0.0F, 1.0F, 0.0F, 1.0F, -1.0F, 1.0F).get(this.ortho);
      new Matrix4f().get(this.identity);

      for (int var2 = 0; var2 < 3; var2++) {
         System.arraycopy(this.identity, 0, this.textureIdentities, var2 * 16, 16);
      }
   }

   void draw(IrisPipeline var1, FrameContext var2, ShadowPass var3, ModelPass var4, FarPass var5) {
      this.models = var4;
      this.far = var5;
      SceneData var6 = var2.scene;
      String var7 = var1.targets.ensure(var2.width, var2.height);
      if (var7 != null) {
         throw new IllegalStateException(var1.pack.id + ": its screen target " + var7 + " could not be made");
      }

      if (var1.shadowOn) {
         var1.shadows.ensure(var1.shadowResolution);
      }

      if (this.linkedFor != var1) {
         this.linkedFor = var1;
         var1.images.load(var1.configured.properties(), var2.width, var2.height);
         var1.report.addAll(var1.images.notes);
         var1.checkPool();
         this.geometry.blocks(var1.configured.blocks());
      }

      var1.uniforms
              .update(
                      var2,
                      var1.pack.clock,
                      var1.shadowDistance,
                      var1.eyeHalflife,
                      var1.wetHalflife,
                      var1.dryHalflife,
                      var1.programs.draws.get(IrisPrograms.Draw.DH_TERRAIN) != null,
                      var6.fogEnd
              );
      this.values(var1, var2);
      var1.images.frame();
      var3.drawLights(var2);
      if (!var1.setupDone) {
         this.stage(var1, ProgramSet.Stage.SETUP, var2);
         var1.setupDone = true;
      }

      this.stage(var1, ProgramSet.Stage.BEGIN, var2);
      if (var1.shadowOn) {
         this.shadowPass(var1, var2, var3);
      }

      this.stage(var1, ProgramSet.Stage.SHADOWCOMP, var2);
      this.stage(var1, ProgramSet.Stage.PREPARE, var2);
      this.opaque(var1, var2, var3);
      this.stage(var1, ProgramSet.Stage.DEFERRED, var2);
      this.translucent(var1, var2, var3);
      this.weather(var1, var2, var3);
      this.stage(var1, ProgramSet.Stage.COMPOSITE, var2);
      this.stage(var1, ProgramSet.Stage.FINAL, var2);
      GL30.glBindFramebuffer(36009, var2.outputDrawFbo);
      GL30.glBindFramebuffer(36008, var2.outputReadFbo);
      GL11.glViewport(var2.viewport[0], var2.viewport[1], var2.viewport[2], var2.viewport[3]);
      this.blood.over(var2, var1.targets.depth, var1.uniforms.projection);
      this.flames.over(var2, var1.targets.depth, var1.uniforms.projection);
   }

   private void values(IrisPipeline var1, FrameContext var2) {
      this.values.clear();
      this.values.putAll(var1.uniforms.values);
      CustomUniforms var3 = var1.custom;
      var3.evaluate(var1x -> {
         float[] var2x = var1.uniforms.values.get(var1x);
         if (var2x == null) {
            return null;
         }

         double[] var3x = new double[var2x.length];

         for (int var4 = 0; var4 < var2x.length; var4++) {
            var3x[var4] = var2x[var4];
         }

         return var3x;
      }, var2.frameSeconds);

      for (CustomUniforms.Entry var5 : var3.uniforms()) {
         double[] var6 = var3.value(var5.declaration().name());
         if (var6 != null) {
            float[] var7 = new float[var6.length];

            for (int var8 = 0; var8 < var6.length; var8++) {
               var7[var8] = (float)var6[var8];
            }

            this.values.put(var5.declaration().name(), var7);
         }
      }
   }

   private void shadowPass(IrisPipeline var1, FrameContext var2, ShadowPass var3) {
      GlDebug.push("minecraft shadow");
      IrisProgram var4 = var1.programs.draws.get(IrisPrograms.Draw.SHADOW);
      var1.shadows.clear();
      GL30.glBindFramebuffer(36160, var1.shadows.framebuffer(var4.drawBuffers));
      GL11.glViewport(0, 0, var1.shadows.size, var1.shadows.size);
      this.geometry.shadow(var1, this.values, var2, var3, this.models);
      this.distant.shadow(var1, this.values, var2, this.far, this.geometry);
      var1.shadows.copyOpaque();
      var1.shadows.mipmaps();
      GlDebug.pop();
   }

   private void opaque(IrisPipeline var1, FrameContext var2, ShadowPass var3) {
      GlDebug.push("minecraft opaque");
      SceneData var4 = var2.scene;
      var1.targets.clear(var4.fogR, var4.fogG, var4.fogB);
      IrisProgram var5 = var1.programs.draws.get(IrisPrograms.Draw.TERRAIN);
      List<Integer> var6 = var5 != null ? var5.drawBuffers : List.of(0);
      int var7 = var5 != null ? var5.tintOutput : -1;
      GL11.glViewport(0, 0, var2.width, var2.height);
      GL11.glClearDepth(1.0);

      for (int var8 = 1; var8 <= 2; var8++) {
         GL30.glBindFramebuffer(36160, var1.targets.framebuffer(List.of(), false, var8, -1));
         GL11.glClear(256);
      }

      GL30.glBindFramebuffer(36160, var1.targets.framebuffer(List.of(), false, 0, 0));
      GL11.glClearColor(0.0F, 0.0F, 0.0F, 0.0F);
      GL11.glClear(16384);
      this.skyInto(var1, var2);
      IrisProgram var10 = var1.programs.draws.get(IrisPrograms.Draw.DH_TERRAIN);
      if (var10 != null) {
         GL30.glBindFramebuffer(36160, var1.targets.framebuffer(var10.drawBuffers, false, 2, -1));
         this.distant.terrain(var1, this.values, var2, this.far, this.geometry);
      }

      GL30.glBindFramebuffer(36160, var1.targets.framebuffer(var6, false, 1, var7));
      GL11.glViewport(0, 0, var2.width, var2.height);
      this.geometry.surfaces(var1, this.values, var2, var3, false);
      IrisProgram var9 = var1.programs.draws.get(IrisPrograms.Draw.ENTITIES);
      if (var9 != null) {
         GL30.glBindFramebuffer(36160, var1.targets.framebuffer(var9.drawBuffers, false, 1, var9.tintOutput));
         this.geometry.models(var1, this.values, var2, var3, this.models);
      }

      MousePick.read(var2, var1.targets.framebuffer(List.of(), false, 1, -1), this.pickInverse.set(var1.uniforms.projection).mul(var2.view).invert());
      var1.targets.copyDepth(var1.targets.depth, var1.targets.depthOpaque);
      var1.targets.copyDepth(var1.targets.farDepth, var1.targets.farDepthOpaque);
      var1.targets.copyDepth(var1.targets.depth, var1.targets.depthNoHand);
      watch(var1, "opaque", var6);
      GlDebug.pop();
   }

   private void skyInto(IrisPipeline var1, FrameContext var2) {
      IrisProgram var3 = var1.programs.draws.get(IrisPrograms.Draw.SKY);
      List<Integer> var4 = var3 != null ? var3.drawBuffers : List.of(0);
      GL30.glBindFramebuffer(36160, var1.targets.framebuffer(var4, false, 1, -1));
      GL11.glViewport(0, 0, var2.width, var2.height);
      this.geometry.sky(var1, this.values, var2);
      watch(var1, "sky", var4);
   }

   private void translucent(IrisPipeline var1, FrameContext var2, ShadowPass var3) {
      IrisProgram var4 = var1.programs.draws.get(IrisPrograms.Draw.WATER);
      IrisProgram var5 = var1.programs.draws.get(IrisPrograms.Draw.VEHICLE_GLASS);
      IrisProgram var6 = var1.programs.draws.get(IrisPrograms.Draw.BLOCK_GLASS);
      GlDebug.push("minecraft translucent");
      GL11.glViewport(0, 0, var2.width, var2.height);
      if (var4 != null) {
         GL30.glBindFramebuffer(36160, var1.targets.framebuffer(var4.drawBuffers, false, 1, -1));
         this.geometry.surfaces(var1, this.values, var2, var3, true);
         watch(var1, "translucent", var4.drawBuffers);
      }

      if (var5 != null) {
         GL30.glBindFramebuffer(36160, var1.targets.framebuffer(var5.drawBuffers, false, 1, -1));
         this.geometry.vehicleGlass(var1, this.values, var2, var3, this.models);
      }

      if (var6 != null) {
         GL30.glBindFramebuffer(36160, var1.targets.framebuffer(var6.drawBuffers, false, 1, -1));
         this.geometry.doorGlass(var1, this.values, var2, var3, this.models);
      }

      GlDebug.pop();
   }

   private void weather(IrisPipeline var1, FrameContext var2, ShadowPass var3) {
      IrisProgram var4 = var1.programs.draws.get(IrisPrograms.Draw.WEATHER);
      IrisProgram var5 = var1.programs.draws.get(IrisPrograms.Draw.LIGHTNING);
      GlDebug.push("minecraft weather");
      GL11.glViewport(0, 0, var2.width, var2.height);
      if (var4 != null) {
         GL30.glBindFramebuffer(36160, var1.targets.framebuffer(var4.drawBuffers, false, 1, -1));
         this.geometry.weather(var1, this.values, var2, var3);
      }

      if (var5 != null) {
         GL30.glBindFramebuffer(36160, var1.targets.framebuffer(var5.drawBuffers, false, 1, -1));
         this.geometry.lightning(var1, this.values, var2);
         watch(var1, "lightning", var5.drawBuffers);
      }

      GlDebug.pop();
   }

   private void stage(IrisPipeline var1, ProgramSet.Stage var2, FrameContext var3) {
      List<IrisPrograms.Pass> var4 = var1.programs.passes.get(var2);
      if (var4 != null && !var4.isEmpty()) {
         GlDebug.push("minecraft " + var2.prefix);
         GL11.glDisable(2929);
         GL11.glDepthMask(false);

         String var5 = switch (var2) {
            case SETUP, BEGIN -> "begin";
            case SHADOWCOMP -> "shadowcomp";
            case PREPARE -> "prepare";
            case DEFERRED -> "deferred";
            default -> "composite";
         };
         HashSet<Integer> var6 = new HashSet<>();

         for (IrisPrograms.Pass var8 : var4) {
            this.computes(var1, var8, var5, var3, var6);
            if (var8.program() != null) {
               this.screen(var1, var8, var5, var3, var2 == ProgramSet.Stage.FINAL, var6);
            }
         }

         GL11.glDepthMask(true);
         GL11.glEnable(2929);
         GlDebug.pop();
      }
   }

   private void computes(IrisPipeline var1, IrisPrograms.Pass var2, String var3, FrameContext var4, Set<Integer> var5) {
      for (int var6 = 0; var6 < var2.computes().size(); var6++) {
         IrisProgram var7 = var2.computes().get(var6);
         var7.use();
         var7.set(this.values);
         var1.bindings.bind(var7, var3, var5);
         int[] var8 = var2.groups().get(var6);
         float[] var9 = var2.scales().get(var6);
         int var10 = var8 != null ? var8[0] : (int)Math.ceil(var4.width * var9[0]);
         int var11 = var8 != null ? var8[1] : (int)Math.ceil(var4.height * var9[1]);
         int var12 = var8 != null ? var8[2] : 1;
         GL43.glDispatchCompute(Math.max(var10, 1), Math.max(var11, 1), Math.max(var12, 1));
         GL42.glMemoryBarrier(-1);
         var1.bindings.unbind(var7);
      }
   }

   private void screen(IrisPipeline var1, IrisPrograms.Pass var2, String var3, FrameContext var4, boolean var5, Set<Integer> var6) {
      IrisProgram var7 = var2.program();
      List<Integer> var8 = var7.drawBuffers;
      if (var5) {
         GL30.glBindFramebuffer(36009, var4.outputDrawFbo);
         System.arraycopy(var4.viewport, 0, this.area, 0, 4);
      } else {
         GL30.glBindFramebuffer(36160, var1.targets.framebuffer(var8, true, 0, -1));
         viewport(var1, var2.name(), var1.targets.buffers[var8.get(0)], this.area);
      }

      GL11.glViewport(this.area[0], this.area[1], this.area[2], this.area[3]);
      var7.use();
      var7.set(this.values);
      var7.set("vp_ModelViewMatrix", this.identity);
      var7.set("vp_ProjectionMatrix", this.ortho);
      var7.set("vp_ModelViewProjectionMatrix", this.ortho);
      var7.set("vp_TextureMatrix", this.textureIdentities);
      var7.set("vp_viewArea", this.area[0], this.area[1], this.area[2], this.area[3]);
      var7.set("uIrisLampColours", 1.0F);
      var1.bindings.bind(var7, var3, var6);
      IrisGeometry.blend(var1, var7, false);
      GL30.glBindVertexArray(this.quadVao);
      GL11.glDrawArrays(5, 0, 4);
      GL30.glBindVertexArray(0);
      GL11.glDisable(3042);
      var1.bindings.unbind(var7);
      if (!var5) {
         var6.addAll(flips(var1, var2.name(), var8));
         watch(var1, var2.name(), var8);
      }
   }

   private static void viewport(IrisPipeline var0, String var1, IrisTargets.Buffer var2, int[] var3) {
      List<String> var4 = ShadersProperties.words(var0.configured.properties().flag("scale." + var1, "1.0"));
      float var6 = 0.0F;
      float var7 = 0.0F;

      float var5;
      try {
         var5 = Float.parseFloat((String)var4.get(0));
         if (var4.size() >= 3) {
            var6 = Float.parseFloat((String)var4.get(1));
            var7 = Float.parseFloat((String)var4.get(2));
         }
      } catch (NumberFormatException var9) {
         throw new IllegalStateException(var0.pack.id + ": scale." + var1 + " is not a scale: " + var4, var9);
      }

      var3[0] = Math.round(var2.width * var6);
      var3[1] = Math.round(var2.height * var7);
      var3[2] = Math.round(var2.width * var5);
      var3[3] = Math.round(var2.height * var5);
   }

   private static Set<Integer> flips(IrisPipeline var0, String var1, List<Integer> var2) {
      LinkedHashSet<Integer> var3 = new LinkedHashSet<>(var2);

      for (Entry<String, Boolean> var5 : var0.configured.properties().flip.entrySet()) {
         String var6 = var5.getKey();
         if (var6.startsWith(var1 + ".")) {
            int var7 = IrisBindings.colortex(var6.substring(var1.length() + 1));
            if (var7 >= 0) {
               if ((Boolean)var5.getValue()) {
                  var3.add(var7);
               } else {
                  var3.remove(var7);
               }
            }
         }
      }

      for (int var9 : var3) {
         if (var2.contains(var9)) {
            var0.targets.flip(var9);
         } else {
            var0.targets.buffers[var9].read = 1 - var0.targets.buffers[var9].read;
         }
      }

      return var3;
   }

   private static void watch(IrisPipeline var0, String var1, List<Integer> var2) {
      if (watcher != null) {
         watcher.written(var0, var1, var2);
      }
   }

   void release() {
      this.geometry.release();
      GL30.glDeleteVertexArrays(this.quadVao);
      GL15.glDeleteBuffers(this.quadVbo);
   }

   interface Watcher {
      void written(IrisPipeline var1, String var2, List<Integer> var3);
   }
}