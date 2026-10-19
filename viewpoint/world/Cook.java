package viewpoint.world;

import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import org.lwjgl.system.MemoryUtil;
import viewpoint.render.ChunkMeshData;
import zombie.core.textures.TextureID;

public final class Cook {
   private static final float ON_WALL = 0.04F;
   private static final float ON_FLOOR = 0.006F;
   private static final ThreadLocal<Cook.Cooker> COOKER = ThreadLocal.withInitial(Cook.Cooker::new);

   static ChunkMeshData mesh(Recipe var0) {
      Cook.Cooked var1 = vertices(var0);
      if (var1 == null) {
         return null;
      }

      ChunkMeshData var2 = new ChunkMeshData(var1.pages(), var1.first(), var1.count(), var1.verts(), var0.level, var1.runs(), var1.pageRuns());
      var2.plants(var1.plantBase(), var1.plantFirst(), var1.plantCount());
      var2.models(var1.modelBase(), var1.modelRuns());

      for (int var3 = 0; var3 < var0.batches.size(); var3++) {
         var2.floorPage = var0.batches.get(var3).floor ? var3 : var2.floorPage;
      }

      return var2;
   }

   public static Cook.Cooked vertices(Recipe var0) {
      Cook.Cooker var1 = COOKER.get();
      var1.n = 0;
      var1.plantFloats = 0;
      var1.runCount = 0;
      var1.owned = false;
      int var2 = var0.batches.size();
      TextureID[] var3 = new TextureID[var2];
      int[] var4 = new int[var2];
      int[] var5 = new int[var2];
      int[] var6 = new int[var2 + 1];
      int[] var7 = new int[var2];
      int[] var8 = new int[var2];

      for (int var9 = 0; var9 < var2; var9++) {
         Recipe.Batch var10 = var0.batches.get(var9);
         var3[var9] = var10.page;
         var4[var9] = var1.n / 14;
         var6[var9] = var1.runCount;
         var7[var9] = var1.plantFloats / 14;

         for (int var11 = 0; var11 < var10.ops.size(); var11++) {
            Recipe.Op var12 = var10.ops.get(var11);
            if (var12.kind == 4) {
               var1.plant(var12);
            } else {
               int var13 = var1.n;
               var1.run(var12);
               var1.claim(var12.owner, var13 / 14, var1.n / 14, var6[var9]);
            }
         }

         var5[var9] = var1.n / 14 - var4[var9];
         var8[var9] = var1.plantFloats / 14 - var7[var9];
      }

      var6[var2] = var1.runCount;
      int[] var14 = var1.models(var0.models);
      var1.surfaces.clear();
      if (var1.n == 0 && var1.plantFloats == 0 && var14 == null) {
         return null;
      }

      FloatBuffer var15 = MemoryUtil.memAllocFloat(var1.n + var1.plantFloats + var1.modelFloats)
         .put(var1.out, 0, var1.n)
         .put(var1.plants, 0, var1.plantFloats)
         .put(var1.instances, 0, var1.modelFloats)
         .flip();
      return new Cook.Cooked(
         var3,
         var4,
         var5,
         var15,
         var1.owned ? Arrays.copyOf(var1.runs, var1.runCount * 3) : null,
         var1.owned ? var6 : null,
         var1.n / 14,
         var7,
         var8,
         (var1.n + var1.plantFloats) / 14,
         var14
      );
   }

   static int put(
      float[] var0,
      int var1,
      float var2,
      float var3,
      float var4,
      float var5,
      float var6,
      float var7,
      float var8,
      float var9,
      float var10,
      float var11,
      float var12,
      float var13,
      float var14,
      int var15
   ) {
      var0[var1++] = var2;
      var0[var1++] = var3;
      var0[var1++] = var4;
      var0[var1++] = var5;
      var0[var1++] = var6;
      var0[var1++] = var7;
      var0[var1++] = var8;
      var0[var1++] = var9;
      var0[var1++] = var10;
      var0[var1++] = var11;
      var0[var1++] = var12;
      var0[var1++] = var13;
      var0[var1++] = var14;
      var0[var1++] = Float.intBitsToFloat(var15);
      return var1;
   }

   private static boolean onWall(float[] var0, int var1) {
      boolean var2 = true;
      boolean var3 = true;

      for (int var4 = var1; var4 < var1 + 24; var4 += 8) {
         if (Math.abs(var0[var4 + 6]) > 0.3F) {
            return false;
         }

         var2 &= Math.abs(var0[var4 + 2] + 0.5F) < 0.04F;
         var3 &= Math.abs(var0[var4] + 0.5F) < 0.04F;
      }

      return var2 || var3;
   }

   private static boolean onFloor(float[] var0, int var1) {
      for (int var2 = var1; var2 < var1 + 24; var2 += 8) {
         if (Math.abs(var0[var2 + 6]) < 0.9F || Math.abs(var0[var2 + 1]) > 0.006F) {
            return false;
         }
      }

      return true;
   }

   private Cook() {
   }

