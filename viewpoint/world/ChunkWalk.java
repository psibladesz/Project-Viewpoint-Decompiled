package viewpoint.world;

import java.util.Arrays;
import viewpoint.core.Frame;
import viewpoint.core.View;
import viewpoint.packs.ModelPacks;
import viewpoint.platform.LiveSettings;
import viewpoint.platform.Profile;
import zombie.iso.IsoCell;
import zombie.iso.IsoChunk;
import zombie.iso.IsoChunkMap;

final class ChunkWalk {
   private static final double LOOK_SLACK = Math.toRadians(23.0);
   private static final float LEVEL_RADIUS = 7.0F;
   private static final int NEAR_RING = 2;
   private static final LiveSettings.Number AT_ONCE = LiveSettings.number(
      "chunks.atOnceRing", "Rebuilt at once (chunks around)", "World/Chunks", 0.0F, 2.0F, 1.0F, 1.0F
   );
   private static final LiveSettings.Toggle OUT_OF_VIEW = LiveSettings.toggle("chunks.buildOutOfView", "Build chunks out of view too", "World/Chunks", false);
   private static final int BASEMENT_RING = 3;
   static final int TRANSLUCENT_RING = 6;
   private static final int RAIN_RING = 6;
   private static final float LOOK_AHEAD = 1.5F;
   private static float lastCamX = Float.NaN;
   private static float lastCamY;
   private static float velocityX;
   private static float velocityY;
   private static long lastNanos;
   private final Frame frame;
   private final IsoCell cell;
   private final IsoChunkMap map;
   private final int playerIndex;
   private final int playerLevel;
   private final int lowest;
   private final int pcx;
   private final int pcy;
   private final int radius;
   private final int stride;
   private final long now;
   private final boolean sun;
   private final float forwardX;
   private final float forwardY;
   private final float forwardUp;
   private final float eyeUp;
   private final float aheadX;
   private final float aheadY;
   private final float fogged;
   private final double cone;
   private int waiting;
   private IsoChunk chunk;
   private int cx;
   private int cy;
   private int ring;
   private boolean near;
   private float centreX;
   private float centreY;
   private float d2;
   private float aheadDX;
   private float aheadDY;
   private float lift;
   private float liftLow;
   private float liftHigh;

   static int walk(Frame var0, IsoCell var1, int var2, int var3, long var4, boolean var6) {
      track(var0, var4);
      return new ChunkWalk(var0, var1, var2, var3, var4, var6).run();
   }

   static void clear() {
      lastCamX = Float.NaN;
      velocityY = 0.0F;
      velocityX = 0.0F;
   }

   private static void track(Frame var0, long var1) {
      float var3 = (float)(var1 - lastNanos) * 1.0E-9F;
      if (!Float.isNaN(lastCamX) && var3 > 0.001F && var3 < 0.5F) {
         float var4 = (var0.camX - lastCamX) / var3;
         float var5 = (var0.camY - lastCamY) / var3;
         if (var4 * var4 + var5 * var5 > 2500.0F) {
            var5 = 0.0F;
            var4 = 0.0F;
         }

         float var6 = Math.min(1.0F, var3 * 4.0F);
         velocityX = velocityX + (var4 - velocityX) * var6;
         velocityY = velocityY + (var5 - velocityY) * var6;
      }

      lastCamX = var0.camX;
      lastCamY = var0.camY;
      lastNanos = var1;
   }

   private ChunkWalk(Frame var1, IsoCell var2, int var3, int var4, long var5, boolean var7) {
      this.frame = var1;
      this.cell = var2;
      this.playerIndex = var3;
      this.playerLevel = var4;
      this.now = var5;
      this.sun = var7;
      this.map = var2.getChunkMap(var3);
      this.pcx = Math.floorDiv((int)Math.floor(var1.camX), 8);
      this.pcy = Math.floorDiv((int)Math.floor(var1.camY), 8);
      this.lowest = var4 < 0 ? var4 - 1 : 0;
      float var8 = (float)Math.cos(var1.viewPitch);
      this.forwardX = (float)Math.cos(var1.viewYaw) * var8;
      this.forwardY = (float)Math.sin(var1.viewYaw) * var8;
      this.forwardUp = (float)Math.sin(var1.viewPitch);
      this.cone = View.halfDiagonal() + LOOK_SLACK;
      this.eyeUp = var1.camZ * 2.4494896F + 1.5F;
      this.aheadX = var1.camX + clamp(velocityX * 1.5F, -40.0F, 40.0F);
      this.aheadY = var1.camY + clamp(velocityY * 1.5F, -40.0F, 40.0F);
      this.fogged = var1.scene.nearEnd() + 8.0F;
      this.radius = View.radiusChunks();
      this.stride = this.radius * 2 + 1;
   }

