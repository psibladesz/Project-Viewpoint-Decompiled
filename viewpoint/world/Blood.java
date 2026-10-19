package viewpoint.world;

import java.util.Arrays;
import viewpoint.core.Frame;
import viewpoint.platform.Caches;
import viewpoint.platform.LongMap;
import viewpoint.render.BloodDraws;
import zombie.GameTime;
import zombie.core.Core;
import zombie.core.properties.PropertyContainer;
import zombie.core.textures.Texture;
import zombie.iso.IsoCell;
import zombie.iso.IsoChunk;
import zombie.iso.IsoGridSquare;
import zombie.iso.IsoObject;
import zombie.iso.IsoWallBloodSplat;
import zombie.iso.SpriteDetails.IsoFlagType;
import zombie.util.list.PZArrayList;

public final class Blood {
   private static final float REACH = 40.0F;
   private static final float FADING = 0.25F;
   private static final int REACH_CHUNKS = (int)Math.ceil(5.0);
   private static final int READS = 24;
   private static final int NORTH = 1;
   private static final int WEST = 2;
   private static final int CORNER = 0;
   private static final int SIDES = 3;
   private static final int UV = 9;
   private static final int RECT = 15;
   private static final int TINT = 19;
   private static final int AGE = 23;
   private static final int KIND = 24;
   private static final int STRIDE = 25;
   private static final float WALL_HOURS = 72.0F;
   private static final float AGED = 0.2F;
   private static final float AGED_ALPHA = 0.25F;
   private static final float WALL_LEAST_ALPHA = 0.15F;
   private static final float WALL_R = 0.7F;
   private static final float WALL_GB = 0.9F;
   private static final float WALL_ALPHA = 0.4F;
   private static final float FRAME_ALONG = 32.0F;
   private static final float FRAME_SLOPE = 16.0F;
   private static final float FRAME_STOREY = 96.0F;
   private static final float FRAME_FOOT = 112.0F;
   private static final long LEVEL_BYTES = 96L;
   private static final LongMap<Blood.Level> levels = new LongMap<>(512);
   private static final float[] decal = new float[25];
   private static final float[] corner = new float[3];
   private static final float[] sides = new float[6];
   private static final float[] uv = new float[6];
   private static final float[] rect = new float[4];
   private static final float[] tint = new float[4];
   private static Blood.Level[] candidateLevel = new Blood.Level[256];
   private static int[] candidateIndex = new int[256];
   private static float[] candidateDistance = new float[256];
   private static float[] sorted = new float[256];
   private static int candidates;

   static void stale(int var0, int var1, int var2) {
      if (var2 != Integer.MIN_VALUE) {
         Blood.Level var4 = levels.get(key(var0, var1, var2));
         if (var4 != null) {
            var4.stale = true;
         }
      } else {
         for (int var3 = -32; var3 < 32; var3++) {
            stale(var0, var1, var3);
         }
      }
   }

   static void staleAll() {
      for (Blood.Level var1 : levels.values()) {
         var1.stale = true;
      }
   }

   static void clear() {
      levels.clear();
   }

   public static void snapshot(Frame var0, IsoCell var1) {
      BloodDraws var2 = var0.scene.blood;
      var2.clear();
      candidates = 0;
      int var3 = Core.getInstance().getOptionBloodDecals();
      if (var3 != 0) {
         int var4 = Math.floorDiv((int)Math.floor(var0.camX), 8);
         int var5 = Math.floorDiv((int)Math.floor(var0.camY), 8);
         int var6 = 0;

         for (int var7 = 0; var7 <= REACH_CHUNKS; var7++) {
            for (int var8 = var5 - var7; var8 <= var5 + var7; var8++) {
               for (int var9 = var4 - var7; var9 <= var4 + var7; var9++) {
                  if (Math.max(Math.abs(var9 - var4), Math.abs(var8 - var5)) == var7) {
                     var6 = chunk(var0, var1.getChunk(var9, var8), var6);
                  }
               }
            }
         }

         list(var0, var2);
      }
   }

   private static int chunk(Frame var0, IsoChunk var1, int var2) {
      if (var1 != null && var1.loaded) {
         for (int var3 = var1.getMinLevel(); var3 <= var1.getMaxLevel(); var3++) {
            long var4 = key(var1.wx, var1.wy, var3);
            Blood.Level var6 = levels.get(var4);
            if (var6 == null) {
               var6 = new Blood.Level();
               levels.put(var4, var6);
            }

            if ((var6.stale || var6.chunk != var1) && var2 < 24) {
               read(var6, var1, var3);
               var2++;
            }

            var6.used = Caches.tick();
            if (var6.chunk == var1) {
               consider(var0, var6);
            }
         }

         return var2;
      } else {
         return var2;
      }
   }