   public record Cooked(
      TextureID[] pages,
      int[] first,
      int[] count,
      FloatBuffer verts,
      int[] runs,
      int[] pageRuns,
      int plantBase,
      int[] plantFirst,
      int[] plantCount,
      int modelBase,
      int[] modelRuns
   ) {
   }

   private static final class Cooker {
      float[] out = new float[114688];
      int n;
      int[] runs = new int[768];
      int runCount;
      boolean owned;
      float[] plants = new float[14336];
      int plantFloats;
      final IdentityHashMap<RoofWindow, RoofSurface> surfaces = new IdentityHashMap<>();
      float[] instances = new float[3584];
      int modelFloats;

      int[] models(List<Recipe.Op> var1) {
         this.modelFloats = 0;
         if (var1 != null && !var1.isEmpty()) {
            Recipe.Op[] var2 = var1.toArray(new Recipe.Op[0]);
            Arrays.sort(var2, Comparator.comparing(var0 -> var0.model.obj.getPath()));
            if (var2.length * 14 > this.instances.length) {
               this.instances = new float[var2.length * 14 * 2];
            }

            int[] var3 = new int[var2.length * 3];
            int var4 = 0;

            for (int var5 = 0; var5 < var2.length; var5++) {
               Recipe.Op var6 = var2[var5];
               int var7 = this.modelFloats;
               this.instances[var7] = -var6.dx;
               this.instances[var7 + 1] = var6.height;
               this.instances[var7 + 2] = -var6.dy;
               this.instances[var7 + 3] = var6.cos;
               this.instances[var7 + 4] = var6.sin;
               this.instances[var7 + 5] = var6.scale;
               this.instances[var7 + 6] = Float.intBitsToFloat(var6.flags);
               this.instances[var7 + 7] = Float.intBitsToFloat(var6.square);
               this.instances[var7 + 8] = Float.intBitsToFloat(var6.model.index);
               Arrays.fill(this.instances, var7 + 9, var7 + 14, 0.0F);
               this.modelFloats += 14;
               if (var4 > 0 && var3[(var4 - 1) * 3] == var6.model.index) {
                  var3[(var4 - 1) * 3 + 2]++;
               } else {
                  var3[var4 * 3] = var6.model.index;
                  var3[var4 * 3 + 1] = var5;
                  var3[var4 * 3 + 2] = 1;
                  var4++;
               }
            }

            return Arrays.copyOf(var3, var4 * 3);
         } else {
            return null;
         }
      }

      void plant(Recipe.Op var1) {
         int var2 = var1.face;
         int var3 = Math.max(1, var2);
         if (this.plantFloats + var3 * 14 > this.plants.length) {
            this.plants = Arrays.copyOf(this.plants, Math.max(this.plants.length * 2, this.plantFloats + var3 * 14));
         }

         for (int var4 = 0; var4 < var3; var4++) {
            int var5 = var1.flags | (var2 == 0 ? 512 : (var4 | var2 << 3) << 16);
            int var6 = this.plantFloats;
            this.plants[var6++] = -var1.dx;
            this.plants[var6++] = var1.height;
            this.plants[var6++] = -var1.dy;
            this.plants[var6++] = Float.intBitsToFloat(var5);
            System.arraycopy(var1.map, 0, this.plants, var6, 4);
            System.arraycopy(var1.card, 0, this.plants, var6 + 4, 4);
            Arrays.fill(this.plants, var6 + 8, this.plantFloats + 14, 0.0F);
            this.plantFloats += 14;
         }
      }

      void claim(int var1, int var2, int var3, int var4) {
         if (var3 != var2) {
            int var5 = (this.runCount - 1) * 3;
            if (this.runCount > var4 && this.runs[var5] == var1 && this.runs[var5 + 1] + this.runs[var5 + 2] == var2) {
               this.runs[var5 + 2] = this.runs[var5 + 2] + (var3 - var2);
            } else {
               if (this.runCount * 3 == this.runs.length) {
                  this.runs = Arrays.copyOf(this.runs, this.runs.length * 2);
               }

               this.runs[this.runCount * 3] = var1;
               this.runs[this.runCount * 3 + 1] = var2;
               this.runs[this.runCount * 3 + 2] = var3 - var2;
               this.runCount++;
               this.owned |= var1 != -1;
            }
         }
      }

      void run(Recipe.Op var1) {
         switch (var1.kind) {
            case 0:
               this.emit(var1);
               break;
            case 1:
               this.face(var1);
               break;
            case 2:
               this.caps(var1);
               break;
            case 3:
            case 4:
            case 5:
            default:
               this.copy(var1.raw);
               break;
            case 6:
               this.copy(RoofCook.vertices(this.surfaces.computeIfAbsent(var1.roof, RoofWindow::build), var1.roof, var1));
               break;
            case 7:
               this.copy(RoofGables.vertices(this.surfaces.computeIfAbsent(var1.roof, RoofWindow::build), var1.roof, var1));
         }
      }

      private void copy(float[] var1) {
         this.reserve(var1.length / 14);
         System.arraycopy(var1, 0, this.out, this.n, var1.length);
         this.n += var1.length;
      }

