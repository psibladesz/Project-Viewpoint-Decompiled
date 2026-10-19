package viewpoint.world;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import viewpoint.platform.VertexFormat;
import viewpoint.visibility.Edges;
import viewpoint.visibility.Owner;
import zombie.core.Core;
import zombie.core.properties.PropertyContainer;
import zombie.core.skinnedmodel.model.ItemModelRenderer;
import zombie.core.textures.Texture;
import zombie.core.textures.TextureID;
import zombie.inventory.InventoryItem;
import zombie.iso.IsoChunk;
import zombie.iso.IsoGridSquare;
import zombie.iso.IsoObject;
import zombie.iso.IsoWorld;
import zombie.iso.SpriteModel;
import zombie.iso.SpriteDetails.IsoFlagType;
import zombie.iso.objects.IsoTree;
import zombie.iso.objects.IsoWindow;
import zombie.iso.objects.IsoWorldInventoryObject;
import zombie.iso.sprite.IsoSprite;
import zombie.iso.sprite.IsoSpriteInstance;
import zombie.scripting.ScriptManager;
import zombie.util.list.PZArrayList;

public final class WorldMesher {
   private static final int MAX_LAYER = 3;
   private static final float STOREY_PIXELS = 96.0F;
   private static boolean baked;
   static int lift;
   private static int stand;
   private static final float LYING = 0.25F;
   static int owner = -1;
   static int square;
   private static Recipe recipe;
   private static final IdentityHashMap<TextureID, Recipe.Batch> byPage = new IdentityHashMap<>();
   private static final IdentityHashMap<Texture, float[]> mappings = new IdentityHashMap<>();
   private static final IdentityHashMap<Texture, float[]> plantCards = new IdentityHashMap<>();
   private static final boolean[] pending = new boolean[1];
   static boolean picturePending;
   static boolean modelsMatter;
   private static final ArrayList<IsoObject> lastModelled = new ArrayList<>();
   static final ArrayList<IsoWorldInventoryObject> lastModelledItems = new ArrayList<>();

   static Recipe gather(IsoChunk var0, int var1, int var2, boolean var3, boolean var4, boolean var5) {
      recipe = new Recipe(var1);
      byPage.clear();
      mappings.clear();
      plantCards.clear();
      lastModelled.clear();
      lastModelledItems.clear();
      BodyCards.begin();
      pending[0] = false;
      picturePending = false;
      modelsMatter = false;
      baked = FloorGather.BAKED.get();
      PackGather.begin(recipe, var4);
      FloorGather.bloodOf(var0, var1);
      SnowGrids.made(IsoWorld.instance.currentCell, var0, var1);
      float var6 = var1 * 2.4494896F;
      RoofGather.begin(var0, var1, var5);

      for (int var7 = 0; var7 < 8; var7++) {
         for (int var8 = 0; var8 < 8; var8++) {
            IsoGridSquare var9 = var0.getGridSquare(var8, var7, var1);
            square = var7 * 8 + var8;
            if (var9 != null) {
               square(var9, var8 + 0.5F, var7 + 0.5F, var6, pending, var3);
            }

            RoofGather.emit(square, var8 + 0.5F, var7 + 0.5F, var6, pending);
         }
      }

      owner = -1;
      Recipe var10 = recipe;
      var10.roof = RoofGather.end();
      var10.pending = pending[0];
      var10.picturePending = picturePending;
      var10.modelsMatter = modelsMatter;
      var10.modelled = lastModelled.isEmpty() ? null : new ArrayList<>(lastModelled);
      var10.modelledItems = lastModelledItems.isEmpty() ? null : new ArrayList<>(lastModelledItems);
      var10.bodies = BodyCards.end();
      var10.floor = FloorGather.build();
      if (baked && var10.floor != null) {
         Recipe.Batch var11 = new Recipe.Batch();
         var11.floor = true;
         var10.batches.add(var11);
         FloorSpans.emit(var11, var0, var1, var10.floor, var6);
      }

      recipe = null;
      PackGather.end();
      byPage.clear();
      mappings.clear();
      plantCards.clear();
      return var10;
   }

