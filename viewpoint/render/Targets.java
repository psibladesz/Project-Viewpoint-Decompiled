package viewpoint.render;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import viewpoint.platform.Gl;

public final class Targets {
   static final int BLOOM_LEVELS = 5;
   int width;
   int height;
   int halfWidth;
   int halfHeight;
   int historyWidth;
   int historyHeight;
   int generation;
   public int albedo;
   public int normal;
   public int engineLight;
   public int depth;
   int velocity;
   int hdr;
   int translucentSum;
   int translucentReveal;
   int translucentFbo;
   int gbufferFbo;
   int hdrFbo;
   int lightFbo;
   public int farDepth;
   public int farFbo;
   int volume;
   int volumeSpare;
   int volumeFbo;
   int volumeSpareFbo;
   int occlusion;
   int clouds;
   int cloudFbo;
   int cloudShare;
   int cloudWidth;
   int cloudHeight;
   int cloudSpare;
   int cloudSpareFbo;
   int cloudTrace;
   int cloudTraceFbo;
   int cloudTraceWidth;
   int cloudTraceHeight;
   int taaInput;
   int taaInputFbo;
   int rain;
   int rainFbo;
   final int[] history = new int[2];
   final int[] historyCount = new int[2];
   final int[] historyFbo = new int[2];
   int antialiased;
   int antialiasedFbo;
   int smaaEdges;
   int smaaEdgesFbo;
   int smaaWeights;
   int smaaWeightsFbo;
   final int[] bloom = new int[5];
   final int[] bloomFbo = new int[5];
   final int[] bloomWidth = new int[5];
   final int[] bloomHeight = new int[5];
   int exposure;
   int rooms;
   int water;
   int noise;
   int noise2d;
   CloudNoise cloudNoise;
   StarField starField;
   ToneLut toneLut;
   FloorPlan floorPlan;
   LampFields lampFields;
   AirLuts air;
   RainTextures rainTextures;
   private final ArrayList<Integer> textures = new ArrayList<>();
   private final ArrayList<Integer> framebuffers = new ArrayList<>();
   private final StringBuilder incomplete = new StringBuilder();
   private static final IntBuffer drawBuffers = BufferUtils.createIntBuffer(4);
   private static final ByteBuffer gridBuffer = BufferUtils.createByteBuffer(4096);
   private long gridsFrame = -1L;

   void createMasks() {
      int var1 = GL11.glGetInteger(32873);
      this.exposure = Gl.texture(33321, 64, 64, 6403, 5121, 9729);
      this.rooms = Gl.texture(33321, 64, 64, 6403, 5121, 9728);
      this.water = Gl.texture(33321, 64, 64, 6403, 5121, 9729);
      this.noise2d = Noise2D.create();
      GL11.glBindTexture(3553, var1);
   }

   void createSky() {
      this.cloudNoise = new CloudNoise();
      this.starField = new StarField();
      this.toneLut = new ToneLut();
      this.floorPlan = new FloorPlan();
   }

   void velocity(boolean var1) {
      drawBuffers.clear();
      drawBuffers.put(36064).put(36065).put(36066);
      drawBuffers.put(var1 ? 36067 : 0).flip();
      GL20.glDrawBuffers(drawBuffers);
   }

   boolean ensure(int var1, int var2, int var3, int var4, int var5) {
      if (this.gbufferFbo != 0
         && var1 == this.width
         && var2 == this.height
         && var3 == this.historyWidth
         && var4 == this.historyHeight
         && var5 == this.cloudShare) {
         return true;
      } else {
         this.delete();
         this.generation++;
         int var6 = GL11.glGetInteger(32873);
         this.albedo = this.texture(32856, var1, var2, 6408, 5121, 9728);
         this.normal = this.texture(34842, var1, var2, 6408, 5126, 9728);
         this.engineLight = this.texture(32856, var1, var2, 6408, 5121, 9728);
         this.depth = this.texture(35056, var1, var2, 34041, 34042, 9728);
         this.hdr = this.texture(34842, var1, var2, 6408, 5126, 9729);
         this.velocity = this.texture(34842, var1, var2, 6408, 5126, 9728);
         this.gbufferFbo = this.framebuffer("g-buffer", this.depth, this.albedo, this.normal, this.engineLight, this.velocity);
         this.velocity(false);
         this.hdrFbo = this.framebuffer("hdr", this.depth, this.hdr);
         this.lightFbo = this.framebuffer("light", 0, this.hdr);
         this.translucentSum = this.texture(34842, var1, var2, 6408, 5126, 9728);
         this.translucentReveal = this.texture(33325, var1, var2, 6403, 5126, 9728);
         this.translucentFbo = this.framebuffer("translucent", this.depth, this.translucentSum, this.translucentReveal);
         this.farDepth = this.texture(35056, var1, var2, 34041, 34042, 9728);
         this.farFbo = this.framebuffer("far", this.farDepth, this.hdr);
         this.makeReduced(var1, var2, var5);
         this.makeLate(var1, var2, var3, var4);
         this.makeBloomChain(var1, var2);
         GL11.glBindTexture(3553, var6);
         this.width = var1;
         this.height = var2;
         this.historyWidth = var3;
         this.historyHeight = var4;
         if (this.incomplete.length() > 0) {
            System.out.println("[Viewpoint] framebuffers incomplete:" + this.incomplete);
            return false;
         } else {
            return true;
         }
      }
   }

