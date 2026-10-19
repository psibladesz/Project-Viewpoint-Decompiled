package viewpoint.light;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

final class LightRecording {
   static final String SUFFIX = ".lightrec";
   private static final int MAGIC = 1348095058;
   private static final int FORMAT = 1;
   static final int OUTSIDE = 1;
   static final int ROOFED = 2;
   static final int OPEN_AIR = 4;
   static final int FLOOR = 8;
   static final int STAIRS = 16;
   static final int ELEVATED = 32;
   static final int SEEN = 64;
   static final int CAN_SEE = 128;
   static final int COULD_SEE = 256;
   static final int DOOR_OPEN_N = 512;
   static final int DOOR_OPEN_W = 1024;
   static final int WINDOW_N = 2048;
   static final int WINDOW_W = 4096;
   static final int WINDOW_OPEN_N = 8192;
   static final int WINDOW_OPEN_W = 16384;
   static final int CURTAIN_W = 32768;
   static final int CURTAIN_N = 65536;
   static final int CURTAIN_E = 131072;
   static final int CURTAIN_S = 262144;
   static final int EDGE_N_SHIFT = 20;
   static final int EDGE_W_SHIFT = 22;

   static void write(Path var0, LightRecording.Recording var1) throws IOException {
      try (DataOutputStream var2 = new DataOutputStream(new BufferedOutputStream(new GZIPOutputStream(Files.newOutputStream(var0))))) {
         write(var2, var1);
      }
   }

   static LightRecording.Recording read(Path var0) throws IOException {
      try (DataInputStream var1 = new DataInputStream(new BufferedInputStream(new GZIPInputStream(Files.newInputStream(var0))))) {
         LightRecording.Recording var2 = read(var1);
         if (var2 == null) {
            throw new IOException(var0 + " is not a light recording this build reads");
         } else {
            return var2;
         }
      }
   }

   static List<Path> recordings(Path var0) throws IOException {
      if (!Files.isDirectory(var0)) {
         return List.of();
      }

      try (Stream<Path> var1 = Files.list(var0)) {
         return var1.filter(var0x -> var0x.getFileName().toString().endsWith(".lightrec")).sorted(Comparator.comparing(Path::getFileName)).toList();
      }
   }

   static void write(DataOutputStream var0, LightRecording.Recording var1) throws IOException {
      var0.writeInt(1348095058);
      var0.writeInt(1);
      var0.writeUTF("E1A69EB743EDE60B213A0FE7F8B83D4FCAB773036D256CC4543A336F3B058A33");
      var0.writeFloat(var1.x());
      var0.writeFloat(var1.y());
      var0.writeFloat(var1.z());
      var0.writeInt(var1.world().size());

      for (Entry<String, Float> var3 : var1.world().entrySet()) {
         var0.writeUTF(var3.getKey());
         var0.writeFloat(var3.getValue());
      }

      writeLamps(var0, var1.lamps());
      writeRooms(var0, var1.rooms());
      writeTorches(var0, var1.torches());
      var0.writeInt(var1.squares().size());

      for (LightRecording.Square var5 : var1.squares()) {
         writeSquare(var0, var5);
      }
   }

   static LightRecording.Recording read(DataInputStream var0) throws IOException {
      if (var0.readInt() == 1348095058 && var0.readInt() == 1) {
         var0.readUTF();
         float var1 = var0.readFloat();
         float var2 = var0.readFloat();
         float var3 = var0.readFloat();
         int var4 = var0.readInt();
         LinkedHashMap<String, Float> var5 = new LinkedHashMap<>();

         for (int var6 = 0; var6 < var4; var6++) {
            var5.put(var0.readUTF(), var0.readFloat());
         }

         List<LightRecording.Lamp> var12 = readLamps(var0);
         List<LightRecording.RoomLight> var7 = readRooms(var0);
         List<LightRecording.Torch> var8 = readTorches(var0);
         int var9 = var0.readInt();
         ArrayList<LightRecording.Square> var10 = new ArrayList<>(var9);

         for (int var11 = 0; var11 < var9; var11++) {
            var10.add(readSquare(var0));
         }

         return new LightRecording.Recording(var1, var2, var3, var5, var12, var7, var8, var10);
      } else {
         return null;
      }
   }