   private static void square(IsoGridSquare var0, float var1, float var2, float var3, boolean[] var4, boolean var5) {
      int var6 = 0;
      PZArrayList var7 = var0.getObjects();

      for (int var8 = 0; var8 < var7.size(); var8++) {
         IsoObject var9 = (IsoObject)var7.get(var8);
         IsoSprite var10 = var9.getSprite();
         float var11 = var3 + rise(var9);
         boolean var12 = var9.hasProperty(IsoFlagType.water);
         if ((var12 || !ShoreBlends.blend(var10)) && !drawnElsewhere(var0, var9, var10, var1, var2, var11, var12, var5, var4)) {
            IsoSprite var13 = var9.isUseSnowSprite() ? var10.getSnowSprite() : var10;
            Texture var14 = var13.getTextureForCurrentFrame(var9.getDir(), var9);
            if (var14 != null) {
               if (var14.isReady() && var14.getTextureId() != null) {
                  boolean var15 = var9 instanceof IsoTree;
                  int var16 = TileMeshes.plantCards(var10, var15 ? var10 : null);
                  if (var16 >= 0) {
                     owner = Owner.of(square, 0);
                     int var17 = TileMeshes.plantFlags(var10, var15 ? var10 : null, var16);
                     plant(var14, var1, var2, var11, var16, var17);
                     plantParts(var9, var16, var17, var1, var2, var11);
                  } else {
                     TileMesh var22 = TileMeshes.get(var10, var14, null);
                     if (var22.vertCount != 0) {
                        lift = var10.solidfloor ? 16 * Math.min(var6++, 3) : 0;
                        int var18 = var15 ? 0 : Edges.ownerKind(var10);
                        owner = Owner.of(square, var18);
                        boolean var19 = WallMesher.wall(var10, var22, var14, var0, var1, var2, var11, glass(var9, var10), var4);
                        boolean var20 = !var19 && !var12 && var18 == 0 && var22.top > 0.25F;
                        stand = var20 ? VertexFormat.standing((int)var1, (int)var2) : 0;
                        if (!var19) {
                           int var21 = var10.solidfloor ? 4 : (glass(var9, var10) ? 1024 : 0);
                           emit(batch(var14.getTextureId()), var22, var14, var1, var2, var11, var21 | (var12 ? 2048 : 0) | stand);
                        }

                        overlays(var9, var10, var22, var19, var1, var2, var11, var4);
                        lift = 0;
                        stand = 0;
                     }
                  }
               } else {
                  var4[0] = true;
               }
            }
         }
      }

      owner = Owner.of(square, 0);
      DeckWater.under(var0, var1, var2, var3, var4);
      FloorDecals.onSquare(var0, var1, var2, var3, var4, var5);
      if (baked) {
         FloorGather.blood(square, var4);
      }
   }

   private static boolean drawnElsewhere(
      IsoGridSquare var0, IsoObject var1, IsoSprite var2, float var3, float var4, float var5, boolean var6, boolean var7, boolean[] var8
   ) {
      if (var2 != null && !PackGather.object(var1, var2, var3, var4, var5, square)) {
         boolean var9 = hasModel(var1);
         modelsMatter |= var9;
         if (var7 && var9) {
            lastModelled.add(var1);
            return true;
         } else {
            return RoofGather.replaces(square, var2) || !var6 && FloorGather.capture(var0, square, var1, var8) && baked;
         }
      } else {
         RoofGather.packed(square, var2);
         return true;
      }
   }

   private static void plantParts(IsoObject var0, int var1, int var2, float var3, float var4, float var5) {
      ArrayList var6 = var0.getAttachedAnimSprite();

      for (int var7 = 0; var6 != null && var7 < var6.size(); var7++) {
         IsoSpriteInstance var8 = (IsoSpriteInstance)var6.get(var7);
         plantPart(var0, var8 == null ? null : var8.parentSprite, var1, var2, var3, var4, var5);
      }

      plantPart(var0, var0.getOverlaySprite(), var1, var2, var3, var4, var5);
   }

