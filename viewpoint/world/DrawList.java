package viewpoint.world;

import java.util.Arrays;
import org.joml.Matrix4f;
import viewpoint.platform.LongMap;
import viewpoint.render.ChunkMeshData;
import viewpoint.render.FloorPage;
import viewpoint.render.SceneData;
import viewpoint.render.WorldRenderer;
import viewpoint.visibility.Rooms;

final class DrawList {
   private static final int MASK = 48;
   private static final Matrix4f lightView = new Matrix4f();
   private static final boolean[] mask = new boolean[2304];
   private static ChunkMeshData[] candMesh = new ChunkMeshData[1024];
   private static float[] candOffset = new float[3072];
   private static float[] candDistance = new float[1024];
   private static float[] candFoot = new float[4096];
   private static float[] candFade = new float[2048];
   private static byte[] candFlags = new byte[1024];
   private static int[] candChunk = new int[2048];
   private static BodyCards[] candBodies = new BodyCards[1024];
   private static int candidates;
   private static final LongMap<ChunkMeshData> handed = new LongMap<>();

   static void begin(SceneData var0, boolean var1) {
      if (var1) {
         boolean var2 = Math.abs(var0.sunY) > 0.99F;
         lightView.setLookAt(var0.sunX, var0.sunY, var0.sunZ, 0.0F, 0.0F, 0.0F, 0.0F, var2 ? 0.0F : 1.0F, var2 ? 1.0F : 0.0F);
      }

      candidates = 0;
   }

   static void candidate(
      ChunkMeshData var0,
      float var1,
      float var2,
      float var3,
      byte var4,
      float var5,
      boolean var6,
      float var7,
      float var8,
      int var9,
      float var10,
      float var11,
      float var12,
      float var13,
      float var14,
      int var15,
      int var16,
      BodyCards var17
   ) {
      if (candidates == candMesh.length) {
         candChunk = Arrays.copyOf(candChunk, candidates * 4);
         candMesh = Arrays.copyOf(candMesh, candidates * 2);
         candBodies = Arrays.copyOf(candBodies, candidates * 2);
         candOffset = Arrays.copyOf(candOffset, candidates * 6);
         candDistance = Arrays.copyOf(candDistance, candidates * 2);
         candFoot = Arrays.copyOf(candFoot, candidates * 8);
         candFade = Arrays.copyOf(candFade, candidates * 4);
         candFlags = Arrays.copyOf(candFlags, candidates * 2);
      }

      candMesh[candidates] = var0;
      candBodies[candidates] = var17;
      candOffset[candidates * 3] = var1;
      candOffset[candidates * 3 + 1] = var2;
      candOffset[candidates * 3 + 2] = var3;
      candDistance[candidates] = var5;
      candFlags[candidates] = var4;
      candFade[candidates * 2] = var13;
      candFade[candidates * 2 + 1] = var14;
      candChunk[candidates * 2] = var15;
      candChunk[candidates * 2 + 1] = var16;
      if (var6) {
         float var18 = -(var7 + 4.0F);
         float var19 = -(var7 - 4.0F);
         float var20 = -(var8 + 4.0F);
         float var21 = -(var8 - 4.0F);
         float var22 = var9 * 2.4494896F - (var10 - 1.5F);
         float var23 = var22 + var11;
         float var24 = var22 + 2.4494896F + 2.0F + var12;
         float var25 = Float.MAX_VALUE;
         float var26 = -Float.MAX_VALUE;
         float var27 = Float.MAX_VALUE;
         float var28 = -Float.MAX_VALUE;

         for (int var29 = 0; var29 < 8; var29++) {
            float var30 = (var29 & 1) == 0 ? var18 : var19;
            float var31 = (var29 & 2) == 0 ? var23 : var24;
            float var32 = (var29 & 4) == 0 ? var20 : var21;
            float var33 = lightView.m00() * var30 + lightView.m10() * var31 + lightView.m20() * var32;
            float var34 = lightView.m01() * var30 + lightView.m11() * var31 + lightView.m21() * var32;
            var25 = Math.min(var25, var33);
            var26 = Math.max(var26, var33);
            var27 = Math.min(var27, var34);
            var28 = Math.max(var28, var34);
         }

         candFoot[candidates * 4] = var25;
         candFoot[candidates * 4 + 1] = var26;
         candFoot[candidates * 4 + 2] = var27;
         candFoot[candidates * 4 + 3] = var28;
      }

      candidates++;
   }

