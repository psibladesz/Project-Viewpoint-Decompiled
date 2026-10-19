package viewpoint.render;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL45;
import viewpoint.platform.LongMap;
import viewpoint.platform.Pool;

public final class BandLight {
   private static final BandLight.Change CLEAR = new BandLight.Change(0, 0, 0, null);
   private static final ConcurrentLinkedQueue<BandLight.Change> CHANGES = new ConcurrentLinkedQueue<>();
   private static final int WINDOW = 72;
   private static final int LEVELS = 32;
   private static final int RETRIES = 64;
   private static final LongMap<int[]> slots = new LongMap<>(4096);
   private static final LongMap<BandLight.Change> waiting = new LongMap<>(256);
   private static final int[] table = new int[165888];
   private static final boolean[] dirty = new boolean[32];
   private static final IntBuffer slice = BufferUtils.createIntBuffer(5184);
   private static final ByteBuffer upload = BufferUtils.createByteBuffer(3200);
   private static int texture;
   private static volatile int shown;
   private static volatile int turnedAway;

   public static void show(int var0, int var1, int var2, byte[] var3) {
      CHANGES.add(new BandLight.Change(var0, var1, var2, var3));
   }

   public static void hide(int var0, int var1, int var2) {
      CHANGES.add(new BandLight.Change(var0, var1, var2, null));
   }

   public static void clear() {
      CHANGES.add(CLEAR);
   }

   public static int grids() {
      return shown;
   }

   public static int turnedAway() {
      return turnedAway;
   }

   public static long share() {
      return Pool.LIGHT_GRIDS.cap() / 3200L / 3L;
   }

   static void update() {
      BandLight.Change var0;
      while ((var0 = CHANGES.poll()) != null) {
         if (var0 == CLEAR) {
            letGoAll();
         } else if (var0.grid == null) {
            letGo(var0);
         } else {
            show(var0);
         }
      }

      retry();
      if (texture == 0) {
         texture = GL45.glCreateTextures(32879);
         GL45.glTextureParameteri(texture, 10241, 9728);
         GL45.glTextureParameteri(texture, 10240, 9728);
         GL45.glTextureStorage3D(texture, 1, 33333, 72, 72, 32);
         Arrays.fill(dirty, true);
      }

      shown = slots.size();
      turnedAway = waiting.size();

      for (int var1 = 0; var1 < 32; var1++) {
         if (dirty[var1]) {
            dirty[var1] = false;
            slice.clear();
            slice.put(table, var1 * 72 * 72, 5184).flip();
            GL45.glTextureSubImage3D(texture, 0, 0, 0, var1, 72, 72, 1, 36244, 5124, slice);
         }
      }
   }

   static void bind(int var0) {
      GL13.glActiveTexture(33984 + var0);
      GL11.glBindTexture(32879, texture);
      GL13.glActiveTexture(33984);
   }

   static void unbind(int var0) {
      GL13.glActiveTexture(33984 + var0);
      GL11.glBindTexture(32879, 0);
      GL13.glActiveTexture(33984);
   }

   private static void show(BandLight.Change var0) {
      if (var0.level >= 0 && var0.level < 32) {
         long var1 = key(var0.chunkX, var0.chunkY, var0.level);
         int[] var3 = slots.get(var1);
         if (var3 == null) {
            int var4 = slots.size() < share() ? MeshArena.takeSlot() : -1;
            if (var4 < 0) {
               waiting.put(var1, var0);
               return;
            }

            var3 = new int[]{var4};
            slots.put(var1, var3);
            setTexel(var0, var4 + 1);
         }

         upload.clear();
         upload.put(var0.grid).flip();
         MeshArena.uploadLight(var3[0], upload);
      }
   }

   private static void letGo(BandLight.Change var0) {
      if (var0.level >= 0 && var0.level < 32) {
         long var1 = key(var0.chunkX, var0.chunkY, var0.level);
         int[] var3 = slots.remove(var1);
         waiting.remove(var1);
         if (var3 != null) {
            MeshArena.freeSlot(var3[0]);
            setTexel(var0, 0);
         }
      }
   }

   private static void letGoAll() {
      for (int[] var1 : slots.values()) {
         MeshArena.freeSlot(var1[0]);
      }

      slots.clear();
      waiting.clear();
      Arrays.fill(table, 0);
      Arrays.fill(dirty, true);
   }

   private static void retry() {
      int var0 = 0;

      for (int var1 = 0; var1 < waiting.capacity() && var0 < 64 && slots.size() < share(); var1++) {
         BandLight.Change var2 = waiting.valueAt(var1);
         if (var2 != null) {
            waiting.removeAt(var1);
            var0++;
            show(var2);
         }
      }
   }

   private static void setTexel(BandLight.Change var0, int var1) {
      int var2 = Math.floorMod(var0.chunkX, 72);
      int var3 = Math.floorMod(var0.chunkY, 72);
      table[(var0.level * 72 + var3) * 72 + var2] = var1;
      dirty[var0.level] = true;
   }

   private static long key(int var0, int var1, int var2) {
      return (long)(var0 & 16777215) << 40 | (long)(var1 & 16777215) << 16 | var2 + 32 & 65535;
   }

   private BandLight() {
   }

   private record Change(int chunkX, int chunkY, int level, byte[] grid) {
   }
}
