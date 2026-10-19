package viewpoint.models;

import java.util.ArrayList;
import viewpoint.core.Frame;
import viewpoint.visibility.Edges;
import viewpoint.visibility.Rooms;
import zombie.core.skinnedmodel.animation.AnimationPlayer;
import zombie.core.skinnedmodel.model.IsoObjectAnimations;
import zombie.core.skinnedmodel.model.IsoObjectModelDrawer;
import zombie.core.skinnedmodel.model.WorldItemModelDrawer;
import zombie.core.skinnedmodel.model.IsoObjectModelDrawer.RenderStatus;
import zombie.core.textures.ColorInfo;
import zombie.core.textures.TextureDraw.GenericDrawer;
import zombie.inventory.InventoryItem;
import zombie.iso.IsoGridSquare;
import zombie.iso.IsoObject;
import zombie.iso.SpriteModel;
import zombie.iso.objects.IsoWorldInventoryObject;

public final class Models {
   public static boolean capturing;
   private static ArrayList<GenericDrawer> sink;
   private static final ArrayList<GenericDrawer> ahead = new ArrayList<>();
   private static final ColorInfo UNLIT = new ColorInfo(0.6F, 0.6F, 0.6F, 1.0F);
   private static final float OFF_THE_ATLAS = 0.0F;

   public static boolean capture(GenericDrawer var0) {
      if (capturing && (var0 instanceof IsoObjectModelDrawer || var0 instanceof WorldItemModelDrawer)) {
         sink.add(var0);
         return true;
      } else {
         return false;
      }
   }

   public static void snapshot(Frame var0, int var1) {
      sink = var0.drawers;
      ModelAsks.begin();

      for (int var2 = 0; var2 < var0.modelObjects.size(); var2++) {
         IsoObject var3 = var0.modelObjects.get(var2);
         IsoGridSquare var4 = var3.getSquare();
         if (var4 != null && var3.getSpriteModel() != null) {
            if (roomHidden(var3, var4)) {
               Rooms.leftOut();
            } else if (ModelAsks.due(var3)) {
               int var5 = sink.size();
               long var6 = System.nanoTime();
               object(var3, var4, var1);
               ModelAsks.asked(var3, System.nanoTime() - var6);
               own(var0, var5, var4, var3, null);
            }
         }
      }

      for (int var8 = 0; var8 < var0.modelItems.size(); var8++) {
         IsoWorldInventoryObject var9 = var0.modelItems.get(var8);
         IsoGridSquare var10 = var9.getSquare();
         if (var10 != null && var9.getItem() != null) {
            if (Rooms.hides(var10.getX(), var10.getY(), var10.getX(), var10.getY(), var10.getZ(), var10.getZ())) {
               Rooms.leftOut();
            } else if (ModelAsks.due(var9)) {
               int var11 = sink.size();
               long var12 = System.nanoTime();
               item(var9, var10);
               ModelAsks.asked(var9, System.nanoTime() - var12);
               own(var0, var11, var10, null, var9.getItem());
            }
         }
      }

      ModelAsks.end();
      sink = null;
   }

   private static boolean roomHidden(IsoObject var0, IsoGridSquare var1) {
      int var2 = var0.sprite == null ? -1 : Edges.ownerKind(var0.sprite);
      int var3 = var1.getX();
      int var4 = var1.getY();
      int var5 = var1.getZ();

      return switch (var2) {
         case 0 -> Rooms.hides(var3, var4, var3, var4, var5, var5);
         case 1 -> Rooms.hides(var3, var4 - 1, var3, var4, var5, var5);
         case 2 -> Rooms.hides(var3 - 1, var4, var3, var4, var5, var5);
         default -> false;
      };
   }

   private static void own(Frame var0, int var1, IsoGridSquare var2, IsoObject var3, InventoryItem var4) {
      int var5 = var0.scene.models.count();

      for (int var6 = var1; var6 < sink.size(); var6++) {
         if (sink.get(var6) instanceof IsoObjectModelDrawer var7) {
            RigidCapture.object(var0, var7, var2, var3);
         } else if (sink.get(var6) instanceof WorldItemModelDrawer var8) {
            RigidCapture.item(var0, var8, var2, var4);
         }
      }

      if (var2 == var0.lootSquare) {
         var0.scene.models.target(var5);
      }
   }

   public static boolean askAhead(IsoObject var0, IsoGridSquare var1, int var2) {
      ArrayList var3 = sink;
      sink = ahead;

      try {
         return object(var0, var1, var2) == RenderStatus.Loading;
      } finally {
         giveBack(var3);
      }
   }

   public static boolean askAhead(IsoWorldInventoryObject var0, IsoGridSquare var1) {
      ArrayList var2 = sink;
      sink = ahead;

      try {
         return item(var0, var1) == zombie.core.skinnedmodel.model.ItemModelRenderer.RenderStatus.Loading;
      } finally {
         giveBack(var2);
      }
   }

   private static void giveBack(ArrayList<GenericDrawer> var0) {
      for (int var1 = 0; var1 < ahead.size(); var1++) {
         ahead.get(var1).postRender();
      }

      ahead.clear();
      sink = var0;
   }

   private static RenderStatus object(IsoObject var0, IsoGridSquare var1, int var2) {
      SpriteModel var3 = var0.getSpriteModel();
      float var4 = var0.sprite != null && var0.sprite.getProperties().isSurfaceOffset() ? var0.sprite.getProperties().getSurface() : 0.0F;
      AnimationPlayer var5 = IsoObjectAnimations.getInstance().getAnimationPlayer(var0);
      ColorInfo var6 = var1.getLightInfo(var2);
      if (var6 == null) {
         var1.cacheLightInfo();
         var6 = var1.getLightInfo(var2);
         if (var6 == null) {
            var6 = UNLIT;
         }
      }

      capturing = true;

      try {
         return var5 == null
            ? IsoObjectModelDrawer.renderMain(var3, var1.getX() + 0.5F, var1.getY() + 0.5F, var1.getZ(), var6, var0.getRenderYOffset() + var4)
            : IsoObjectModelDrawer.renderMain(var3, var1.getX() + 0.5F, var1.getY() + 0.5F, var1.getZ(), var6, var0.getRenderYOffset() + var4, var5);
      } finally {
         capturing = false;
      }
   }

   private static zombie.core.skinnedmodel.model.ItemModelRenderer.RenderStatus item(IsoWorldInventoryObject var0, IsoGridSquare var1) {
      capturing = true;

      try {
         return WorldItemModelDrawer.renderMain(
            var0.getItem(), var1, var0.getRenderSquare(), var0.getX() + var0.xoff, var0.getY() + var0.yoff, var0.getZ() + var0.zoff, 0.0F, 0.0F, false
         );
      } finally {
         capturing = false;
      }
   }

   public static void release(Frame var0) {
      for (int var1 = 0; var1 < var0.drawers.size(); var1++) {
         var0.drawers.get(var1).postRender();
      }

      var0.drawers.clear();
   }

   private Models() {
   }
}
