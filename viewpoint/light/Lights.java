package viewpoint.light;

import java.util.ArrayList;
import java.util.Arrays;
import viewpoint.core.Frame;
import viewpoint.platform.ShaderPacks;
import viewpoint.render.SceneData;
import zombie.iso.IsoCell;
import zombie.iso.IsoGridSquare;

public final class Lights {
   private static final int RADIUS = 24;
   private static final int CANDIDATES = 512;
   private static final SourceRegister.Entry[] found = new SourceRegister.Entry[512];
   private static final float[] distance = new float[512];
   private static final Integer[] order = new Integer[512];
   private static final ArrayList<SourceRegister.Entry> on = new ArrayList<>();
   private static final boolean[] casts = new boolean[48];
   private static final float[] strength = new float[48];
   private static final int[] lampRank = new int[48];
   private static final int[] lampX = new int[48];
   private static final int[] lampY = new int[48];
   private static final int[] lampZ = new int[48];
   private static final float[] lampReach = new float[48];
   private static final float[] lampDistance = new float[48];
   private static final float[] lampStrength = new float[48];
   private static final long[] lampKey = new long[48];
   private static final boolean[] lampCasts = new boolean[48];

   public static void snapshot(Frame var0, IsoCell var1) {
      var0.scene.lightCount = 0;
      int var2 = registered(var0);
      Arrays.sort(order, 0, var2, (var0x, var1x) -> Float.compare(distance[var0x], distance[var1x]));
      int var3 = Math.min(var2, 48);
      var0.scene.lampShadowCount = shadows(var3, ShaderPacks.drawn().pipeline.lampShadows);

      for (int var4 = 0; var4 < 2; var4++) {
         for (int var5 = 0; var5 < var3; var5++) {
            if (casts[var5] == (var4 == 0)) {
               put(var0, var1, var5);
            }
         }
      }

      Arrays.fill(found, 0, var2, null);
   }

   private static int shadows(int var0, int var1) {
      Arrays.fill(casts, false);
      int var2 = 0;

      for (int var3 = 0; var3 < var0; var3++) {
         SourceRegister.Lamp var4 = found[order[var3]].lamp;
         if (var4 != null) {
            lampRank[var2] = var3;
            lampX[var2] = var4.x();
            lampY[var2] = var4.y();
            lampZ[var2] = var4.z();
            lampReach[var2] = var4.radius();
            lampKey[var2] = key(var4);
            lampDistance[var2] = distance[order[var3]];
            var2++;
         }
      }

      int var5 = ShadowLamps.choose(var2, lampX, lampY, lampZ, lampReach, lampKey, lampDistance, var1, lampCasts, lampStrength);

      for (int var6 = 0; var6 < var2; var6++) {
         casts[lampRank[var6]] = lampCasts[var6];
         strength[lampRank[var6]] = lampStrength[var6];
      }

      return var5;
   }

   private static void put(Frame var0, IsoCell var1, int var2) {
      int var3 = order[var2];
      float var4 = smoothstep(24.0F, 18.0F, distance[var3]) * Math.min(1.0F, (48 - var2) / 8.0F);
      SourceRegister.Lamp var5 = found[var3].lamp;
      put(var0, var1, found[var3], var4);
      SceneData var6 = var0.scene;
      int var7 = var6.lightCount - 1;
      var6.lights[var7 * 12 + 9] = casts[var2] ? strength[var2] : 0.0F;
      var6.lightKeys[var7] = var5 == null ? -1L : key(var5);
   }

   private static long key(SourceRegister.Lamp var0) {
      return (long)(var0.x() & 16777215) << 40 | (long)(var0.y() & 16777215) << 16 | (var0.z() & 0xFF) << 8 | var0.radius() & 0xFF;
   }

