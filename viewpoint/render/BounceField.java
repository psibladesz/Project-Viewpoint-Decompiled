package viewpoint.render;

import java.nio.FloatBuffer;
import java.util.Arrays;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import viewpoint.platform.Gl;
import viewpoint.platform.GlDebug;
import viewpoint.platform.GlProgram;
import viewpoint.platform.GpuParts;

final class BounceField {
   private static final int MAP = 128;
   private static final int MAP_CHUNKS = 16;
   static final int WAYS = 6;
   static final int STOREYS = 3;
   private static final int GROUND = 3;
   private static final int ROWS = 4;
   private static final int[] COPY_ORDER = new int[]{1, 0, 2, 3};
   private static final int[] SIDES = new int[]{0, 2};
   private static final int COPIES_PER_FRAME = 24;
   private final GlProgram colour = GlProgram.create("floor colour", "screen.vert", "floor_colour.frag");
   private final GlProgram leaving = GlProgram.create("bounce light", "screen.vert", "bounce_emit.frag");
   private final Targets targets;
   private final ShadowPass shadows;
   private final FloorBakes floors;
   private final RadianceCascades cascades;
   private final int colourMap;
   private final int colourFbo;
   private final int field;
   private final int fieldFbo;
   private final int[] leavingLight = new int[4];
   private final int[] leavingWalls = new int[4];
   private final int[] rising = new int[4];
   private final int[] leavingFbo = new int[4];
   private final BedColours beds = new BedColours(128);
   private final long[] mapKey = new long[1024];
   private final int[] mapSlice = new int[this.mapKey.length];
   private final FloorPage[] mapPage = new FloorPage[this.mapKey.length];
   private boolean cleared;
   private int cornerX;
   private int cornerY;
   private int madeLevel = Integer.MIN_VALUE;
   private int madeX;
   private int madeY;
   private int turn;

   BounceField(Targets var1, ShadowPass var2, FloorBakes var3) {
      this.targets = var1;
      this.shadows = var2;
      this.floors = var3;
      this.colour.sampler("uPages", 0);
      this.leaving.sampler("uFloorColours", 0);
      this.leaving.sampler("uExposure", 8);
      this.leaving.sampler("uRising", 141);
      ShadowPass.samplers(this.leaving);
      this.cascades = new RadianceCascades();
      byte var4 = 64;
      int var5 = GL11.glGetInteger(32873);
      int var6 = GL11.glGetInteger(36006);
      this.colourMap = Gl.texture(32856, 128, 512, 6408, 5121, 9728);

      for (int var7 = 0; var7 < 4; var7++) {
         this.leavingLight[var7] = Gl.texture(34842, var4, var4, 6408, 5126, 9728);
         this.leavingWalls[var7] = Gl.texture(34842, var4, var4, 6408, 5126, 9728);
         this.rising[var7] = Gl.texture(34842, var4, var4, 6408, 5126, 9728);
      }

      this.field = Gl.texture(34842, var4 * 6, var4 * 3, 6408, 5126, 9728);
      GL11.glBindTexture(3553, var5);
      this.colourFbo = framebuffer(this.colourMap);

      for (int var8 = 0; var8 < 4; var8++) {
         this.leavingFbo[var8] = framebuffer(this.leavingLight[var8], this.leavingWalls[var8], this.rising[var8]);
      }

      this.fieldFbo = framebuffer(this.field);
      GL30.glBindFramebuffer(36160, this.colourFbo);
      GL11.glClearColor(0.0F, 0.0F, 0.0F, 0.0F);
      GL11.glClear(16384);
      GL30.glBindFramebuffer(36160, var6);
      Arrays.fill(this.mapKey, Long.MIN_VALUE);
   }