   private static void read(Blood.Level var0, IsoChunk var1, int var2) {
      var0.chunk = var1;
      var0.z = var2;
      var0.count = 0;
      var0.stale = false;

      for (int var3 = 0; var3 < 64; var3++) {
         IsoGridSquare var4 = var1.getGridSquare(var3 % 8, var3 / 8, var2);
         if (var4 != null) {
            walls(var0, var4, var3 % 8, var3 / 8);
         }
      }
   }

   private static void walls(Blood.Level var0, IsoGridSquare var1, int var2, int var3) {
      PZArrayList var4 = var1.getObjects();

      for (int var5 = 0; var5 < var4.size(); var5++) {
         IsoObject var6 = (IsoObject)var4.get(var5);
         if (var6.wallBloodSplats != null && !var6.wallBloodSplats.isEmpty() && var6.getSprite() != null) {
            PropertyContainer var7 = var6.getSprite().getProperties();
            boolean var8 = var7.has(IsoFlagType.WallNW) || var7.has(IsoFlagType.WallN) || var7.has(IsoFlagType.WindowN) || var7.has(IsoFlagType.DoorWallN);
            boolean var9 = var7.has(IsoFlagType.WallNW) || var7.has(IsoFlagType.WallW) || var7.has(IsoFlagType.WindowW) || var7.has(IsoFlagType.DoorWallW);

            for (int var10 = 0; var10 < var6.wallBloodSplats.size(); var10++) {
               IsoWallBloodSplat var11 = (IsoWallBloodSplat)var6.wallBloodSplats.get(var10);
               if (var11 != null && var11.sprite != null) {
                  boolean var12 = var11.isNorthSprite() || var11.isWestSprite();
                  if (var8 && (!var12 || var11.isNorthSprite())) {
                     wall(var0, var6, var11, var2, var3, 1);
                  }

                  if (var9 && (!var12 || var11.isWestSprite())) {
                     wall(var0, var6, var11, var2, var3, 2);
                  }
               }
            }
         }
      }
   }

   private static void wall(Blood.Level var0, IsoObject var1, IsoWallBloodSplat var2, int var3, int var4, int var5) {
      Texture var6 = var2.sprite.getTextureForCurrentFrame(var1.getDir(), var1);
      if (var6 != null) {
         if (var6.isReady() && var6.getTextureId() != null) {
            float[] var7 = WorldMesher.textureMapping(var6);
            boolean var8 = var5 == 1;
            float var9 = var8 ? -0.5F : -0.46875F;
            float var10 = var8 ? -0.4375F : -0.5F;
            float var11 = 32.0F * (var9 - var10 + 1.0F);
            float var12 = 112.0F + 16.0F * (var9 + var10);
            float var13 = var8 ? 32.0F : -32.0F;
            Arrays.fill(decal, 0.0F);
            decal[0] = var3;
            decal[1] = var4;
            decal[3 + (var8 ? 0 : 1)] = 1.0F;
            decal[8] = 2.4494896F;
            decal[9] = var7[4] + var11 * var7[6];
            decal[10] = var7[5] + var12 * var7[7];
            decal[11] = var13 * var7[6];
            decal[12] = 16.0F * var7[7];
            decal[14] = -96.0F * var7[7];
            System.arraycopy(var7, 0, decal, 15, 4);
            decal[19] = 0.7F;
            decal[20] = 0.9F;
            decal[21] = 0.9F;
            decal[22] = 0.4F;
            decal[23] = var2.worldAge;
            decal[24] = var5;
            add(var0, var6);
         } else {
            var0.stale = true;
         }
      }
   }

   private static void add(Blood.Level var0, Texture var1) {
      if ((var0.count + 1) * 25 > var0.values.length) {
         var0.values = Arrays.copyOf(var0.values, var0.values.length * 2);
         var0.textures = Arrays.copyOf(var0.textures, var0.textures.length * 2);
      }

      System.arraycopy(decal, 0, var0.values, var0.count * 25, 25);
      var0.textures[var0.count++] = var1;
   }

   private static void consider(Frame var0, Blood.Level var1) {
      float var2 = var1.chunk.wx * 8 - var0.camX;
      float var3 = var1.chunk.wy * 8 - var0.camY;
      float var4 = (var1.z - var0.camZ) * 2.4494896F;

      for (int var5 = 0; var5 < var1.count; var5++) {
         int var6 = var5 * 25;
         float var7 = var2 + var1.values[var6 + 0];
         float var8 = var3 + var1.values[var6 + 0 + 1];
         float var9 = var7 * var7 + var8 * var8 + var4 * var4;
         if (var9 <= 1600.0F) {
            candidate(var1, var5, var9);
         }
      }
   }

