package viewpoint.far;

import java.util.Arrays;
import viewpoint.core.Frame;
import viewpoint.render.FarGpu;
import viewpoint.render.GroundPage;
import viewpoint.render.SceneData;

final class ShellBlock {
   final int bx;
   final int by;
   ShellBlock.Piece piece;
   ShellBlock.Piece previous;
   ShellBlock.Piece coming;
   final Dissolve fade = new Dissolve();
   int generation;
   int want;
   int[] interiors = ShellInteriors.NONE;
   int[] wantInteriors = ShellInteriors.NONE;
   boolean exact;
   boolean wantExact;
   boolean busy;
   long seen;
   float distance;
   float top;

   ShellBlock(int var1, int var2) {
      this.bx = var1;
      this.by = var2;
   }

   int tier() {
      return this.piece == null ? -1 : this.piece.tier;
   }

   int builtTier() {
      return this.coming != null ? this.coming.tier : this.tier();
   }

   void emit(SceneData var1, Frame var2, float var3) {
      int var4 = this.bx - var1.shellMaskX;
      int var5 = this.by - var1.shellMaskY;
      int var6 = var4 >= 0 && var5 >= 0 && var4 < 53 && var5 < 53 ? var5 * 53 + var4 : -1;
      if (this.piece != null) {
         this.draw(var1, this.piece, var2, 0.0F, Dissolve.top(var3)).maskIndex = var6;
      }

      if (this.previous != null) {
         this.draw(var1, this.previous, var2, var3, 2.0F);
      }

      if (var6 >= 0) {
         var1.shellMask[var6 * 3] = this.piece == null ? 0 : this.piece.mask();
         var1.shellMask[var6 * 3 + 1] = Dissolve.share(var3);
         var1.shellMask[var6 * 3 + 2] = this.previous == null ? 0 : this.previous.mask();
      }
   }

   private FarGpu.BlockDraw draw(SceneData var1, ShellBlock.Piece var2, Frame var3, float var4, float var5) {
      if (var1.shellCount == var1.shells.length) {
         var1.shells = Arrays.copyOf(var1.shells, var1.shells.length * 2);
      }

      FarGpu.BlockDraw var6 = var1.shells[var1.shellCount];
      if (var6 == null) {
         var6 = var1.shells[var1.shellCount] = new FarGpu.BlockDraw();
      }

      var1.shellCount++;
      var6.shell = var2.shell;
      var6.inside = var2.inside;
      var6.ground = var2.ground;
      var6.lite = var2.tier == 1;
      var6.distance = this.distance;
      var6.maskIndex = -1;
      var6.fadeLo = var4;
      var6.fadeHi = var5;
      byte var7 = 4;
      var6.worldX = Math.floorDiv(this.bx, var7) * 256;
      var6.worldY = Math.floorDiv(this.by, var7) * 256;
      var6.blockX = this.bx * 64 - var6.worldX;
      var6.blockY = this.by * 64 - var6.worldY;
      var6.originX = (float)((double)var3.camX - var6.worldX);
      var6.originY = -var3.camZ * 2.4494896F;
      var6.originZ = (float)((double)var3.camY - var6.worldY);
      var6.top = this.top + Relief.top(var1, Math.floorDiv(this.bx, var7), Math.floorDiv(this.by, var7));
      var6.reliefLow = Relief.low(var1, Math.floorDiv(this.bx, var7), Math.floorDiv(this.by, var7));
      return var6;
   }

   static final class Piece {
      final FarGpu.Shell shell;
      final FarGpu.Mesh inside;
      final GroundPage ground;
      final int tier;

      Piece(FarGpu.Shell var1, FarGpu.Mesh var2, GroundPage var3, int var4) {
         this.shell = var1;
         this.inside = var2;
         this.ground = var3;
         this.tier = var4;
      }

      long bytes() {
         return (this.shell == null ? 0L : this.shell.bytes())
            + (this.inside == null ? 0L : this.inside.bytes())
            + (this.ground == null ? 0L : this.ground.bytes());
      }

      boolean uploaded() {
         return (this.shell == null || this.shell.uploaded()) && (this.inside == null || this.inside.uploaded());
      }

      int vertices() {
         return this.shell == null ? 0 : this.shell.vertices;
      }

      byte mask() {
         return (byte)(this.tier == 0 ? 2 : (this.ground != null ? 3 : 1));
      }

      void retire() {
         FarGpu.retire(this.shell);
         FarGpu.retire(this.inside);
      }
   }
}
