package viewpoint.models;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import viewpoint.core.Frame;
import viewpoint.platform.LiveSettings;
import viewpoint.platform.Pool;
import viewpoint.platform.Profile;
import zombie.core.skinnedmodel.ModelManager;
import zombie.iso.objects.IsoDeadBody;

public final class Corpses {
   private static final String SECTION = "Debug/Corpses";
   private static final LiveSettings.Toggle MODELS = LiveSettings.toggle("corpses.models", "Corpses as 3D models", "Debug/Corpses", true);
   private static final LiveSettings.Number LIMIT = LiveSettings.number(
      "corpses.limit", "3D corpses' distance (squares ahead, at most 40)", "Debug/Corpses", 2.0F, 40.0F, 1.0F, 40.0F
   );
   private static final LiveSettings.Number BAND = LiveSettings.number("corpses.band", "Fade band (squares)", "Debug/Corpses", 0.0F, 20.0F, 1.0F, 6.0F);
   private static final LiveSettings.Number MOST = LiveSettings.number("corpses.most", "Most 3D corpses", "Debug/Corpses", 0.0F, 512.0F, 1.0F, 256.0F);
   private static final LiveSettings.Toggle EVERY = LiveSettings.toggle(
      "corpses.every", "Every corpse within reach in 3D (no count limit)", "Debug/Corpses", false
   );
   private static final LiveSettings.Toggle IN_VIEW = LiveSettings.toggle("corpses.inView", "3D corpses only in the cone ahead", "Debug/Corpses", true);
   private static final LiveSettings.Number RANDOMNESS = LiveSettings.number(
      "corpses.randomness", "Pick's randomness (0: the nearest first)", "Debug/Corpses", 0.0F, 2.0F, 0.05F, 0.5F
   );
   private static final LiveSettings.Number SWAP_SECONDS = LiveSettings.number(
      "corpses.swapSeconds", "Swap dissolve (seconds)", "Debug/Corpses", 0.05F, 3.0F, 0.05F, 0.6F
   );
   private static final LiveSettings.Number POSES = LiveSettings.number(
      "corpses.posesPerFrame", "Corpses posed a frame", "Debug/Corpses", 1.0F, 64.0F, 1.0F, 8.0F
   );
   private static final LiveSettings.Toggle SHADOWS = LiveSettings.toggle("corpses.shadows", "Corpses cast shadows", "Debug/Corpses", true);
   private static final LiveSettings.Toggle TINT = LiveSettings.toggle(
      "corpses.tint", "Tint by form: models green, dissolving yellow, cards untinted", "Debug/Corpses", false
   );
   private static final int FRESH_FRAMES = 60;
   private static final float MAX_FRAME_SECONDS = 0.1F;
   private static final int RESTING_FRAMES = 1800;
   private static final IdentityHashMap<IsoDeadBody, Corpses.Corpse> known = new IdentityHashMap<>();
   private static final LinkedHashMap<IsoDeadBody, Corpses.Corpse> resting = new LinkedHashMap<>();
   private static final ArrayList<Corpses.Corpse> listed = new ArrayList<>();
   private static final CorpsePicks.Bodies bodies = new CorpsePicks.Bodies();
   private static long frameNumber;
   private static long lastNanos;
   private static long restingBytes;
   private static long poseBytes;
   private static float camX;
   private static float camY;
   private static float camZ;
   private static float limit;
   private static boolean culling;
   private static volatile int[] counts = new int[6];
   private static volatile long pickNanos;

   public static void begin(Frame var0) {
      for (Corpses.Corpse var2 : listed) {
         var2.listed = false;
      }

      listed.clear();
      frameNumber = var0.number;
      camX = var0.camX;
      camY = var0.camY;
      camZ = var0.camZ;
      limit = LIMIT.get();
      culling = IN_VIEW.get();
      CorpseView.begin(var0);
   }

