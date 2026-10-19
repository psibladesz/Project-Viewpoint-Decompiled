package viewpoint.render;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.IdentityHashMap;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL33;
import org.lwjgl.opengl.GL43;
import viewpoint.platform.Gl;
import viewpoint.platform.GlProgram;
import zombie.core.textures.TextureID;

public final class CorpseCards {
   private static final byte VIEW = 5;
   private static final int CARD_VERTICES = 6;
   private static final int[] CORNERS = new int[]{0, 1, 2, 0, 2, 3};
   private static final ArrayList<CorpseCards.Bucket> buckets = new ArrayList<>();
   private static final IdentityHashMap<Object, CorpseCards.Bucket> byPage = new IdentityHashMap<>();
   private static final Object NO_PAGE = new Object();
   private static int bucketCount;
   private static int texture;
   private static FloatBuffer upload = BufferUtils.createFloatBuffer(1792);

   public static float[] baked(float[] var0) {
      float[] var1 = new float[84];
      int var13 = 0;

      for (int var6 : CORNERS) {
         int var7 = var6 * 4;
         var1[var13++] = var0[var7];
         var1[var13++] = var0[16];
         var1[var13++] = var0[var7 + 1];
         var1[var13++] = var0[var7 + 2];
         var1[var13++] = var0[var7 + 3];
         System.arraycopy(var0, 20, var1, var13, 4);
         var13 += 4;
         var1[var13++] = 0.0F;
         var1[var13++] = 1.0F;
         var1[var13++] = 0.0F;
         var1[var13++] = 0.0F;
         var1[var13++] = var0[17];
      }

      return var1;
   }

   static void draw(SceneData var0, GlProgram var1, byte var2, int var3, boolean var4) {
      if (builds(var1) && (var2 & 5) != 0 && var0.cardCount != 0) {
         begin();

         for (int var5 = 0; var5 < var0.meshCount; var5++) {
            if (Meshes.drawn(var0, var5, var2, var3)) {
               collect(var0, var5, var4 ? var0.meshModelSquares[var5] : -1L, false);
            }
         }

         issue(var0, var1);
      }
   }

   static void drawTarget(SceneData var0, GlProgram var1, int var2, long var3) {
      if (builds(var1) && var0.meshCardCount[var2] != 0) {
         begin();
         collect(var0, var2, var3, true);
         issue(var0, var1);
      }
   }

   private static boolean builds(GlProgram var0) {
      if (!var0.has("uCards")) {
         return false;
      }

      var0.sampler("uCardTexels", 90);
      return true;
   }

   private static void begin() {
      bucketCount = 0;
      byPage.clear();
   }

   private static void collect(SceneData var0, int var1, long var2, boolean var4) {
      for (int var5 = var0.meshCardFirst[var1]; var5 < var0.meshCardFirst[var1] + var0.meshCardCount[var1]; var5++) {
         int var6 = var5 * 28;
         int var7 = Float.floatToRawIntBits(var0.cards[var6 + 19]);
         boolean var8 = var0.cards[var6 + 24] >= 1.0F;
         if ((var2 >>> var7 & 1L) != 0L && (!var4 || !var8)) {
            bucket(var0.cardPages[var5]).add(var5, var1);
         }
      }
   }

   private static void issue(SceneData var0, GlProgram var1) {
      int var2 = 0;

      for (int var3 = 0; var3 < bucketCount; var3++) {
         var2 += buckets.get(var3).count;
      }

      if (var2 != 0) {
         fill(var0.cards, var2);
         long var8 = FrameStream.put(upload, FrameStream.textureAlignment());
         if (texture == 0) {
            texture = GL11.glGenTextures();
         }

         GL13.glActiveTexture(34074);
         GL11.glBindTexture(35882, texture);
         GL43.glTexBufferRange(35882, 34836, FrameStream.buffer(), var8, var2 * 28L * 4L);
         GL13.glActiveTexture(33984);
         var1.sampler("uMeshRecords", 89);
         MeshArena.bindForModels(89, 48);
         var1.setInt("uCards", 1);
         int var5 = 0;

         for (int var6 = 0; var6 < bucketCount; var6++) {
            CorpseCards.Bucket var7 = buckets.get(var6);
            if (var7.page != null) {
               Meshes.bindPage(var7.page);
            }

            Gl.generated(var5 * 6, var7.count * 6);
            var5 += var7.count;
         }

         var1.setInt("uCards", 0);
         MeshArena.unbindForModels(89, 48);
         GL13.glActiveTexture(34074);
         GL11.glBindTexture(35882, 0);
         GL13.glActiveTexture(33984);
         GL33.glBindSampler(0, 0);
      }
   }

   private static void fill(float[] var0, int var1) {
      if (upload.capacity() < var1 * 28) {
         upload = BufferUtils.createFloatBuffer(var1 * 28 * 2);
      }

      upload.clear();

      for (int var2 = 0; var2 < bucketCount; var2++) {
         CorpseCards.Bucket var3 = buckets.get(var2);

         for (int var4 = 0; var4 < var3.count; var4++) {
            int var5 = upload.position();
            upload.put(var0, var3.cards[var4 * 2] * 28, 28);
            upload.put(var5 + 18, Float.intBitsToFloat(var3.cards[var4 * 2 + 1]));
         }
      }

      upload.flip();
   }

   private static CorpseCards.Bucket bucket(TextureID var0) {
      Object var1 = var0 == null ? NO_PAGE : var0;
      CorpseCards.Bucket var2 = byPage.get(var1);
      if (var2 == null) {
         if (bucketCount == buckets.size()) {
            buckets.add(new CorpseCards.Bucket());
         }

         var2 = buckets.get(bucketCount++);
         var2.page = var0;
         var2.count = 0;
         byPage.put(var1, var2);
      }

      return var2;
   }

   private CorpseCards() {
   }

   private static final class Bucket {
      TextureID page;
      int[] cards = new int[128];
      int count;

      void add(int var1, int var2) {
         if (this.count * 2 == this.cards.length) {
            this.cards = Arrays.copyOf(this.cards, this.cards.length * 2);
         }

         this.cards[this.count * 2] = var1;
         this.cards[this.count * 2 + 1] = var2;
         this.count++;
      }
   }
}
