package viewpoint.render;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL30;
import viewpoint.platform.Gl;
import viewpoint.platform.GlProgram;
import viewpoint.platform.GpuParts;
import viewpoint.platform.Profile;

final class FarShadow {
   private static final int FRAMES = 30;
   private static final int WIDE_FRAMES = 60;
   private static final int NEAR_FRAMES = 600;
   private static final float SERVED_PAST = 1.25F;
   private static final float WIDE_STEP = 256.0F;
   private static final float WIDE_RECENTRE = 256.0F;
   private static final float WIDE_DEPTH = 3.0F;
   private static final byte NEAR_CASTERS = 27;
   private final GlProgram near = GlProgram.create("far shadow near", "shadow.vert", "shadow.frag");
   private final GlProgram shell = GlProgram.create("far shadow shell", "far_shell.vert", "far_shadow_shell.frag");
   private final GlProgram box = GlProgram.create("far shadow box", "far.vert", "far_shadow_box.frag");
   private final GlProgram cell = GlProgram.create("far shadow cell", "far.vert", "far_shadow_cell.frag");
   private final FarShadowMap sharp = new FarShadowMap(4096, 384.0F, 1200.0F, 64.0F, Gl.UNIT_CASCADE[3], 33190);
   private final FarShadowMap wide = new FarShadowMap(4096, 0.0F, 0.0F, 256.0F, Gl.UNIT_CASCADE[4], 33189);
   private final TreeBaker trees;
   private final FarCasters casters = new FarCasters();
   private boolean active;
   private int mask;
   private int shellMask;
   private FarShadowMap counting;
   private int cascade = -1;
   private final int[] cascadePieces = new int[3];
   private final long[] cascadeVertices = new long[3];
   private float sunDegrees;

   FarShadow(TreeBaker var1) {
      this.trees = var1;
      Profile.farMapsReport = () -> "sharp " + this.sharp.report() + "; wide " + this.wide.report() + "; " + this.cascadesReport();
      this.near.sampler("uTexture", 0);
      ShellArena.samplers(this.shell);
      this.shell.sampler("uTreeAtlas", 42);

      for (GlProgram var5 : new GlProgram[]{this.shell, this.box, this.cell}) {
         var5.sampler("uMask", 27);
         var5.sampler("uShellMask", 28);
      }
   }

   FarShadowMap map(int var1) {
      return var1 == 0 ? this.sharp : this.wide;
   }

   void off() {
      this.active = false;
      this.sharp.valid = false;
      this.wide.valid = false;
   }

   void draw(FrameContext var1, int var2, int var3, boolean var4) {
      this.active = var1.pipeline.farShadows;
      this.mask = var2;
      this.shellMask = var3;
      SceneData var5 = var1.scene;
      boolean var6 = var1.sun && var1.pipeline.farShadows;
      double var7 = -var5.originX;
      double var9 = -var5.originZ;
      boolean var11 = this.drawSharp(var5, var6, var4, var7, var9);
      float var12 = (float)Math.ceil((var5.fogEnd + 256.0F) / 256.0F) * 256.0F;
      this.wide.span(var12, var12 * 3.0F);
      this.wide.age++;
      if (var6 && !(var12 <= this.sharp.reach)) {
         boolean var13 = !this.wide.valid || var4 && stale(var5, this.wide, 60, var7, var9);
         if (var13 && !var11) {
            this.redraw(var5, this.wide, 19, var7, var9);
         }

         this.wide.carry(var5);
      } else {
         this.wide.valid = false;
      }
   }

   private boolean drawSharp(SceneData var1, boolean var2, boolean var3, double var4, double var6) {
      this.sharp.age++;
      if (!var2) {
         this.sharp.valid = false;
         return false;
      }

      boolean var8 = !this.sharp.valid || var3 && stale(var1, this.sharp, 30, var4, var6);
      if (var8) {
         this.redraw(var1, this.sharp, 8, var4, var6);
      }

      this.sharp.carry(var1);
      return var8;
   }

   private static boolean stale(SceneData var0, FarShadowMap var1, int var2, double var3, double var5) {
      return var1.moved(var0, var3, var5)
         || var1.age >= var2 && farSum(var0) != var1.drawnFar
         || var1.age >= var2 && FarRelief.sum(var0) != var1.drawnRelief
         || var1.age >= 600 && nearSum(var0) != var1.drawnNear;
   }

   private void redraw(SceneData var1, FarShadowMap var2, int var3, double var4, double var6) {
      var2.drawnFar = farSum(var1);
      var2.drawnNear = nearSum(var1);
      var2.drawnRelief = FarRelief.sum(var1);
      GpuParts.begin(var3);
      var2.begin(var1, var4, var6);
      this.near.use();
      ShadowPass.casters(this.near);
      float var8 = 100000.0F;
      this.near.set("uEye", var1.sunX * var8, var1.sunY * var8, var1.sunZ * var8);
      this.near.set("uViewProjection", var2.matrix);
      var2.count(0);
      Meshes.draw(var1, this.near, (byte)27);
      var2.counted(0, var1.meshCount, 0L);
      this.counting = var2;
      this.castInto(var1, var2.matrix, -1, 0.0F);
      this.counting = null;
      var2.end();
      GpuParts.end(var3);
   }

