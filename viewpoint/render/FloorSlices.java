package viewpoint.render;

import java.util.ArrayList;
import java.util.Arrays;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL42;

final class FloorSlices {
   static final int LAYERS = 32;
   static final long ARRAY_BYTES = 41943040L;
   final int size;
   final int mips;
   final int perArray;
   final boolean surfaces;
   private final ArrayList<int[]> arrays = new ArrayList<>();
   private boolean[] used = new boolean[0];
   private int inUse;

   FloorSlices(int var1, int var2, boolean var3) {
      this(var1, var2, 32, var3);
   }

   static FloorSlices bounded(int var0, int var1, boolean var2) {
      FloorSlices var3 = new FloorSlices(var0, var1, var2);
      return new FloorSlices(var0, var1, (int)Math.max(1L, 41943040L / var3.sliceBytes()), var2);
   }

   FloorSlices(int var1, int var2, int var3, boolean var4) {
      this.size = var1;
      this.mips = var2;
      this.perArray = Math.max(1, Math.min(32, var3));
      this.surfaces = var4;
   }

   long sliceBytes() {
      long var1 = 0L;

      for (int var3 = 0; var3 < this.mips; var3++) {
         long var4 = Math.max(1, this.size >> var3);
         var1 += var4 * var4 * 4L;
      }

      return this.surfaces ? 2L * var1 : var1;
   }

   long arrayBytes() {
      return this.sliceBytes() * this.perArray;
   }

   long madeBytes() {
      byte var1 = 0;

      for (int[] var3 : this.arrays) {
         var1 += var3 != null ? 1 : 0;
      }

      return var1 * this.arrayBytes();
   }

   int inUse() {
      return this.inUse;
   }

   int take(long var1) {
      for (int var3 = 0; var3 < this.used.length; var3++) {
         if (!this.used[var3] && var3 % 32 < this.perArray && this.arrays.get(var3 / 32) != null) {
            return this.claim(var3);
         }
      }

      if (this.madeBytes() + this.arrayBytes() > var1) {
         return -1;
      }

      int var5 = this.arrays.indexOf(null);
      var5 = var5 < 0 ? this.arrays.size() : var5;
      int[] var4 = new int[]{this.make(), 0, this.surfaces ? this.make() : 0};
      if (var5 == this.arrays.size()) {
         this.arrays.add(var4);
         this.used = Arrays.copyOf(this.used, this.arrays.size() * 32);
      } else {
         this.arrays.set(var5, var4);
      }

      return this.claim(var5 * 32);
   }

   private int claim(int var1) {
      this.used[var1] = true;
      this.arrays.get(var1 / 32)[1]++;
      this.inUse++;
      return var1;
   }

   void free(int var1) {
      if (var1 >= 0 && this.used[var1]) {
         this.used[var1] = false;
         this.inUse--;
         int[] var2 = this.arrays.get(var1 / 32);
         if (--var2[1] == 0) {
            letGo(var2);
            this.arrays.set(var1 / 32, null);
         }
      }
   }

   Object array(int var1) {
      return this.arrays.get(var1 / 32);
   }

   int texture(int var1) {
      return this.arrays.get(var1 / 32)[0];
   }

   int surface(int var1) {
      return this.arrays.get(var1 / 32)[2];
   }

   static int layer(int var0) {
      return var0 % 32;
   }

   void clear() {
      for (int[] var2 : this.arrays) {
         if (var2 != null) {
            letGo(var2);
         }
      }

      this.arrays.clear();
      this.used = new boolean[0];
      this.inUse = 0;
   }

   private static void letGo(int[] var0) {
      int var1 = var0[0];
      int var2 = var0[2];
      Retirement.retireDrawing(() -> GL11.glDeleteTextures(var2 == 0 ? new int[]{var1} : new int[]{var1, var2}));
   }

   private int make() {
      int var1 = GL11.glGetInteger(35869);
      int var2 = GL11.glGenTextures();
      GL11.glBindTexture(35866, var2);
      GL42.glTexStorage3D(35866, this.mips, 32856, this.size, this.size, this.perArray);
      GL11.glTexParameteri(35866, 10241, 9987);
      GL11.glTexParameteri(35866, 10240, 9729);
      GL11.glTexParameteri(35866, 10242, 33071);
      GL11.glTexParameteri(35866, 10243, 33071);
      GL11.glTexParameteri(35866, 33085, this.mips - 1);
      if (GL.getCapabilities().GL_EXT_texture_filter_anisotropic) {
         float var3 = GL11.glGetFloat(34047);
         GL11.glTexParameterf(35866, 34046, Math.min(16.0F, var3));
      }

      GL11.glBindTexture(35866, var1);
      System.out
         .println("[Viewpoint] floor pages: an array of " + this.perArray + " slices of " + this.size + " texels made (" + (this.arrayBytes() >> 20) + " MB)");
      return var2;
   }
}