   private int run() {
      Arrays.fill(this.frame.scene.farMask, 0, this.stride * this.stride, (byte)0);
      this.frame.scene.farMaskX = this.pcx - this.radius;
      this.frame.scene.farMaskY = this.pcy - this.radius;
      this.frame.scene.farMaskSize = this.stride;

      for (int var1 = -this.radius; var1 <= this.radius; var1++) {
         for (int var2 = -this.radius; var2 <= this.radius; var2++) {
            this.visit(var2, var1);
         }
      }

      return this.waiting;
   }

   private void visit(int var1, int var2) {
      this.cx = this.pcx + var1;
      this.cy = this.pcy + var2;
      this.centreX = (this.cx + 0.5F) * 8.0F - this.frame.camX;
      this.centreY = (this.cy + 0.5F) * 8.0F - this.frame.camY;
      this.d2 = this.centreX * this.centreX + this.centreY * this.centreY;
      this.lift = this.frame.scene.relief == null ? 0.0F : this.frame.scene.relief.scene(this.centreX + this.frame.camX, this.centreY + this.frame.camY);
      this.liftRange();
      this.chunk = this.map.getChunkForGridSquare(this.cx * 8, this.cy * 8);
      if (this.chunk != null && this.chunk.loaded) {
         if (this.d2 > this.fogged * this.fogged) {
            this.leave(var1, var2);
         } else {
            ChunkCache.Entry var3 = ChunkCache.entryFor(this.chunk);
            float var4 = var3.leaveStart != 0L ? this.leftShare(var3) : (var3.lastSeenFrame != ChunkCache.frameCounter - 1L ? 0.0F : -1.0F);
            var3.leaveStart = 0L;
            var3.lastSeenFrame = ChunkCache.frameCounter;
            this.ring = Math.max(Math.abs(var1), Math.abs(var2));
            this.near = this.ring <= 2;
            this.aheadDX = (this.cx + 0.5F) * 8.0F - this.aheadX;
            this.aheadDY = (this.cy + 0.5F) * 8.0F - this.aheadY;

            for (int var5 = this.ring <= 3 ? this.chunk.minLevel : Math.max(this.lowest, this.chunk.minLevel); var5 <= this.chunk.maxLevel; var5++) {
               ChunkCache.Level var6 = var3.levels[var5 + 32];
               if (var6 == null) {
                  var6 = var3.levels[var5 + 32] = new ChunkCache.Level();
               }

               if (var4 >= 0.0F && var6.data != null) {
                  dropCrossFade(var6);
                  var6.fadeStart = this.now - (long)(var4 * 6.0E8F);
                  var6.fadeNanos = 600000000L;
               }

               float var7 = (var5 + 0.5F) * 2.4494896F + this.lift - this.eyeUp;
               boolean var8 = this.near || this.inView(this.centreX, this.centreY, var7, this.forwardX, this.forwardY, this.forwardUp);
               this.refresh(var6, var5, var8, var7);
               float var9 = ChunkCache.fadeOf(var6, this.now);
               if (var5 == 0 && var6.data != null) {
                  float var10 = var6.fading != null ? 1.0F : Math.min(var9, 1.0F);
                  this.frame.scene.farMask[(var2 + this.radius) * this.stride + var1 + this.radius] = (byte)Math.round(var10 * 255.0F);
               }

               if (var6.data != null || var6.fading != null) {
                  this.list(var6, var5, var8, var7, var9);
               }
            }
         }
      }
   }

