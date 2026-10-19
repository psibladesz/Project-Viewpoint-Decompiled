package viewpoint.render;

import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.system.MemoryUtil;
import viewpoint.platform.Gl;
import viewpoint.platform.GlProgram;

public final class FarRelief {
   private static final float CASTER_GROUND = 2.0F;
   private static final double GROUND_STEP = 0.5;
   public static final int SPLIT = Math.max(4, 32);
   private static final float SCALE = 655.35F;
   private static final int STRIP_TEXELS = 1048576;
   private static volatile boolean on;
   private static volatile FarRelief.Field walled;
   private static FarRelief.Field uploaded;
   private static int texture;
   private static int waterTexture;
   private static int wallsTexture;
   private static int flowTexture;
   private static int bound = -1;
   private static SceneData drawing;

   public static void on(boolean var0) {
      on = var0;
   }

   public static void walled(FarRelief.Field var0) {
      walled = var0;
   }

   public static FarRelief.Field walls() {
      return on ? walled : null;
   }

   public static int cut() {
      return on ? 4 : Integer.MAX_VALUE;
   }

   public static boolean uploadDue() {
      FarRelief.Field var0 = walled;
      return var0 != null && var0 != uploaded;
   }

   public static void prepare() {
      FarRelief.Field var0 = walled;
      if (var0 != null && var0 != uploaded) {
         upload(var0);
      }
   }

   static void frame(SceneData var0) {
      drawing = var0;
      bound = -1;
      GlProgram.onUse(var0x -> apply(var0x, drawing, false));
   }

   static void apply(GlProgram var0, SceneData var1, boolean var2) {
      if (var0.has("uReliefGrid")) {
         FarRelief.Lift var3 = var1 == null ? null : var1.relief;
         FarRelief.Field var4 = var3 == null ? null : var3.grid;
         if (var4 != null && var4 != uploaded) {
            upload(var4);
         }

         onUnit(var4 != null ? texture : 0, var4 != null ? waterTexture : 0, var4 != null ? wallsTexture : 0);
         var0.setInt("uRelief", 91);
         var0.setInt("uReliefFlow", 124);
         var0.setInt("uReliefWater", 92);
         var0.setInt("uReliefWalls", 93);
         var0.set("uReliefScale", 655.35F);
         var0.set("uReliefSplit", SPLIT);
         if (var3 == null) {
            var0.set("uReliefGrid", 0.0F, 0.0F, 0.0F, 0.0F);
         } else {
            var0.set("uReliefGrid", var4.width, var4.height, var3.levels, var3.ground + (var2 ? 2.0F : 0.0F));
            var0.set("uReliefCamera", (float)var3.camX, (float)var3.camY);
         }
      }
   }

   static long sum(SceneData var0) {
      FarRelief.Lift var1 = var0.relief;
      return var1 == null ? 0L : Math.round(var1.ground / 0.5) * 83492791L ^ Float.floatToIntBits(var1.levels) ^ 25214903917L;
   }

   static double sink(SceneData var0) {
      return sink(var0.relief);
   }

   static double sink(FarRelief.Lift var0) {
      return var0 == null ? 0.0 : (double)var0.ground * var0.levels * 2.4494896F;
   }

   static void unbind() {
      onUnit(0, 0, 0);
   }

   private static void onUnit(int var0, int var1, int var2) {
      if (var0 != bound) {
         int var3 = GL11.glGetInteger(34016);
         Gl.bind(91, var0);
         Gl.bind(92, var1);
         Gl.bind(93, var2);
         Gl.bind(124, var0 == 0 ? 0 : flowTexture);
         GL13.glActiveTexture(var3);
         bound = var0;
      }
   }

   private static void upload(FarRelief.Field var0) {
      int var1 = GL11.glGetInteger(34016);
      int var2 = texture;
      int var3 = waterTexture;
      int var4 = wallsTexture;
      int var5 = flowTexture;
      if (var2 != 0) {
         Retirement.retireDrawing(() -> GL11.glDeleteTextures(new int[]{var2, var3, var4, var5}));
      }

      int var6 = GL11.glGetInteger(3317);
      GL11.glPixelStorei(3317, 2);
      texture = upload(91, var0.ground, var0.width, var0.height);
      waterTexture = upload(92, var0.water, FarRelief.Field.waterNodes(var0.width), FarRelief.Field.waterNodes(var0.height));
      GL11.glPixelStorei(3317, 1);
      wallsTexture = uploadWalls(var0);
      flowTexture = uploadFlow(var0);
      GL11.glPixelStorei(3317, var6);
      GL13.glActiveTexture(var1);
      bound = 0;
      uploaded = var0;
   }

