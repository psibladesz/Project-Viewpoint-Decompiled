package viewpoint.render;

import java.util.ArrayList;
import java.util.Locale;
import viewpoint.platform.LiveSettings;
import viewpoint.platform.LongMap;
import viewpoint.platform.Pool;

final class FloorBakes {
   private static final String SECTION = "World/Floors";
   private static final LiveSettings.Number PAGES = LiveSettings.number(
      "floors.bakePagesPerFrame", "Near pages baked a frame", "World/Floors", 1.0F, 16.0F, 1.0F, 2.0F
   );
   private static final LiveSettings.Number BUDGET = LiveSettings.number("floors.bakeBudgetMs", "Bake budget (ms)", "World/Floors", 0.5F, 20.0F, 0.5F, 2.0F);
   private static final LiveSettings.Number SHARE = LiveSettings.number(
      "floors.detailShare", "Near floor texels (times the art's own)", "World/Floors", 0.25F, 2.0F, 0.25F, 1.0F
   );
   private static final LiveSettings.Number RING = LiveSettings.number(
      "floors.detailChunks", "Near floors within (chunks)", "World/Floors", 1.0F, 16.0F, 1.0F, 8.0F
   );
   private static final LiveSettings.Number LOW_PAGES = LiveSettings.number(
      "floors.lowPagesPerFrame", "Distant pages baked a frame", "World/Floors", 1.0F, 64.0F, 1.0F, 4.0F
   );
   private static final int LOW_TEXELS = 16;
   private static final int MIPS = 5;
   private static final int LEAST_TEXELS = 16;
   private static final int MOST_TEXELS = 256;
   private static final int TEXEL_STEP = 8;
   private static final int UNSEEN_ART_TEXELS = 64;
   private static final long FORGET_FRAMES = 600L;
   private static final long APPEAR_NANOS = 600000000L;
   private static final float SHARE_SCALE = 0.4F;
   private final FloorBaker baker = new FloorBaker();
   private final LongMap<FloorBakes.Slot> slots = new LongMap<>(256);
   private final ArrayList<FloorBakes.Slot> inView = new ArrayList<>();
   private final ArrayList<FloorBakes.Slot> waiting = new ArrayList<>();
   private FloorSlices detail;
   private FloorSlices low;
   private long frame;
   private int[] meshSlice = new int[512];
   private float[] meshShare = new float[512];
   static final int LOW = 1048576;
   private int bakeFilter = -1;
   private boolean surfaces;
   private float artTexels;
   private FloorBakes.Slot nearest;
   private int waitingLow;
   private int waitingDetail;
   private int bakedDetail;
   private int bakedLow;
   private int frames;
   private double bakeMs;

   void update(SceneData var1, boolean var2) {
      this.frame++;
      this.frames++;
      this.see(var1);
      this.tiers(var2);
      long var3 = System.nanoTime();
      long var5 = var3 + (long)(BUDGET.get() * 1000000.0);
      this.bakeLow(var5);
      this.bakeDetail(var5);
      this.bakeMs = this.bakeMs + (System.nanoTime() - var3) * 1.0E-6;
      if (this.frame % 60L == 0L) {
         this.forget();
      }

      this.slices(var1);
      Pool.FLOORS.use(this.detail.madeBytes() + this.low.madeBytes());
   }

   private void tiers(boolean var1) {
      int var2 = detailTexels(this.artTexels, SHARE.get()) * 10;
      boolean var3 = this.bakeFilter != FloorFilter.bakeFilter() || this.surfaces != var1;
      this.bakeFilter = FloorFilter.bakeFilter();
      this.surfaces = var1;
      if (this.low != null && var3) {
         this.low.clear();
         this.low = null;

         for (FloorBakes.Slot var5 : this.slots.values()) {
            var5.low = -1;
            var5.lowPage = null;
         }
      }

      if (this.low == null) {
         this.low = new FloorSlices(160, 5, this.surfaces);
      }

      if (this.detail == null || this.detail.size != var2 || var3) {
         if (this.detail != null) {
            this.detail.clear();
            this.baker.clearScratch();

            for (int var6 = 0; var6 < this.slots.capacity(); var6++) {
               FloorBakes.Slot var7 = this.slots.valueAt(var6);
               if (var7 != null) {
                  var7.detail = -1;
                  var7.detailPage = null;
               }
            }
         }

         this.detail = FloorSlices.bounded(var2, 5, this.surfaces);
      }
   }

   static int detailTexels(float var0, float var1) {
      float var2 = var0 > 0.0F ? var0 : 64.0F;
      int var3 = Math.round(var2 * var1 / 8.0F) * 8;
      return Math.max(16, Math.min(256, var3));
   }

