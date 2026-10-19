package viewpoint.world;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Iterator;
import viewpoint.platform.Caches;
import viewpoint.platform.LiveSettings;
import zombie.core.textures.Texture;
import zombie.iso.SpriteDetails.IsoFlagType;
import zombie.iso.SpriteDetails.IsoObjectType;
import zombie.iso.sprite.IsoSprite;
import zombie.tileDepth.TileDepthTextureAssignmentManager;
import zombie.tileDepth.TileGeometryManager;
import zombie.tileDepth.TileGeometryFile.Geometry;

public final class TileMeshes {
   private static final String GAME = "game";
   private static final float SQUARE_PIXELS = 45.254833F;
   private static final float UNIT_PIXELS = 16.0F;
   private static final float FOOT_PIXELS = 16.0F;
   private static final int CROSSED_CARDS = 3;
   static final LiveSettings.Choice GRASS_CARDS = LiveSettings.choice(
      "plants.grassCards", "Grass and bushes", "World/Plants", new String[]{"One card facing you", "Two crossed planes", "Three crossed planes"}, 1
   );
   private static final IdentityHashMap<IsoSprite, TileMeshes.Held> cache = new IdentityHashMap<>();
   private static long bytes;
   private static final TileMesh FLOOR = floor();
   private static final TileMesh WALL_N = wall(true, false);
   private static final TileMesh WALL_W = wall(false, true);
   private static final TileMesh WALL_NW = wall(true, true);
   static final float WALL_ART_W = -0.46875F;
   static final float WALL_ART_N = -0.4375F;

   static TileMesh floorMesh() {
      return FLOOR;
   }

   public static TileMesh get(IsoSprite var0, Texture var1, IsoSprite var2) {
      TileMeshes.Held var3 = cache.get(var0);
      if (var3 == null) {
         var3 = new TileMeshes.Held(create(var0, var1, var2));
         cache.put(var0, var3);
         bytes = bytes + size(var3.mesh);
      }

      var3.used = Caches.tick();
      return var3.mesh;
   }

   private static long size(TileMesh var0) {
      return var0.data.length * 4L + 64L;
   }

   public static TileMesh peek(IsoSprite var0, Texture var1, IsoSprite var2) {
      TileMeshes.Held var3 = cache.get(var0);
      return var3 != null ? var3.mesh : create(var0, var1, var2);
   }

   private static TileMesh create(IsoSprite var0, Texture var1, IsoSprite var2) {
      if (var2 != null || isPlant(var0)) {
         return crossed(var1, 3);
      }

      if (var0.solidfloor) {
         return FLOOR;
      }

      IsoObjectType var3 = var0.getType();
      boolean var4 = var3 == IsoObjectType.windowFN
         || var3 == IsoObjectType.windowFW
         || var3 == IsoObjectType.doorN
         || var3 == IsoObjectType.doorW
         || var3 == IsoObjectType.curtainN
         || var3 == IsoObjectType.curtainS
         || var3 == IsoObjectType.curtainW
         || var3 == IsoObjectType.curtainE;
      boolean var5 = !var4 && edge(var0, IsoFlagType.WallN, IsoFlagType.WindowN, IsoFlagType.DoorWallN);
      boolean var6 = !var4 && edge(var0, IsoFlagType.WallW, IsoFlagType.WindowW, IsoFlagType.DoorWallW);
      if ((var4 || !var0.getProperties().has(IsoFlagType.WallNW)) && (!var5 || !var6)) {
         if (var5) {
            return WALL_N;
         }

         if (var6) {
            return WALL_W;
         }

         ArrayList var7 = geometryFor(var0);
         if (var7 != null && !var7.isEmpty()) {
            MeshBuilder var8 = new MeshBuilder();

            for (int var9 = 0; var9 < var7.size(); var9++) {
               var8.add((Geometry)var7.get(var9));
            }

            return var8.build();
         } else {
            return edgeQuads(var0);
         }
      } else {
         return WALL_NW;
      }
   }

   private static TileMesh edgeQuads(IsoSprite var0) {
      boolean var1 = var0.cutN
         || edge(
            var0, IsoFlagType.DoorWallN, IsoFlagType.WindowN, IsoFlagType.collideN, IsoFlagType.HoppableN, IsoFlagType.TallHoppableN, IsoFlagType.transparentN
         );
      boolean var2 = var0.cutW
         || edge(
            var0, IsoFlagType.DoorWallW, IsoFlagType.WindowW, IsoFlagType.collideW, IsoFlagType.HoppableW, IsoFlagType.TallHoppableW, IsoFlagType.transparentW
         );
      if (var1 && var2) {
         return WALL_NW;
      } else if (var1) {
         return WALL_N;
      } else {
         return var2 ? WALL_W : TileMesh.EMPTY;
      }
   }

