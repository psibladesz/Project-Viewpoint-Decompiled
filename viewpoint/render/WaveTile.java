package viewpoint.render;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import viewpoint.platform.Gl;
import viewpoint.platform.GlDebug;
import viewpoint.platform.GlProgram;
import viewpoint.platform.GpuParts;

final class WaveTile {
   static final int SIZE = 128;
   static final int CASCADES = 2;
   static final int STILL = 2;
   static final int LAYERS = 4;
   static final float PERIOD = 256.0F;
   static final double[] TILE_METRES = new double[]{40.0, 6.0};
   static final double DEPTH = 6.0;
   static final double FETCH = 2000.0;
   static final double CALM = 3.0;
   static final double SPEED_STEP = 1.0;
   static final double ANGLE_STEP = Math.toRadians(15.0);
   static final float WHITECAP = 0.5F;
   static final double WIND_EASE = 4.0;
   private static final int STAGES = Integer.numberOfTrailingZeros(128);
   private static final long SEED = 5744613L;
   private final GlProgram modulate = GlProgram.create("wave modulate", "screen.vert", "wave_modulate.frag");
   private final GlProgram fft = GlProgram.create("wave fft", "screen.vert", "wave_fft.frag");
   private final GlProgram unpack = GlProgram.create("wave unpack", "screen.vert", "wave_unpack.frag");
   private final int[] spectra = new int[2];
   private final int[] before = new int[2];
   private final int[] layerFbo = new int[4];
   private final int[] fieldA = new int[2];
   private final int[] fieldB = new int[2];
   private final int[] fieldFbo = new int[2];
   private int tile;
   private double speed = -1.0;
   private double angle;
   private double easeStart;
   private float ease = 1.0F;
   float time;
   private boolean made;

   WaveTile() {
      this.modulate.sampler("uSpectrum", 125);
      this.modulate.sampler("uSpectrumBefore", 126);
      this.fft.sampler("uInputA", 125);
      this.fft.sampler("uInputB", 126);
      this.unpack.sampler("uInputA", 125);
      this.unpack.sampler("uInputB", 126);
   }

   void draw(FrameContext var1) {
      GL13.glActiveTexture(34106);
      GL11.glBindTexture(35866, 0);
      if (!var1.pipeline.water) {
         GL13.glActiveTexture(33984);
      } else {
         if (this.tile == 0) {
            this.create();
         }

         this.wind(var1.scene);
         this.easeStart = Math.min(this.easeStart, var1.scene.cloudTime);
         this.ease = (float)Math.min(1.0, (var1.scene.cloudTime - this.easeStart) / 4.0);
         if (var1.pipeline.waterFft || !this.made) {
            GlDebug.push("waves");
            GpuParts.begin(30);
            GL11.glDisable(2929);
            GL11.glDisable(3042);
            GL11.glViewport(0, 0, 128, 128);
            this.time = var1.pipeline.waterFft ? var1.scene.cloudTime % 256.0F : 0.0F;

            for (int var2 = 0; var2 < 2; var2++) {
               this.cascade(var2, this.time, var2);
               if (!this.made) {
                  this.cascade(var2, 0.0F, 2 + var2);
               }
            }

            GL13.glActiveTexture(33984);
            GL11.glBindTexture(35866, this.tile);
            GL30.glGenerateMipmap(35866);
            GL11.glBindTexture(35866, 0);
            GpuParts.end(30);
            GlDebug.pop();
            this.made = this.ease >= 1.0F;
         }

         GL13.glActiveTexture(34106);
         GL11.glBindTexture(35866, this.tile);
         GL13.glActiveTexture(33984);
      }
   }

   int texture() {
      return this.tile;
   }

   WaveSpectrum.Sea sea() {
      return new WaveSpectrum.Sea(this.speed, this.angle, 2000.0, 6.0);
   }

   private void wind(SceneData var1) {
      double var2 = Math.hypot(var1.windX, var1.windZ) / 0.8164965311686198;
      double var4 = Math.max(3.0, Math.round(var2 / 1.0) * 1.0);
      double var6 = Math.round(Math.atan2(var1.windX, var1.windZ) / ANGLE_STEP) * ANGLE_STEP + Math.PI;
      if (var4 != this.speed || var6 != this.angle) {
         boolean var8 = this.speed < 0.0;
         this.speed = var4;
         this.angle = var6;
         WaveSpectrum.Sea var9 = new WaveSpectrum.Sea(var4, var6, 2000.0, 6.0);
         FloatBuffer var10 = BufferUtils.createFloatBuffer(65536);

         for (int var11 = 0; var11 < 2; var11++) {
            int var12 = this.before[var11];
            this.before[var11] = this.spectra[var11];
            this.spectra[var11] = var12;
            double[] var13 = band(var11);
            var10.clear();
            var10.put(WaveSpectrum.initial(128, TILE_METRES[var11], var13[0], var13[1], var9, 5744613L + var11)).flip();
            upload(this.spectra[var11], var10);
            if (var8) {
               upload(this.before[var11], var10);
            }
         }

         GL11.glBindTexture(3553, 0);
         this.easeStart = var8 ? var1.cloudTime - 4.0 : var1.cloudTime;
         this.made = false;
      }
   }

