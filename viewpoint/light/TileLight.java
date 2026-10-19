package viewpoint.light;

import java.util.HashMap;
import zombie.core.properties.IsoPropertyType;
import zombie.core.properties.PropertyContainer;
import zombie.iso.SpriteDetails.IsoFlagType;
import zombie.iso.SpriteDetails.IsoObjectType;
import zombie.iso.sprite.IsoSprite;
import zombie.iso.sprite.IsoSpriteManager;

public final class TileLight {
   static final int HAS = 1;
   static final int TRANS = 2;
   static final int SOLID_FLOOR = 4;
   static final int TRANSPARENT_FLOOR = 8;
   static final int NO_START = 16;
   static final int COLLIDE_N = 32;
   static final int COLLIDE_W = 64;
   static final int TRANSPARENT_N = 128;
   static final int TRANSPARENT_W = 256;
   static final int DOOR_FLAG_N = 512;
   static final int DOOR_FLAG_W = 1024;
   static final int SOLID = 2048;
   static final int BLOCKSIGHT = 4096;
   static final int BLOCK_RAIN = 8192;
   static final int OPAQUE_FLOOR = 16384;
   static final int OPAQUE_CUT_N = 32768;
   static final int OPAQUE_CUT_W = 65536;
   static final int DOOR_N = 131072;
   static final int DOOR_N_SEE = 262144;
   static final int DOOR_N_OPEN = 524288;
   static final int DOOR_W = 1048576;
   static final int DOOR_W_SEE = 2097152;
   static final int DOOR_W_OPEN = 4194304;
   static final int WINDOW_N = 8388608;
   static final int WINDOW_W = 16777216;
   static final int EXISTS = 536870912;
   static final int ROOM_LOOKUP = 1073741824;
   public static final int LAMP_RADIUS_SHIFT = 24;
   public static final int STREET = 1073741824;
   static final int MAX_PACKED_RADIUS = 63;
   private static final int DEFAULT_RADIUS = 10;
   private static final HashMap<String, Long> byName = new HashMap<>();

   public static long of(String var0) {
      Long var1 = byName.get(var0);
      if (var1 != null) {
         return var1;
      }

      IsoSprite var2 = var0 == null ? null : (IsoSprite)IsoSpriteManager.instance.getNamedMap().get(var0);

      long var3;
      try {
         var3 = var2 == null ? 0L : (long)lamp(var2) << 32 | square(var2) & 4294967295L;
      } catch (RuntimeException var6) {
         var3 = var2 == null ? 0L : 1L;
      }

      byName.put(var0, var3);
      return var3;
   }

   public static void forget() {
      byName.clear();
   }

   private static int square(IsoSprite var0) {
      PropertyContainer var1 = var0.getProperties();
      if (!var1.has(IsoFlagType.blueprint)
         && !var1.has(IsoFlagType.FloorOverlay)
         && !var1.has(IsoFlagType.WallOverlay)
         && !var1.has(IsoPropertyType.COUNTERTOP)) {
         int var2 = 1
            | flag(var1, IsoFlagType.trans, 2)
            | flag(var1, IsoFlagType.solidfloor, 4)
            | flag(var1, IsoFlagType.transparentFloor, 8)
            | flag(var1, IsoFlagType.noStart, 16)
            | flag(var1, IsoFlagType.collideN, 32)
            | flag(var1, IsoFlagType.collideW, 64)
            | flag(var1, IsoFlagType.transparentN, 128)
            | flag(var1, IsoFlagType.transparentW, 256)
            | flag(var1, IsoFlagType.doorN, 512)
            | flag(var1, IsoFlagType.doorW, 1024)
            | flag(var1, IsoFlagType.solid, 2048)
            | flag(var1, IsoFlagType.blocksight, 4096)
            | flag(var1, IsoFlagType.BlockRain, 8192);
         if (!var1.has(IsoFlagType.water) && var1.has(IsoFlagType.solidfloor) && !var1.has(IsoFlagType.transparentFloor)) {
            var2 |= 16384;
         }

         if (var1.has(IsoFlagType.cutN) && !var1.has(IsoFlagType.transparentN) && !var1.has(IsoFlagType.WallSE)) {
            var2 |= 32768;
         }

         if (var1.has(IsoFlagType.cutW) && !var1.has(IsoFlagType.transparentW) && !var1.has(IsoFlagType.WallSE)) {
            var2 |= 65536;
         }

         return var2 | special(var0, var1);
      } else {
         return 1;
      }
   }

   private static int special(IsoSprite var0, PropertyContainer var1) {
      IsoObjectType var2 = var0.getTileType();
      if (var2 == IsoObjectType.doorN || var2 == IsoObjectType.doorW) {
         boolean var3 = var1.has(IsoFlagType.open);
         boolean var4 = var3 || var1.has(IsoPropertyType.DOOR_TRANS);
         boolean var5 = var2 == IsoObjectType.doorN != var3;
         return var5 ? 131072 | (var4 ? 262144 : 0) | (var3 ? 524288 : 0) : 1048576 | (var4 ? 2097152 : 0) | (var3 ? 4194304 : 0);
      } else if (var2 == IsoObjectType.lightswitch) {
         return 0;
      } else if (var2 == IsoObjectType.curtainN || var2 == IsoObjectType.curtainS || var2 == IsoObjectType.curtainE || var2 == IsoObjectType.curtainW) {
         return 0;
      } else if (var1.has(IsoFlagType.windowN)) {
         return 8388608;
      } else {
         return var1.has(IsoFlagType.windowW) ? 16777216 : 0;
      }
   }

   private static int lamp(IsoSprite var0) {
      PropertyContainer var1 = var0.getProperties();
      if (var0.getTileType() == IsoObjectType.lightswitch && var1.has(IsoPropertyType.RED_LIGHT)) {
         int var2 = 10;
         if (var1.has(IsoPropertyType.LIGHT_RADIUS) && Integer.parseInt(var1.get(IsoPropertyType.LIGHT_RADIUS)) > 0) {
            var2 = Integer.parseInt(var1.get(IsoPropertyType.LIGHT_RADIUS));
         }

         return channel(var1.get(IsoPropertyType.RED_LIGHT))
            | channel(var1.get(IsoPropertyType.GREEN_LIGHT)) << 8
            | channel(var1.get(IsoPropertyType.BLUE_LIGHT)) << 16
            | Math.min(var2, 63) << 24
            | (var1.has("streetlight") ? 1073741824 : 0);
      } else {
         return 0;
      }
   }

   private static int channel(String var0) {
      return var0 == null ? 0 : Math.max(0, Math.min(255, Math.round(Float.parseFloat(var0))));
   }

   private static int flag(PropertyContainer var0, IsoFlagType var1, int var2) {
      return var0.has(var1) ? var2 : 0;
   }

   private TileLight() {
   }
}