   private static void candidate(Blood.Level var0, int var1, float var2) {
      if (candidates == candidateLevel.length) {
         candidateLevel = Arrays.copyOf(candidateLevel, candidates * 2);
         candidateIndex = Arrays.copyOf(candidateIndex, candidates * 2);
         candidateDistance = Arrays.copyOf(candidateDistance, candidates * 2);
      }

      candidateLevel[candidates] = var0;
      candidateIndex[candidates] = var1;
      candidateDistance[candidates++] = var2;
   }

   private static void list(Frame var0, BloodDraws var1) {
      float var2 = Float.MAX_VALUE;
      if (candidates > 1024) {
         sorted = sorted.length >= candidates ? sorted : new float[candidateDistance.length];
         System.arraycopy(candidateDistance, 0, sorted, 0, candidates);
         Arrays.sort(sorted, 0, candidates);
         var2 = sorted[1023];
      }

      float var3 = (float)GameTime.getInstance().getWorldAgeHours();

      for (int var4 = 0; var4 < candidates && !var1.full(); var4++) {
         if (candidateDistance[var4] <= var2) {
            float var5 = (40.0F - (float)Math.sqrt(candidateDistance[var4])) / 10.0F;
            draw(var0, var1, candidateLevel[var4], candidateIndex[var4], var3, Math.min(1.0F, var5));
         }
      }

      Arrays.fill(candidateLevel, 0, candidates, null);
   }

   private static void draw(Frame var0, BloodDraws var1, Blood.Level var2, int var3, float var4, float var5) {
      float[] var6 = var2.values;
      int var7 = var3 * 25;
      int var8 = (int)var6[var7 + 24];
      IsoChunk var9 = var2.chunk;
      corner[0] = -(var9.wx * 8 + var6[var7 + 0] - var0.camX);
      corner[1] = (var2.z - var0.camZ) * 2.4494896F + var6[var7 + 0 + 2];
      corner[2] = -(var9.wy * 8 + var6[var7 + 0 + 1] - var0.camY);

      for (int var10 = 0; var10 < 2; var10++) {
         sides[var10 * 3] = -var6[var7 + 3 + var10 * 3];
         sides[var10 * 3 + 1] = var6[var7 + 3 + var10 * 3 + 2];
         sides[var10 * 3 + 2] = -var6[var7 + 3 + var10 * 3 + 1];
      }

      System.arraycopy(var6, var7 + 9, uv, 0, 6);
      System.arraycopy(var6, var7 + 15, rect, 0, 4);
      age(var6, var7, var8, var4);
      tint[3] = tint[3] * var5;
      var1.add(var2.textures[var3].getID(), corner, sides, uv, rect, tint, facing(var8));
   }

   private static void age(float[] var0, int var1, int var2, float var3) {
      float var4 = var3 - var0[var1 + 23];
      float var5 = var4 >= 0.0F && var4 < 72.0F ? 1.0F - var4 / 72.0F : 0.0F;
      float var6 = 0.2F + var5 * 0.8F;
      float var7 = var0[var1 + 19 + 3] * (0.25F + var5 * 0.75F);
      tint[0] = var0[var1 + 19] * var6;
      tint[1] = var0[var1 + 19 + 1] * var6;
      tint[2] = var0[var1 + 19 + 2] * var6;
      tint[3] = Math.max(var7, 0.15F);
   }

   private static float facing(int var0) {
      float var1 = sides[1] * sides[5] - sides[2] * sides[4];
      float var2 = sides[0] * sides[4] - sides[1] * sides[3];
      float var3 = var0 == 1 ? -var2 : -var1;
      return var3 >= 0.0F ? 1.0F : -1.0F;
   }

   private static long key(int var0, int var1, int var2) {
      return (long)(var0 & 16777215) << 40 | (long)(var1 & 16777215) << 16 | var2 + 32 & 65535;
   }

   private Blood() {
   }

   static {
      Caches.register(new Caches.Cache() {
         @Override
         public long bytes() {
            long var1 = 0L;

            for (Blood.Level var4 : Blood.levels.values()) {
               var1 += var4.bytes();
            }

            return var1;
         }

         @Override
         public void list(Caches.Survey var1) {
            for (Blood.Level var3 : Blood.levels.values()) {
               var1.add(var3.used, var3.bytes());
            }
         }

         @Override
         public void evictBefore(long var1) {
            Blood.levels.removeIf(var2 -> var2.used < var1);
         }
      });
   }

   private static final class Level {
      IsoChunk chunk;
      int z;
      float[] values = new float[100];
      Texture[] textures = new Texture[4];
      int count;
      boolean stale = true;
      long used;

      long bytes() {
         return 96L + this.values.length * 4L + this.textures.length * 8L;
      }
   }
}
