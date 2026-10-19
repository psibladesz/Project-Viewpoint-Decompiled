package viewpoint.far;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.CRC32C;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import viewpoint.render.FarRelief;

final class ReliefFile {
   static final String RESOURCE = "/viewpoint/relief-fitted.bin";
   static final int VERSION = 18;
   private static final int PNG_SIZE = 16;
   private static final int MAGIC = 1448104518;

   private ReliefFile() {
   }

   static void write(ReliefFile.Fitted var0, OutputStream var1) throws IOException {
      try (DataOutputStream var2 = new DataOutputStream(new GZIPOutputStream(var1))) {
         var2.writeInt(1448104518);
         var2.writeInt(18);
         ReliefRules var3 = var0.rules();
         var2.writeInt(var3.smoothingSquares());
         var2.writeInt(var3.townSmoothingSquares());

         for (float var7 : new float[]{
                 var3.height(),
                 var3.crossSlope(),
                 var3.grade(),
                 var3.gradeChange(),
                 var3.bank(),
                 var3.padMargin(),
                 var3.driveway(),
                 var3.freeboard(),
                 var3.creekFall(),
                 var3.townSlope()
         }) {
            var2.writeFloat(var7);
         }

         var2.writeInt(var3.lotSquares());
         var2.writeLong(var0.digest());
         var2.writeInt(var0.counts().water());
         var2.writeInt(var0.counts().buildings());
         var2.writeInt(var0.counts().pads());
         var2.writeInt(var0.counts().streets());
         FarRelief.Field var14 = var0.grid();
         var2.writeInt(var14.width());
         var2.writeInt(var14.height());
         writePlanar(var2, var14.ground(), var14.width());

         for (short var8 : var14.water()) {
            var2.writeShort(var8);
         }

         var2.write(var14.walls() != null ? var14.walls() : new byte[var14.width() * var14.height()]);
         int var16 = FarRelief.Field.flowCells(var14.width()) * FarRelief.Field.flowCells(var14.height()) * 4;
         var2.write(var14.flow() != null ? var14.flow() : new byte[var16]);
         long[] var18 = var14.wet() != null ? var14.wet() : new long[(var14.width() * var14.height() + 63) / 64];

         for (long var10 : var18) {
            var2.writeLong(var10);
         }
      }
   }

   private static void writePlanar(DataOutputStream var0, short[] var1, int var2) throws IOException {
      byte[] var3 = new byte[var1.length];
      byte[] var4 = new byte[var1.length];

      for (int var5 = 0; var5 < var1.length; var5++) {
         short var6 = (short)(var1[var5] - planar(var1, var5, var2));
         int var7 = var6 << 1 ^ var6 >> 31;
         var3[var5] = (byte)(var7 >> 8);
         var4[var5] = (byte)var7;
      }

      var0.write(var3);
      var0.write(var4);
   }

   private static short[] readPlanar(DataInputStream var0, int var1, int var2) throws IOException {
      byte[] var3 = new byte[var1];
      byte[] var4 = new byte[var1];
      var0.readFully(var3);
      var0.readFully(var4);
      short[] var5 = new short[var1];

      for (int var6 = 0; var6 < var1; var6++) {
         int var7 = (var3[var6] & 255) << 8 | var4[var6] & 255;
         var5[var6] = (short)(planar(var5, var6, var2) + (var7 >>> 1 ^ -(var7 & 1)));
      }

      return var5;
   }

   private static int planar(short[] var0, int var1, int var2) {
      int var3 = var1 % var2;
      int var4 = var3 > 0 ? var0[var1 - 1] & 65535 : 0;
      int var5 = var1 >= var2 ? var0[var1 - var2] & 65535 : 0;
      if (var1 >= var2 && var3 != 0) {
         return var4 + var5 - (var0[var1 - var2 - 1] & 65535);
      } else {
         return var1 < var2 ? var4 : var5;
      }
   }