   private void makeLate(int var1, int var2, int var3, int var4) {
      this.taaInput = this.texture(34842, var1, var2, 6408, 5126, 9729);
      this.taaInputFbo = this.framebuffer("taa input", 0, this.taaInput);
      this.rain = this.texture(32856, var1, var2, 6408, 5121, 9728);
      this.rainFbo = this.framebuffer("rain", this.depth, this.rain);

      for (int var5 = 0; var5 < 2; var5++) {
         this.history[var5] = this.texture(34842, var3, var4, 6408, 5126, 9729);
         this.historyCount[var5] = this.texture(33325, var3, var4, 6403, 5126, 9728);
         this.historyFbo[var5] = this.framebuffer("taa history " + var5, 0, this.history[var5], this.historyCount[var5]);
      }

      this.antialiased = this.texture(34842, var1, var2, 6408, 5126, 9729);
      this.antialiasedFbo = this.framebuffer("antialiased", 0, this.antialiased);
      this.smaaEdges = this.texture(33323, var1, var2, 33319, 5121, 9729);
      this.smaaEdgesFbo = this.framebuffer("smaa edges", 0, this.smaaEdges);
      this.smaaWeights = this.texture(32856, var1, var2, 6408, 5121, 9729);
      this.smaaWeightsFbo = this.framebuffer("smaa weights", 0, this.smaaWeights);
   }

   private void makeReduced(int var1, int var2, int var3) {
      int var4 = Math.max(1, var1 / 2);
      int var5 = Math.max(1, var2 / 2);
      this.volume = this.half(var4, var5);
      this.volumeFbo = this.framebuffer("volume", 0, this.volume);
      this.volumeSpare = this.half(var4, var5);
      this.volumeSpareFbo = this.framebuffer("volume spare", 0, this.volumeSpare);
      int var6 = Math.max(1, var1 * var3 / 100);
      int var7 = Math.max(1, var2 * var3 / 100);
      this.clouds = this.half(var6, var7);
      this.cloudFbo = this.framebuffer("clouds", 0, this.clouds);
      this.cloudSpare = this.half(var6, var7);
      this.cloudSpareFbo = this.framebuffer("clouds spare", 0, this.cloudSpare);
      this.cloudTraceWidth = (var6 + 4 - 1) / 4;
      this.cloudTraceHeight = (var7 + 4 - 1) / 4;
      this.cloudTrace = this.half(this.cloudTraceWidth, this.cloudTraceHeight);
      this.cloudTraceFbo = this.framebuffer("cloud trace", 0, this.cloudTrace);
      this.halfWidth = var4;
      this.halfHeight = var5;
      this.cloudShare = var3;
      this.cloudWidth = var6;
      this.cloudHeight = var7;
   }

   private void makeBloomChain(int var1, int var2) {
      int var3 = var1;
      int var4 = var2;

      for (int var5 = 0; var5 < 5; var5++) {
         var3 = Math.max(1, var3 / 2);
         var4 = Math.max(1, var4 / 2);
         this.bloomWidth[var5] = var3;
         this.bloomHeight[var5] = var4;
         this.bloom[var5] = this.texture(34842, var3, var4, 6408, 5126, 9729);
         this.bloomFbo[var5] = this.framebuffer("bloom " + var5, 0, this.bloom[var5]);
      }
   }

   void uploadGrids(SceneData var1, long var2) {
      if (this.gridsFrame != var2) {
         this.gridsFrame = var2;
         GL13.glActiveTexture(34064);
         GL11.glPixelStorei(3317, 1);
         grid(this.exposure, var1.exposure);
         grid(this.rooms, var1.rooms);
         grid(this.water, var1.water);
         GL11.glPixelStorei(3317, 4);
         GL13.glActiveTexture(33984);
      }
   }

   private static void grid(int var0, byte[] var1) {
      GL11.glBindTexture(3553, var0);
      gridBuffer.clear();
      gridBuffer.put(var1).flip();
      GL11.glTexSubImage2D(3553, 0, 0, 0, 64, 64, 6403, 5121, gridBuffer);
   }

   private int half(int var1, int var2) {
      return this.texture(34842, var1, var2, 6408, 5126, 9729);
   }

   private int texture(int var1, int var2, int var3, int var4, int var5, int var6) {
      int var7 = Gl.texture(var1, var2, var3, var4, var5, var6);
      this.textures.add(var7);
      return var7;
   }

   private int framebuffer(String var1, int var2, int... var3) {
      int var4 = GL30.glGenFramebuffers();
      this.framebuffers.add(var4);
      GL30.glBindFramebuffer(36160, var4);

      for (int var5 = 0; var5 < var3.length; var5++) {
         GL30.glFramebufferTexture2D(36160, 36064 + var5, 3553, var3[var5], 0);
      }

      if (var2 != 0) {
         GL30.glFramebufferTexture2D(36160, 33306, 3553, var2, 0);
      }

      if (var3.length > 1) {
         drawBuffers.clear();

         for (int var6 = 0; var6 < var3.length; var6++) {
            drawBuffers.put(36064 + var6);
         }

         drawBuffers.flip();
         GL20.glDrawBuffers(drawBuffers);
      } else if (var3.length == 0) {
         GL11.glDrawBuffer(0);
         GL11.glReadBuffer(0);
      }

      int var7 = GL30.glCheckFramebufferStatus(36160);
      if (var7 != 36053) {
         this.incomplete.append(' ').append(var1).append(" 0x").append(Integer.toHexString(var7));
      }

      return var4;
   }

   private void delete() {
      for (int var2 : this.framebuffers) {
         GL30.glDeleteFramebuffers(var2);
      }

      for (int var4 : this.textures) {
         GL11.glDeleteTextures(var4);
      }

      this.framebuffers.clear();
      this.textures.clear();
      this.incomplete.setLength(0);
   }
}