   void draw(FrameContext var1, boolean var2) {
      Gl.bind(107, 0);
      if (var2) {
         SceneData var3 = var1.scene;
         this.cornerX = (int)Math.round(-var3.originX - var3.exposureAX);
         this.cornerY = (int)Math.round(-var3.originZ - var3.exposureAY);
         GlDebug.push("bounce");
         GpuParts.begin(25);
         int var4 = var1.storey();
         int var5 = 0;
         boolean var6 = var4 - 1 > 0;

         for (int var10 : COPY_ORDER) {
            if (var10 != 3 || var6) {
               var5 = this.copyFloors(var10, var10 == 3 ? 0 : var4 + var10 - 1, var5);
            }
         }

         this.beds(var4);
         this.drawStoreys(var1, var4, var6);
         GpuParts.end(25);
         GlDebug.pop();
         this.cleared = false;
      } else if (!this.cleared) {
         this.madeLevel = Integer.MIN_VALUE;
         this.beds.skip();
         GL30.glBindFramebuffer(36160, this.fieldFbo);
         GL11.glClearColor(0.0F, 0.0F, 0.0F, 0.0F);
         GL11.glClear(16384);
         this.cleared = true;
      }

      Gl.bind(107, this.field);
      this.beds.bind();
   }

   private void drawStoreys(FrameContext var1, int var2, boolean var3) {
      boolean var4 = var2 == this.madeLevel && this.cornerX == this.madeX && this.cornerY == this.madeY;
      int var5 = SIDES[this.turn];
      this.turn ^= 1;

      for (int var6 = 0; var6 < 3; var6++) {
         if (!var4 || var6 == 1 || var6 == var5) {
            int var7 = var2 + var6 - 1;
            int var8 = var6 > 0 ? var6 - 1 : (var3 ? 3 : -1);
            if (var8 == 3) {
               this.lightSquares(var1, 3, 0, var2, -1);
            }

            this.lightSquares(var1, var6, var7, var2, var8);
            this.cascades.draw(this.leavingLight[var6], this.leavingWalls[var6], this.fieldFbo, 64, var6, this.cornerX, this.cornerY, var7);
         }
      }

      this.madeLevel = var2;
      this.madeX = this.cornerX;
      this.madeY = this.cornerY;
   }

   private void beds(int var1) {
      int var2 = 1 - var1;
      if (var2 >= 0 && var2 < 3) {
         this.beds.draw(this.colourMap, var2);
      } else {
         this.beds.skip();
      }
   }

   boolean bedColours() {
      return this.beds.made();
   }

   private int copyFloors(int var1, int var2, int var3) {
      byte var4 = 64;
      byte var5 = 8;
      int var6 = var3;
      int var7 = Math.floorDiv(this.cornerX, var5);
      int var8 = Math.floorDiv(this.cornerY, var5);
      int var9 = Math.floorDiv(this.cornerX + var4 - 1, var5);
      int var10 = Math.floorDiv(this.cornerY + var4 - 1, var5);
      GL30.glBindFramebuffer(36160, this.colourFbo);
      this.colour.use();
      this.colour.setInt("uLevel", FloorBakes.lowLastLevel());
      GL13.glActiveTexture(33984);

      for (int var11 = var8; var11 <= var10 && var6 < 24; var11++) {
         for (int var12 = var7; var12 <= var9 && var6 < 24; var12++) {
            int var13 = (var1 * 16 + Math.floorMod(var11, 16)) * 16 + Math.floorMod(var12, 16);
            long var14 = FloorPage.key(var12, var11, var2);
            int var16 = this.floors.lowSlice(var12, var11, var2);
            FloorPage var17 = this.floors.lowPage(var12, var11, var2);
            if (this.mapKey[var13] != var14 || this.mapSlice[var13] != var16 || this.mapPage[var13] != var17) {
               this.copy(var13, var16);
               this.mapKey[var13] = var14;
               this.mapSlice[var13] = var16;
               this.mapPage[var13] = var17;
               var6++;
            }
         }
      }

      GL11.glBindTexture(35866, 0);
      return var6;
   }

   private void copy(int var1, int var2) {
      int var3 = var1 % 16 * 8;
      int var4 = var1 / 16 * 8;
      GL11.glViewport(var3, var4, 8, 8);
      if (var2 < 0) {
         GL11.glEnable(3089);
         GL11.glScissor(var3, var4, 8, 8);
         GL11.glClearColor(0.0F, 0.0F, 0.0F, 0.0F);
         GL11.glClear(16384);
         GL11.glDisable(3089);
      } else {
         GL11.glBindTexture(35866, this.floors.texture(var2));
         this.colour.setInt("uLayer", FloorBakes.layer(var2));
         this.colour.set("uCorner", var3, var4);
         Gl.screenTriangle();
      }
   }

