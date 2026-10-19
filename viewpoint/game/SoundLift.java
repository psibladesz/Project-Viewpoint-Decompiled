package viewpoint.game;

import fmod.javafmod;
import fmod.fmod.FMODSoundEmitter;
import java.lang.reflect.Field;
import java.util.List;
import viewpoint.core.View;
import viewpoint.far.Relief;
import viewpoint.render.FarRelief;
import zombie.network.GameServer;

public final class SoundLift {
   private static final Field INSTANCES = field("fmod.fmod.FMODSoundEmitter", "instances");
   private static final Field EVENT = field("fmod.fmod.FMODSoundEmitter$EventSound", "eventInstance");
   private static final Field CHANNEL = field("fmod.fmod.FMODSoundEmitter$FileSound", "channel");
   private static final Field IS_3D = field("fmod.fmod.FMODSoundEmitter$FileSound", "is3d");
   private static boolean raised;

   private SoundLift() {
   }

   public static void ticked(FMODSoundEmitter var0) {
      FarRelief.Lift var1 = View.enabled ? Relief.current() : null;
      if (!GameServer.server && (var1 != null || raised) && (var0.x != 0.0F || var0.y != 0.0F)) {
         raised |= var1 != null;
         float var2 = var1 == null ? 0.0F : var1.scene(var0.x, var0.y) * 3.0F / 2.4494896F;
         float var3 = var0.z * 3.0F + var2;

         try {
            for (Object var5 : (List)INSTANCES.get(var0)) {
               place(var5, var0.x, var0.y, var3);
            }
         } catch (IllegalAccessException var6) {
            throw new IllegalStateException("a sound emitter's sounds cannot be read", var6);
         }
      }
   }

   private static void place(Object var0, float var1, float var2, float var3) throws IllegalAccessException {
      if (EVENT.getDeclaringClass().isInstance(var0)) {
         long var4 = EVENT.getLong(var0);
         if (var4 != 0L) {
            javafmod.FMOD_Studio_EventInstance3D(var4, var1, var2, var3);
         }
      } else if (CHANNEL.getDeclaringClass().isInstance(var0) && IS_3D.getByte(var0) == 1) {
         long var6 = CHANNEL.getLong(var0);
         if (var6 != 0L) {
            javafmod.FMOD_Channel_Set3DAttributes(var6, var1, var2, var3, 0.0F, 0.0F, 0.0F);
         }
      }
   }

   private static Field field(String var0, String var1) {
      try {
         Field var2 = Class.forName(var0).getDeclaredField(var1);
         var2.setAccessible(true);
         return var2;
      } catch (ReflectiveOperationException var3) {
         throw new IllegalStateException("no " + var0 + "." + var1 + " to raise the sounds by", var3);
      }
   }
}