   public static void listed(IsoDeadBody var0, Object var1, float var2, boolean var3) {
      float var4 = var0.getX();
      float var5 = var0.getY();
      float var6 = var0.getZ();
      float var7 = var4 - camX;
      float var8 = var5 - camY;
      float var9 = (var6 - camZ) * 2.4494896F;
      float var10 = (float)Math.sqrt(var7 * var7 + var8 * var8 + var9 * var9);
      if (!(var10 >= limit) && (!culling || CorpseView.sees(var4, var5, var6))) {
         Corpses.Corpse var11 = known.get(var0);
         if (var11 == null) {
            var11 = back(var0);
            known.put(var0, var11);
         } else if (var11.listed) {
            return;
         }

         var11.listed = true;
         var11.distance = var10;
         var11.look = var1;
         var11.coverage = var2;
         var11.eligible = var3 && var0.getDoRender();
         listed.add(var11);
      }
   }

   public static void pick(Frame var0) {
      long var1 = System.nanoTime();
      float var3 = lastNanos == 0L ? 0.0F : Math.min(0.1F, (float)(var1 - lastNanos) * 1.0E-9F);
      lastNanos = var1;
      frameNumber = var0.number;
      forgetUnlisted();
      if (!MODELS.get()) {
         letGoAll();
      } else {
         looks();
         choose(var3);
         poses();
         makeRoom();
      }

      report(System.nanoTime() - var1);
   }

   public static float share(IsoDeadBody var0) {
      Corpses.Corpse var1 = known.get(var0);
      return var1 != null && var1.listed ? var1.share : 0.0F;
   }

   public static void capture(Frame var0) {
      int var1 = 33 | (SHADOWS.get() ? 2 : 0);
      boolean var2 = TINT.get();

      for (Corpses.Corpse var4 : listed) {
         CorpsePose var5 = var4.pose;
         if (var5 != null && var5.built() && !var5.drawable()) {
            var0.scene.models.look(var5.textures, var4.distance);
            var5.hold(var0);
         }

         CorpsePose var6 = var5 != null && var5.drawable() ? var5 : var4.previous;
         float var7 = Math.min(var4.share, var4.coverage);
         if (var6 != null && var7 > 0.0F) {
            CorpseCapture.draw(var0, var4.body, var6, var7, var1, var2);
            var6.hold(var0);
            var6.lastUsed = frameNumber;
         }
      }
   }

   public static void release(Frame var0) {
      for (int var1 = 0; var1 < var0.corpseInstances.size(); var1++) {
         ModelManager.instance.derefModelInstance(var0.corpseInstances.get(var1));
      }

      var0.corpseInstances.clear();
   }

   public static void forget() {
      letGoAll();
      known.clear();
      listed.clear();
      lastNanos = 0L;
   }

   private static void forgetUnlisted() {
      Iterator var0 = known.values().iterator();

      while (var0.hasNext()) {
         Corpses.Corpse var1 = (Corpses.Corpse)var0.next();
         if (!var1.listed) {
            var0.remove();
            rest(var1);
         }
      }

      var0 = resting.values().iterator();

      while (var0.hasNext()) {
         Corpses.Corpse var3 = (Corpses.Corpse)var0.next();
         if (frameNumber - var3.restedAt <= 1800L) {
            break;
         }

         var0.remove();
         restingBytes = restingBytes - var3.restBytes;
         letGo(var3);
      }
   }

   private static void rest(Corpses.Corpse var0) {
      if (var0.previous != null) {
         var0.previous.letGo();
         var0.previous = null;
      }

      if (var0.pose != null && var0.pose.built()) {
         var0.picked = false;
         var0.progress = var0.share = 0.0F;
         var0.restedAt = frameNumber;
         var0.restBytes = var0.pose.bytes();
         restingBytes = restingBytes + var0.restBytes;
         resting.put(var0.body, var0);
      } else {
         letGo(var0);
      }
   }

   private static Corpses.Corpse back(IsoDeadBody var0) {
      Corpses.Corpse var1 = resting.remove(var0);
      if (var1 == null) {
         return new Corpses.Corpse(var0, frameNumber);
      }

      restingBytes = restingBytes - var1.restBytes;
      var1.firstListed = frameNumber;
      return var1;
   }