   private void see(SceneData var1) {
      this.inView.clear();
      this.nearest = null;

      for (int var2 = 0; var2 < var1.meshCount; var2++) {
         FloorPage var3 = var1.meshFloor[var2];
         if (var3 != null && (var1.meshFlags[var2] & 5) != 0) {
            FloorBakes.Slot var4 = this.slots.get(var3.key());
            if (var4 == null) {
               var4 = new FloorBakes.Slot();
               this.slots.put(var3.key(), var4);
            }

            var4.page = var3;
            this.artTexels = Math.max(this.artTexels, var3.own() == null ? 0.0F : var3.own().texels());
            if (var4.seen != this.frame) {
               var4.seen = this.frame;
               float var5 = var1.meshOffsets[var2 * 3] - 4.0F;
               float var6 = var1.meshOffsets[var2 * 3 + 2] - 4.0F;
               var4.distance = Math.max(Math.abs(var5), Math.abs(var6)) / 8.0F;
               this.inView.add(var4);
               this.nearest = this.nearest != null && !(var4.distance < this.nearest.distance) ? this.nearest : var4;
            }
         }
      }
   }

   private void bakeLow(long var1) {
      this.waiting.clear();

      for (FloorBakes.Slot var4 : this.inView) {
         if (var4.low < 0 || var4.lowPage != var4.page) {
            this.waiting.add(var4);
         }
      }

      this.waiting.sort((var0, var1x) -> var0.low < 0 != var1x.low < 0 ? (var0.low < 0 ? -1 : 1) : Float.compare(var0.distance, var1x.distance));
      this.waitingLow = this.waiting.size();
      int var6 = LOW_PAGES.getInt();

      for (int var7 = 0; var7 < this.waiting.size() && var7 < var6 && (var7 == 0 || System.nanoTime() < var1); var7++) {
         FloorBakes.Slot var5 = this.waiting.get(var7);
         if (var5.low < 0 && (var5.low = this.take(this.low, lowBytes(), var5, false)) < 0) {
            break;
         }

         var5.lowPage = var5.page;
         this.bake(var5.lowPage, this.low, var5.low);
         this.bakedLow++;
      }
   }

   private void bakeDetail(long var1) {
      this.waiting.clear();
      float var3 = RING.get();

      for (FloorBakes.Slot var5 : this.inView) {
         if (var5.distance <= var3 && (var5.detail < 0 || var5.detailPage != var5.page)) {
            this.waiting.add(var5);
         }
      }

      this.waiting.sort((var0, var1x) -> Float.compare(var0.distance, var1x.distance));
      this.waitingDetail = this.waiting.size();
      int var7 = PAGES.getInt();

      for (int var8 = 0; var8 < this.waiting.size() && var8 < var7 && System.nanoTime() < var1; var8++) {
         FloorBakes.Slot var6 = this.waiting.get(var8);
         if (var6.detail < 0 && (var6.detail = this.take(this.detail, detailBytes(), var6, true)) < 0) {
            break;
         }

         var6.detailPage = var6.page;
         this.bake(var6.detailPage, this.detail, var6.detail);
         this.bakedDetail++;
      }
   }

   private void bake(FloorPage var1, FloorSlices var2, int var3) {
      this.baker.bake(var1, var2, var3, var1.wx * 8 - 1, var1.wy * 8 - 1);
   }

   private int take(FloorSlices var1, long var2, FloorBakes.Slot var4, boolean var5) {
      int var6 = var1.take(var2);
      if (var6 >= 0) {
         return var6;
      }

      FloorBakes.Slot var7 = giver(this.slots, var4, var5, this.frame);
      if (var7 == null) {
         return -1;
      }

      if (var5) {
         var6 = var7.detail;
         var7.detail = -1;
         var7.detailPage = null;
      } else {
         var6 = var7.low;
         var7.low = -1;
         var7.lowPage = null;
      }

      return var6;
   }

   static FloorBakes.Slot giver(LongMap<FloorBakes.Slot> var0, FloorBakes.Slot var1, boolean var2, long var3) {
      FloorBakes.Slot var5 = null;

      for (int var6 = 0; var6 < var0.capacity(); var6++) {
         FloorBakes.Slot var7 = (FloorBakes.Slot)var0.valueAt(var6);
         int var8 = var7 == null ? -1 : (var2 ? var7.detail : var7.low);
         if (var8 >= 0 && var7 != var1) {
            boolean var9 = var5 == null || var7.seen < var5.seen || var7.seen == var5.seen && var7.distance > var5.distance;
            if (var9 && (var7.seen < var3 || var7.distance > var1.distance)) {
               var5 = var7;
            }
         }
      }

      return var5;
   }

