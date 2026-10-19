package viewpoint.light;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import viewpoint.platform.Caches;
import viewpoint.platform.LiveSettings;
import viewpoint.render.LampReadbacks;
import viewpoint.render.SpotDraws;
import zombie.core.skinnedmodel.model.ModelMesh;
import zombie.core.skinnedmodel.model.VehicleModelInstance;
import zombie.core.textures.Texture;
import zombie.scripting.objects.VehicleScript;
import zombie.scripting.objects.VehicleScript.Model;
import zombie.vehicles.BaseVehicle;

public final class LampBakes implements Caches.Cache {
   private static final LiveSettings.Toggle ON = LiveSettings.toggle("debug.lampsOnGlass", "Lamps on the model's glass", "Debug/Lamp placement", true);
   private static final int ASKS = 1;
   private static final int WAITING = 2;
   private static final long RETRY = 300L;
   private static final long KIND_BYTES = 512L;
   private static final int NEW = 0;
   private static final int ASKED = 1;
   private static final int BAKING = 2;
   private static final int DONE = 3;
   private static final ThreadPoolExecutor WORKER = new ThreadPoolExecutor(1, 1, 30L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(), var0 -> {
      Thread var1 = new Thread(var0, "Viewpoint lamp glass");
      var1.setDaemon(true);
      var1.setPriority(1);
      return var1;
   });
   private static final IdentityHashMap<VehicleScript, ArrayList<LampBakes.Kind>> kinds = new IdentityHashMap<>();
   private static int asks;
   private static int waiting;
   private static int kindCount;
   private static int found;
   private static final ConcurrentLinkedQueue<LampBakes.Found> FOUND = new ConcurrentLinkedQueue<>();

   static void frame(SpotDraws var0) {
      var0.lampAsks.clear();
      asks = 0;

      for (LampReadbacks.Read var1 = LampReadbacks.taken(); var1 != null; var1 = LampReadbacks.taken()) {
         bake(var1);
      }

      for (LampBakes.Found var2 = FOUND.poll(); var2 != null; var2 = FOUND.poll()) {
         keep(var2);
      }
   }

   static LampGlass.Glass[] of(BaseVehicle var0, SpotDraws var1) {
      LampBakes.Kind var2 = ON.get() ? kind(var0) : null;
      if (var2 == null) {
         return null;
      }

      var2.used = Caches.tick();
      if (var2.state == 3) {
         return var2.glass;
      }

      boolean var3 = var2.state == 1 && var2.used - var2.asked > 300L;
      if ((var2.state == 0 && waiting < 2 || var3) && asks < 1) {
         var1.lampAsks.add(new LampReadbacks.Ask(var2, var2.mesh, var2.mask, var2.lights));
         waiting = waiting + (var2.state == 0 ? 1 : 0);
         var2.state = 1;
         var2.asked = var2.used;
         asks++;
      }

      return null;
   }

   static String describe() {
      return found + "/" + kindCount + " kinds' glass";
   }

