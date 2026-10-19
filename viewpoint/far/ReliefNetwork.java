package viewpoint.far;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class ReliefNetwork {
   static final float STEP = 4.0F;
   private static final float CLEARANCE = 2.0F;
   private static final float CULVERT = 16.0F;
   private static final float BUCKET = 16.0F;
   private final ReliefRoads streets;
   private final int width;
   private final int height;
   private final int spacing;
   private final float[] natural;
   private final float[] water;
   private final ReliefGround ground;
   private final ReliefRules rules;
   private int[] street;
   private int[] root;
   private float[] xs;
   private float[] ys;
   private float[] widths;
   private int count;
   private List<RoadTees.Tee> tees;
   private int[] teeSample;
   private int[] endSample;
   private float[] profile;
   private RoadGrades grades;

   private ReliefNetwork(ReliefRoads var1, float[] var2, float[] var3, ReliefGround var4, int var5, int var6, int var7, ReliefRules var8) {
      this.streets = var1;
      this.natural = var2;
      this.water = var3;
      this.ground = var4;
      this.width = var5;
      this.height = var6;
      this.spacing = var7;
      this.rules = var8;
   }

   static ReliefNetwork.Roads of(ReliefRoads var0, float[] var1, float[] var2, ReliefGround var3, int var4, int var5, int var6, ReliefRules var7) {
      return built(var0, var1, var2, var3, var4, var5, var6, var7).stamp();
   }

   static ReliefNetwork built(ReliefRoads var0, float[] var1, float[] var2, ReliefGround var3, int var4, int var5, int var6, ReliefRules var7) {
      ReliefNetwork var8 = new ReliefNetwork(var0, var1, var2, var3, var4, var5, var6, var7);
      var8.sample();
      var8.join();
      float[] var9 = var8.midway(var8.own());
      var9 = var8.midway(var8.averaged(var9));
      var8.grades = var8.grades(var8.bridge(var9));
      var8.grades.relax(var9, var8.adjacency(), null);
      var8.profile = var9;
      return var8;
   }

   ReliefNetwork.Roads stamp() {
      return new RoadStamp(this.samples(), this.natural, this.water, this.ground, this.width, this.height, this.spacing, this.rules)
              .stamp(this.profile, this.grades);
   }

   void settle(RoadGrades.Ties var1) {
      int[][] var2 = this.adjacency();
      this.grades.relax(this.profile, var2, var1);
      this.grades.relax(this.profile, var2, null);
   }

   ReliefNetwork.Samples samples() {
      return new ReliefNetwork.Samples(this.street, this.xs, this.ys, this.widths, this.root, this.count);
   }

   private RoadGrades grades(float[] var1) {
      return new RoadGrades(this.street, this.xs, this.ys, this.root, var1, this.count, this.rules);
   }

   private void sample() {
      short var1 = 1024;
      this.street = new int[var1];
      this.xs = new float[var1];
      this.ys = new float[var1];
      this.widths = new float[var1];
      this.tees = RoadTees.of(this.streets);
      this.teeSample = new int[this.tees.size()];
      this.endSample = new int[2 * this.streets.streets.length];
      Map<Long, List<Integer>> var2 = this.teesOn();

      for (int var3 = 0; var3 < this.streets.streets.length; var3++) {
         float[] var4 = this.streets.streets[var3];
         List<Integer> var5 = List.of();
         this.endSample[2 * var3] = this.count;

         for (int var6 = 1; var6 + 3 < var4.length; var6 += 2) {
            int var7 = this.count;
            List<Integer> var8 = this.segment(var3, var6, var4, var2.getOrDefault(key(var3, var6), List.of()));

            for (int var10 : var5) {
               this.teeSample[var10] = var7;
            }

            var5 = var8;
         }

         if (var4.length >= 3) {
            this.add(var3, var4[var4.length - 2], var4[var4.length - 1], var4[0]);
         }

         this.endSample[2 * var3 + 1] = this.count - 1;

         for (int var12 : var5) {
            this.teeSample[var12] = this.count - 1;
         }
      }
   }

   private List<Integer> segment(int var1, int var2, float[] var3, List<Integer> var4) {
      float var5 = var3[var2];
      float var6 = var3[var2 + 1];
      float var7 = var3[var2 + 2];
      float var8 = var3[var2 + 3];
      int var9 = Math.max(1, (int)Math.ceil(Math.hypot(var7 - var5, var8 - var6) / 4.0));
      float[] var10 = new float[var9];
      int[] var11 = new int[var4.size()];
      ArrayList<Integer> var12 = new ArrayList<>();

      for (int var13 = 0; var13 < var9; var13++) {
         var10[var13] = (float)var13 / var9;
      }

      for (int var15 = 0; var15 < var4.size(); var15++) {
         float var14 = this.tees.get((Integer)var4.get(var15)).t();
         var11[var15] = Math.round(var14 * var9);
         if (var11[var15] >= var9) {
            var12.add((Integer)var4.get(var15));
         } else if (var11[var15] > 0) {
            var10[var11[var15]] = var14;
         }
      }

      for (int var16 = 0; var16 < var9; var16++) {
         this.add(var1, var5 + (var7 - var5) * var10[var16], var6 + (var8 - var6) * var10[var16], var3[0]);

         for (int var17 = 0; var17 < var4.size(); var17++) {
            if (var11[var17] == var16) {
               this.teeSample[var4.get(var17)] = this.count - 1;
            }
         }
      }

      return var12;
   }

   private Map<Long, List<Integer>> teesOn() {
      HashMap<Long, List<Integer>> var1 = new HashMap<>();

      for (int var2 = 0; var2 < this.tees.size(); var2++) {
         RoadTees.Tee var3 = this.tees.get(var2);
         var1.computeIfAbsent(key(var3.other(), var3.point()), var0 -> new ArrayList<>()).add(var2);
      }

      var1.values().forEach(var1x -> var1x.sort(Comparator.comparingDouble(var1xx -> this.tees.get(var1xx).t())));
      return var1;
   }

   private void add(int var1, float var2, float var3, float var4) {
      if (this.count == this.street.length) {
         int var5 = this.count * 2;
         this.street = Arrays.copyOf(this.street, var5);
         this.xs = Arrays.copyOf(this.xs, var5);
         this.ys = Arrays.copyOf(this.ys, var5);
         this.widths = Arrays.copyOf(this.widths, var5);
      }

      this.street[this.count] = var1;
      this.xs[this.count] = var2;
      this.ys[this.count] = var3;
      this.widths[this.count] = var4;
      this.count++;
   }

   private void join() {
      this.root = new int[this.count];
      HashMap<Long, int[]> var1 = new HashMap<>();
      float var2 = 16.0F;

      for (int var3 = 0; var3 < this.count; var3++) {
         this.root[var3] = var3;
         long var4 = key((int)Math.floor(this.xs[var3] / var2), (int)Math.floor(this.ys[var3] / var2));
         int[] var6 = (int[])var1.get(var4);
         var6 = var6 == null ? new int[]{0} : var6;
         if (var6[0] + 1 == var6.length) {
            var6 = Arrays.copyOf(var6, var6.length * 2);
         }

         var6[++var6[0]] = var3;
         var1.put(var4, var6);
      }

      HashMap<Integer, Integer> var15 = new HashMap<>();
      int[][] var16 = new int[this.count][];

      for (int var5 = 0; var5 < this.count; var5++) {
         int var21 = (int)Math.floor(this.xs[var5] / var2);
         int var7 = (int)Math.floor(this.ys[var5] / var2);
         var15.clear();

         for (int var8 = -1; var8 <= 1; var8++) {
            for (int var9 = -1; var9 <= 1; var9++) {
               int[] var10 = (int[])var1.get(key(var21 + var9, var7 + var8));

               for (int var11 = 1; var10 != null && var11 <= var10[0]; var11++) {
                  int var12 = var10[var11];
                  float var13 = Math.max(this.widths[var5], this.widths[var12]) * 0.5F;
                  Integer var14 = (Integer)var15.get(this.street[var12]);
                  if (this.street[var12] != this.street[var5]
                          && this.distance(var5, var12) <= var13
                          && (var14 == null || this.distance(var5, var12) < this.distance(var5, var14))) {
                     var15.put(this.street[var12], var12);
                  }
               }
            }
         }

         var16[var5] = var15.values().stream().mapToInt(Integer::intValue).toArray();
      }

      for (int var17 = 0; var17 < this.count; var17++) {
         for (int var25 : var16[var17]) {
            for (int var29 : var16[var25]) {
               if (var29 == var17) {
                  this.union(var17, var25);
               }
            }
         }
      }

      for (int var18 = 0; var18 < this.tees.size(); var18++) {
         this.union(this.endSample[2 * this.tees.get(var18).street() + this.tees.get(var18).end()], this.teeSample[var18]);
      }

      for (int var19 = 0; var19 < this.count; var19++) {
         this.root[var19] = this.find(var19);
      }
   }

   private float distance(int var1, int var2) {
      return (float)Math.hypot(this.xs[var2] - this.xs[var1], this.ys[var2] - this.ys[var1]);
   }

   private float[] own() {
      float[] var1 = new float[this.count];

      for (int var2 = 0; var2 < this.count; var2++) {
         var1[var2] = this.wet(var2) ? Float.NaN : this.at(this.natural, this.xs[var2], this.ys[var2]);
      }

      return var1;
   }

   private float[] midway(float[] var1) {
      float var2 = this.rules.metresASquare(this.rules.grade());
      float[] var3 = this.sweep(var1, var2, false);
      float[] var4 = this.sweep(var1, var2, true);
      float[] var5 = new float[this.count];

      for (int var6 = 0; var6 < this.count; var6++) {
         boolean var7 = Float.isInfinite(var3[var6]) || Float.isInfinite(var4[var6]);
         var5[var6] = var7 ? this.at(this.water, this.xs[var6], this.ys[var6]) + 2.0F : (var3[var6] + var4[var6]) * 0.5F;
      }

      return var5;
   }

   private float[] sweep(float[] var1, float var2, boolean var3) {
      int[][] var4 = this.adjacency();
      float[] var5 = new float[this.count];
      Arrays.fill(var5, Float.POSITIVE_INFINITY);
      IndexHeap var6 = new IndexHeap(this.count);

      for (int var7 = 0; var7 < this.count; var7++) {
         int var8 = this.root[var7];
         if (!Float.isNaN(var1[var7])) {
            float var9 = var3 ? -var1[var7] : var1[var7];
            if (var9 < var5[var8]) {
               var5[var8] = var9;
               var6.push(var8, var9);
            }
         }
      }

      while (!var6.empty()) {
         int var11 = var6.pop();

         for (int var13 = 0; var13 < var4[var11].length; var13 += 2) {
            int var15 = var4[var11][var13];
            float var10 = var5[var11] + var2 * Float.intBitsToFloat(var4[var11][var13 + 1]);
            if (var10 < var5[var15]) {
               var5[var15] = var10;
               var6.push(var15, var10);
            }
         }
      }

      float[] var12 = new float[this.count];

      for (int var14 = 0; var14 < this.count; var14++) {
         var12[var14] = var3 ? -var5[this.root[var14]] : var5[this.root[var14]];
      }

      return var12;
   }

   private int[][] adjacency() {
      int[] var1 = new int[this.count];

      for (int var2 = 0; var2 + 1 < this.count; var2++) {
         if (this.street[var2] == this.street[var2 + 1] && this.root[var2] != this.root[var2 + 1]) {
            var1[this.root[var2]]++;
            var1[this.root[var2 + 1]]++;
         }
      }

      int[][] var7 = new int[this.count][];

      for (int var3 = 0; var3 < this.count; var3++) {
         var7[var3] = new int[2 * var1[var3]];
         var1[var3] = 0;
      }

      for (int var8 = 0; var8 + 1 < this.count; var8++) {
         int var4 = this.root[var8];
         int var5 = this.root[var8 + 1];
         if (this.street[var8] == this.street[var8 + 1] && var4 != var5) {
            int var6 = Float.floatToIntBits((float)Math.hypot(this.xs[var8 + 1] - this.xs[var8], this.ys[var8 + 1] - this.ys[var8]));
            var7[var4][var1[var4]++] = var5;
            var7[var4][var1[var4]++] = var6;
            var7[var5][var1[var5]++] = var4;
            var7[var5][var1[var5]++] = var6;
         }
      }

      return var7;
   }

   private float[] averaged(float[] var1) {
      float var2 = this.rules.grade() * 8.0F / Math.max(this.rules.gradeChange(), 1.0E-4F);
      int[][] var3 = this.adjacency();
      float[] var4 = new float[this.count];
      float[] var5 = new float[this.count];
      float[] var6 = new float[this.count];
      Arrays.fill(var5, Float.POSITIVE_INFINITY);
      int[] var7 = new int[this.count];
      IndexHeap var8 = new IndexHeap(64);

      for (int var9 = 0; var9 < this.count; var9++) {
         for (int var10 = 1; var10 < var3[var9].length; var10 += 2) {
            var6[var9] += Float.intBitsToFloat(var3[var9][var10]) * 0.5F;
         }
      }

      for (int var18 = 0; var18 < this.count; var18++) {
         if (this.root[var18] == var18) {
            int var20 = 0;
            float var11 = 0.0F;
            float var12 = 0.0F;
            var5[var18] = 0.0F;
            var8.push(var18, 0.0F);

            while (!var8.empty()) {
               float var13 = var8.peekKey();
               int var14 = var8.pop();
               if (!(var13 > var5[var14])) {
                  var7[var20++] = var14;
                  var11 += var1[var14] * var6[var14];
                  var12 += var6[var14];

                  for (int var15 = 0; var15 < var3[var14].length; var15 += 2) {
                     int var16 = var3[var14][var15];
                     float var17 = var13 + Float.intBitsToFloat(var3[var14][var15 + 1]);
                     if (var17 <= var2 && var17 < var5[var16]) {
                        var5[var16] = var17;
                        var8.push(var16, var17);
                     }
                  }
               }
            }

            var4[var18] = var12 > 0.0F ? var11 / var12 : var1[var18];

            for (int var22 = 0; var22 < var20; var22++) {
               var5[var7[var22]] = Float.POSITIVE_INFINITY;
            }
         }
      }

      float[] var19 = new float[this.count];

      for (int var21 = 0; var21 < this.count; var21++) {
         var19[var21] = var4[this.root[var21]];
      }

      return var19;
   }

   private float[] bridge(float[] var1) {
      float[] var2 = new float[this.count];
      float[] var3 = new float[this.count];
      Arrays.fill(var2, Float.NaN);
      Arrays.fill(var3, Float.NaN);
      int var4 = -1;

      for (int var5 = 0; var5 <= this.count; var5++) {
         if (var5 >= this.count || !this.wet(var5)) {
            if (var5 < this.count && var4 >= 0 && this.street[var5] == this.street[var4] && var5 - var4 > 1) {
               float var6 = Math.hypot(this.xs[var5] - this.xs[var4], this.ys[var5] - this.ys[var4]) > 16.0 ? 2.0F : 0.0F;

               for (int var7 = var4 + 1; var7 < var5; var7++) {
                  float var8 = (float)(var7 - var4) / (var5 - var4);
                  var3[var7] = this.at(this.water, this.xs[var7], this.ys[var7]) + var6;
                  var2[var7] = Math.max(var1[var4] + (var1[var5] - var1[var4]) * var8, var3[var7]);
               }
            }

            var4 = var5;
         }
      }

      float[] var9 = this.sweep(var2, this.rules.metresASquare(this.rules.grade()), true);

      for (int var10 = 0; var10 < this.count; var10++) {
         var1[var10] = Float.isNaN(var2[var10]) ? Math.max(var1[var10], var9[var10]) : var2[var10];
      }

      return var3;
   }

   private boolean wet(int var1) {
      int var2 = (int)(this.xs[var1] / 4.0F);
      int var3 = (int)(this.ys[var1] / 4.0F);
      return this.ground.at(var2, var3) == 1;
   }

   private float at(float[] var1, float var2, float var3) {
      return ReliefWater.sample(var1, this.width, this.height, this.spacing, var2, var3);
   }

   private static long key(int var0, int var1) {
      return (long)var0 << 32 | var1 & 4294967295L;
   }

   private void union(int var1, int var2) {
      int var3 = this.find(var1);
      int var4 = this.find(var2);
      if (var3 != var4) {
         this.root[Math.max(var3, var4)] = Math.min(var3, var4);
      }
   }

   private int find(int var1) {
      while (this.root[var1] != var1) {
         this.root[var1] = this.root[this.root[var1]];
         var1 = this.root[var1];
      }

      return var1;
   }

   record Roads(float[] height, float[] distance, boolean[] band, float steepest, float sharpest, int[] breaks, int samples) {
   }

   record Samples(int[] street, float[] xs, float[] ys, float[] widths, int[] root, int count) {
   }
}