   private static int registered(Frame var0) {
      LightSources.register().on(on);
      int var1 = 0;

      for (int var2 = 0; var2 < on.size() && var1 < 512; var2++) {
         SourceRegister.Entry var3 = on.get(var2);
         float var4;
         if (var3.lamp != null) {
            var4 = distance(var3.lamp.x() + 0.5F, var3.lamp.y() + 0.5F, var3.lamp.z(), var0);
         } else {
            float var5 = Math.max(var3.room.x(), Math.min(var3.room.x() + var3.room.width(), var0.camX));
            float var6 = Math.max(var3.room.y(), Math.min(var3.room.y() + var3.room.height(), var0.camY));
            var4 = distance(var5, var6, var3.room.z(), var0);
         }

         if (var4 < 24.0F && (var3.lamp == null || var3.lamp.radius() > 0)) {
            var1 = candidate(var3, var4, var1);
         }
      }

      on.clear();
      return var1;
   }

   private static int candidate(SourceRegister.Entry var0, float var1, int var2) {
      found[var2] = var0;
      distance[var2] = var1;
      order[var2] = var2;
      return var2 + 1;
   }

   private static float distance(float var0, float var1, int var2, Frame var3) {
      float var4 = var0 - var3.camX;
      float var5 = var1 - var3.camY;
      float var6 = (var2 - var3.camZ) * 2.0F;
      return (float)Math.sqrt(var4 * var4 + var5 * var5 + var6 * var6);
   }

   private static void put(Frame var0, IsoCell var1, SourceRegister.Entry var2, float var3) {
      SourceRegister.Lamp var4 = var2.lamp;
      if (var4 != null) {
         put(var0, var1, var4, var2.fixture, var3);
      } else {
         SourceRegister.RoomLight var5 = var2.room;
         float var6 = 7.0F + 0.5F * (float)Math.hypot(var5.width(), var5.height());
         float var7 = var5.x() + 0.5F * var5.width();
         float var8 = var5.y() + 0.5F * var5.height();
         int var9 = put(var0, var7, var8, var5.z(), 2.1F, var6, var3);
         var0.scene.lights[var9 + 4] = 0.8980392F;
         var0.scene.lights[var9 + 5] = 0.84705883F;
         var0.scene.lights[var9 + 6] = 0.81960785F;
         var0.scene.lights[var9 + 8] = 0.0F;
      }
   }

   private static void put(Frame var0, IsoCell var1, SourceRegister.Lamp var2, LampFixture var3, float var4) {
      int var5 = put(var0, var2.x() + var3.x(), var2.y() + var3.y(), var2.z(), var3.rise(), var2.radius(), var4);
      var0.scene.lights[var5 + 4] = var2.r();
      var0.scene.lights[var5 + 5] = var2.g();
      var0.scene.lights[var5 + 6] = var2.b();
      IsoGridSquare var6 = var1.getGridSquare(var2.x(), var2.y(), var2.z());
      var0.scene.lights[var5 + 8] = var6 != null && !var6.isOutside() ? 0.0F : 1.0F;
   }

   static float lift(SceneData var0, float var1, float var2) {
      return var0.relief == null ? 0.0F : var0.relief.scene(var1, var2);
   }

   private static int put(Frame var0, float var1, float var2, int var3, float var4, float var5, float var6) {
      SceneData var7 = var0.scene;
      int var8 = var7.lightCount * 12;
      var7.lights[var8] = -(var1 - var0.camX);
      var7.lights[var8 + 1] = (var3 - var0.camZ) * 2.4494896F + var4 + lift(var7, var1, var2);
      var7.lights[var8 + 2] = -(var2 - var0.camY);
      var7.lights[var8 + 3] = var5;
      var7.lights[var8 + 7] = var6;
      var7.lights[var8 + 10] = var4;
      var7.lights[var8 + 11] = var3;
      var7.lightCount++;
      return var8;
   }

   private static float smoothstep(float var0, float var1, float var2) {
      float var3 = Math.max(0.0F, Math.min(1.0F, (var2 - var0) / (var1 - var0)));
      return var3 * var3 * (3.0F - 2.0F * var3);
   }

   private Lights() {
   }
}
