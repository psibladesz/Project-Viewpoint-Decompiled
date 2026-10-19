package viewpoint.game;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import viewpoint.platform.LiveSettings;
import zombie.core.opengl.RenderThread;
import zombie.core.textures.TextureFBO;
import zombie.iso.fboRenderChunk.FBORenderChunk;
import zombie.iso.fboRenderChunk.FBORenderChunkManager;

public final class IsoChunkTextures {
   public static final LiveSettings.Toggle ON = LiveSettings.toggle(
      "game.releaseIsoChunkTextures", "Isometric chunk textures let go in first person", "Debug/Fixes", true
   );
   private static final int WAIT_FRAMES = 3;
   private static final long TEXEL_BYTES = 6L;
   private static int frames;
   private static Field store;

   public static int update(boolean var0) {
      if (!var0) {
         frames = 0;
         return heldMiB();
      }

      if (++frames == 3 && ON.get()) {
         release();
      }

      return 0;
   }

   public static int heldMiB() {
      FBORenderChunkManager var0 = FBORenderChunkManager.instance;
      long var1 = 0L;

      for (FBORenderChunk var4 : var0.chunks.values()) {
         var1 += held(var4);
      }

      for (ArrayList<FBORenderChunk> var8 : store(var0).values()) {
         for (FBORenderChunk var6 : var8) {
            var1 += held(var6);
         }
      }

      return (int)(var1 >> 20);
   }

   private static long held(FBORenderChunk var0) {
      return var0.fbo == null ? 0L : (long)var0.w * var0.h * 6L;
   }

   private static void release() {
      long var0 = System.nanoTime();
      FBORenderChunkManager var2 = FBORenderChunkManager.instance;
      var2.clearCache();
      var2.recycle();
      ArrayList<TextureFBO> var3 = new ArrayList<>();

      for (ArrayList<FBORenderChunk> var5 : store(var2).values()) {
         for (FBORenderChunk var7 : var5) {
            if (var7.fbo != null) {
               var3.add(var7.fbo);
               var7.fbo = null;
            }
         }

         var5.clear();
      }

      store(var2).clear();
      var2.renderChunk = null;
      double var8 = (System.nanoTime() - var0) * 1.0E-6;
      RenderThread.queueInvokeOnRenderContext(
         () -> {
            long var3x = System.nanoTime();

            for (TextureFBO var6 : var3) {
               var6.destroy();
            }

            System.out
               .println(
                  String.format(
                     "[Viewpoint] the isometric view's %d chunk textures let go: %.1f ms on the main thread, %.1f ms on the render thread",
                     var3.size(),
                     var8,
                     (System.nanoTime() - var3x) * 1.0E-6
                  )
               );
         }
      );
   }

   private static HashMap<Integer, ArrayList<FBORenderChunk>> store(FBORenderChunkManager var0) {
      try {
         if (store == null) {
            store = FBORenderChunkManager.class.getDeclaredField("sizeChunkStore");
            store.setAccessible(true);
         }

         return (HashMap<Integer, ArrayList<FBORenderChunk>>)store.get(var0);
      } catch (ReflectiveOperationException var2) {
         throw new IllegalStateException("FBORenderChunkManager.sizeChunkStore could not be read (Compat)", var2);
      }
   }

   private IsoChunkTextures() {
   }

   static {
      ON.describe(
         "Frees about 16 MB of video memory, and as much of the system's commit, a chunk the isometric view has drawn; that view draws them again when it comes back."
      );
   }
}
