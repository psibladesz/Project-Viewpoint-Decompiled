package viewpoint.game;

import java.lang.reflect.Field;
import java.util.ArrayList;
import viewpoint.core.Frame;
import viewpoint.input.FreeCam;
import viewpoint.platform.Tuning;
import viewpoint.world.WorldMesher;
import zombie.characters.Capability;
import zombie.characters.IsoGameCharacter;
import zombie.characters.IsoPlayer;
import zombie.chat.ChatElement;
import zombie.inventory.types.Radio;
import zombie.iso.IsoCamera;
import zombie.iso.IsoCell;
import zombie.iso.IsoGridSquare;
import zombie.iso.IsoMovingObject;
import zombie.iso.objects.IsoRadio;
import zombie.iso.objects.IsoTelevision;
import zombie.iso.objects.IsoWaveSignal;
import zombie.network.GameClient;
import zombie.radio.devices.DeviceData;
import zombie.scripting.objects.CharacterTrait;
import zombie.ui.TextDrawObject;
import zombie.vehicles.BaseVehicle;
import zombie.vehicles.VehiclePart;

public final class WorldText {
   private static final Field NOTE = field("haloNote");
   private static final Field NOTE_TIME = field("haloDispTime");
   private static final int NOTE_GAP = 2;
   private static final WorldToScreen screen = new WorldToScreen();
   private static final float[] pixel = new float[2];

   public static void snapshot(Frame var0, IsoCell var1, int var2) {
      float var3 = IsoCamera.getScreenWidth(var2);
      float var4 = IsoCamera.getScreenHeight(var2);
      boolean var5 = screen.set(var0, FreeCam.active ? FreeCam.place : null, var3, var4);

      for (IsoMovingObject var7 : var1.getObjectList()) {
         if (var7 instanceof IsoGameCharacter var8) {
            character(var8, var2, var5, var3, var4);
         } else if (var7 instanceof BaseVehicle var9) {
            vehicle(var9, var2);
         }
      }

      ArrayList var10 = var1.getStaticUpdaterObjectList();

      for (int var11 = 0; var11 < var10.size(); var11++) {
         if (var10.get(var11) instanceof IsoWaveSignal var12) {
            device(var12, var2);
         }
      }
   }

   private static void character(IsoGameCharacter var0, int var1, boolean var2, float var3, float var4) {
      boolean var5 = var0 == IsoPlayer.getInstance();
      TextDrawObject var6 = var5 ? note(var0) : null;
      ChatElement var7 = var0.getChatElement();
      if (var6 != null || var7.getHasChatToDisplay()) {
         if (var5 && var2) {
            pixel[0] = var3 * 0.5F;
            pixel[1] = var4 * (0.5F - Tuning.subtitleRise);
         } else {
            float var8 = var0.getVehicle() != null ? Tuning.vehicleTextHeight : Tuning.headTextHeight;
            if (!screen.pixel(var0.getX(), var0.getY(), var0.getZ(), var8, pixel)) {
               return;
            }
         }

         int var10 = (int)pixel[0];
         int var9 = (int)pixel[1];
         if (var6 != null) {
            var9 -= var6.getHeight() + 2;
            var6.AddBatchedDraw(var10, var9, true, noteAlpha(var0));
         }

         if (!var0.isInvisible() || var0 == IsoCamera.frameState.camCharacter || seesStats()) {
            var7.renderBatched(var1, var10, var9, radioUnheard(var0));
         }
      }
   }

   private static void vehicle(BaseVehicle var0, int var1) {
      for (int var2 = 0; var2 < var0.getPartCount(); var2++) {
         VehiclePart var3 = var0.getPartByIndex(var2);
         ChatElement var4 = var3.getChatElement();
         if (var4 != null && var4.getHasChatToDisplay()) {
            if (var3.getDeviceData() != null && !var3.getDeviceData().getIsTurnedOn()) {
               var4.clear(var1);
            } else if (screen.pixel(var0.getX(), var0.getY(), var0.getZ(), Tuning.vehicleTextHeight, pixel)) {
               var4.renderBatched(var1, (int)pixel[0], (int)pixel[1]);
            }
         }
      }
   }

   private static void device(IsoWaveSignal var0, int var1) {
      ChatElement var2 = var0.getChatElement();
      if (var2.getHasChatToDisplay()) {
         DeviceData var3 = var0.getDeviceData();
         boolean var4 = IsoPlayer.getInstance().hasTrait(CharacterTrait.DEAF);
         IsoGridSquare var5 = var0.getSquare();
         if ((var3 == null || var3.getIsTurnedOn())
            && (!var4 || !(var0 instanceof IsoRadio))
            && (!var4 || !(var0 instanceof IsoTelevision) || var5 == null || var5.isSeen(var1))) {
            float var6 = WorldMesher.rise(var0) + Tuning.deviceTextHeight;
            if (screen.pixel(var0.getX() + 0.5F, var0.getY() + 0.5F, var0.getZ(), var6, pixel)) {
               var2.renderBatched(var1, (int)pixel[0], (int)pixel[1]);
            }
         } else {
            var2.clear(var1);
         }
      }
   }

   private static boolean radioUnheard(IsoGameCharacter var0) {
      Radio var1 = var0.getEquipedRadio();
      DeviceData var2 = var1 == null ? null : var1.getDeviceData();
      return var2 != null && (var0 != IsoPlayer.getInstance() && var2.getHeadphoneType() >= 0 || !var2.getIsTurnedOn());
   }

   private static boolean seesStats() {
      return GameClient.client
         && IsoCamera.getCameraCharacter() instanceof IsoPlayer var0
         && var0.getRole() != null
         && var0.getRole().hasCapability(Capability.CanSeePlayersStats);
   }

   private static TextDrawObject note(IsoGameCharacter var0) {
      if (NOTE != null && NOTE_TIME != null && !(var0.getHaloTimerCount() <= 0.0F)) {
         try {
            return (TextDrawObject)NOTE.get(var0);
         } catch (IllegalAccessException var2) {
            return null;
         }
      } else {
         return null;
      }
   }

   private static float noteAlpha(IsoGameCharacter var0) {
      try {
         return Math.min(1.0F, var0.getHaloTimerCount() / (NOTE_TIME.getFloat(var0) / 4.0F));
      } catch (IllegalAccessException var2) {
         return 1.0F;
      }
   }

   private static Field field(String var0) {
      try {
         Field var1 = IsoGameCharacter.class.getDeclaredField(var0);
         var1.setAccessible(true);
         return var1;
      } catch (ReflectiveOperationException | RuntimeException var2) {
         System.out.println("[Viewpoint] world text: the player's notes cannot be shown: " + var2);
         return null;
      }
   }

   private WorldText() {
   }
}
