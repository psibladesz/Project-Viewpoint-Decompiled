package viewpoint.game;

import fmod.javafmod;
import fmod.fmod.SoundListener;
import viewpoint.core.View;
import viewpoint.input.FreeCam;
import viewpoint.input.Look;
import zombie.characters.IsoPlayer;
import zombie.network.GameServer;
import zombie.scripting.objects.CharacterTrait;

public final class Hearing {
   static final float LEVEL_UNITS = 3.0F;
   private static final int FIRST = 0;
   static final float ZOOMED_IN = 0.0F;
   private static final float[] forward = new float[3];
   private static final float[] up = new float[3];

   public static void listen(SoundListener var0) {
      if (var0.index == 0 && View.enabled && !GameServer.server) {
         FreeCam.Place var1 = FreeCam.centre();
         IsoPlayer var2 = IsoPlayer.players[0];
         boolean var3 = var1 != null && var2 != null && !var2.hasTrait(CharacterTrait.DEAF);
         float var4 = var3 ? (float)var1.x() : var0.x;
         float var5 = var3 ? (float)var1.y() : var0.y;
         float var6 = (var3 ? (float)var1.z() : var0.z) * 3.0F;
         orient(var1 != null ? var1.yaw() : Look.yaw, var1 != null ? var1.pitch() : Look.pitch, forward, up);
         javafmod.FMOD_Studio_Listener3D(0, var4, var5, var6, 0.0F, 0.0F, 0.0F, forward[0], forward[1], forward[2], up[0], up[1], up[2]);
      }
   }

   public static float cameraZoom(float var0) {
      return View.enabled ? 0.0F : var0;
   }

   static void orient(float var0, float var1, float[] var2, float[] var3) {
      float var4 = (float)Math.cos(var1);
      float var5 = (float)Math.cos(var0) * var4;
      float var6 = (float)Math.sin(var0) * var4;
      float var7 = (float)Math.sin(var1) * 3.0F / 2.4494896F;
      float var8 = (float)Math.sqrt(var5 * var5 + var6 * var6 + var7 * var7);
      var2[0] = var5 / var8;
      var2[1] = var6 / var8;
      var2[2] = var7 / var8;
      float var9 = -var2[2] * var2[0];
      float var10 = -var2[2] * var2[1];
      float var11 = 1.0F - var2[2] * var2[2];
      float var12 = (float)Math.sqrt(var9 * var9 + var10 * var10 + var11 * var11);
      var3[0] = var9 / var12;
      var3[1] = var10 / var12;
      var3[2] = var11 / var12;
   }

   private Hearing() {
   }
}
