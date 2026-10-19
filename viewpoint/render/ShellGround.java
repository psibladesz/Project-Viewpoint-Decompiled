package viewpoint.render;

import imgui.ImGui;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Iterator;
import viewpoint.platform.LiveSettings;
import viewpoint.platform.Pool;
import viewpoint.platform.Profile;

public final class ShellGround {
   private static final String LODS = "World/Levels of detail";
   private static final String WORK = "World/Far world";
   public static final LiveSettings.Toggle BAKED = LiveSettings.toggle("lod.groundBaked", "Shell ground baked", "World/Levels of detail", true);
   public static final LiveSettings.Number TEXELS_FULL = LiveSettings.number(
      "lod.groundTexelsFull", "Full shell ground, texels per tile", "World/Levels of detail", 1.0F, 16.0F, 1.0F, 8.0F
   );
   public static final LiveSettings.Number TEXELS_LITE = LiveSettings.number(
      "lod.groundTexelsLite", "Light shell ground, texels per tile (0: none)", "World/Levels of detail", 0.0F, 8.0F, 1.0F, 2.0F
   );
   private static final LiveSettings.Number PAGES = LiveSettings.number(
      "far.groundBakesPerFrame", "Ground pages baked a frame", "World/Far world", 1.0F, 16.0F, 1.0F, 2.0F
   );
   private static final LiveSettings.Number BUDGET = LiveSettings.number(
      "far.groundBakeBudgetMs", "Ground bake budget (ms)", "World/Far world", 0.25F, 10.0F, 0.25F, 1.0F
   );
   private static final int KEEP_FRAMES = 60;
   private static final byte MASK_LITE = 1;
   private static final long ARRAY_BYTES = 16777216L;
   private static final double RECENT = 0.02;
   private final FloorBaker baker = new FloorBaker();
   private FloorSlices full;
   private FloorSlices lite;
   private final IdentityHashMap<GroundPage, ShellGround.Slot> slots = new IdentityHashMap<>();
   private final ArrayList<ShellGround.Slot> waiting = new ArrayList<>();
   private long frame;
   private int bakeFilter = -1;
   private int blocksFull;
   private int blocksLite;
   private int bakedFull;
   private int bakedLite;
   private double recentBakes;
   private double recentBakeMs;

   public static long pageBytes(int var0) {
      return var0 <= 0 ? 0L : new FloorSlices(66 * var0, mips(var0), false).sliceBytes();
   }

   void update(SceneData var1) {
      this.frame++;
      this.tiers();
      this.see(var1);
      long var2 = System.nanoTime();
      int var4 = this.bake(var2 + (long)(BUDGET.get() * 1000000.0F));
      this.recentBakes = this.recentBakes + (var4 - this.recentBakes) * 0.02;
      this.recentBakeMs = this.recentBakeMs + ((System.nanoTime() - var2) * 1.0E-6 - this.recentBakeMs) * 0.02;
      this.give(var1);
      this.forget();
   }

   private void tiers() {
      boolean var1 = this.bakeFilter != FloorFilter.bakeFilter();
      this.bakeFilter = FloorFilter.bakeFilter();
      this.full = this.tier(this.full, TEXELS_FULL.getInt(), false, var1);
      this.lite = this.tier(this.lite, TEXELS_LITE.getInt(), true, var1);
   }

   private FloorSlices tier(FloorSlices var1, int var2, boolean var3, boolean var4) {
      int var5 = 66 * var2;
      if (var1 != null && var1.size == var5 && !var4) {
         return var1;
      }

      if (var1 != null) {
         var1.clear();

         for (ShellGround.Slot var7 : this.slots.values()) {
            var7.slice = var7.lite == var3 ? -1 : var7.slice;
         }
      }

      return var2 <= 0 ? null : new FloorSlices(var5, mips(var2), (int)Math.max(1L, 16777216L / pageBytes(var2)), false);
   }

   private void see(SceneData var1) {
      this.waiting.clear();
      this.blocksFull = this.blocksLite = 0;

      for (int var2 = 0; var2 < var1.shellCount; var2++) {
         FarGpu.BlockDraw var3 = var1.shells[var2];
         if (var3.lite) {
            this.blocksLite++;
         } else {
            this.blocksFull++;
         }

         if (var3.ground != null) {
            ShellGround.Slot var4 = this.slots.computeIfAbsent(var3.ground, ShellGround.Slot::new);
            var4.seen = this.frame;
            var4.lite = var3.lite;
            var4.distance = var3.distance;
            if (var4.slice < 0) {
               this.waiting.add(var4);
            }
         }
      }
   }