   private static long nearSum(SceneData var0) {
      long var1 = 0L;

      for (int var3 = 0; var3 < var0.meshCount; var3++) {
         if ((var0.meshFlags[var3] & 27) != 0) {
            long var4 = System.identityHashCode(var0.meshes[var3]) * -7046029254386353131L;
            var1 += var4 ^ var4 >>> 29;
         }
      }

      return var1;
   }

   boolean active() {
      return this.active;
   }

   float cascadeReach(SceneData var1) {
      return this.sharp.valid ? (var1.nearReach + 8.0F) * 1.25F : 0.0F;
   }

   static long farSum(SceneData var0) {
      long var1 = var0.shellCount * 31L + var0.farCount;

      for (int var3 = 0; var3 < var0.shellCount; var3++) {
         FarGpu.BlockDraw var4 = var0.shells[var3];
         long var5 = System.identityHashCode(var4.shell) * -7046029254386353131L + System.identityHashCode(var4.inside);
         var1 += var5 ^ var5 >>> 29;
      }

      for (int var7 = 0; var7 < var0.farCount; var7++) {
         FarGpu.CellDraw var8 = var0.far[var7];
         long var9 = System.identityHashCode(var8.mesh) * -7046029254386353131L;
         var1 += var9 ^ var9 >>> 29;
      }

      return var1;
   }

   long castersKey(SceneData var1, Matrix4f var2, float var3, float var4, float var5) {
      if (!this.active) {
         return 0L;
      }

      this.casters.serve(var1, true, this.cascadeReach(var1), var3, var4, var5);
      long var6 = 0L;

      for (int var8 = 0; var8 < var1.shellCount; var8++) {
         FarGpu.BlockDraw var9 = var1.shells[var8];
         if ((var9.shell != null || var9.inside != null) && this.casters.meets(var1, var2, var9)) {
            var6 += LayerCasters.place(System.identityHashCode(var9.shell), System.identityHashCode(var9.inside), shell(var1, var9.maskIndex));
         }
      }

      for (int var13 = 0; var13 < var1.farCount; var13++) {
         FarGpu.CellDraw var15 = var1.far[var13];
         if (var15.mesh != null && this.casters.meets(var1, var2, var15)) {
            var6 += LayerCasters.place(System.identityHashCode(var15.mesh), 0L, shells(var1, var15));
         }
      }

      for (int var14 = 0; var14 < var1.farMaskSize * var1.farMaskSize; var14++) {
         int var16 = LayerCasters.share(var1.farMask[var14]);
         if (var16 != 0) {
            int var10 = var14 / var1.farMaskSize;
            int var11 = var14 % var1.farMaskSize;
            var6 += LayerCasters.place(var1.farMaskX + var11, var1.farMaskY + var10, var16);
         }
      }

      var6 = var6 * 31L + Float.floatToIntBits(var1.nearReach);
      return var6 * 31L + Float.floatToIntBits(this.cascadeReach(var1));
   }

   private static long shell(SceneData var0, int var1) {
      if (var1 < 0) {
         return 0L;
      }

      byte[] var2 = var0.shellMask;
      return (var2[var1 * 3] & 255) << 16 | LayerCasters.share(var2[var1 * 3 + 1]) << 8 | var2[var1 * 3 + 2] & 0xFF;
   }

   private static long shells(SceneData var0, FarGpu.CellDraw var1) {
      byte var2 = 4;
      long var3 = 1L;

      for (int var5 = 0; var5 < var2; var5++) {
         for (int var6 = 0; var6 < var2; var6++) {
            int var7 = Math.floorDiv(var1.worldX, 64) + var6 - var0.shellMaskX;
            int var8 = Math.floorDiv(var1.worldY, 64) + var5 - var0.shellMaskY;
            boolean var9 = var7 >= 0 && var8 >= 0 && var7 < 53 && var8 < 53;
            var3 = var3 * 31L + (var9 ? shell(var0, var8 * 53 + var7) : 0L);
         }
      }

      return var3;
   }

   void castInto(SceneData var1, Matrix4f var2, int var3, float var4) {
      this.cascade = var3;
      if (var3 >= 0) {
         this.cascadePieces[var3] = 0;
         this.cascadeVertices[var3] = 0L;
         this.sunDegrees = (float)Math.toDegrees(Math.asin(Math.max(-1.0F, Math.min(1.0F, var1.sunY))));
      }

      if (this.active) {
         this.casters.serve(var1, var3 >= 0, this.cascadeReach(var1), var4, 0.0F, 0.0F);
         GL11.glEnable(34383);
         Gl.bind(27, this.mask);
         Gl.bind(28, this.shellMask);
         this.trees.bind(true);
         this.drawShell(var1, var2);
         this.drawInsides(var1, var2);
         this.drawCells(var1, var2);
         this.trees.bind(false);
         Gl.bind(27, 0);
         Gl.bind(28, 0);
         GL13.glActiveTexture(33984);
         GL30.glBindVertexArray(0);
         GL11.glDisable(34383);
      }
   }

