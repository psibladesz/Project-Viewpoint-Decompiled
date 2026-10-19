package viewpoint.world;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import viewpoint.packs.PackBind;
import viewpoint.render.FloorArt;
import viewpoint.render.PackModel;
import zombie.core.textures.TextureID;
import zombie.iso.IsoObject;
import zombie.iso.objects.IsoWorldInventoryObject;

public final class Recipe {
   public static final int FACE_FRONT = 0;
   public static final int FACE_BACK = 1;
   public static final int FACE_CAPS = 2;
   public static final int ROOF_SEEN = 1;
   public static final int ROOF_UNSEEN = 2;
   public static final int ROOF_LENT = 4;
   public static final int ROOF_WALLED = 3;
   final int level;
   final ArrayList<Recipe.Batch> batches = new ArrayList<>();
   final IdentityHashMap<TextureID, Recipe.Batch> pages = new IdentityHashMap<>();
   boolean pending;
   boolean picturePending;
   boolean modelsMatter;
   boolean packsMatter;
   ArrayList<Recipe.Op> models;
   ArrayList<PackModel> waiting;
   ArrayList<IsoObject> modelled;
   ArrayList<IsoWorldInventoryObject> modelledItems;
   BodyCards bodies;
   FloorArt floor;
   RoofWindow roof;
   private long fingerprint;
   static final long SEED = -7046029254386353131L;

   public Recipe(int var1) {
      this.level = var1;
   }

   long fingerprint() {
      if (this.fingerprint != 0L) {
         return this.fingerprint;
      }

      long var1 = mix(-7046029254386353131L, this.level);
      var1 = mix(var1, this.roof == null ? 0L : this.roof.hash);

      for (Recipe.Batch var4 : this.batches) {
         var1 = mix(var1, System.identityHashCode(var4.page));
         var1 = mix(var1, var4.ops.size());

         for (Recipe.Op var6 : var4.ops) {
            var1 = mix(var1, (long)var6.kind << 32 | var6.face & 4294967295L);
            var1 = mix(var1, (long)var6.flags << 32 | var6.lift & 4294967295L);
            var1 = mix(var1, var6.mesh == null ? 0L : var6.mesh.hash);
            var1 = mix(var1, var6.map);
            var1 = mix(var1, var6.raw);
            var1 = mix(var1, var6.card);
            var1 = mix(var1, (long)Float.floatToRawIntBits(var6.dx) << 32 | Float.floatToRawIntBits(var6.dy) & 4294967295L);
            var1 = mix(var1, (long)Float.floatToRawIntBits(var6.height) << 32 | Float.floatToRawIntBits(var6.fill) & 4294967295L);
            var1 = mix(var1, (long)Float.floatToRawIntBits(var6.dirX) << 32 | Float.floatToRawIntBits(var6.dirZ) & 4294967295L);
            var1 = mix(var1, var6.owner);
         }
      }

      var1 = this.mixModels(var1);
      var1 ^= var1 >>> 33;
      var1 *= -49064778989728563L;
      var1 ^= var1 >>> 33;
      this.fingerprint = var1 == 0L ? 1L : var1;
      return this.fingerprint;
   }

   private long mixModels(long var1) {
      if (this.models == null) {
         return var1;
      }

      var1 = mix(var1, this.models.size());

      for (Recipe.Op var4 : this.models) {
         var1 = mix(var1, (long)var4.model.index << 32 | var4.square & 4294967295L);
         var1 = mix(var1, (long)var4.flags << 32 | var4.owner & 4294967295L);
         var1 = mix(var1, new float[]{var4.dx, var4.dy, var4.height, var4.cos, var4.sin, var4.scale});
      }

      return var1;
   }

   static long mix(long var0, long var2) {
      var0 = (var0 ^ var2) * -7046029254386353131L;
      return var0 ^ var0 >>> 29;
   }

   static long mix(long var0, float[] var2) {
      if (var2 == null) {
         return mix(var0, -1L);
      }

      var0 = mix(var0, var2.length);

      for (float var6 : var2) {
         var0 = mix(var0, Float.floatToRawIntBits(var6));
      }

      return var0;
   }