   private static void writeLamps(DataOutputStream var0, List<LightRecording.Lamp> var1) throws IOException {
      var0.writeInt(var1.size());

      for (LightRecording.Lamp var3 : var1) {
         var0.writeInt(var3.id());
         var0.writeInt(var3.x());
         var0.writeInt(var3.y());
         var0.writeInt(var3.z());
         var0.writeInt(var3.radius());
         var0.writeFloat(var3.r());
         var0.writeFloat(var3.g());
         var0.writeFloat(var3.b());
         var0.writeBoolean(var3.active());
         var0.writeInt(var3.life());
         var0.writeBoolean(var3.hydroPowered());
         var0.writeLong(var3.building());
         var0.writeBoolean(var3.streetLight());
         var0.writeInt(var3.switches());
      }
   }

   private static List<LightRecording.Lamp> readLamps(DataInputStream var0) throws IOException {
      int var1 = var0.readInt();
      ArrayList<LightRecording.Lamp> var2 = new ArrayList<>(var1);

      for (int var3 = 0; var3 < var1; var3++) {
         var2.add(
                 new LightRecording.Lamp(
                         var0.readInt(),
                         var0.readInt(),
                         var0.readInt(),
                         var0.readInt(),
                         var0.readInt(),
                         var0.readFloat(),
                         var0.readFloat(),
                         var0.readFloat(),
                         var0.readBoolean(),
                         var0.readInt(),
                         var0.readBoolean(),
                         var0.readLong(),
                         var0.readBoolean(),
                         var0.readInt()
                 )
         );
      }

      return var2;
   }

   private static void writeRooms(DataOutputStream var0, List<LightRecording.RoomLight> var1) throws IOException {
      var0.writeInt(var1.size());

      for (LightRecording.RoomLight var3 : var1) {
         var0.writeInt(var3.id());
         var0.writeInt(var3.x());
         var0.writeInt(var3.y());
         var0.writeInt(var3.z());
         var0.writeInt(var3.width());
         var0.writeInt(var3.height());
         var0.writeBoolean(var3.active());
         var0.writeLong(var3.room());
         var0.writeLong(var3.building());
      }
   }

   private static List<LightRecording.RoomLight> readRooms(DataInputStream var0) throws IOException {
      int var1 = var0.readInt();
      ArrayList<LightRecording.RoomLight> var2 = new ArrayList<>(var1);

      for (int var3 = 0; var3 < var1; var3++) {
         var2.add(
                 new LightRecording.RoomLight(
                         var0.readInt(),
                         var0.readInt(),
                         var0.readInt(),
                         var0.readInt(),
                         var0.readInt(),
                         var0.readInt(),
                         var0.readBoolean(),
                         var0.readLong(),
                         var0.readLong()
                 )
         );
      }

      return var2;
   }

   private static void writeTorches(DataOutputStream var0, List<LightRecording.Torch> var1) throws IOException {
      var0.writeInt(var1.size());

      for (LightRecording.Torch var3 : var1) {
         var0.writeInt(var3.id());
         var0.writeFloat(var3.x());
         var0.writeFloat(var3.y());
         var0.writeFloat(var3.z());
         var0.writeFloat(var3.r());
         var0.writeFloat(var3.g());
         var0.writeFloat(var3.b());
         var0.writeFloat(var3.angleX());
         var0.writeFloat(var3.angleY());
         var0.writeFloat(var3.dist());
         var0.writeFloat(var3.strength());
         var0.writeBoolean(var3.cone());
         var0.writeFloat(var3.dot());
         var0.writeInt(var3.focusing());
      }
   }