   private void drawShell(SceneData var1, Matrix4f var2) {
      boolean var3 = false;
      int var4 = 0;
      long var5 = 0L;

      for (int var7 = 0; var7 < var1.shellCount; var7++) {
         FarGpu.BlockDraw var8 = var1.shells[var7];
         int var9 = var8.shell != null && this.casters.meets(var1, var2, var8) ? var8.shell.arenaFirst() : -1;
         if (var9 >= 0) {
            if (!var3) {
               var3 = true;
               this.shell.use();
               casterUniforms(this.shell, var1, var2);
               this.shell.setInt("uCasting", 1);
               GL13.glActiveTexture(33984);
               ShellArena.begin();
            }

            int var10 = ShellArena.record(var8.originX, var8.originY, var8.originZ, var8.worldX, var8.worldY, 0, 0.0F, 2.0F);
            var4++;

            for (int var11 = 0; var11 < var8.shell.pages.length; var11++) {
               if (var8.shell.pages[var11] != null) {
                  ShellArena.command(var8.shell, var11, var9 + var8.shell.first[var11], var8.shell.count[var11], var10);
                  var5 += var8.shell.count[var11];
               }
            }
         }
      }

      if (var3) {
         this.count(1);
         ShellArena.draw(this.shell);
         this.counted(1, var4, var5);
         GL11.glBindTexture(3553, 0);
      }
   }

   private void count(int var1) {
      if (this.counting != null) {
         this.counting.count(var1);
      }
   }

   private void counted(int var1, int var2, long var3) {
      if (this.counting != null) {
         this.counting.counted(var1, var2, var3);
      } else if (this.cascade >= 0) {
         this.cascadePieces[this.cascade] = this.cascadePieces[this.cascade] + var2;
         this.cascadeVertices[this.cascade] = this.cascadeVertices[this.cascade] + var3;
      }
   }

   private String cascadesReport() {
      return String.format(
         "cascades near %d (%.2fM vertices), mid %d (%.2fM), far %d (%.2fM), sun %.0f degrees up",
         this.cascadePieces[0],
         this.cascadeVertices[0] / 1000000.0,
         this.cascadePieces[1],
         this.cascadeVertices[1] / 1000000.0,
         this.cascadePieces[2],
         this.cascadeVertices[2] / 1000000.0,
         this.sunDegrees
      );
   }

   private void drawInsides(SceneData var1, Matrix4f var2) {
      this.box.use();
      casterUniforms(this.box, var1, var2);
      this.count(2);
      int var3 = 0;
      long var4 = 0L;

      for (int var6 = 0; var6 < var1.shellCount; var6++) {
         FarGpu.BlockDraw var7 = var1.shells[var6];
         if (var7.inside != null && var7.inside.vertices != 0 && this.casters.meets(var1, var2, var7)) {
            this.box.set("uOrigin", var7.originX, var7.originY, var7.originZ);
            this.box.set("uCellWorld", var7.worldX, var7.worldY);
            FarGpu.draw(var7.inside);
            var3++;
            var4 += var7.inside.vertices;
         }
      }

      this.counted(2, var3, var4);
   }

   private void drawCells(SceneData var1, Matrix4f var2) {
      this.cell.use();
      casterUniforms(this.cell, var1, var2);
      this.count(3);
      int var3 = 0;
      long var4 = 0L;

      for (int var6 = 0; var6 < var1.farCount; var6++) {
         FarGpu.CellDraw var7 = var1.far[var6];
         if (var7.mesh != null && var7.mesh.vertices != 0 && this.casters.meets(var1, var2, var7)) {
            this.cell.set("uOrigin", var7.originX, var7.originY, var7.originZ);
            this.cell.set("uCellWorld", var7.worldX, var7.worldY);
            FarGpu.draw(var7.mesh);
            var3++;
            var4 += var7.mesh.vertices;
         }
      }

      this.counted(3, var3, var4);
   }

   private static void casterUniforms(GlProgram var0, SceneData var1, Matrix4f var2) {
      float var3 = 100000.0F;
      var0.set("uViewProjection", var2);
      var0.set("uMaskGrid", var1.farMaskX, var1.farMaskY, var1.farMaskSize);
      var0.set("uShellGrid", var1.shellMaskX, var1.shellMaskY, 53.0F);
      var0.set("uNearReach", var1.nearReach);
      FarRelief.apply(var0, var1, true);
      if (var0.has("uEye")) {
         var0.set("uEye", var1.sunX * var3, var1.sunY * var3, var1.sunZ * var3);
      }
   }

   void bind(boolean var1) {
      this.sharp.bind(var1);
      this.wide.bind(var1);
   }
}