   public static void place(Recipe var0, TextureID var1, TileMesh var2, float[] var3, float var4, float var5, float var6, int var7) {
      placed(var0, var1, 0, var2, var3, var4, var5, var6).flags = var7;
   }

   public static void placeFace(
      Recipe var0, TextureID var1, TileMesh var2, float[] var3, float var4, float var5, float var6, float var7, float var8, int var9, float var10
   ) {
      Recipe.Op var11 = placed(var0, var1, 1, var2, var3, var4, var5, var6);
      var11.dirX = var7;
      var11.dirZ = var8;
      var11.face = var9;
      var11.fill = var10;
   }

   public static void model(Recipe var0, PackBind var1, boolean var2, float var3, float var4, float var5, int var6, int var7, int var8) {
      Recipe.Op var9 = new Recipe.Op();
      var9.kind = 5;
      var9.model = var1.model;
      var9.dx = var3 + var1.x;
      var9.dy = var4 + var1.y;
      var9.height = var5 + var1.z;
      var9.cos = var2 ? var1.cos : 1.0F;
      var9.sin = var2 ? var1.sin : 0.0F;
      var9.scale = var1.scale;
      var9.square = var6;
      var9.flags = var7;
      var9.owner = var8;
      if (var0.models == null) {
         var0.models = new ArrayList<>();
      }

      var0.models.add(var9);
   }

   static void placeRoof(Recipe var0, RoofWindow var1, TextureID var2, float[] var3, float var4, float var5, float var6, int var7) {
      placeRoof(var0, var1, var2, var3, var4, var5, var6, var7, 0.0F, 6);
   }

   static void placeRoof(Recipe var0, RoofWindow var1, TextureID var2, float[] var3, float var4, float var5, float var6, int var7, float var8, int var9) {
      Recipe.Op var10 = placed(var0, var2, var9, null, var3, var4, var5, var6);
      var10.roof = var1;
      var10.face = var7;
      var10.fill = var8;
   }

   static void placeCaps(Recipe var0, TextureID var1, TileMesh var2, float[] var3, float var4, float var5, float var6) {
      placed(var0, var1, 2, var2, var3, var4, var5, var6);
   }

   public static void bakedFloor(Recipe var0, float var1, float var2, float var3, float var4, float var5, float[] var6) {
      raw(var0, floorVertices(var1, var2, var3, var4, var5, var6));
   }

   public static void bakedFloor(Recipe var0, float var1, float var2, float var3, float var4, float var5, float[] var6, float var7, float var8) {
      raw(var0, floorVertices(var1, var2, var3, var4, var5, var6, var7, var8));
   }

   public static void bakedFloorFan(Recipe var0, float[] var1, float var2, float var3, float var4, float var5, float var6, float[] var7) {
      int var8 = var1.length / 2;
      float[] var9 = new float[3 * var8 * 14];
      float var10 = (var2 + var4) * 0.5F;
      float var11 = (var3 + var5) * 0.5F;
      int var12 = 0;

      for (int var13 = 0; var13 < var8; var13++) {
         int var14 = (var13 + 1) % var8;
         var12 = floorVertex(var9, var12, var10, var11, var2, var3, var4, var5, var6, var7);
         var12 = floorVertex(var9, var12, var1[2 * var13], var1[2 * var13 + 1], var2, var3, var4, var5, var6, var7);
         var12 = floorVertex(var9, var12, var1[2 * var14], var1[2 * var14 + 1], var2, var3, var4, var5, var6, var7);
      }

      raw(var0, var9);
   }

   private static int floorVertex(float[] var0, int var1, float var2, float var3, float var4, float var5, float var6, float var7, float var8, float[] var9) {
      float var10 = var9[0] + (var9[2] - var9[0]) * (var2 - var4) / (var6 - var4);
      float var11 = var9[1] + (var9[3] - var9[1]) * (var3 - var5) / (var7 - var5);
      return Cook.put(var0, var1, -var2, var8, -var3, var10, var11, 0.0F, 0.0F, 1.0F, 1.0F, 0.0F, 1.0F, 0.0F, 0.0F, 68);
   }