   private static List<LightRecording.Torch> readTorches(DataInputStream var0) throws IOException {
      int var1 = var0.readInt();
      ArrayList<LightRecording.Torch> var2 = new ArrayList<>(var1);

      for (int var3 = 0; var3 < var1; var3++) {
         var2.add(
                 new LightRecording.Torch(
                         var0.readInt(),
                         var0.readFloat(),
                         var0.readFloat(),
                         var0.readFloat(),
                         var0.readFloat(),
                         var0.readFloat(),
                         var0.readFloat(),
                         var0.readFloat(),
                         var0.readFloat(),
                         var0.readFloat(),
                         var0.readFloat(),
                         var0.readBoolean(),
                         var0.readFloat(),
                         var0.readInt()
                 )
         );
      }

      return var2;
   }

   private static void writeSquare(DataOutputStream var0, LightRecording.Square var1) throws IOException {
      var0.writeInt(var1.x());
      var0.writeInt(var1.y());
      var0.writeInt(var1.z());
      var0.writeInt(var1.flags());
      var0.writeInt(var1.passes());
      var0.writeLong(var1.building());
      var0.writeLong(var1.room());
      var0.writeInt(var1.lightLevel());
      var0.writeInt(var1.light());
      var0.writeFloat(var1.dark());
      var0.writeFloat(var1.targetDark());

      for (int var5 : var1.corners()) {
         var0.writeInt(var5);
      }

      var0.writeByte(var1.reached().length);

      for (LightRecording.Reached var9 : var1.reached()) {
         var0.writeInt(var9.id());
         var0.writeInt(var9.x());
         var0.writeInt(var9.y());
         var0.writeInt(var9.z());
         var0.writeInt(var9.radius());
         var0.writeFloat(var9.r());
         var0.writeFloat(var9.g());
         var0.writeFloat(var9.b());
         var0.writeInt(var9.flags());
      }

      var0.writeInt(var1.ours());
   }

   private static LightRecording.Square readSquare(DataInputStream var0) throws IOException {
      int var1 = var0.readInt();
      int var2 = var0.readInt();
      int var3 = var0.readInt();
      int var4 = var0.readInt();
      int var5 = var0.readInt();
      long var6 = var0.readLong();
      long var8 = var0.readLong();
      int var10 = var0.readInt();
      int var11 = var0.readInt();
      float var12 = var0.readFloat();
      float var13 = var0.readFloat();
      int[] var14 = new int[8];

      for (int var15 = 0; var15 < var14.length; var15++) {
         var14[var15] = var0.readInt();
      }

      LightRecording.Reached[] var17 = new LightRecording.Reached[var0.readUnsignedByte()];

      for (int var16 = 0; var16 < var17.length; var16++) {
         var17[var16] = new LightRecording.Reached(
                 var0.readInt(),
                 var0.readInt(),
                 var0.readInt(),
                 var0.readInt(),
                 var0.readInt(),
                 var0.readFloat(),
                 var0.readFloat(),
                 var0.readFloat(),
                 var0.readInt()
         );
      }

      return new LightRecording.Square(var1, var2, var3, var4, var5, var6, var8, var10, var11, var12, var13, var14, var17, var0.readInt());
   }

   private LightRecording() {
   }

   record Lamp(
           int id,
           int x,
           int y,
           int z,
           int radius,
           float r,
           float g,
           float b,
           boolean active,
           int life,
           boolean hydroPowered,
           long building,
           boolean streetLight,
           int switches
   ) {
   }

   record Reached(int id, int x, int y, int z, int radius, float r, float g, float b, int flags) {
   }

   record Recording(
           float x,
           float y,
           float z,
           Map<String, Float> world,
           List<LightRecording.Lamp> lamps,
           List<LightRecording.RoomLight> rooms,
           List<LightRecording.Torch> torches,
           List<LightRecording.Square> squares
   ) {
   }

   record RoomLight(int id, int x, int y, int z, int width, int height, boolean active, long room, long building) {
   }

   record Square(
           int x,
           int y,
           int z,
           int flags,
           int passes,
           long building,
           long room,
           int lightLevel,
           int light,
           float dark,
           float targetDark,
           int[] corners,
           LightRecording.Reached[] reached,
           int ours
   ) {
   }

   record Torch(
           int id,
           float x,
           float y,
           float z,
           float r,
           float g,
           float b,
           float angleX,
           float angleY,
           float dist,
           float strength,
           boolean cone,
           float dot,
           int focusing
   ) {
   }
}