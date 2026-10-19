package viewpoint.light;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import viewpoint.platform.SettingsWindow;
import viewpoint.visibility.Edges;
import zombie.GameTime;
import zombie.ZomboidFileSystem;
import zombie.characters.IsoGameCharacter.TorchInfo;
import zombie.core.Color;
import zombie.core.opengl.RenderSettings;
import zombie.core.opengl.RenderSettings.PlayerRenderSettings;
import zombie.core.textures.ColorInfo;
import zombie.iso.IsoCell;
import zombie.iso.IsoChunk;
import zombie.iso.IsoChunkMap;
import zombie.iso.IsoGridSquare;
import zombie.iso.IsoLightSource;
import zombie.iso.IsoObject;
import zombie.iso.IsoRoomLight;
import zombie.iso.IsoWorld;
import zombie.iso.LightingJNI;
import zombie.iso.IsoGridSquare.ILighting;
import zombie.iso.IsoGridSquare.ResultLight;
import zombie.iso.SpriteDetails.IsoObjectType;
import zombie.iso.objects.IsoCurtain;
import zombie.iso.objects.IsoDoor;
import zombie.iso.objects.IsoLightSwitch;
import zombie.iso.objects.IsoThumpable;
import zombie.iso.objects.IsoWindow;
import zombie.iso.weather.ClimateManager;

public final class LightRecorder {
   private static final String FOLDER = "viewpoint-recordings";
   private static final int RING = 5;
   private static final SettingsWindow.Button RECORD = SettingsWindow.button(
      "Debug/Profiling",
      "Record light",
      "The engine's light around you, with everything it was given, saved to Zomboid/viewpoint-recordings to fit the light model. Look around first: the engine lights only what you see."
   );

   public static void poll(IsoCell var0, int var1, float var2, float var3, float var4) {
      if (RECORD.take()) {
         record(var0, var1, var2, var3, var4);
      }
   }

