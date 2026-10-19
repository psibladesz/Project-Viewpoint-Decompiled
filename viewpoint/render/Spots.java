package viewpoint.render;

import java.nio.FloatBuffer;
import java.util.Arrays;
import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL45;
import viewpoint.platform.Gl;
import viewpoint.platform.GlDebug;
import viewpoint.platform.GlProgram;
import viewpoint.platform.GpuParts;
import viewpoint.platform.Tuning;

final class Spots {
   final FlashShadow flash;
   private final SpotShadows maps;
   private final ModelPass models;
   private final LampReadbacks readbacks = new LampReadbacks();
   private final SpotShadows.MapView carView = new SpotShadows.MapView();
   private final SpotCoronas coronas = new SpotCoronas();
   private final SpotMarks marks = new SpotMarks();
   private final boolean[] drawn = new boolean[5];
   private final FloatBuffer records = BufferUtils.createFloatBuffer(792);
   private final int texture;
   private int count;

   Spots(GlProgram var1, ModelPass var2) {
      this.maps = new SpotShadows(var1);
      this.flash = new FlashShadow(this.maps, var2);
      this.models = var2;
      this.texture = GL45.glCreateTextures(3553);
      GL45.glTextureParameteri(this.texture, 10241, 9728);
      GL45.glTextureParameteri(this.texture, 10240, 9728);
      GL45.glTextureStorage2D(this.texture, 1, 34836, 6, 33);
   }

   void draw(FrameContext var1) {
      SceneData var2 = var1.scene;
      this.readbacks.update(var2);
      this.records.clear();
      this.count = 0;
      boolean var3 = var1.pipeline.flashlightShadow;
      int var4 = var2.spots.carMapCount;
      if (this.maps.fit(var4 > 0 ? 2 : (var3 ? 1 : 0))) {
         Arrays.fill(this.drawn, false);
      }

      if (this.flash.aim(var1)) {
         this.flash.write(this.records, var1.scene, var3 ? 0 : -1);
         this.count++;
         if (var3) {
            GlDebug.push("flashlight shadow");
            GpuParts.begin(6);
            this.flash.drawMap(var1.scene, 0);
            GpuParts.end(6);
            GlDebug.pop();
         }
      }

      int var5 = Math.min(var2.spots.lampCount, 33 - this.count);
      this.records.put(var2.spots.lamps, 0, var5 * 24);
      this.count += var5;
      GlDebug.push("headlight shadows");
      GpuParts.begin(46);

      for (int var6 = 1; var6 <= var4; var6++) {
         this.carMap(var2, var6);
      }

      GpuParts.end(46);
      GlDebug.pop();
      if (this.count > 0) {
         this.records.flip();
         GL45.glTextureSubImage2D(this.texture, 0, 0, 0, 6, this.count, 6408, 5126, this.records);
      }
   }

   private void carMap(SceneData var1, int var2) {
      float[] var3 = var1.spots.carMaps;
      int var4 = var2 * 11;
      int var5 = var2 - 1;
      if (!(var3[var4] < 0.0F)) {
         if (var3[var4] == 0.0F && this.drawn[var2]) {
            this.maps.keep(var2, var1);
         } else {
            SpotShadows.MapView var6 = this.carView;
            float var7 = var3[var4 + 9];
            float var8 = var3[var4 + 10];
            var6.layer = 1;
            var6.side = var3[var4 + 1];
            var6.u = var5 % 2 * 0.5F;
            var6.v = var5 / 2 * 0.5F;
            System.arraycopy(var3, var4 + 2, var6.lens, 0, 3);
            System.arraycopy(var3, var4 + 5, var6.dir, 0, 3);
            var6.reach = var3[var4 + 8];
            var6.fovY = 2.0F * (float)Math.atan(var8);
            var6.aspect = var7 / var8;
            var6.cone = (float)Math.atan(Math.sqrt(var7 * var7 + var8 * var8));
            var6.texel = 2.0F * var7 / (var6.side * 2048.0F);
            Matrix4f var9 = this.maps.place(var2, var6, var1);
            this.maps.drawWorld(var1, var6);
            this.models.shadowAhead(var1, var9, var6.lens, var6.dir, var6.reach);
            this.maps.end();
            this.drawn[var2] = true;
         }
      }
   }

   void uniforms(GlProgram var1) {
      var1.setInt("uSpotCount", this.count);
      var1.set("uLensGlow", Tuning.lensGlow);
      this.maps.uniforms(var1);
   }

   void coronas(FrameContext var1, int var2) {
      this.coronas.draw(var1, this, this.count, var2);
      this.marks.draw(var1, this, this.count);
   }

   void bind(boolean var1) {
      Gl.bind(140, var1 ? this.texture : 0);
      this.maps.bind(var1);
   }

   int mapsTexture() {
      return this.maps.texture();
   }

   static void sampler(GlProgram var0) {
      var0.sampler("uSpots", 140);
      var0.sampler("uSpotShadow", 21);
   }
}