   private static int upload(int var0, short[] var1, int var2, int var3) {
      GL13.glActiveTexture(33984 + var0);
      int var4 = Gl.texture(33322, var2, var3, 6403, 5123, 9728);
      int var5 = Math.max(1, Math.min(var3, 1048576 / var2));
      ShortBuffer var6 = MemoryUtil.memAllocShort(var5 * var2);

      try {
         for (int var7 = 0; var7 < var3; var7 += var5) {
            int var8 = Math.min(var5, var3 - var7);
            var6.clear();
            var6.put(var1, var7 * var2, var8 * var2).flip();
            GL11.glTexSubImage2D(3553, 0, 0, var7, var2, var8, 6403, 5123, var6);
         }
      } finally {
         MemoryUtil.memFree(var6);
      }

      GL11.glBindTexture(3553, 0);
      return var4;
   }

   private static int uploadWalls(FarRelief.Field var0) {
      GL13.glActiveTexture(34077);
      int var1 = var0.width;
      int var2 = var0.height;
      int var3 = Gl.texture(33321, var1, var2, 6403, 5121, 9728);
      int var4 = Math.max(1, Math.min(var2, 1048576 / var1));
      ByteBuffer var5 = MemoryUtil.memAlloc(var4 * var1);

      try {
         for (int var6 = 0; var6 < var2; var6 += var4) {
            int var7 = Math.min(var4, var2 - var6);
            var5.clear();
            if (var0.walls != null) {
               var5.put(var0.walls, var6 * var1, var7 * var1);
            } else {
               var5.put(new byte[var7 * var1]);
            }

            var5.flip();
            GL11.glTexSubImage2D(3553, 0, 0, var6, var1, var7, 6403, 5121, var5);
         }
      } finally {
         MemoryUtil.memFree(var5);
      }

      GL11.glBindTexture(3553, 0);
      return var3;
   }

   private static int uploadFlow(FarRelief.Field var0) {
      GL13.glActiveTexture(34108);
      boolean var1 = var0.flow == null;
      int var2 = var1 ? 1 : FarRelief.Field.flowCells(var0.width);
      int var3 = var1 ? 1 : FarRelief.Field.flowCells(var0.height);
      int var4 = Gl.texture(32856, var2, var3, 6408, 5121, 9729);
      ByteBuffer var5 = MemoryUtil.memAlloc(var2 * var3 * 4);

      try {
         var5.put(var1 ? new byte[]{-128, -128, 0, 0} : var0.flow).flip();
         GL11.glTexSubImage2D(3553, 0, 0, 0, var2, var3, 6408, 5121, var5);
      } finally {
         MemoryUtil.memFree(var5);
      }

      GL11.glBindTexture(3553, 0);
      return var4;
   }

   private FarRelief() {
   }

   public record Field(int width, int height, short[] ground, short[] water, byte[] walls, byte[] flow, long[] wet) {
      public static final float CENTIMETRES = 100.0F;
      private static final int WATER_EVERY = 1;

      public static FarRelief.Field of(int var0, int var1, float[] var2, float[] var3) {
         return of(var0, var1, var2, var3, null);
      }

      public static FarRelief.Field of(int var0, int var1, float[] var2, float[] var3, byte[] var4) {
         return of(var0, var1, var2, var3, var4, null);
      }

      public static FarRelief.Field of(int var0, int var1, float[] var2, float[] var3, byte[] var4, byte[] var5) {
         return of(var0, var1, var2, var3, var4, var5, null);
      }

      public static FarRelief.Field of(int var0, int var1, float[] var2, float[] var3, byte[] var4, byte[] var5, boolean[] var6) {
         int var7 = waterNodes(var0);
         int var8 = waterNodes(var1);
         short[] var9 = new short[var0 * var1];
         short[] var10 = new short[var7 * var8];

         for (int var11 = 0; var11 < var9.length; var11++) {
            var9[var11] = centimetres(var2[var11]);
         }

         for (int var14 = 0; var14 < var8; var14++) {
            for (int var12 = 0; var12 < var7; var12++) {
               int var13 = Math.min(var1 - 1, var14 * 1) * var0 + Math.min(var0 - 1, var12 * 1);
               var10[var14 * var7 + var12] = centimetres(var3 != null ? var3[var13] : var2[var13]);
            }
         }

         return new FarRelief.Field(var0, var1, var9, var10, var4, var5, var6 == null ? null : bits(var6));
      }

      public static long[] bits(boolean[] var0) {
         long[] var1 = new long[(var0.length + 63) / 64];

         for (int var2 = 0; var2 < var0.length; var2++) {
            var1[var2 >> 6] = var1[var2 >> 6] | (var0[var2] ? 1L << (var2 & 63) : 0L);
         }

         return var1;
      }

      public static int flowCells(int var0) {
         return (var0 - 1) / 4 + 1;
      }

