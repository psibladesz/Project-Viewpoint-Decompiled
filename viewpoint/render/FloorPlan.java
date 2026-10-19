package viewpoint.render;

import java.nio.IntBuffer;
import java.util.Arrays;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;

final class FloorPlan {
   static final int SIZE = 128;
   static final int KNOWN = 1073741824;
   static final int INTERIOR = 256;
   private static final int SQUARES = 64;
   private final int texture;
   private final int[][] written = new int[1280][];
   private final int[][] writtenWalls = new int[1280][];
   private final long[] keys = new long[1280];
   private long[] interior = new long[0];
   private final IntBuffer texels = BufferUtils.createIntBuffer(192);

   FloorPlan() {
      int var1 = GL11.glGetInteger(35869);
      this.texture = GL11.glGenTextures();
      GL11.glBindTexture(35866, this.texture);
      GL12.glTexImage3D(35866, 0, 36209, 128, 128, 5, 0, 36248, 5125, BufferUtils.createIntBuffer(245760));
      GL11.glTexParameteri(35866, 10241, 9728);
      GL11.glTexParameteri(35866, 10240, 9728);
      GL11.glBindTexture(35866, var1);
      Arrays.fill(this.keys, Long.MIN_VALUE);
   }

   void update(PlanSnapshot var1) {
      if (var1.interior != this.interior) {
         this.interior = var1.interior;
         Arrays.fill(this.keys, Long.MIN_VALUE);
      }

      GL13.glActiveTexture(34094);
      GL11.glBindTexture(35866, this.texture);
      GL11.glPixelStorei(3317, 4);

      for (int var2 = 0; var2 < 5; var2++) {
         for (int var3 = 0; var3 < 16; var3++) {
            for (int var4 = 0; var4 < 16; var4++) {
               int var5 = (var2 * 16 + var3) * 16 + var4;
               this.write(var1, var5, var1.chunkX + var4, var1.chunkY + var3, var1.level + var2);
            }
         }
      }

      GL13.glActiveTexture(33984);
   }

   private void write(PlanSnapshot var1, int var2, int var3, int var4, int var5) {
      int var6 = Math.floorMod(var3, 16);
      int var7 = Math.floorMod(var4, 16);
      int var8 = Math.floorMod(var5, 5);
      int var9 = (var8 * 16 + var7) * 16 + var6;
      long var10 = (var3 & 16777215L) << 32 | (var4 & 16777215L) << 8 | var5 & 0xFF;
      int[] var12 = var1.passes[var2];
      int[] var13 = var1.walls[var2];
      if (this.keys[var9] != var10 || this.written[var9] != var12 || this.writtenWalls[var9] != var13) {
         this.keys[var9] = var10;
         this.written[var9] = var12;
         this.writtenWalls[var9] = var13;
         byte[] var14 = var1.flags[var2];
         long[] var15 = var1.rooms[var2];
         this.texels.clear();

         for (int var16 = 0; var16 < 64; var16++) {
            boolean var17 = var15 != null && var15[var16] != -1L && Arrays.binarySearch(this.interior, var15[var16]) >= 0;
            this.texels
               .put(var12 == null ? 0 : var12[var16] | 1073741824)
               .put(var14 == null ? 0 : var14[var16] & 0xFF | (var17 ? 256 : 0))
               .put(var13 == null ? 0 : var13[var16]);
         }

         this.texels.flip();
         byte var18 = 8;
         GL12.glTexSubImage3D(35866, 0, var6 * var18, var7 * var18, var8, var18, var18, 1, 36248, 5125, this.texels);
      }
   }

   void bind() {
      GL13.glActiveTexture(34094);
      GL11.glBindTexture(35866, this.texture);
      GL13.glActiveTexture(33984);
   }
}
