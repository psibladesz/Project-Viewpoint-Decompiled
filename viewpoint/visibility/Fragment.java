package viewpoint.visibility;

import java.util.Arrays;

final class Fragment {
   static final int SIZE = 8;
   static final int SQUARES = 64;
   final int chunkX;
   final int chunkY;
   final int level;
   final boolean bottom;
   final long known;
   final long floor;
   final long stairs;
   final long roofed;
   final long northWall;
   final long westWall;
   final long northOpening;
   final long westOpening;
   final long northDoor;
   final long westDoor;
   final long northWindow;
   final long westWindow;
   private final long[] rooms;
   static final int KNOWN = 0;
   static final int FLOOR = 1;
   static final int STAIRS = 2;
   static final int ROOFED = 3;
   static final int NORTH_WALL = 4;
   static final int WEST_WALL = 5;
   static final int NORTH_OPENING = 6;
   static final int WEST_OPENING = 7;
   static final int NORTH_DOOR = 8;
   static final int WEST_DOOR = 9;
   static final int NORTH_WINDOW = 10;
   static final int WEST_WINDOW = 11;
   static final int MASKS = 12;

   Fragment(int var1, int var2, int var3, boolean var4, long[] var5, long[] var6) {
      this.chunkX = var1;
      this.chunkY = var2;
      this.level = var3;
      this.bottom = var4;
      this.known = var5[0];
      this.floor = var5[1];
      this.stairs = var5[2];
      this.roofed = var5[3];
      this.northWall = var5[4];
      this.westWall = var5[5];
      this.northOpening = var5[6];
      this.westOpening = var5[7];
      this.northDoor = var5[8];
      this.westDoor = var5[9];
      this.northWindow = var5[10];
      this.westWindow = var5[11];
      this.rooms = (long[])var6.clone();
   }

   long room(int var1) {
      return this.rooms[var1];
   }

   boolean has(long var1, int var3) {
      return (var1 >>> var3 & 1L) != 0L;
   }

   static long key(int var0, int var1, int var2) {
      return (long)(var0 & 16777215) << 40 | (long)(var1 & 16777215) << 16 | var2 + 32 & 65535;
   }

   long key() {
      return key(this.chunkX, this.chunkY, this.level);
   }

   boolean sameLayout(Fragment var1) {
      return var1 != null
         && var1.chunkX == this.chunkX
         && var1.chunkY == this.chunkY
         && var1.level == this.level
         && var1.bottom == this.bottom
         && var1.known == this.known
         && var1.floor == this.floor
         && var1.stairs == this.stairs
         && var1.roofed == this.roofed
         && var1.northWall == this.northWall
         && var1.westWall == this.westWall
         && var1.northOpening == this.northOpening
         && var1.westOpening == this.westOpening
         && var1.northDoor == this.northDoor
         && var1.westDoor == this.westDoor
         && var1.northWindow == this.northWindow
         && var1.westWindow == this.westWindow
         && Arrays.equals(var1.rooms, this.rooms);
   }
}