   private static void choose(float var0) {
      CorpsePicks.Limits var1 = new CorpsePicks.Limits(
         LIMIT.get(),
         Math.min(BAND.get(), LIMIT.get()),
         Math.min(EVERY.get() ? Integer.MAX_VALUE : MOST.getInt(), room()),
         RANDOMNESS.get(),
         var0 / SWAP_SECONDS.get()
      );
      bodies.size(listed.size());

      for (int var2 = 0; var2 < listed.size(); var2++) {
         Corpses.Corpse var3 = listed.get(var2);
         bodies.distance[var2] = var3.distance;
         bodies.random[var2] = var3.random;
         bodies.ready[var2] = var3.pose != null && var3.pose.look == var3.look && var3.pose.drawable() || var3.previous != null;
         bodies.fresh[var2] = frameNumber - var3.firstListed <= 60L;
         bodies.eligible[var2] = var3.eligible;
         bodies.picked[var2] = var3.picked;
         bodies.progress[var2] = var3.progress;
      }

      CorpsePicks.pick(bodies, var1);

      for (int var4 = 0; var4 < listed.size(); var4++) {
         Corpses.Corpse var5 = listed.get(var4);
         var5.picked = bodies.picked[var4];
         var5.progress = bodies.progress[var4];
         var5.share = bodies.share[var4];
      }
   }

   private static int room() {
      return poseBytes == 0L ? Integer.MAX_VALUE : (int)Math.min(2147483647L, (long)(Pool.CORPSES.cap() * 0.8) / poseBytes);
   }

   private static void looks() {
      for (Corpses.Corpse var1 : listed) {
         if (var1.pose != null && var1.pose.look != var1.look) {
            if (var1.previous == null && var1.picked && var1.pose.drawable()) {
               var1.previous = var1.pose;
            } else {
               var1.pose.letGo();
            }

            var1.pose = null;
         }
      }
   }

   private static void poses() {
      for (Corpses.Corpse var1 : listed) {
         if (var1.pose != null) {
            var1.pose.advance();
            if (var1.picked) {
               var1.pose.lastUsed = frameNumber;
            }
         }

         if (var1.previous != null && (!var1.picked || var1.pose != null && var1.pose.drawable())) {
            var1.previous.letGo();
            var1.previous = null;
         }
      }

      for (int var4 = POSES.getInt(); var4 > 0 && !Pool.CORPSES.over(0.9); var4--) {
         Corpses.Corpse var5 = null;

         for (Corpses.Corpse var3 : listed) {
            if (var3.picked && var3.pose == null && (var5 == null || before(var3, var5))) {
               var5 = var3;
            }
         }

         if (var5 == null) {
            return;
         }

         var5.pose = new CorpsePose(var5.body, var5.look);
         var5.pose.lastUsed = frameNumber;
      }
   }

   private static boolean before(Corpses.Corpse var0, Corpses.Corpse var1) {
      if (var0.previous != null != (var1.previous != null)) {
         return var0.previous != null;
      }

      boolean var2 = frameNumber - var0.firstListed <= 60L;
      boolean var3 = frameNumber - var1.firstListed <= 60L;
      return var2 != var3 ? var2 : var0.distance < var1.distance;
   }

   private static void makeRoom() {
      long var0 = restingBytes;
      long var2 = 0L;
      int var4 = 0;

      for (Corpses.Corpse var6 : listed) {
         var0 += bytes(var6);
         if (var6.pose != null && var6.pose.drawable()) {
            var2 += var6.pose.bytes();
            var4++;
         }
      }

      poseBytes = var4 > 0 ? var2 / var4 : poseBytes;
      Pool.CORPSES.use(var0);

      while (Pool.CORPSES.over(0.9) && !resting.isEmpty()) {
         Corpses.Corpse var8 = resting.values().iterator().next();
         resting.remove(var8.body);
         restingBytes = restingBytes - var8.restBytes;
         var0 -= var8.restBytes;
         letGo(var8);
         Pool.CORPSES.use(var0);
         if (!Pool.CORPSES.over(0.8)) {
            return;
         }
      }

      while (Pool.CORPSES.over(0.9)) {
         Corpses.Corpse var9 = null;

         for (Corpses.Corpse var7 : listed) {
            if (var7.pose != null && !var7.picked && (var9 == null || var7.pose.lastUsed < var9.pose.lastUsed)) {
               var9 = var7;
            }
         }

         if (var9 == null) {
            return;
         }

         var0 -= bytes(var9);
         letGo(var9);
         Pool.CORPSES.use(var0);
         if (!Pool.CORPSES.over(0.8)) {
            return;
         }
      }
   }