   private void leave(int var1, int var2) {
      ChunkCache.Entry var3 = ChunkCache.existing(this.chunk);
      if (var3 != null) {
         if (var3.leaveStart == 0L) {
            if (var3.lastSeenFrame != ChunkCache.frameCounter - 1L) {
               return;
            }

            ChunkCache.Level var4 = var3.levels[32];
            float var5 = var4 != null && var4.data != null && var4.fading == null ? ChunkCache.fadeOf(var4, this.now) : 1.0F;
            var3.leaveStart = this.now - (long)((1.0F - var5) * 6.0E8F);
         }

         float var9 = this.leftShare(var3);
         if (var9 <= 0.0F) {
            var3.leaveStart = 0L;
         } else {
            var3.lastSeenFrame = ChunkCache.frameCounter;
            this.ring = Math.max(Math.abs(var1), Math.abs(var2));
            this.near = this.ring <= 2;

            for (int var10 = this.ring <= 3 ? this.chunk.minLevel : Math.max(this.lowest, this.chunk.minLevel); var10 <= this.chunk.maxLevel; var10++) {
               ChunkCache.Level var6 = var3.levels[var10 + 32];
               if (var6 != null && var6.data != null) {
                  dropCrossFade(var6);
                  float var7 = (var10 + 0.5F) * 2.4494896F + this.lift - this.eyeUp;
                  boolean var8 = this.near || this.inView(this.centreX, this.centreY, var7, this.forwardX, this.forwardY, this.forwardUp);
                  if (var10 == 0) {
                     this.frame.scene.farMask[(var2 + this.radius) * this.stride + var1 + this.radius] = (byte)Math.round(var9 * 255.0F);
                  }

                  this.list(var6, var10, var8, var7, var9);
               }
            }
         }
      }
   }

   private float leftShare(ChunkCache.Entry var1) {
      return Math.max(0.0F, 1.0F - (float)(this.now - var1.leaveStart) / 6.0E8F);
   }

   private static void dropCrossFade(ChunkCache.Level var0) {
      ChunkBudget.letGo(var0.fading);
      var0.fading = null;
      var0.fadingModelled = null;
      var0.fadingModelledItems = null;
      var0.fadingBodies = null;
   }

   private void refresh(ChunkCache.Level var1, int var2, boolean var3, float var4) {
      boolean var5 = this.ring <= 5 || this.ring == 6 && var1.data != null && var1.gatheredModelsNear;
      int var6 = ModelPacks.ring();
      var1.packsNear = var6 >= 0 && (this.ring <= var6 || this.ring == var6 + 1 && var1.data != null && var1.gatheredPacksNear);
      boolean var7 = var1.gatheredModelsNear != var5;
      boolean var8 = var1.gatheredPacksNear != var1.packsNear;
      if (var1.data == null || (!var7 || !var1.modelsMatter) && (!var8 || !var1.packsMatter)) {
         if (var1.data != null && (var7 || var8)) {
            var1.gatheredModelsNear = var5;
            var1.gatheredPacksNear = var1.packsNear;
            Profile.ringSpared();
         }
      } else {
         if (!var1.dirty) {
            var1.reason = 3;
         }

         var1.dirty = true;
         var1.retryFrame = 0L;
      }

      if (!var1.dirty && (this.acrossLoaded(var1) || MapFacades.arrived(var1.missedCell, var1.mapGeneration))) {
         var1.reason = 1;
         var1.dirty = true;
      }

      boolean var9 = var1.dirty && ChunkCache.frameCounter >= var1.retryFrame;
      boolean var10 = this.ring <= AT_ONCE.getInt() && (var1.data != null || var2 == this.playerLevel);
      if (var9 && var10) {
         ChunkBuilds.rebuild(var1, this.chunk, var2, this.playerIndex, var5, this.cell, this.now, 150000000L, true);
      } else if (var9 && (var3 || OUT_OF_VIEW.get() || !ChunkBudget.tight)) {
         ChunkBuilds.queue(var1, this.chunk, var2, var5, (var3 ? 0.0F : 1.0E7F) + this.aheadDX * this.aheadDX + this.aheadDY * this.aheadDY + var4 * var4);
         if (var3) {
            this.waiting++;
         }
      }
   }