   static void commit(SceneData var0, boolean var1, int var2) {
      if (var1) {
         Arrays.fill(mask, false);
         float var3 = var0.shadowReach + 24.0F;
         float var4 = 2.0F * var3 / 48.0F;

         for (int var5 = 0; var5 < candidates; var5++) {
            if ((candFlags[var5] & 1) != 0) {
               stamp(var5, var3, var4);
            }
         }

         for (int var9 = 0; var9 < candidates; var9++) {
            if (covered(var9, var3, var4)) {
               byte var6 = candFlags[var9];
               if (overlaps(var9, WorldRenderer.cascadeRadius(0, var0) + 2.0F)) {
                  var6 = (byte)(var6 | 2);
               }

               if (var2 > 1 && overlaps(var9, WorldRenderer.cascadeRadius(1, var0) + 2.0F)) {
                  var6 = (byte)(var6 | 16);
               }

               if (var2 > 2 && overlaps(var9, WorldRenderer.cascadeRadius(2, var0) + 2.0F)) {
                  var6 = (byte)(var6 | 8);
               }

               candFlags[var9] = var6;
            }
         }
      }

      for (int var7 = 0; var7 < candidates; var7++) {
         if (candFlags[var7] != 0) {
            handed.put(FloorPage.key(candChunk[var7 * 2], candChunk[var7 * 2 + 1], candMesh[var7].level), candMesh[var7]);
         }
      }

      for (int var8 = 0; var8 < candidates; var8++) {
         if (candFlags[var8] != 0) {
            hand(var0, var8);
         }

         candMesh[var8] = null;
         candBodies[var8] = null;
      }

      handed.clear();
      var0.sortFrontToBack();
   }

   private static void hand(SceneData var0, int var1) {
      byte var2 = candFlags[var1];
      int var3 = var0.meshCount;
      var0.addMesh(
         candMesh[var1],
         candOffset[var1 * 3],
         candOffset[var1 * 3 + 1],
         candOffset[var1 * 3 + 2],
         var2,
         candDistance[var1],
         candFade[var1 * 2],
         candFade[var1 * 2 + 1]
      );
      var0.meshFloor[var3] = candMesh[var1].floorPage < 0 ? null : FloorPages.page(candChunk[var1 * 2], candChunk[var1 * 2 + 1], candMesh[var1].level);
      var0.meshBelow[var3] = handed.get(FloorPage.key(candChunk[var1 * 2], candChunk[var1 * 2 + 1], candMesh[var1].level - 1));
      var0.meshAbove[var3] = handed.get(FloorPage.key(candChunk[var1 * 2], candChunk[var1 * 2 + 1], candMesh[var1].level + 1));
      if ((var2 & 5) != 0) {
         Rooms.plan(var0, var3, candMesh[var1], candChunk[var1 * 2], candChunk[var1 * 2 + 1]);
         if (candBodies[var1] != null) {
            candBodies[var1].into(var0, var3);
         }
      }
   }

   private static boolean overlaps(int var0, float var1) {
      return candFoot[var0 * 4] < var1 && candFoot[var0 * 4 + 1] > -var1 && candFoot[var0 * 4 + 2] < var1 && candFoot[var0 * 4 + 3] > -var1;
   }

   private static void stamp(int var0, float var1, float var2) {
      int var3 = cellOf(candFoot[var0 * 4], var1, var2);
      int var4 = cellOf(candFoot[var0 * 4 + 1], var1, var2);
      int var5 = cellOf(candFoot[var0 * 4 + 2], var1, var2);
      int var6 = cellOf(candFoot[var0 * 4 + 3], var1, var2);

      for (int var7 = var5; var7 <= var6; var7++) {
         for (int var8 = var3; var8 <= var4; var8++) {
            mask[var7 * 48 + var8] = true;
         }
      }
   }

   private static boolean covered(int var0, float var1, float var2) {
      int var3 = cellOf(candFoot[var0 * 4], var1, var2);
      int var4 = cellOf(candFoot[var0 * 4 + 1], var1, var2);
      int var5 = cellOf(candFoot[var0 * 4 + 2], var1, var2);
      int var6 = cellOf(candFoot[var0 * 4 + 3], var1, var2);

      for (int var7 = var5; var7 <= var6; var7++) {
         for (int var8 = var3; var8 <= var4; var8++) {
            if (mask[var7 * 48 + var8]) {
               return true;
            }
         }
      }

      return false;
   }

   private static int cellOf(float var0, float var1, float var2) {
      return Math.max(0, Math.min(47, (int)((var0 + var1) / var2)));
   }

   private DrawList() {
   }
}