   private void forget() {
      for (int var1 = 0; var1 < this.slots.capacity(); var1++) {
         FloorBakes.Slot var2 = this.slots.valueAt(var1);
         if (var2 != null && this.frame - var2.seen > 600L) {
            this.detail.free(var2.detail);
            this.low.free(var2.low);
            this.slots.removeAt(var1);
         }
      }
   }

   private void slices(SceneData var1) {
      if (this.meshSlice.length < var1.meshCount) {
         this.meshSlice = new int[var1.meshes.length];
         this.meshShare = new float[var1.meshes.length];
      }

      long var2 = System.nanoTime();

      for (int var4 = 0; var4 < var1.meshCount; var4++) {
         FloorPage var5 = var1.meshFloor[var4];
         FloorBakes.Slot var6 = var5 == null ? null : this.slots.get(var5.key());
         this.meshSlice[var4] = var6 == null ? -1 : (var6.detail >= 0 ? var6.detail : (var6.low >= 0 ? var6.low | 1048576 : -1));
         this.meshShare[var4] = share(var6, this.meshSlice[var4], var2);
      }
   }

   int meshSlice(int var1) {
      return this.meshSlice[var1];
   }

   float meshShare(int var1) {
      return this.meshShare[var1];
   }

   private static float share(FloorBakes.Slot var0, int var1, long var2) {
      if (var0 != null && var1 >= 0) {
         if (var0.appeared == 0L) {
            var0.appeared = var2;
         }

         return Math.min(1.0F, (float)(var2 - var0.appeared) / 6.0E8F);
      } else {
         if (var0 != null) {
            var0.appeared = 0L;
         }

         return 1.0F;
      }
   }

   int lowSlice(int var1, int var2, int var3) {
      FloorBakes.Slot var4 = this.slots.get(FloorPage.key(var1, var2, var3));
      return var4 != null && var4.low >= 0 && var4.lowPage == var4.page ? var4.low | 1048576 : -1;
   }

   FloorPage lowPage(int var1, int var2, int var3) {
      FloorBakes.Slot var4 = this.slots.get(FloorPage.key(var1, var2, var3));
      return var4 == null ? null : var4.lowPage;
   }

   static int lowLastLevel() {
      return 4;
   }

   int texture(int var1) {
      return (var1 & 1048576) != 0 ? this.low.texture(var1 & -1048577) : this.detail.texture(var1);
   }

   int surface(int var1) {
      return (var1 & 1048576) != 0 ? this.low.surface(var1 & -1048577) : this.detail.surface(var1);
   }

   Object array(int var1) {
      return (var1 & 1048576) != 0 ? this.low.array(var1 & -1048577) : this.detail.array(var1);
   }

   static int layer(int var0) {
      return FloorSlices.layer(var0 & -1048577);
   }

   static float recordLayer(int var0, float var1) {
      return ((var0 & 1048576) != 0 ? -1.0F - layer(var0) : layer(var0)) + 0.4F * (1.0F - var1);
   }

   int tierSize(int var1) {
      return (var1 & 1048576) != 0 ? this.low.size : this.detail.size;
   }

   FloorPage nearestPage() {
      return this.nearest == null ? null : this.nearest.page;
   }

   int nearestSlice() {
      return this.nearest == null ? -1 : (this.nearest.detail >= 0 ? this.nearest.detail : (this.nearest.low >= 0 ? this.nearest.low | 1048576 : -1));
   }

   String figures() {
      return this.detail == null
         ? "No floor in view yet."
         : String.format(
            "Levels in view with a floor: %d. Slices: near %d, distant %d (%d MiB of %d). Waiting: near %d, distant %d.",
            this.inView.size(),
            this.detail.inUse(),
            this.low.inUse(),
            this.detail.madeBytes() + this.low.madeBytes() >> 20,
            Pool.FLOORS.cap() >> 20,
            this.waitingDetail,
            this.waitingLow
         );
   }

   private static long lowBytes() {
      return Pool.FLOORS.cap() / 4L;
   }

   private static long detailBytes() {
      return Pool.FLOORS.cap() - lowBytes();
   }

   String report() {
      String var1 = this.frames != 0 && this.detail != null
         ? String.format(
            Locale.ROOT,
            "baked/frame near %.2f far %.2f, %.2f ms; slices near %d far %d",
            (double)this.bakedDetail / this.frames,
            (double)this.bakedLow / this.frames,
            this.bakeMs / this.frames,
            this.detail.inUse(),
            this.low.inUse()
         )
         : "none";
      this.bakedDetail = this.bakedLow = this.frames = 0;
      this.bakeMs = 0.0;
      return var1;
   }

   static final class Slot {
      FloorPage page;
      FloorPage lowPage;
      FloorPage detailPage;
      int low = -1;
      int detail = -1;
      long seen;
      float distance;
      long appeared;
   }
}