   private static ArrayList<Geometry> geometryFor(IsoSprite var0) {
      ArrayList var1 = lookup(var0.tilesetName, var0.tileSheetIndex);
      if ((var1 == null || var1.isEmpty()) && var0.name != null) {
         String var2 = TileDepthTextureAssignmentManager.getInstance().getAssignedTileName("game", var0.name);
         int var3 = var2 == null ? -1 : var2.lastIndexOf(95);
         if (var3 > 0) {
            try {
               var1 = lookup(var2.substring(0, var3), Integer.parseInt(var2.substring(var3 + 1)));
            } catch (NumberFormatException var5) {
            }
         }
      }

      return var1;
   }

   private static ArrayList<Geometry> lookup(String var0, int var1) {
      return var0 != null && var1 >= 0 ? TileGeometryManager.getInstance().getGeometry("game", var0, var1 % 8, var1 / 8) : null;
   }

   private static boolean edge(IsoSprite var0, IsoFlagType... var1) {
      for (IsoFlagType var5 : var1) {
         if (var0.getProperties().has(var5)) {
            return true;
         }
      }

      return false;
   }

   static int plantCards(IsoSprite var0, IsoSprite var1) {
      if (var1 == null && !isPlant(var0)) {
         return -1;
      }

      if (tree(var0, var1)) {
         return 3;
      }

      int var2 = GRASS_CARDS.get();
      return var2 == 0 ? 0 : var2 + 1;
   }

   static int plantFlags(IsoSprite var0, IsoSprite var1, int var2) {
      return var2 > 0 && !tree(var0, var1) ? 4096 : 0;
   }

   private static boolean tree(IsoSprite var0, IsoSprite var1) {
      return (var1 != null ? var1 : var0).getType() == IsoObjectType.tree;
   }

   public static boolean isPlant(IsoSprite var0) {
      return var0.getType() == IsoObjectType.tree || var0.getProperties().has(IsoFlagType.vegitation) || var0.getProperties().has(IsoFlagType.canBeCut);
   }

   public static TileMesh crossed(Texture var0, int var1) {
      MeshBuilder var2 = new MeshBuilder();
      var2.crossed(plantHalfWidth(var0), plantHeight(var0), var1);
      return var2.build();
   }

   public static float plantHalfWidth(Texture var0) {
      return var0.getWidthOrig() / WorldMesher.pixelScale(var0) / 90.50967F;
   }

   public static float plantHeight(Texture var0) {
      return (var0.getHeightOrig() / WorldMesher.pixelScale(var0) - 16.0F) / 39.191833F;
   }

   public static float[] plantCard(Texture var0) {
      float var1 = plantAcross(var0);
      float var2 = plantUp(var0);
      float var3 = var0.getWidthOrig() * 0.5F;
      float var4 = plantFoot(var0);
      float var5 = var0.getOffsetX();
      float var6 = var0.getOffsetY();
      return new float[]{(var5 - var3) * var1, (var5 + var0.getWidth() - var3) * var1, (var4 - var6 - var0.getHeight()) * var2, (var4 - var6) * var2};
   }

   public static float plantAcross(Texture var0) {
      return 1.0F / (WorldMesher.pixelScale(var0) * 45.254833F);
   }

   public static float plantUp(Texture var0) {
      return 1.0F / (WorldMesher.pixelScale(var0) * 16.0F * 2.4494896F);
   }

   public static float plantFoot(Texture var0) {
      return var0.getHeightOrig() - 16.0F * WorldMesher.pixelScale(var0);
   }

   private static TileMesh floor() {
      MeshBuilder var0 = new MeshBuilder();
      var0.floor();
      return var0.build();
   }

   static TileMesh wall(boolean var0) {
      return var0 ? WALL_N : WALL_W;
   }

   private static TileMesh wall(boolean var0, boolean var1) {
      MeshBuilder var2 = new MeshBuilder();
      if (var0) {
         var2.edgeQuad(2, -0.4375F);
      }

      if (var1) {
         var2.edgeQuad(0, -0.46875F);
      }

      return var2.build();
   }

   private TileMeshes() {
   }

   static {
      GRASS_CARDS.describe(
         "How the grass, bushes and small plants round you stand: one card turned to face you, or two or three planes crossed through the plant, which look the same from every side."
      );
      Caches.register(new Caches.Cache() {
         @Override
         public long bytes() {
            return TileMeshes.bytes;
         }

         @Override
         public void list(Caches.Survey var1) {
            for (TileMeshes.Held var3 : TileMeshes.cache.values()) {
               var1.add(var3.used, TileMeshes.size(var3.mesh));
            }
         }

         @Override
         public void evictBefore(long var1) {
            Iterator var3 = TileMeshes.cache.values().iterator();

            while (var3.hasNext()) {
               TileMeshes.Held var4 = (TileMeshes.Held)var3.next();
               if (var4.used < var1) {
                  TileMeshes.bytes = TileMeshes.bytes - TileMeshes.size(var4.mesh);
                  var3.remove();
               }
            }
         }
      });
   }

   private static final class Held {
      final TileMesh mesh;
      long used;

      Held(TileMesh var1) {
         this.mesh = var1;
      }
   }
}