   static ReliefFile.Fitted read(InputStream var0) throws IOException {
      try (DataInputStream var1 = new DataInputStream(new GZIPInputStream(var0))) {
         if (var1.readInt() != 1448104518) {
            throw new IOException("not a fitted terrain relief");
         }

         if (var1.readInt() != 18) {
            return null;
         }

         ReliefRules var2 = new ReliefRules(
                 var1.readInt(),
                 var1.readInt(),
                 var1.readFloat(),
                 var1.readFloat(),
                 var1.readFloat(),
                 var1.readFloat(),
                 var1.readFloat(),
                 var1.readFloat(),
                 var1.readFloat(),
                 var1.readFloat(),
                 var1.readFloat(),
                 var1.readFloat(),
                 var1.readInt()
         );
         long var3 = var1.readLong();
         ReliefData.Counts var5 = new ReliefData.Counts(var1.readInt(), var1.readInt(), var1.readInt(), var1.readInt());
         int var6 = var1.readInt();
         int var7 = var1.readInt();
         short[] var8 = readPlanar(var1, var6 * var7, var6);
         short[] var9 = new short[FarRelief.Field.waterNodes(var6) * FarRelief.Field.waterNodes(var7)];

         for (int var10 = 0; var10 < var9.length; var10++) {
            var9[var10] = var1.readShort();
         }

         byte[] var17 = new byte[var6 * var7];
         var1.readFully(var17);
         byte[] var11 = new byte[FarRelief.Field.flowCells(var6) * FarRelief.Field.flowCells(var7) * 4];
         var1.readFully(var11);
         long[] var12 = new long[(var6 * var7 + 63) / 64];

         for (int var13 = 0; var13 < var12.length; var13++) {
            var12[var13] = var1.readLong();
         }

         return new ReliefFile.Fitted(new FarRelief.Field(var6, var7, var8, var9, var17, var11, var12), var2, var3, var5);
      }
   }

   static ReliefFile.Fitted read(File var0) throws IOException {
      if (!var0.isFile()) {
         return null;
      }

      try (InputStream var1 = Files.newInputStream(var0.toPath())) {
         return read(var1);
      }
   }

   static void write(ReliefFile.Fitted var0, File var1) throws IOException {
      File var2 = var1.getAbsoluteFile().getParentFile();
      Files.createDirectories(var2.toPath());
      File var3 = new File(var2, var1.getName() + ".part");

      try (OutputStream var4 = Files.newOutputStream(var3.toPath())) {
         write(var0, var4);
      }

      Files.move(var3.toPath(), var1.toPath(), StandardCopyOption.REPLACE_EXISTING);
   }

   static ReliefFile.Fitted shipped() throws IOException {
      try (InputStream var0 = ReliefFile.class.getResourceAsStream("/viewpoint/relief-fitted.bin")) {
         return var0 == null ? null : read(var0);
      }
   }

   static long digest(String var0) throws IOException {
      File var1 = new File(var0);
      if (!var1.isDirectory()) {
         throw new IOException("no map folder at " + var0);
      } else {
         return digest(var0, LotBuildings.headers(var1));
      }
   }

   static long digest(String var0, List<File> var1) throws IOException {
      CRC32C var2 = new CRC32C();

      byte[] var3;
      try (InputStream var4 = ReliefFile.class.getResourceAsStream("/viewpoint/relief.png")) {
         if (var4 == null) {
            throw new FileNotFoundException("/viewpoint/relief.png is not in the mod");
         }

         var3 = var4.readAllBytes();
         var2.update(var3);
      }

      ByteBuffer var14 = ByteBuffer.wrap(var3, 16, 8);
      int var5 = (var14.getInt() - 1) * 8;
      int var6 = (var14.getInt() - 1) * 8;
      File var7 = new File(var0);
      ArrayList<File> var8 = new ArrayList<File>(LotBuildings.within(var1, var5, var6));
      var8.sort(Comparator.comparing(File::getName));

      for (File var10 : var8) {
         var2.update(var10.getName().getBytes(StandardCharsets.UTF_8));
         var2.update(Files.readAllBytes(var10.toPath()));
      }

      File var15 = new File(var7, "streets.xml");
      if (var15.isFile()) {
         var2.update(Files.readAllBytes(var15.toPath()));
      }

      for (File var11 : FlowPoints.files(var7, var8)) {
         var2.update(Files.readAllBytes(var11.toPath()));
      }

      image(new File(var7, "pyramid.zip"), var2);
      return var2.getValue();
   }

   private static void image(File var0, CRC32C var1) throws IOException {
      if (var0.isFile()) {
         try (ZipFile var2 = new ZipFile(var0)) {
            ArrayList<ZipEntry> var3 = new ArrayList<>();
            Enumeration<? extends ZipEntry> var4 = var2.entries();
            String var5 = "0/";

            while (var4.hasMoreElements()) {
               ZipEntry var6 = var4.nextElement();
               if (var6.getName().startsWith(var5) || var6.getName().equals("pyramid.txt")) {
                  var3.add(var6);
               }
            }

            var3.sort((var0x, var1x) -> var0x.getName().compareTo(var1x.getName()));

            for (ZipEntry var7 : var3) {
               var1.update(var7.getName().getBytes(StandardCharsets.UTF_8));
               var1.update(ByteBuffer.allocate(8).putLong(var7.getCrc()).flip());
            }
         }
      }
   }

   record Fitted(FarRelief.Field grid, ReliefRules rules, long digest, ReliefData.Counts counts) {
   }
}