   private static void upload(int var0, FloatBuffer var1) {
      GL11.glBindTexture(3553, var0);
      GL11.glTexSubImage2D(3553, 0, 0, 0, 128, 128, 6408, 5126, var1);
   }

   static double[] band(int var0) {
      double var1 = 201.06192982974676 / TILE_METRES[var0];
      return new double[]{var0 == 0 ? 0.0 : 201.06192982974676 / TILE_METRES[var0 - 1], var1};
   }

   private void cascade(int var1, float var2, int var3) {
      GL30.glBindFramebuffer(36160, this.fieldFbo[0]);
      this.modulate.use();
      this.modulate.set("uTileMetres", (float)TILE_METRES[var1]);
      this.modulate.set("uDepth", 6.0F);
      this.modulate.set("uTime", var2);
      this.modulate.set("uPeriod", 256.0F);
      this.modulate.set("uSpectrumMix", this.ease);
      Gl.bind(125, this.spectra[var1]);
      Gl.bind(126, this.before[var1]);
      Gl.screenTriangle();
      this.fft.use();
      int var4 = 0;

      for (int var5 = 1; var5 >= 0; var5--) {
         this.fft.setInt("uAlong", var5);

         for (int var6 = 1; var6 <= STAGES; var6++) {
            GL30.glBindFramebuffer(36160, this.fieldFbo[1 - var4]);
            this.fft.set("uSpan", 1 << var6);
            Gl.bind(125, this.fieldA[var4]);
            Gl.bind(126, this.fieldB[var4]);
            Gl.screenTriangle();
            var4 = 1 - var4;
         }
      }

      GL30.glBindFramebuffer(36160, this.layerFbo[var3]);
      this.unpack.use();
      this.unpack.set("uSquaresAMetre", 0.81649655F);
      this.unpack.set("uWhitecap", 0.5F);
      Gl.bind(125, this.fieldA[var4]);
      Gl.bind(126, this.fieldB[var4]);
      Gl.screenTriangle();
      Gl.bind(126, 0);
      Gl.bind(125, 0);
   }

   private void create() {
      int var1 = GL11.glGetInteger(32873);

      for (int var2 = 0; var2 < 2; var2++) {
         this.spectra[var2] = Gl.texture(34836, 128, 128, 6408, 5126, 9728);
         this.before[var2] = Gl.texture(34836, 128, 128, 6408, 5126, 9728);
      }

      for (int var3 = 0; var3 < 2; var3++) {
         this.fieldA[var3] = Gl.texture(34836, 128, 128, 6408, 5126, 9728);
         this.fieldB[var3] = Gl.texture(34836, 128, 128, 6408, 5126, 9728);
         this.fieldFbo[var3] = GL30.glGenFramebuffers();
         GL30.glBindFramebuffer(36160, this.fieldFbo[var3]);
         GL30.glFramebufferTexture2D(36160, 36064, 3553, this.fieldA[var3], 0);
         GL30.glFramebufferTexture2D(36160, 36065, 3553, this.fieldB[var3], 0);
         GL20.glDrawBuffers(new int[]{36064, 36065});
         complete("field " + var3);
      }

      GL11.glBindTexture(3553, var1);
      this.tile = GL11.glGenTextures();
      GL11.glBindTexture(35866, this.tile);
      GL12.glTexImage3D(35866, 0, 34842, 128, 128, 4, 0, 6408, 5126, (ByteBuffer)null);
      GL30.glGenerateMipmap(35866);
      GL11.glTexParameteri(35866, 10241, 9987);
      GL11.glTexParameteri(35866, 10240, 9729);
      GL11.glTexParameteri(35866, 10242, 10497);
      GL11.glTexParameteri(35866, 10243, 10497);

      for (int var4 = 0; var4 < 4; var4++) {
         this.layerFbo[var4] = GL30.glGenFramebuffers();
         GL30.glBindFramebuffer(36160, this.layerFbo[var4]);
         GL30.glFramebufferTextureLayer(36160, 36064, this.tile, 0, var4);
         complete("layer " + var4);
      }

      GL11.glBindTexture(35866, 0);
   }

   static long seed(int var0) {
      return 5744613L + var0;
   }

   private static void complete(String var0) {
      int var1 = GL30.glCheckFramebufferStatus(36160);
      if (var1 != 36053) {
         throw new IllegalStateException("the waves' " + var0 + " target is incomplete: 0x" + Integer.toHexString(var1));
      }
   }
}
