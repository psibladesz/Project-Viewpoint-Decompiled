package viewpoint.interact;

import java.util.Arrays;
import viewpoint.visibility.Edges;
import viewpoint.world.TileMesh;
import viewpoint.world.TileMeshes;
import viewpoint.world.WorldMesher;
import zombie.core.textures.Texture;
import zombie.iso.IsoGridSquare;
import zombie.iso.IsoObject;
import zombie.iso.sprite.IsoSprite;
import zombie.pathfind.VehiclePoly;
import zombie.vehicles.BaseVehicle;

final class LootBoxes {
   private static final float THIN = 0.1F;
   private static final float PLAIN_TALL = 0.81649655F;
   private static final float VEHICLE_TALL = 1.0F;
   private static final float ANIMAL_LONG = 2.0F;
   private static final float ANIMAL_TALL = 3.0F;
   private static final float[] packBox = new float[6];
   private static float[] boxes = new float[144];

   static float[] all() {
      return boxes;
   }

   static void empty(int var0) {
      if (boxes.length < (var0 + 1) * 9) {
         boxes = Arrays.copyOf(boxes, Math.max(boxes.length * 2, (var0 + 1) * 9));
      }

      int var1 = var0 * 9;

      for (int var2 = 0; var2 < 3; var2++) {
         boxes[var1 + var2] = Float.MAX_VALUE;
         boxes[var1 + 3 + var2] = -Float.MAX_VALUE;
      }

      boxes[var1 + 6] = 1.0F;
      boxes[var1 + 7] = 0.0F;
      boxes[var1 + 8] = -1.0F;
   }

   static void grow(int var0, float var1, float var2, float var3, float var4, float var5, float var6) {
      int var7 = var0 * 9;
      boxes[var7] = Math.min(boxes[var7], var1);
      boxes[var7 + 1] = Math.min(boxes[var7 + 1], var2);
      boxes[var7 + 2] = Math.min(boxes[var7 + 2], var3);
      boxes[var7 + 3] = Math.max(boxes[var7 + 3], var4);
      boxes[var7 + 4] = Math.max(boxes[var7 + 4], var5);
      boxes[var7 + 5] = Math.max(boxes[var7 + 5], var6);
   }

   static void shape(int var0, IsoObject var1, IsoGridSquare var2) {
      IsoSprite var3 = var1.getSprite();
      if (PackBoxes.of(var1, var2, packBox)) {
         grow(var0, packBox[0], packBox[1], packBox[2], packBox[3], packBox[4], packBox[5]);
         thicken(var0);
      } else {
         Texture var4 = var3 == null ? null : var3.getTextureForCurrentFrame(var1.getDir(), var1);
         TileMesh var5 = var4 == null ? null : TileMeshes.get(var3, var4, null);
         float var6 = var2.x + 0.5F;
         float var7 = var2.y + 0.5F;
         float var8 = var2.z * 2.4494896F + WorldMesher.rise(var1);
         int var9 = var3 == null ? 0 : Edges.ownerKind(var3);
         boolean var10 = var5 != null && var5.vertCount > 0 && !WorldMesher.hasModel(var1);
         int var11 = var0 * 9;
         if (!var10 && var9 == 1) {
            grow(var0, var2.x, var2.y - 0.1F, var8, var2.x + 1.0F, var2.y + 0.1F, var8 + 2.4494896F);
            boxes[var11 + 8] = 1.0F;
         } else if (!var10 && var9 == 2) {
            grow(var0, var2.x - 0.1F, var2.y, var8, var2.x + 0.1F, var2.y + 1.0F, var8 + 2.4494896F);
            boxes[var11 + 8] = 0.0F;
         } else if (var5 != null && var5.vertCount != 0) {
            float[] var12 = var5.data;

            for (int var13 = 0; var13 < var12.length; var13 += 8) {
               grow(
                  var0,
                  var6 + var12[var13],
                  var7 + var12[var13 + 2],
                  var8 + var12[var13 + 1],
                  var6 + var12[var13],
                  var7 + var12[var13 + 2],
                  var8 + var12[var13 + 1]
               );
            }

            boolean var15 = boxes[var11 + 3] - boxes[var11] < 0.2F;
            boolean var14 = boxes[var11 + 4] - boxes[var11 + 1] < 0.2F;
            boxes[var11 + 8] = var15 == var14 ? -1.0F : (var15 ? 0.0F : 1.0F);
            thicken(var0);
         } else {
            grow(var0, var6 - 0.5F, var7 - 0.5F, var8, var6 + 0.5F, var7 + 0.5F, var8 + 0.81649655F);
         }
      }
   }

   static void footprint(int var0, BaseVehicle var1) {
      VehiclePoly var2 = var1.getPoly();
      float[] var3 = new float[]{var2.x1, var2.x2, var2.x3, var2.x4};
      float[] var4 = new float[]{var2.y1, var2.y2, var2.y3, var2.y4};
      byte var5 = 1;

      for (int var6 = 2; var6 < 4; var6++) {
         var5 = (byte)(distance(var3, var4, var6) > distance(var3, var4, var5) ? var6 : var5);
      }

      int var15 = var5 == 1 ? 2 : 1;
      int var7 = var5 == 3 ? 2 : 3;
      float var8 = (float)Math.sqrt(distance(var3, var4, var15));
      float var9 = (float)Math.sqrt(distance(var3, var4, var7));
      float var10 = (var3[0] + var3[var5]) * 0.5F;
      float var11 = (var4[0] + var4[var5]) * 0.5F;
      float var12 = (float)Math.atan2(var4[var15] - var4[0], var3[var15] - var3[0]);
      float var13 = var1.getZ() * 2.4494896F;
      float var14 = var1.getScript() == null ? 1.0F : Math.max(1.0F, var1.getScript().getExtents().y);
      grow(var0, var10 - var8 * 0.5F, var11 - var9 * 0.5F, var13, var10 + var8 * 0.5F, var11 + var9 * 0.5F, var13 + var14);
      boxes[var0 * 9 + 6] = (float)Math.cos(var12);
      boxes[var0 * 9 + 7] = (float)Math.sin(var12);
   }

   static void animal(float[] var0, int var1, float var2, float var3, float var4, float var5, float var6, float var7) {
      int var8 = var1 * 9;
      float var9 = 2.0F * var5;
      var0[var8] = var2 - var9;
      var0[var8 + 1] = var3 - var5;
      var0[var8 + 2] = var4;
      var0[var8 + 3] = var2 + var9;
      var0[var8 + 4] = var3 + var5;
      var0[var8 + 5] = var4 + 3.0F * var5;
      var0[var8 + 6] = var6;
      var0[var8 + 7] = var7;
      var0[var8 + 8] = -1.0F;
   }

   static void animal(int var0, float var1, float var2, float var3, float var4, float var5, float var6) {
      animal(boxes, var0, var1, var2, var3, var4, var5, var6);
   }

   private static float distance(float[] var0, float[] var1, int var2) {
      float var3 = var0[var2] - var0[0];
      float var4 = var1[var2] - var1[0];
      return var3 * var3 + var4 * var4;
   }

   private static void thicken(int var0) {
      int var1 = var0 * 9;

      for (int var2 = 0; var2 < 2; var2++) {
         float var3 = (boxes[var1 + var2] + boxes[var1 + 3 + var2]) * 0.5F;
         boxes[var1 + var2] = Math.min(boxes[var1 + var2], var3 - 0.1F);
         boxes[var1 + 3 + var2] = Math.max(boxes[var1 + 3 + var2], var3 + 0.1F);
      }
   }

   private LootBoxes() {
   }
}