   private static void plantPart(IsoObject var0, IsoSprite var1, int var2, int var3, float var4, float var5, float var6) {
      if (!PackGather.part(var1, var4, var5, var6, square)) {
         Texture var7 = var1 == null ? null : var1.getTextureForCurrentFrame(var0.getDir(), var0);
         if (var7 != null && var7.isReady() && var7.getTextureId() != null) {
            plant(var7, var4, var5, var6, var2, var3 | 2);
         }
      }
   }

   private static void plant(Texture var0, float var1, float var2, float var3, int var4, int var5) {
      Recipe.Op var6 = op(batch(var0.getTextureId()), 4, null, var0, var1, var2, var3);
      var6.face = var4;
      var6.flags = var5;
      var6.card = plantCard(var0);
   }

   private static float[] plantCard(Texture var0) {
      float[] var1 = plantCards.get(var0);
      if (var1 == null) {
         var1 = TileMeshes.plantCard(var0);
         plantCards.put(var0, var1);
      }

      return var1;
   }

   private static void overlays(IsoObject var0, IsoSprite var1, TileMesh var2, boolean var3, float var4, float var5, float var6, boolean[] var7) {
      ArrayList var8 = var0.getAttachedAnimSprite();
      if (var8 != null) {
         for (int var9 = 0; var9 < var8.size(); var9++) {
            IsoSpriteInstance var10 = (IsoSpriteInstance)var8.get(var9);
            if (var10 != null && var10.parentSprite != null) {
               overlay(var0, var1, var10.parentSprite, var2, var3, var4, var5, var6, var7);
            }
         }
      }

      if (var0.getOverlaySprite() != null) {
         overlay(var0, var1, var0.getOverlaySprite(), var2, var3, var4, var5, var6, var7);
      }
   }

   private static void overlay(IsoObject var0, IsoSprite var1, IsoSprite var2, TileMesh var3, boolean var4, float var5, float var6, float var7, boolean[] var8) {
      if (!ShoreBlends.blend(var2) && !PackGather.part(var2, var5, var6, var7, square)) {
         Texture var9 = var2.getTextureForCurrentFrame(var0.getDir(), var0);
         if (var9 != null) {
            if (var9.isReady() && var9.getTextureId() != null) {
               Recipe.Batch var10 = batch(var9.getTextureId());
               if (!var4) {
                  emit(var10, var3, var9, var5, var6, var7, 2 | stand);
               } else {
                  PropertyContainer var11 = var1.getProperties();
                  PropertyContainer var12 = var2.getProperties();
                  boolean var13 = var11.has(IsoFlagType.WallNW)
                     || var11.has(IsoFlagType.WallN)
                     || var11.has(IsoFlagType.WindowN)
                     || var11.has(IsoFlagType.DoorWallN);
                  boolean var14 = var11.has(IsoFlagType.WallNW)
                     || var11.has(IsoFlagType.WallW)
                     || var11.has(IsoFlagType.WindowW)
                     || var11.has(IsoFlagType.DoorWallW);
                  boolean var15 = var12.has(IsoFlagType.attachedN) || var12.has(IsoFlagType.WindowN);
                  boolean var16 = var12.has(IsoFlagType.attachedW) || var12.has(IsoFlagType.WindowW);
                  if (!var15 && !var16) {
                     var16 = true;
                     var15 = true;
                  }

                  if (var15 && var13) {
                     owner = Owner.of(square, 1);
                     emitFace(var10, var3, var9, var5, var6, var7, 0.0F, 1.0F, 0, 0.0F, 2);
                  }

                  if (var16 && var14) {
                     owner = Owner.of(square, 2);
                     emitFace(var10, var3, var9, var5, var6, var7, 1.0F, 0.0F, 0, 0.0F, 2);
                  }
               }
            } else {
               var8[0] = true;
            }
         }
      }
   }

   public static float pixelScale(Texture var0) {
      return var0.getWidthOrig() == 64 && var0.getHeightOrig() == 128 ? 1.0F : 2.0F;
   }

   public static float rise(IsoObject var0) {
      return var0.getRenderYOffset() / 96.0F * 2.4494896F;
   }