   private static void record(IsoCell var0, int var1, float var2, float var3, float var4) {
      try {
         List var5 = squares(var0, var1, var2, var3);
         List var6 = lamps(var0);
         List var7 = rooms(var0);
         List var8 = torches();
         Path var9 = new File(ZomboidFileSystem.instance.getCacheDir(), "viewpoint-recordings").toPath();
         Files.createDirectories(var9);
         Path var10 = var9.resolve("light-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")) + ".lightrec");
         LightRecording.write(var10, new LightRecording.Recording(var2, var3, var4, world(var1), var6, var7, var8, var5));
         int var11 = inView(var5);
         System.out
            .println(
               "[Viewpoint] light recording: "
                  + var5.size()
                  + " squares ("
                  + var11
                  + " in view), "
                  + var6.size()
                  + " lamps, "
                  + var7.size()
                  + " room lights, "
                  + var8.size()
                  + " torches, at hour "
                  + String.format("%.2f", GameTime.getInstance().getTimeOfDay())
                  + " into "
                  + var10
            );
         RECORD.status("saved " + var10.getFileName() + ": " + var5.size() + " squares, " + var11 + " in view");
      } catch (IOException | RuntimeException var12) {
         System.out.println("[Viewpoint] light recording failed: " + var12);
         RECORD.status("failed: " + var12.getMessage());
      }
   }

   private static List<LightRecording.Square> squares(IsoCell var0, int var1, float var2, float var3) {
      IsoChunkMap var4 = var0.getChunkMap(var1);
      int var5 = Math.floorDiv((int)Math.floor(var2), 8);
      int var6 = Math.floorDiv((int)Math.floor(var3), 8);
      ArrayList var7 = new ArrayList();

      for (int var8 = var6 - 5; var8 <= var6 + 5; var8++) {
         for (int var9 = var5 - 5; var9 <= var5 + 5; var9++) {
            IsoChunk var10 = var4.getChunkForGridSquare(var9 * 8, var8 * 8);
            if (var10 != null && var10.loaded && var10.wx == var9 && var10.wy == var8) {
               for (int var11 = var10.minLevel; var11 <= var10.maxLevel; var11++) {
                  for (int var12 = 0; var12 < 8; var12++) {
                     for (int var13 = 0; var13 < 8; var13++) {
                        IsoGridSquare var14 = var10.getGridSquare(var13, var12, var11);
                        if (var14 != null) {
                           var7.add(square(var14, var1));
                        }
                     }
                  }
               }
            }
         }
      }

      return var7;
   }

   private static LightRecording.Square square(IsoGridSquare var0, int var1) {
      ILighting var2 = var0.lighting[var1];
      ColorInfo var3 = var2 == null ? null : var2.lightInfo();
      int[] var4 = new int[8];
      LightRecording.Reached[] var5 = new LightRecording.Reached[0];
      if (var2 != null) {
         for (int var6 = 0; var6 < var4.length; var6++) {
            var4[var6] = var2.lightverts(var6);
         }

         var5 = reached(var2);
      }

      int var9 = var0.getX();
      int var7 = var0.getY();
      int var8 = var0.getZ();
      return new LightRecording.Square(
         var9,
         var7,
         var8,
         flags(var0, var2),
         LayoutCapture.passes(var0),
         var0.getBuildingDef() == null ? -1L : var0.getBuildingDef().getID(),
         var0.getRoom() == null ? -1L : var0.getRoomID(),
         var0.lightLevel,
         var3 == null ? 0 : GridTexels.pack(var3.r, var3.g, var3.b),
         var2 == null ? 0.0F : var2.darkMulti(),
         var2 == null ? 0.0F : var2.targetDarkMulti(),
         var4,
         var5,
         OwnedLight.lightAt(var9, var7, var8)
      );
   }

   private static int recorded(int var0) {
      return var0 == 4 ? 2 : var0;
   }

   private static int flags(IsoGridSquare var0, ILighting var1) {
      int var2 = (var0.isOutside() ? 1 : 0)
         | (var0.haveRoof ? 2 : 0)
         | (var0.getOpenAir() ? 4 : 0)
         | (!var0.isSolidFloor() && !var0.TreatAsSolidFloor() ? 0 : 8)
         | (var0.HasStairs() ? 16 : 0)
         | (elevated(var0) ? 32 : 0)
         | recorded(Edges.edge(var0, true)) << 20
         | recorded(Edges.edge(var0, false)) << 22
         | (doorOpen(var0.getDoor(Edges.facing(true))) ? 512 : 0)
         | (doorOpen(var0.getDoor(Edges.facing(false))) ? 1024 : 0);
      if (var1 != null) {
         var2 |= (var1.bSeen() ? 64 : 0) | (var1.bCanSee() ? 128 : 0) | (var1.bCouldSee() ? 256 : 0);
      }

      for (IsoObject var4 : var0.getSpecialObjects()) {
         if (var4 instanceof IsoWindow var5) {
            boolean var7 = var5.isNorth();
            var2 |= var7 ? 2048 : 4096;
            var2 |= !var5.IsOpen() ? 0 : (var7 ? 8192 : 16384);
         } else if (var4 instanceof IsoCurtain var6 && !var6.IsOpen()) {
            var2 |= curtain(var6.getType());
         }
      }

      return var2;
   }

   private static boolean elevated(IsoGridSquare var0) {
      return var0.has(IsoObjectType.stairsTN) || var0.has(IsoObjectType.stairsMN) || var0.has(IsoObjectType.stairsTW) || var0.has(IsoObjectType.stairsMW);
   }

   private static boolean doorOpen(IsoObject var0) {
      return var0 instanceof IsoDoor var2 && var2.IsOpen() || var0 instanceof IsoThumpable var1 && var1.isDoor() && var1.IsOpen();
   }

   private static int curtain(IsoObjectType var0) {
      return var0 == IsoObjectType.curtainW
         ? 32768
         : (var0 == IsoObjectType.curtainN ? 65536 : (var0 == IsoObjectType.curtainE ? 131072 : (var0 == IsoObjectType.curtainS ? 262144 : 0)));
   }

   private static LightRecording.Reached[] reached(ILighting var0) {
      LightRecording.Reached[] var1 = new LightRecording.Reached[var0.resultLightCount()];

      for (int var2 = 0; var2 < var1.length; var2++) {
         ResultLight var3 = var0.getResultLight(var2);
         var1[var2] = new LightRecording.Reached(var3.id, var3.x, var3.y, var3.z, var3.radius, var3.r, var3.g, var3.b, var3.flags);
      }

      return var1;
   }

   private static List<LightRecording.Lamp> lamps(IsoCell var0) {
      ArrayList var1 = new ArrayList();

      for (IsoLightSource var3 : var0.getLamppostPositions()) {
         boolean var4 = !var3.switches.isEmpty() && ((IsoLightSwitch)var3.switches.get(0)).streetLight;
         long var5 = var3.localToBuilding != null && var3.localToBuilding.def != null ? var3.localToBuilding.def.getID() : -1L;
         var1.add(
            new LightRecording.Lamp(
               var3.id,
               var3.x,
               var3.y,
               var3.z,
               var3.radius,
               var3.r,
               var3.g,
               var3.b,
               var3.active,
               var3.life,
               var3.hydroPowered,
               var5,
               var4,
               var3.switches.size()
            )
         );
      }

      return var1;
   }

   private static List<LightRecording.RoomLight> rooms(IsoCell var0) {
      ArrayList var1 = new ArrayList();

      for (IsoRoomLight var3 : var0.roomLights) {
         long var4 = var3.room != null && var3.room.def != null ? var3.room.def.getID() : -1L;
         long var6 = var3.room != null && var3.room.building != null && var3.room.building.def != null ? var3.room.building.def.getID() : -1L;
         var1.add(new LightRecording.RoomLight(var3.id, var3.x, var3.y, var3.z, var3.width, var3.height, var3.active, var4, var6));
      }

      return var1;
   }

   private static List<LightRecording.Torch> torches() {
      ArrayList<TorchInfo> var0 = new ArrayList<>();
      LightingJNI.getTorches(var0);
      ArrayList var1 = new ArrayList(var0.size());

      for (TorchInfo var3 : var0) {
         var1.add(
            new LightRecording.Torch(
               var3.id, var3.x, var3.y, var3.z, var3.r, var3.g, var3.b, var3.angleX, var3.angleY, var3.dist, var3.strength, var3.cone, var3.dot, var3.focusing
            )
         );
      }

      return var1;
   }

   private static Map<String, Float> world(int var0) {
      LinkedHashMap var1 = new LinkedHashMap();
      GameTime var2 = GameTime.getInstance();
      PlayerRenderSettings var3 = RenderSettings.getInstance().getPlayerSettings(var0);
      ClimateManager var4 = ClimateManager.getInstance();
      var1.put("player", (float)var0);
      var1.put("timeOfDay", var2.getTimeOfDay());
      var1.put("night", var2.getNight());
      var1.put("skyLightLevel", (float)var2.getSkyLightLevel());
      var1.put("ambientForPlayer", RenderSettings.getInstance().getAmbientForPlayer(var0));
      var1.put("settings.ambient", var3.getAmbient());
      var1.put("settings.night", var3.getNight());
      var1.put("settings.viewDistance", var3.getViewDistance());
      var1.put("settings.rmod", var3.getRmod());
      var1.put("settings.gmod", var3.getGmod());
      var1.put("settings.bmod", var3.getBmod());
      var1.put("climate.ambient", var4.getAmbient());
      var1.put("climate.dayLightStrength", var4.getDayLightStrength());
      var1.put("climate.nightStrength", var4.getNightStrength());
      var1.put("climate.globalLightIntensity", var4.getGlobalLightIntensity());
      colour(var1, "climate.exterior", var4.getGlobalLight().getExterior());
      colour(var1, "climate.interior", var4.getGlobalLight().getInterior());
      var1.put("hydroPower", IsoWorld.instance.isHydroPowerOn() ? 1.0F : 0.0F);
      var1.put("flashlight", Flashlight.active ? 1.0F : 0.0F);
      return var1;
   }

   private static void colour(Map<String, Float> var0, String var1, Color var2) {
      var0.put(var1 + ".r", var2.r);
      var0.put(var1 + ".g", var2.g);
      var0.put(var1 + ".b", var2.b);
      var0.put(var1 + ".a", var2.a);
   }

   private static int inView(List<LightRecording.Square> var0) {
      int var1 = 0;

      for (LightRecording.Square var3 : var0) {
         if ((var3.flags() & 128) != 0) {
            var1++;
         }
      }

      return var1;
   }

   private LightRecorder() {
   }
}