   private void lightSquares(FrameContext var1, int var2, int var3, int var4, int var5) {
      byte var6 = 64;
      GL30.glBindFramebuffer(36160, this.leavingFbo[var2]);
      GL11.glViewport(0, 0, var6, var6);
      this.leaving.use();
      var1.camera(this.leaving);
      this.shadows.sunUniforms(this.leaving, var1);
      SurfacePass.lamps(this.leaving, var1.scene);
      this.leaving.set("uMapCorner", Math.floorMod(this.cornerX, 128), Math.floorMod(this.cornerY, 128));
      this.leaving.set("uCorner", this.cornerX, this.cornerY);
      this.leaving.setInt("uPlanLevel", var3);
      this.leaving.setInt("uMapStorey", var2);
      this.leaving.setInt("uCameraStorey", var3 == var4 ? 1 : 0);
      this.leaving.setInt("uRisingHeld", var5 < 0 ? 0 : 1);
      this.leaving.set("uFloorY", var1.floorY() + (var3 - var4) * 2.4494896F);
      this.shadows.bind(true);
      Gl.bind(8, this.targets.exposure);
      Gl.bind(141, var5 < 0 ? 0 : this.rising[var5]);
      Gl.bind(0, this.colourMap);
      Gl.screenTriangle();
      Gl.bind(141, 0);
      this.shadows.bind(false);
   }

   void paint(int var1, int var2, int var3, int var4, float var5, float var6, float var7) {
      int var8 = GL11.glGetInteger(36006);
      GL30.glBindFramebuffer(36160, this.colourFbo);
      GL11.glEnable(3089);
      GL11.glClearColor(var5, var6, var7, 1.0F);

      for (int var9 = var2; var9 < var2 + var4; var9++) {
         for (int var10 = var1; var10 < var1 + var3; var10++) {
            GL11.glScissor(Math.floorMod(var10, 128), 128 + Math.floorMod(var9, 128), 1, 1);
            GL11.glClear(16384);
         }
      }

      GL11.glDisable(3089);
      GL30.glBindFramebuffer(36160, var8);
   }

   float[][] means() {
      byte var1 = 64;
      return new float[][]{
         mean(texels(this.colourMap, 128, 512), 49152),
         mean(texels(this.leavingLight[1], var1, var1), var1 * var1),
         mean(texels(this.field, var1 * 6, var1 * 3), var1 * 6 * var1 * 3)
      };
   }

   float[] fieldTexels() {
      byte var1 = 64;
      return texels(this.field, var1 * 6, var1 * 3);
   }

   float[] leavingTexels() {
      byte var1 = 64;
      return texels(this.leavingLight[1], var1, var1);
   }

   private static float[] mean(float[] var0, int var1) {
      float[] var2 = new float[4];

      for (int var3 = 0; var3 < var1 * 4; var3++) {
         var2[var3 % 4] = var2[var3 % 4] + var0[var3] / var1;
      }

      return var2;
   }

   private static float[] texels(int var0, int var1, int var2) {
      FloatBuffer var3 = BufferUtils.createFloatBuffer(var1 * var2 * 4);
      GL13.glActiveTexture(33984);
      int var4 = GL11.glGetInteger(32873);
      GL11.glBindTexture(3553, var0);
      GL11.glGetTexImage(3553, 0, 6408, 5126, var3);
      GL11.glBindTexture(3553, var4);
      float[] var5 = new float[var1 * var2 * 4];
      var3.get(var5);
      return var5;
   }

   static int framebuffer(int... var0) {
      int var1 = GL30.glGenFramebuffers();
      GL30.glBindFramebuffer(36160, var1);
      int[] var2 = new int[var0.length];

      for (int var3 = 0; var3 < var0.length; var3++) {
         var2[var3] = 36064 + var3;
         GL30.glFramebufferTexture2D(36160, var2[var3], 3553, var0[var3], 0);
      }

      GL20.glDrawBuffers(var2);
      int var4 = GL30.glCheckFramebufferStatus(36160);
      if (var4 != 36053) {
         throw new IllegalStateException("the bounce field's target is incomplete: 0x" + Integer.toHexString(var4));
      } else {
         return var1;
      }
   }
}
