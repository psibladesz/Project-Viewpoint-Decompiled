package viewpoint.light;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Stack;
import viewpoint.platform.Build;
import viewpoint.platform.Profile;
import zombie.core.properties.PropertyContainer;
import zombie.iso.IsoCell;
import zombie.iso.IsoLightSource;
import zombie.iso.IsoRoomLight;
import zombie.iso.LightingJNI;
import zombie.iso.SpriteDetails.IsoFlagType;
import zombie.iso.objects.IsoLightSwitch;

public final class LightSources {
   private static final SourceRegister register = new SourceRegister();
   private static final HashSet<Integer> fixturesNamed = new HashSet<>();
   private static final int FIXTURES_NAMED = 256;
   private static int polled = -1;
   private static int readings;
   private static volatile int lamps;
   private static volatile int lampsOn;
   private static volatile int rooms;
   private static volatile int roomsOn;
   private static volatile int mapLamps;
   private static volatile int mapLampsOn;
   private static volatile int mapRooms;
   private static volatile int mapRoomsOn;
   private static volatile int changes;
   private static volatile int marked;
   private static volatile int rememberedChunks;
   private static volatile int rememberedLamps;
   private static volatile long captureNanos;
   private static int changesReported;
   private static int markedReported;
   private static long captureReported;

   public static void update(IsoCell var0, int var1) {
      int var2 = LightingJNI.getUpdateCounter(var1);
      if (var2 != -1 && var2 != polled) {
         register.begin(polled == -1 ? null : LightJobs::mark);
         polled = var2;
         readings++;
         Stack var3 = var0.getLamppostPositions();

         for (int var4 = 0; var4 < var3.size(); var4++) {
            IsoLightSource var5 = (IsoLightSource)var3.get(var4);
            if (var5.id != 0 && var5.life == -1) {
               register.lamp(var5.id, var5.x, var5.y, var5.z, var5.radius, engine(var5.r), engine(var5.g), engine(var5.b), building(var5), var5.active);
               LampFixture var6 = fixture(var5);
               register.fixture(var5.id, var6);
               name(var5, var6);
            }
         }

         ArrayList var9 = var0.roomLights;

         for (int var10 = 0; var10 < var9.size(); var10++) {
            IsoRoomLight var11 = (IsoRoomLight)var9.get(var10);
            if (var11.id != 0 && var11.room != null && var11.room.def != null) {
               long var7 = var11.room.building != null && var11.room.building.def != null ? var11.room.building.def.getID() : -1L;
               register.room(var11.id, var11.x, var11.y, var11.z, var11.width, var11.height, var11.room.def.getID(), var7, var11.active);
            }
         }

         register.finish();
         LightMemory.capture(var0);
         publish();
      }
   }

   public static void forget() {
      register.clear();
      fixturesNamed.clear();
      polled = -1;
   }

   static SourceRegister register() {
      return register;
   }

   static int readings() {
      return readings;
   }

   static float engine(float var0) {
      return Math.max(0.0F, Math.min(1.0F, var0 * 2.0F));
   }

   static LampFixture fixture(IsoLightSource var0) {
      if (var0.switches.isEmpty()) {
         return LampFixture.MIDDLE;
      }

      IsoLightSwitch var1 = (IsoLightSwitch)var0.switches.get(0);
      PropertyContainer var2 = var1.getProperties();
      return var2 == null
         ? LampFixture.of(var1.streetLight, false, false, false, false)
         : LampFixture.of(
            var1.streetLight,
            var2.has(IsoFlagType.attachedN),
            var2.has(IsoFlagType.attachedS),
            var2.has(IsoFlagType.attachedW),
            var2.has(IsoFlagType.attachedE)
         );
   }

   private static void name(IsoLightSource var0, LampFixture var1) {
      if (!Build.RELEASE && fixturesNamed.size() < 256 && fixturesNamed.add(var0.id)) {
         String var2 = !var0.switches.isEmpty() && ((IsoLightSwitch)var0.switches.get(0)).getSprite() != null
            ? ((IsoLightSwitch)var0.switches.get(0)).getSprite().getName()
            : "none";
         System.out.println("[Viewpoint] lamp " + var0.id + " at " + var0.x + "," + var0.y + "," + var0.z + " (" + var2 + "): " + var1);
      }
   }

   static long building(IsoLightSource var0) {
      return var0.localToBuilding != null && var0.localToBuilding.def != null ? var0.localToBuilding.def.getID() : -1L;
   }

   static void publish() {
      lamps = register.lamps();
      lampsOn = register.lampsOn();
      rooms = register.rooms();
      roomsOn = register.roomsOn();
      mapLamps = register.mapLamps();
      mapLampsOn = register.mapLampsOn();
      mapRooms = register.mapRooms();
      mapRoomsOn = register.mapRoomsOn();
      changes = changes + register.takeChanges();
      marked = marked + register.takeMarked();
      rememberedChunks = LightMemory.chunks();
      rememberedLamps = LightMemory.lampsHeld();
      captureNanos = captureNanos + LightMemory.takeCaptureNanos();
   }

   private static String report() {
      int var0 = changes;
      int var1 = marked;
      long var2 = captureNanos;
      String var4 = "lamps "
         + lamps
         + " ("
         + lampsOn
         + " on), room lights "
         + rooms
         + " ("
         + roomsOn
         + " on), the map's lamps "
         + mapLamps
         + " ("
         + mapLampsOn
         + " on) and room lights "
         + mapRooms
         + " ("
         + mapRoomsOn
         + " on), changes "
         + (var0 - changesReported)
         + ", levels marked "
         + (var1 - markedReported)
         + String.format(", remembered chunks %d (%d lamps, captured in %.1f ms)", rememberedChunks, rememberedLamps, (var2 - captureReported) / 1000000.0);
      changesReported = var0;
      markedReported = var1;
      captureReported = var2;
      return var4;
   }

   private LightSources() {
   }

   static {
      Profile.lightSourcesReport = LightSources::report;
   }

   interface Marks {
      void mark(int var1, int var2, int var3);
   }
}