   private static void raw(Recipe var0, float[] var1) {
      Recipe.Batch var2 = null;

      for (Recipe.Batch var4 : var0.batches) {
         var2 = var4.floor ? var4 : var2;
      }

      if (var2 == null) {
         var2 = new Recipe.Batch();
         var2.floor = true;
         var0.batches.add(var2);
      }

      Recipe.Op var5 = new Recipe.Op();
      var5.kind = 3;
      var5.raw = var1;
      var2.ops.add(var5);
   }

   public static void billboard(Recipe var0, TextureID var1, float var2, float var3, float var4, float var5, float var6, float[] var7, int var8) {
      card(var0, var1, var2, var3, var4, -var5, var5, 0.0F, var6, var7, 128 | var8);
   }

   public static void card(
      Recipe var0, TextureID var1, float var2, float var3, float var4, float var5, float var6, float var7, float var8, float[] var9, int var10
   ) {
      float[] var11 = new float[84];
      int[] var12 = new int[]{0, 1, 2, 0, 2, 3};
      int var13 = 0;

      for (int var17 : var12) {
         boolean var18 = var17 == 1 || var17 == 2;
         boolean var19 = var17 >= 2;
         var13 = Cook.put(
            var11,
            var13,
            -var2,
            var4 + (var19 ? var8 : var7),
            -var3,
            var18 ? var9[2] : var9[0],
            var19 ? var9[1] : var9[3],
            var9[0],
            var9[1],
            var9[2],
            var9[3],
            0.0F,
            0.5F,
            0.0F,
            var18 ? var6 : var5,
            var10
         );
      }

      placed(var0, var1, 3, null, null, var2, var3, var4).raw = var11;
   }

   static float[] floorVertices(float var0, float var1, float var2, float var3, float var4, float[] var5) {
      return floorVertices(var0, var1, var2, var3, var4, var5, 0.0F, 0.0F);
   }

   static float[] floorVertices(float var0, float var1, float var2, float var3, float var4, float[] var5, float var6, float var7) {
      float var8 = var6 == 0.0F && var7 == 0.0F ? 1.0F : 0.0F;
      float[] var9 = new float[84];
      int[] var10 = new int[]{0, 1, 2, 0, 2, 3};
      int var11 = 0;

      for (int var15 : var10) {
         boolean var16 = var15 == 1 || var15 == 2;
         boolean var17 = var15 >= 2;
         var11 = Cook.put(
            var9,
            var11,
            -(var16 ? var2 : var0),
            var4,
            -(var17 ? var3 : var1),
            var16 ? var5[2] : var5[0],
            var17 ? var5[3] : var5[1],
            0.0F,
            0.0F,
            1.0F,
            1.0F,
            var6,
            var8,
            var7,
            0.0F,
            68
         );
      }

      return var9;
   }

   private static Recipe.Op placed(Recipe var0, TextureID var1, int var2, TileMesh var3, float[] var4, float var5, float var6, float var7) {
      Recipe.Batch var8 = var0.pages.get(var1);
      if (var8 == null) {
         var8 = new Recipe.Batch();
         var8.page = var1;
         var0.pages.put(var1, var8);
         var0.batches.add(var8);
      }

      Recipe.Op var9 = new Recipe.Op();
      var9.kind = var2;
      var9.mesh = var3;
      var9.map = var4;
      var9.dx = var5;
      var9.dy = var6;
      var9.height = var7;
      var8.ops.add(var9);
      return var9;
   }

   static final class Batch {
      TextureID page;
      boolean floor;
      final ArrayList<Recipe.Op> ops = new ArrayList<>();
   }

   static final class Op {
      static final int EMIT = 0;
      static final int FACE = 1;
      static final int CAPS = 2;
      static final int RAW = 3;
      static final int PLANT = 4;
      static final int MODEL = 5;
      static final int ROOF = 6;
      static final int GABLE = 7;
      int kind;
      int face;
      int flags;
      int lift;
      int owner = -1;
      TileMesh mesh;
      RoofWindow roof;
      float[] map;
      float[] raw;
      float[] card;
      float dx;
      float dy;
      float height;
      float dirX;
      float dirZ;
      float fill;
      PackModel model;
      float cos;
      float sin;
      float scale;
      int square;
   }
}