   private static void letGoAll() {
      for (Corpses.Corpse var1 : known.values()) {
         letGo(var1);
         var1.picked = false;
         var1.progress = var1.share = 0.0F;
      }

      for (Corpses.Corpse var3 : resting.values()) {
         letGo(var3);
      }

      resting.clear();
      restingBytes = 0L;
      Pool.CORPSES.use(0L);
   }

   private static void letGo(Corpses.Corpse var0) {
      if (var0.pose != null) {
         var0.pose.letGo();
         var0.pose = null;
      }

      if (var0.previous != null) {
         var0.previous.letGo();
         var0.previous = null;
      }
   }

   private static long bytes(Corpses.Corpse var0) {
      return (var0.pose == null ? 0L : var0.pose.bytes()) + (var0.previous == null ? 0L : var0.previous.bytes());
   }

   private static void report(long var0) {
      byte var2 = 0;
      byte var3 = 0;
      byte var4 = 0;
      byte var5 = 0;

      for (Corpses.Corpse var7 : listed) {
         var2 += var7.share > 0.0F ? 1 : 0;
         var3 += var7.share > 0.0F && var7.share < 1.0F ? 1 : 0;
         var4 += !var7.picked || var7.pose != null && var7.pose.drawable() ? 0 : 1;
         var5 += var7.pose != null ? 1 : 0;
      }

      counts = new int[]{listed.size(), var2, var3, var4, var5, resting.size()};
      pickNanos = var0;
      Profile.corpsesReport = Corpses::describe;
   }

   private static String describe() {
      int[] var0 = counts;
      return String.format(
         "%d listed, %d models (%d dissolving), %d waiting, %d posed, %d resting, %.2f ms",
         var0[0],
         var0[1],
         var0[2],
         var0[3],
         var0[4],
         var0[5],
         pickNanos * 1.0E-6
      );
   }

   private static float random(IsoDeadBody var0) {
      long var1 = Float.floatToIntBits(var0.getX());
      var1 = var1 * 31L + Float.floatToIntBits(var0.getY());
      var1 = var1 * 31L + Float.floatToIntBits(var0.getZ());
      var1 = var1 * 31L + Float.floatToIntBits(var0.getAngle());
      var1 *= -7046029254386353131L;
      var1 ^= var1 >>> 29;
      return (float)(var1 >>> 40) / 1.6777216E7F;
   }

   private Corpses() {
   }

   static {
      LIMIT.describe(
         "How far ahead a corpse may be a 3D model: the length of the cone that starts at the player and runs ahead, as wide as the screen. The chance of a corpse being a model falls along it from the player's feet, and over its last squares (Fade band) the models dissolve to the flat pictures. At most 40: the ring of chunks the models are drawn in."
      );
      MOST.describe(
         "At most this many corpses are 3D models, and no more than the posed corpses' pool holds (Cap: posed corpses, below; about 1.2 MiB a corpse). With fewer, the models thin out along the whole cone rather than stopping short of its distance."
      );
      EVERY.describe(
         "For a strong PC: every corpse in the cone, within the distance above, is a 3D model, however many lie there, in place of Most 3D corpses. The posed corpses' pool still bounds them, about 1.2 MiB a corpse: raise its cap (below) for a big pile."
      );
      IN_VIEW.describe(
         "On, a corpse is considered for a 3D model only while it lies in the cone that starts at the player and runs ahead as wide as the screen (with a small margin), and is on screen: any other keeps its picture with nothing done for it each frame, and one posed lately takes its pose back as it comes into the cone. Off, to compare: every corpse within the distance, all round the player."
      );
      Pool.CORPSES
         .describe(
            "The most the posed corpses hold, about 1.2 MiB a corpse: it bounds how many corpses are 3D models. Raised or lowered, it takes effect at once."
         );
      Pool.CORPSES.alsoIn("Debug/Corpses");
   }

   private static final class Corpse {
      final IsoDeadBody body;
      final float random;
      long firstListed;
      long restedAt;
      long restBytes;
      Object look;
      float coverage;
      float distance;
      float progress;
      float share;
      boolean listed;
      boolean eligible;
      boolean picked;
      CorpsePose pose;
      CorpsePose previous;

      Corpse(IsoDeadBody var1, long var2) {
         this.body = var1;
         this.random = Corpses.random(var1);
         this.firstListed = var2;
      }
   }
}
