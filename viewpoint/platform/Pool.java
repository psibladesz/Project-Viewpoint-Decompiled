package viewpoint.platform;

public final class Pool {
   public static final double FULL = 0.9;
   public static final double ROOMY = 0.8;
   private static final long MIB = 1048576L;
   private static final int MIN_MIB = 16;
   private static final int RANGE = 8;
   private static final int STEP_MIB = 16;
   private static final String SECTION = "World/Memory";
   public static final Pool NEAR_MESHES = new Pool("near meshes", "memory.nearMeshMiB", 512);
   public static final Pool LIGHT_GRIDS = new Pool("light grids", "memory.lightAtlasMiB", 64);
   public static final Pool SHELL = new Pool("shell", "memory.shellMiB", 768);
   public static final Pool FAR = new Pool("far cells", "memory.farMiB", 192);
   public static final Pool RELIEF = new Pool("terrain relief", "memory.reliefMiB", 192);
   public static final Pool MODEL_MESHES = new Pool("model meshes", "memory.modelMeshMiB", 256);
   public static final Pool CPU = new Pool("cpu caches", "memory.cpuCacheMiB", 320);
   public static final Pool FLOORS = new Pool("floor pages", "memory.floorMiB", 512);
   public static final Pool PACKS = new Pool("model packs", "memory.packMiB", 768);
   public static final Pool IRIS = new Pool("Minecraft shaders", "memory.minecraftMiB", 4096);
   public static final Pool CORPSES = new Pool("posed corpses", "memory.corpseMiB", 256);
   public static final Pool SOUNDS = new Pool("sound files", "memory.soundMiB", 128);
   static final Pool[] ALL = new Pool[]{NEAR_MESHES, LIGHT_GRIDS, SHELL, FAR, RELIEF, MODEL_MESHES, CPU, FLOORS, PACKS, IRIS, CORPSES, SOUNDS};
   final String name;
   final String setting;
   private final LiveSettings.Number chosen;
   private volatile long limit = Long.MAX_VALUE;
   private volatile long used;

   private Pool(String var1, String var2, int var3) {
      this.name = var1;
      this.setting = var2;
      this.chosen = LiveSettings.number(var2, "Cap: " + var1 + " (MiB)", "World/Memory", 16.0F, var3 * 8, 16.0F, var3);
      this.chosen.describe("The most this pool holds; lowered, it takes full effect after a restart.");
   }

   public void alsoIn(String var1) {
      this.chosen.alsoIn(var1);
   }

   public void describe(String var1) {
      this.chosen.describe(var1);
   }

   public long cap() {
      return Math.min(this.chosen.getInt() * 1048576L, this.limit);
   }

   public void limit(long var1) {
      this.limit = var1;
   }

   public void use(long var1) {
      this.used = var1;
   }

   public long used() {
      return this.used;
   }

   public boolean over(double var1) {
      return this.used > this.cap() * var1;
   }

   public long excess(double var1) {
      return Math.max(0L, this.used - (long)(this.cap() * var1));
   }

   void set(String var1) {
      this.chosen.set(var1 == null ? this.chosen.fallback : this.parse(var1));
   }

   void carryOver(String var1) {
      if (var1 != null) {
         LiveSettings.carryOver(this.setting, Float.toString(this.parse(var1)));
      }
   }

   private float parse(String var1) {
      try {
         return Integer.parseInt(var1.trim());
      } catch (NumberFormatException var3) {
         System.out.println("[Viewpoint] " + this.setting + " is not a number of MiB: " + var1);
         return this.chosen.fallback;
      }
   }

   static String report() {
      StringBuilder var0 = new StringBuilder(" | memory MiB used/cap:");

      for (Pool var4 : ALL) {
         var0.append(String.format(" %s %.0f/%d,", var4.name, var4.used / 1048576.0, var4.cap() / 1048576L));
      }

      var0.setLength(var0.length() - 1);
      return var0.toString();
   }
}
