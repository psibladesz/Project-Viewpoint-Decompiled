package viewpoint.world;

import java.util.ArrayList;
import viewpoint.packs.ModelPacks;
import viewpoint.packs.PackBind;
import viewpoint.packs.PackLoads;
import viewpoint.render.PackModel;
import viewpoint.visibility.Edges;
import viewpoint.visibility.Owner;
import zombie.core.properties.PropertyContainer;
import zombie.iso.IsoObject;
import zombie.iso.SpriteDetails.IsoFlagType;
import zombie.iso.sprite.IsoSprite;
import zombie.iso.sprite.IsoSpriteInstance;

final class PackGather {
   private static Recipe recipe;
   private static boolean on;
   private static boolean bound;
   private static long now;

   static void begin(Recipe var0, boolean var1) {
      recipe = var0;
      on = var1 && ModelPacks.any();
      bound = false;
      now = System.nanoTime();
   }

   static void end() {
      recipe.packsMatter = bound;
      recipe = null;
   }

   static boolean object(IsoObject var0, IsoSprite var1, float var2, float var3, float var4, int var5) {
      PackBind var6 = bind(var1);
      if (var6 != null && on && ready(var6)) {
         place(var6, var1, var2, var3, var4, var5);
         ArrayList var7 = var0.getAttachedAnimSprite();

         for (int var8 = 0; var7 != null && var8 < var7.size(); var8++) {
            IsoSpriteInstance var9 = (IsoSpriteInstance)var7.get(var8);
            part(var9 == null ? null : var9.parentSprite, var2, var3, var4, var5);
         }

         part(var0.getOverlaySprite(), var2, var3, var4, var5);
         return true;
      } else {
         return false;
      }
   }

   static boolean part(IsoSprite var0, float var1, float var2, float var3, int var4) {
      PackBind var5 = var0 == null ? null : bind(var0);
      if (var5 != null && on && ready(var5)) {
         place(var5, var0, var1, var2, var3, var4);
         return true;
      } else {
         return false;
      }
   }

   private static PackBind bind(IsoSprite var0) {
      PackBind var1 = ModelPacks.any() ? ModelPacks.bind(var0) : null;
      if (var1 != null && !MapRoofs.vanilla(var0)) {
         bound = true;
         return var1;
      } else {
         return null;
      }
   }

   private static boolean ready(PackBind var0) {
      PackModel var1 = var0.model;
      if (PackLoads.want(var1, now)) {
         return true;
      }

      if (var1.state() != 4) {
         if (recipe.waiting == null) {
            recipe.waiting = new ArrayList<>();
         }

         if (!recipe.waiting.contains(var1)) {
            recipe.waiting.add(var1);
         }
      }

      return false;
   }

   private static void place(PackBind var0, IsoSprite var1, float var2, float var3, float var4, int var5) {
      PropertyContainer var6 = var1.getProperties();
      boolean var7 = var6.has(IsoFlagType.attachedN)
         || var6.has(IsoFlagType.attachedW)
         || var6.has(IsoFlagType.attachedS)
         || var6.has(IsoFlagType.attachedE)
         || var6.has(IsoFlagType.attachedNW)
         || var6.has(IsoFlagType.attachedSE);
      boolean var8 = var6.has(IsoFlagType.doorN) || var6.has(IsoFlagType.doorW);
      Recipe.model(recipe, var0, PackBind.turns(var1), var2, var3, var4, var5, (var7 ? 2 : 0) | (var8 ? 16384 : 0), Owner.of(var5, Edges.ownerKind(var1)));
   }

   private PackGather() {
   }
}