   static Recipe.Batch batch(TextureID var0) {
      Recipe.Batch var1 = byPage.get(var0);
      if (var1 == null) {
         var1 = new Recipe.Batch();
         var1.page = var0;
         byPage.put(var0, var1);
         recipe.batches.add(var1);
      }

      return var1;
   }

   private static void emit(Recipe.Batch var0, TileMesh var1, Texture var2, float var3, float var4, float var5) {
      emit(var0, var1, var2, var3, var4, var5, 0);
   }

   private static float[] mapping(Texture var0) {
      float[] var1 = mappings.get(var0);
      if (var1 == null) {
         var1 = textureMapping(var0);
         mappings.put(var0, var1);
      }

      return var1;
   }

   public static float[] textureMapping(Texture var0) {
      float var1 = pixelScale(var0);
      float var2 = 1.0F / var0.getWidthHW();
      float var3 = 1.0F / var0.getHeightHW();
      return new float[]{
         var0.getXStart() + 0.5F * var2,
         var0.getYStart() + 0.5F * var3,
         var0.getXEnd() - 0.5F * var2,
         var0.getYEnd() - 0.5F * var3,
         var0.getXStart() + (var0.getWidthOrig() * 0.5F - 32.0F * var1 - var0.getOffsetX()) * var2,
         var0.getYStart() + (var0.getHeightOrig() - 128.0F * var1 - var0.getOffsetY()) * var3,
         var1 * var2,
         var1 * var3,
         var1,
         var2,
         var3
      };
   }

   static Recipe.Op op(Recipe.Batch var0, int var1, TileMesh var2, Texture var3, float var4, float var5, float var6) {
      Recipe.Op var7 = new Recipe.Op();
      var7.kind = var1;
      var7.mesh = var2;
      var7.map = mapping(var3);
      var7.dx = var4;
      var7.dy = var5;
      var7.height = var6;
      var7.lift = lift;
      var7.owner = owner;
      var0.ops.add(var7);
      return var7;
   }

   static void raw(Recipe.Batch var0, float[] var1) {
      Recipe.Op var2 = new Recipe.Op();
      var2.kind = 3;
      var2.raw = var1;
      var2.owner = owner;
      var0.ops.add(var2);
   }

   static void emitFace(Recipe.Batch var0, TileMesh var1, Texture var2, float var3, float var4, float var5, float var6, float var7, int var8, float var9) {
      emitFace(var0, var1, var2, var3, var4, var5, var6, var7, var8, var9, 0);
   }

   static void emitFace(
      Recipe.Batch var0, TileMesh var1, Texture var2, float var3, float var4, float var5, float var6, float var7, int var8, float var9, int var10
   ) {
      Recipe.Op var11 = op(var0, 1, var1, var2, var3, var4, var5);
      var11.dirX = var6;
      var11.dirZ = var7;
      var11.face = var8;
      var11.fill = var9;
      var11.flags = var10;
   }

   static void emit(Recipe.Batch var0, TileMesh var1, Texture var2, float var3, float var4, float var5, int var6) {
      op(var0, 0, var1, var2, var3, var4, var5).flags = var6;
   }

   private static boolean glass(IsoObject var0, IsoSprite var1) {
      PropertyContainer var2 = var1.getProperties();
      return var0 instanceof IsoWindow
         || var2.has(IsoFlagType.windowN)
         || var2.has(IsoFlagType.windowW)
         || var2.has(IsoFlagType.WallNTrans)
         || var2.has(IsoFlagType.WallWTrans)
         || var2.has(IsoFlagType.transparentN)
         || var2.has(IsoFlagType.transparentW)
         || var2.has("WallNWTrans")
         || var2.has("doorTrans");
   }

   public static boolean hasModel(IsoObject var0) {
      SpriteModel var1 = var0.getSpriteModel();
      return var1 != null && var1.modelScriptName != null && ScriptManager.instance.getModelScript(var1.modelScriptName) != null;
   }

   public static boolean hasModel(InventoryItem var0) {
      return var0 != null && Core.getInstance().isOption3DGroundItem() && var0.getScriptItem().isWorldRender() && ItemModelRenderer.itemHasModel(var0);
   }

   private WorldMesher() {
   }
}