   private static LampBakes.Kind kind(BaseVehicle var0) {
      if (var0.sprite != null && var0.sprite.modelSlot != null && var0.sprite.modelSlot.model instanceof VehicleModelInstance var1) {
         ModelMesh var7 = var1.model == null ? null : var1.model.mesh;
         VehicleScript var3 = var0.getScript();
         if (var7 != null && var7.isReady() && var1.textureMask != null && var3 != null) {
            ArrayList<LampBakes.Kind> var4 = kinds.computeIfAbsent(var3, var0x -> new ArrayList<>());

            for (LampBakes.Kind var6 : var4) {
               if (var6.mesh == var7 && var6.mask == var1.textureMask && var6.lights == var1.textureLights) {
                  return var6;
               }
            }

            float[] var8 = placing(var3, var1, var7);
            if (var8 == null) {
               return null;
            }

            LampBakes.Kind var9 = new LampBakes.Kind(var3, var7, var1.textureMask, var1.textureLights, var8);
            var4.add(var9);
            kindCount++;
            return var9;
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   private static float[] placing(VehicleScript var0, VehicleModelInstance var1, ModelMesh var2) {
      Model var3 = var0.getModel();
      if (var3 == null) {
         return null;
      }

      Vector3f var4 = var3.getOffset();
      Vector3f var5 = var3.getRotate();
      float var6 = var0.getModelScale() * var1.scale;
      float var7 = var1.modelScript != null && var1.modelScript.invertX ? -var6 : var6;
      Quaternionf var8 = new Quaternionf().rotationXYZ((float)Math.toRadians(var5.x), (float)Math.toRadians(var5.y), (float)Math.toRadians(var5.z));
      Matrix4f var9 = new Matrix4f()
         .scaling(-1.0F, 1.0F, 1.0F)
         .mul(new Matrix4f().translationRotateScale(-var4.x, var4.y, var4.z, var8.x, var8.y, var8.z, var8.w, var7, var6, var6));
      if (var2.transform != null) {
         var9.mul(new Matrix4f(var2.transform).transpose());
      }

      return var9.get(new float[16]);
   }

   private static void bake(LampReadbacks.Read var0) {
      if (var0.key() instanceof LampBakes.Kind var1 && var1.state == 1) {
         var1.state = 2;
         waiting--;
         if (var0.positions() == null) {
            keep(new LampBakes.Found(var1, new LampGlass.Glass[4], 0L));
         } else {
            LampGlass.Image var5 = new LampGlass.Image(var0.mask(), var0.maskWidth(), var0.maskHeight());
            LampGlass.Image var3 = var0.lights() == null ? null : new LampGlass.Image(var0.lights(), var0.lightsWidth(), var0.lightsHeight());
            float[] var4 = var1.placing;
            WORKER.execute(() -> {
               long var5x = System.nanoTime();

               LampGlass.Glass[] var7;
               try {
                  var7 = LampGlass.find(var0.positions(), var0.uvs(), var0.indices(), var4, var5, var3);
               } catch (RuntimeException var9) {
                  System.out.println("[Viewpoint] headlights: a vehicle's lamps' glass could not be found: " + var9);
                  var7 = new LampGlass.Glass[4];
               }

               FOUND.add(new LampBakes.Found(var1, var7, System.nanoTime() - var5x));
            });
         }
      }
   }

   private static void keep(LampBakes.Found var0) {
      if (var0.kind().state == 2) {
         var0.kind().glass = var0.glass();
         var0.kind().state = 3;
         found++;
         String[] var1 = new String[]{"head left", "head right", "rear left", "rear right"};
         StringBuilder var2 = new StringBuilder("[Viewpoint] headlights: ")
            .append(var0.kind().script.getFullName())
            .append(String.format("'s lamps on its model's glass (%.1f ms):", var0.nanos() / 1000000.0));

         for (int var3 = 0; var3 < 4; var3++) {
            LampGlass.Glass var4 = var0.glass()[var3];
            var2.append(var3 == 0 ? " " : "; ").append(var1[var3]);
            var2.append(
               var4 == null
                  ? " none (on its box)"
                  : String.format(
                     " at %.2f %.2f %.2f facing %.2f %.2f %.2f, %.2f across, %d piece(s)%s",
                     var4.x(),
                     var4.y(),
                     var4.z(),
                     var4.nx(),
                     var4.ny(),
                     var4.nz(),
                     var4.size(),
                     var4.pieces(),
                     var4.shared() ? " from the other side's texels" : ""
                  )
            );
         }

         System.out.println(var2);
      }
   }

   @Override
   public long bytes() {
      return kindCount * 512L;
   }

   @Override
   public void list(Caches.Survey var1) {
      for (ArrayList<LampBakes.Kind> var3 : kinds.values()) {
         for (LampBakes.Kind var5 : var3) {
            var1.add(var5.used, 512L);
         }
      }
   }

   @Override
   public void evictBefore(long var1) {
      for (ArrayList var4 : kinds.values()) {
         for (int var5 = var4.size() - 1; var5 >= 0; var5--) {
            LampBakes.Kind var6 = (LampBakes.Kind)var4.get(var5);
            if (var6.used < var1 && (var6.state == 0 || var6.state == 3)) {
               found = found - (var6.state == 3 ? 1 : 0);
               kindCount--;
               var4.remove(var5);
            }
         }
      }

      kinds.values().removeIf(ArrayList::isEmpty);
   }

   private LampBakes() {
   }

   static {
      ON.describe(
         "Puts each vehicle's lamps on the glass its own model lights for them (found once for each kind of vehicle); off, every lamp stands on the vehicle's box, moved by the sliders below."
      );
      Caches.register(new LampBakes());
   }

   private record Found(LampBakes.Kind kind, LampGlass.Glass[] glass, long nanos) {
   }

   private static final class Kind {
      final VehicleScript script;
      final ModelMesh mesh;
      final Texture mask;
      final Texture lights;
      final float[] placing;
      LampGlass.Glass[] glass;
      int state = 0;
      long asked;
      long used;

      Kind(VehicleScript var1, ModelMesh var2, Texture var3, Texture var4, float[] var5) {
         this.script = var1;
         this.mesh = var2;
         this.mask = var3;
         this.lights = var4;
         this.placing = var5;
      }
   }
}