   private int bake(long var1) {
      this.waiting.sort((var0, var1x) -> Float.compare(var0.distance, var1x.distance));
      int var3 = 0;

      for (ShellGround.Slot var5 : this.waiting) {
         FloorSlices var6 = var5.lite ? this.lite : this.full;
         if (var3 >= PAGES.getInt() || var3 > 0 && System.nanoTime() > var1) {
            break;
         }

         if (var6 != null) {
            var5.slice = var6.take(Long.MAX_VALUE);
            this.baker.bake(var5.page, var6, var5.slice);
            var3++;
         }
      }

      return var3;
   }

   private void give(SceneData var1) {
      this.bakedFull = this.bakedLite = 0;

      for (int var2 = 0; var2 < var1.shellCount; var2++) {
         FarGpu.BlockDraw var3 = var1.shells[var2];
         ShellGround.Slot var4 = var3.ground == null ? null : this.slots.get(var3.ground);
         FloorSlices var5 = var4 == null ? null : (var4.lite ? this.lite : this.full);
         var3.groundArray = null;
         if (var4 != null) {
            if (var4.slice >= 0 && var5 != null) {
               var3.groundArray = var5.array(var4.slice);
               var3.groundTexture = var5.texture(var4.slice);
               var3.groundLayer = FloorSlices.layer(var4.slice);
               this.bakedFull = this.bakedFull + (var4.lite ? 0 : 1);
               this.bakedLite = this.bakedLite + (var4.lite ? 1 : 0);
            } else if (var3.maskIndex >= 0) {
               var1.shellMask[var3.maskIndex * 3] = 1;
            }
         }
      }
   }

   private void forget() {
      Iterator var1 = this.slots.values().iterator();

      while (var1.hasNext()) {
         ShellGround.Slot var2 = (ShellGround.Slot)var1.next();
         if (this.frame - var2.seen > 60L) {
            FloorSlices var3 = var2.lite ? this.lite : this.full;
            if (var3 != null) {
               var3.free(var2.slice);
            }

            var1.remove();
         }
      }
   }

   private static int mips(int var0) {
      return 32 - Integer.numberOfLeadingZeros(66 * var0);
   }

   String report() {
      return String.format(
         "blocks drawn full %d lite %d, from pages %d + %d, pages held %d, %.0f MiB, baked/frame %.2f, %.2f ms",
         this.blocksFull,
         this.blocksLite,
         this.bakedFull,
         this.bakedLite,
         this.slots.size(),
         this.madeBytes() / 1048576.0,
         this.recentBakes,
         this.recentBakeMs
      );
   }

   private long madeBytes() {
      return (this.full == null ? 0L : this.full.madeBytes()) + (this.lite == null ? 0L : this.lite.madeBytes());
   }

   void drawFigures() {
      ImGui.separator();
      ImGui.textUnformatted(
         String.format("Blocks drawn: full %d (%d from pages), lite %d (%d from pages)", this.blocksFull, this.bakedFull, this.blocksLite, this.bakedLite)
      );
      ImGui.textUnformatted(
         String.format(
            "Shell: %.2fM vertices, pool %d of %d MiB, %d blocks waiting to upload",
            Profile.shellVertices / 1000000.0,
            Pool.SHELL.used() >> 20,
            Pool.SHELL.cap() >> 20,
            Profile.shellUploads
         )
      );
      ImGui.textUnformatted(
         String.format("Ground pages: %d held, %.0f MiB made, %d waiting", this.slots.size(), this.madeBytes() / 1048576.0, this.waiting.size())
      );
      ImGui.textUnformatted(String.format("Ground bakes: %.2f a frame, %.2f ms a frame", this.recentBakes, this.recentBakeMs));
      ImGui.textUnformatted(
         String.format("GPU ms: far %.2f, g-buffer %.2f, shadow %.2f", Profile.recentGpuMs(6), Profile.recentGpuMs(3), Profile.recentGpuMs(2))
      );
      ImGui.textUnformatted(
         String.format(
            "CPU ms: far pass %.2f, floors and ground bake %.2f, far world (main) %.2f",
            Profile.recentCpuMs(6),
            Profile.recentCpuMs(1),
            Profile.recentFarMainMs
         )
      );
      if (ImGui.isItemHovered()) {
         ImGui.setTooltip(
            "Switch \"Shell ground baked\" and compare: the G-buffer holds the shell's band, the shadow maps the far shadow map; the ground bake runs in the floors pass."
         );
      }
   }

   private static final class Slot {
      final GroundPage page;
      boolean lite;
      int slice = -1;
      long seen;
      float distance;

      Slot(GroundPage var1) {
         this.page = var1;
      }
   }
}