      void reserve(int var1) {
         int var2 = var1 * 14;
         if (this.n + var2 > this.out.length) {
            this.out = Arrays.copyOf(this.out, Math.max(this.out.length * 2, this.n + var2));
         }
      }

      private void caps(Recipe.Op var1) {
         float[] var2 = var1.map;
         float[] var3 = var1.mesh.data;
         float var4 = var1.dx;
         float var5 = var1.dy;
         float var6 = var1.height;
         this.reserve(var1.mesh.vertCount);

         for (int var7 = 0; var7 < var3.length; var7 += 24) {
            float var8 = (var3[var7 + 5] + var3[var7 + 8 + 5] + var3[var7 + 16 + 5]) / 3.0F;
            float var9 = (var3[var7 + 7] + var3[var7 + 8 + 7] + var3[var7 + 16 + 7]) / 3.0F;
            if (!(Math.abs(var9) > 0.2F) && !(Math.abs(var8) > 0.2F)) {
               for (int var10 = var7; var10 < var7 + 24; var10 += 8) {
                  this.n = Cook.put(
                     this.out,
                     this.n,
                     -(var4 + var3[var10]),
                     var6 + var3[var10 + 1],
                     -(var5 + var3[var10 + 2]),
                     var2[4] + var3[var10 + 3] * var2[6],
                     var2[5] + var3[var10 + 4] * var2[7],
                     var2[0],
                     var2[1],
                     var2[2],
                     var2[3],
                     -var3[var10 + 5],
                     var3[var10 + 6],
                     -var3[var10 + 7],
                     0.0F,
                     var1.lift
                  );
               }
            }
         }
      }

      private void face(Recipe.Op var1) {
         float[] var2 = var1.map;
         float[] var3 = var1.mesh.data;
         float var4 = var1.dx;
         float var5 = var1.dy;
         float var6 = var1.height;
         float var7 = var1.dirX;
         float var8 = var1.dirZ;
         float var9 = var1.fill;
         int var10 = var1.face;
         int var11 = var1.flags;
         this.reserve(var1.mesh.vertCount);

         for (int var12 = 0; var12 < var3.length; var12 += 24) {
            float var13 = (var3[var12 + 5] + var3[var12 + 8 + 5] + var3[var12 + 16 + 5]) / 3.0F;
            float var14 = (var3[var12 + 7] + var3[var12 + 8 + 7] + var3[var12 + 16 + 7]) / 3.0F;
            float var15 = Math.abs(var13 * var7 + var14 * var8);
            float var16 = Math.abs(var13 * var8 + var14 * var7);
            boolean var17 = var15 > 0.2F && (var8 > 0.5F ? var15 >= var16 : var15 > var16);
            if (var10 == 2 ? !var17 : var17) {
               float var18 = var10 == 1 ? -1.0F : 1.0F;

               for (int var19 = var12; var19 < var12 + 24; var19 += 8) {
                  float var20 = var10 == 2 ? -var3[var19 + 5] : -var7 * var18;
                  float var21 = var10 == 2 ? var3[var19 + 6] : 0.0F;
                  float var22 = var10 == 2 ? -var3[var19 + 7] : -var8 * var18;
                  this.n = Cook.put(
                     this.out,
                     this.n,
                     -(var4 + var3[var19]),
                     var6 + var3[var19 + 1],
                     -(var5 + var3[var19 + 2]),
                     var2[4] + var3[var19 + 3] * var2[6],
                     var2[5] + var3[var19 + 4] * var2[7],
                     var2[0],
                     var2[1],
                     var2[2],
                     var2[3],
                     var20,
                     var21,
                     var22,
                     var10 == 1 ? var9 : 0.0F,
                     (var10 == 2 ? 0 : 1) + (var10 == 1 ? 8 : 0) + var11 + var1.lift
                  );
               }
            }
         }
      }

      private void emit(Recipe.Op var1) {
         float[] var2 = var1.map;
         float[] var3 = var1.mesh.data;
         float var4 = var1.dx;
         float var5 = var1.dy;
         float var6 = var1.height;
         float var7 = var2[8];
         float var8 = var2[9];
         float var9 = var2[10];
         int var10 = var1.flags & -57344;
         int var11 = var1.flags & 57343;
         this.reserve(var1.mesh.vertCount);

         for (int var12 = 0; var12 < var3.length; var12 += 24) {
            int var13 = (var11 != 0 || !Cook.onWall(var3, var12) && !Cook.onFloor(var3, var12) ? var11 : 2) | var10;

            for (int var14 = var12; var14 < var12 + 24; var14 += 8) {
               this.n = Cook.put(
                  this.out,
                  this.n,
                  -(var4 + var3[var14]),
                  var6 + var3[var14 + 1],
                  -(var5 + var3[var14 + 2]),
                  var2[4] + var3[var14 + 3] * var7 * var8,
                  var2[5] + var3[var14 + 4] * var7 * var9,
                  var2[0],
                  var2[1],
                  var2[2],
                  var2[3],
                  -var3[var14 + 5],
                  var3[var14 + 6],
                  -var3[var14 + 7],
                  0.0F,
                  var13 + var1.lift
               );
            }
         }
      }
   }
}
