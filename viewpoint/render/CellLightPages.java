package viewpoint.render;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL45;
import viewpoint.platform.LongMap;

public final class CellLightPages {
   private static final CellLightPages.Change CLEAR = new CellLightPages.Change(0, 0, null, null);
   private static final ConcurrentLinkedQueue<CellLightPages.Change> CHANGES = new ConcurrentLinkedQueue<>();
   private static final int WINDOW = 16;
   private static final int LAYERS = 96;
   private static final int TEXELS = 128;
   private static final LongMap<int[]> layers = new LongMap<>(128);
   private static final LongMap<int[]> cards = new LongMap<>(128);
   private static final ArrayDeque<Integer> freeLayers = new ArrayDeque<>();
   private static final int[] table = new int[256];
   private static final IntBuffer tableUpload = BufferUtils.createIntBuffer(256);
   private static final ByteBuffer upload = BufferUtils.createByteBuffer(65536);
   private static int pages;
   private static int tableTexture;
   private static int nextLayer;
   private static int cardsChanged;
   private static boolean tableDirty = true;

   public static void show(int var0, int var1, byte[] var2, int[] var3) {
      CHANGES.add(new CellLightPages.Change(var0, var1, var2, var3));
   }

   public static void hide(int var0, int var1) {
      CHANGES.add(new CellLightPages.Change(var0, var1, null, null));
   }

   public static void clear() {
      CHANGES.add(CLEAR);
   }

   static void update() {
      if (pages == 0) {
         pages = GL45.glCreateTextures(35866);
         GL45.glTextureParameteri(pages, 10241, 9729);
         GL45.glTextureParameteri(pages, 10240, 9729);
         GL45.glTextureParameteri(pages, 10242, 33071);
         GL45.glTextureParameteri(pages, 10243, 33071);
         GL45.glTextureStorage3D(pages, 1, 32856, 128, 128, 96);
         tableTexture = GL45.glCreateTextures(3553);
         GL45.glTextureParameteri(tableTexture, 10241, 9728);
         GL45.glTextureParameteri(tableTexture, 10240, 9728);
         GL45.glTextureStorage2D(tableTexture, 1, 33333, 16, 16);
      }

      CellLightPages.Change var0;
      while ((var0 = CHANGES.poll()) != null) {
         if (var0 == CLEAR) {
            letGoAll();
         } else if (var0.page == null) {
            letGo(var0.cellX, var0.cellY);
         } else {
            show(var0);
         }
      }

      if (tableDirty) {
         tableDirty = false;
         tableUpload.clear();
         tableUpload.put(table).flip();
         GL45.glTextureSubImage2D(tableTexture, 0, 0, 0, 16, 16, 36244, 5124, tableUpload);
      }
   }

   static void bind(int var0, int var1) {
      GL13.glActiveTexture(33984 + var0);
      GL11.glBindTexture(35866, pages);
      GL13.glActiveTexture(33984 + var1);
      GL11.glBindTexture(3553, tableTexture);
      GL13.glActiveTexture(33984);
   }

   static void unbind(int var0, int var1) {
      GL13.glActiveTexture(33984 + var0);
      GL11.glBindTexture(35866, 0);
      GL13.glActiveTexture(33984 + var1);
      GL11.glBindTexture(3553, 0);
      GL13.glActiveTexture(33984);
   }

   static int cardsChanged() {
      return cardsChanged;
   }

   static void cards(IntBuffer var0) {
      for (int[] var2 : cards.values()) {
         var0.put(var2, 0, Math.min(var2.length, var0.remaining()));
      }
   }

   private static void show(CellLightPages.Change var0) {
      long var1 = (long)var0.cellX << 32 | var0.cellY & 4294967295L;

      if (var0.cards != null) {
         cards.put(var1, var0.cards);
      } else {
         cards.remove(var1);
      }

      cardsChanged++;
      int[] var3 = layers.get(var1);
      if (var3 == null) {
         int var4 = !freeLayers.isEmpty() ? freeLayers.pop() : (nextLayer < 96 ? nextLayer++ : -1);
         if (var4 < 0) {
            return;
         }

         var3 = new int[]{var4};
         layers.put(var1, var3);
         setTexel(var0.cellX, var0.cellY, var4 + 1);
      }

      upload.clear();
      upload.put(var0.page).flip();
      GL11.glPixelStorei(3317, 4);
      GL45.glTextureSubImage3D(pages, 0, 0, 0, var3[0], 128, 128, 1, 6408, 5121, upload);
   }

   private static void letGo(int var0, int var1) {
      long var2 = (long)var0 << 32 | var1 & 4294967295L;
      int[] var4 = layers.remove(var2);
      cardsChanged = cardsChanged + (cards.remove(var2) != null ? 1 : 0);
      if (var4 != null) {
         freeLayers.push(var4[0]);
         setTexel(var0, var1, 0);
      }
   }

   private static void letGoAll() {
      for (int[] var1 : layers.values()) {
         freeLayers.push(var1[0]);
      }

      layers.clear();
      cards.clear();
      cardsChanged++;
      Arrays.fill(table, 0);
      tableDirty = true;
   }

   private static void setTexel(int var0, int var1, int var2) {
      table[Math.floorMod(var1, 16) * 16 + Math.floorMod(var0, 16)] = var2;
      tableDirty = true;
   }

   private CellLightPages() {
   }

   private record Change(int cellX, int cellY, byte[] page, int[] cards) {
   }
}