   private boolean acrossLoaded(ChunkCache.Level var1) {
      if (var1.acrossUnloaded != 0 && var1.adjacentLoads != this.chunk.adjacentChunkLoadedCounter) {
         var1.adjacentLoads = this.chunk.adjacentChunkLoadedCounter;
         return (var1.acrossUnloaded & 1) != 0 && this.cell.getChunk(this.chunk.wx - 1, this.chunk.wy) != null
            || (var1.acrossUnloaded & 2) != 0 && this.cell.getChunk(this.chunk.wx, this.chunk.wy - 1) != null;
      } else {
         return false;
      }
   }

   private void liftRange() {
      this.liftLow = this.liftHigh = this.lift;
      if (this.frame.scene.relief != null) {
         for (int var1 = 0; var1 <= 8; var1 += 4) {
            for (int var2 = 0; var2 <= 8; var2 += 4) {
               float var3 = this.frame.scene.relief.scene(this.cx * 8 + var2, this.cy * 8 + var1);
               this.liftLow = Math.min(this.liftLow, var3);
               this.liftHigh = Math.max(this.liftHigh, var3);
            }
         }
      }
   }

   private void list(ChunkCache.Level var1, int var2, boolean var3, float var4, float var5) {
      byte var6 = (byte)((var3 ? 1 : 0) | (var3 && this.ring <= 6 ? 4 : 0) | (this.ring <= 6 ? 64 : 0));
      if (var3) {
         this.models(var1);
      }

      if (var1.bodies != null) {
         var1.bodies.list(var1.fading != null ? 1.0F : Math.min(var5, 1.0F), var1.fading == null || var1.fadingBodies != null);
      }

      float var7 = -(this.cx * 8 - this.frame.camX);
      float var8 = -this.frame.camZ * 2.4494896F;
      float var9 = -(this.cy * 8 - this.frame.camY);
      float var10 = this.d2 + var4 * var4;
      if (!var3 && !OUT_OF_VIEW.get()) {
         ChunkBudget.outOfView(var1, var10);
      }

      if (var1.data != null) {
         DrawList.candidate(
            var1.data,
            var7,
            var8,
            var9,
            var6,
            var10,
            this.sun,
            this.centreX,
            this.centreY,
            var2,
            this.eyeUp,
            this.liftLow,
            this.liftHigh,
            0.0F,
            var5 >= 1.0F ? 2.0F : var5,
            this.cx,
            this.cy,
            var1.bodies
         );
      }

      if (var1.fading != null) {
         DrawList.candidate(
            var1.fading,
            var7,
            var8,
            var9,
            var6,
            var10,
            this.sun,
            this.centreX,
            this.centreY,
            var2,
            this.eyeUp,
            this.liftLow,
            this.liftHigh,
            var5,
            2.0F,
            this.cx,
            this.cy,
            var1.fadingBodies
         );
      }
   }

   private void models(ChunkCache.Level var1) {
      if (var1.modelsNear && var1.modelled != null) {
         this.frame.modelObjects.addAll(var1.modelled);
      }

      if (var1.modelsNear && var1.modelledItems != null) {
         this.frame.modelItems.addAll(var1.modelledItems);
      }

      if (var1.fading != null && !var1.modelsNear && var1.fadingModelled != null) {
         this.frame.modelObjects.addAll(var1.fadingModelled);
      }

      if (var1.fading != null && !var1.modelsNear && var1.fadingModelledItems != null) {
         this.frame.modelItems.addAll(var1.fadingModelledItems);
      }
   }

   private boolean inView(float var1, float var2, float var3, float var4, float var5, float var6) {
      var1 -= View.cameraX;
      var2 -= View.cameraY;
      var3 -= View.cameraUp;
      float var7 = (float)Math.sqrt(var1 * var1 + var2 * var2 + var3 * var3);
      if (var7 <= 7.0F) {
         return true;
      }

      double var8 = Math.max(-1.0, Math.min(1.0, (var1 * var4 + var2 * var5 + var3 * var6) / var7));
      return Math.acos(var8) <= this.cone + Math.asin(7.0F / var7);
   }

   private static float clamp(float var0, float var1, float var2) {
      return var0 < var1 ? var1 : Math.min(var0, var2);
   }

   static {
      OUT_OF_VIEW.describe(
         "Builds and keeps every loaded chunk whether it is in view or not, so the hills about you cast their shadows as you turn; costs the main thread the building, and the pools room."
      );
   }
}