      public static int waterNodes(int var0) {
         return (var0 - 1) / 1 + 1;
      }

      public static short centimetres(float var0) {
         return (short)Math.max(0, Math.min(65535, Math.round(var0 * 100.0F)));
      }

      public float metres(int var1) {
         return (this.ground[var1] & 65535) / 100.0F;
      }

      public float metresAt(double var1, double var3) {
         double var5 = var1 / 4.0;
         double var7 = var3 / 4.0;
         int var9 = (int)Math.floor(var5);
         int var10 = (int)Math.floor(var7);
         float var11 = (float)(var5 - var9);
         float var12 = (float)(var7 - var10);
         int var13 = this.wall(var9, var10);
         if (var13 != 0) {
            float var14 = ReliefWall.bend(var13, 0, var9, var1, var11, var12);
            float var15 = ReliefWall.bend(var13, 1, var10, var3, var12, var11);
            var11 = var14;
            var12 = var15;
         }

         float var16 = this.node(var9, var10) * (1.0F - var11) + this.node(var9 + 1, var10) * var11;
         float var17 = this.node(var9, var10 + 1) * (1.0F - var11) + this.node(var9 + 1, var10 + 1) * var11;
         return var16 * (1.0F - var12) + var17 * var12;
      }

      public boolean wet(double var1, double var3) {
         int var5 = (int)Math.round(var1 / 4.0);
         int var6 = (int)Math.round(var3 / 4.0);
         boolean var7 = this.wet != null && var5 >= 0 && var6 >= 0 && var5 < this.width && var6 < this.height;
         int var8 = var6 * this.width + var5;
         return var7 && (this.wet[var8 >> 6] & 1L << (var8 & 63)) != 0L;
      }

      public float waterAt(double var1, double var3) {
         int var5 = waterNodes(this.width);
         int var6 = waterNodes(this.height);
         double var7 = var1 / 4.0;
         double var9 = var3 / 4.0;
         int var11 = (int)Math.floor(var7);
         int var12 = (int)Math.floor(var9);
         float var13 = (float)(var7 - var11);
         float var14 = (float)(var9 - var12);
         float var15 = this.waterNode(var11, var12, var5, var6) * (1.0F - var13) + this.waterNode(var11 + 1, var12, var5, var6) * var13;
         float var16 = this.waterNode(var11, var12 + 1, var5, var6) * (1.0F - var13) + this.waterNode(var11 + 1, var12 + 1, var5, var6) * var13;
         return var15 * (1.0F - var14) + var16 * var14;
      }

      private float waterNode(int var1, int var2, int var3, int var4) {
         int var5 = Math.max(0, Math.min(var3 - 1, var1));
         int var6 = Math.max(0, Math.min(var4 - 1, var2));
         return (this.water[var6 * var3 + var5] & 65535) / 100.0F;
      }

      public int wall(int var1, int var2) {
         boolean var3 = this.walls != null && var1 >= 0 && var2 >= 0 && var1 < this.width - 1 && var2 < this.height - 1;
         return var3 ? this.walls[var2 * this.width + var1] & 0xFF : 0;
      }

      private float node(int var1, int var2) {
         int var3 = Math.max(0, Math.min(this.width - 1, var1));
         int var4 = Math.max(0, Math.min(this.height - 1, var2));
         return this.metres(var4 * this.width + var3);
      }

      public long bytes() {
         return (this.ground.length + this.water.length) * 2L
            + (this.walls == null ? 0 : this.walls.length)
            + (this.flow == null ? 0 : this.flow.length)
            + (this.wet == null ? 0L : this.wet.length * 8L);
      }
   }

   public record Grid(int width, int height, float[] metres, float[] water) {
      public Grid(int var1, int var2, float[] var3) {
         this(var1, var2, var3, null);
      }
   }

   public record Lift(FarRelief.Field grid, float levels, float ground, double camX, double camY, float[] bands) {
      public Lift(FarRelief.Field var1, float var2, float var3, double var4, double var6) {
         this(var1, var2, var3, var4, var6, null);
      }

      public float at(double var1, double var3) {
         return this.levels <= 0.0F ? 0.0F : (this.grid.metresAt(var1, var3) - this.ground) * this.levels;
      }

      public float scene(double var1, double var3) {
         return this.at(var1, var3) * 2.4494896F;
      }

      public float sceneAt(float var1, float var2) {
         return this.scene(this.camX - var1, this.camY - var2);
      }

      public float riseAbout(float var1, float var2, float var3) {
         float var4 = this.sceneAt(var1 + var3, var2) - this.sceneAt(var1 - var3, var2);
         float var5 = this.sceneAt(var1, var2 + var3) - this.sceneAt(var1, var2 - var3);
         return 0.5F * (Math.abs(var4) + Math.abs(var5));
      }
   }
}
