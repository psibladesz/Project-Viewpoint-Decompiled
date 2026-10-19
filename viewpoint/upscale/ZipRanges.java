package viewpoint.upscale;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

final class ZipRanges {
   private static final int END = 101010256;
   private static final int CENTRAL = 33639248;
   private static final int LOCAL = 67324752;
   private static final int END_SIZE = 22;
   private static final int CENTRAL_SIZE = 46;
   private static final int LOCAL_SIZE = 30;
   private static final int MOST_COMMENT = 65535;
   private static final int STORED = 0;
   private static final int DEFLATED = 8;
   private static final long ZIP64 = 4294967295L;
   private static final int BUFFER = 65536;
   private final ZipRanges.Source source;

   ZipRanges(ZipRanges.Source var1) {
      this.source = var1;
   }

   ZipRanges.Entry find(String var1) throws IOException, InterruptedException {
      long var2 = this.source.size();
      int var4 = (int)Math.min(var2, 65557L);
      ByteBuffer var5 = this.read(var2 - var4, var4);
      int var6 = var4 - 22;

      while (var6 >= 0 && var5.getInt(var6) != 101010256) {
         var6--;
      }

      if (var6 < 0) {
         throw new IOException("no zip end record: not a zip archive");
      } else {
         long var7 = Integer.toUnsignedLong(var5.getInt(var6 + 12));
         long var9 = Integer.toUnsignedLong(var5.getInt(var6 + 16));
         if (var7 != 4294967295L && var9 != 4294967295L) {
            return entry(this.read(var9, (int)var7), var1);
         } else {
            throw new IOException("a zip64 archive, which is not read");
         }
      }
   }

   InputStream open(ZipRanges.Entry var1) throws IOException, InterruptedException {
      ByteBuffer var2 = this.read(var1.header(), 30);
      if (var2.getInt(0) != 67324752) {
         throw new IOException("no local header for " + var1.name());
      }

      long var3 = var1.header() + 30L + Short.toUnsignedInt(var2.getShort(26)) + Short.toUnsignedInt(var2.getShort(28));
      InputStream var5 = this.source.range(var3, var1.compressed());

      return switch (var1.method()) {
         case 0 -> var5;
         case 8 -> inflating(var5);
         default -> {
            var5.close();
            throw new IOException(var1.name() + " is stored by method " + var1.method() + ", which is not read");
         }
      };
   }

   private static ZipRanges.Entry entry(ByteBuffer var0, String var1) throws IOException {
      int var2 = 0;

      while (var2 + 46 <= var0.limit()) {
         if (var0.getInt(var2) != 33639248) {
            throw new IOException("the zip's central directory is broken at " + var2);
         }

         int var3 = Short.toUnsignedInt(var0.getShort(var2 + 28));
         int var4 = Short.toUnsignedInt(var0.getShort(var2 + 30));
         int var5 = Short.toUnsignedInt(var0.getShort(var2 + 32));
         byte[] var6 = new byte[var3];
         var0.get(var2 + 46, var6);
         if (new String(var6, StandardCharsets.UTF_8).equals(var1)) {
            long var7 = Integer.toUnsignedLong(var0.getInt(var2 + 20));
            long var9 = Integer.toUnsignedLong(var0.getInt(var2 + 24));
            long var11 = Integer.toUnsignedLong(var0.getInt(var2 + 42));
            if (var7 != 4294967295L && var9 != 4294967295L && var11 != 4294967295L) {
               return new ZipRanges.Entry(var1, Short.toUnsignedInt(var0.getShort(var2 + 10)), var7, var9, var11);
            }

            throw new IOException(var1 + " is a zip64 entry, which is not read");
         }

         var2 += 46 + var3 + var4 + var5;
      }

      throw new IOException(var1 + " is not in the archive");
   }

   private ByteBuffer read(long var1, int var3) throws IOException, InterruptedException {
      try (InputStream var4 = this.source.range(var1, var3)) {
         byte[] var5 = var4.readNBytes(var3);
         if (var5.length != var3) {
            throw new IOException("the archive ended " + (var3 - var5.length) + " bytes early at " + var1);
         } else {
            return ByteBuffer.wrap(var5).order(ByteOrder.LITTLE_ENDIAN);
         }
      }
   }

   private static InputStream inflating(InputStream var0) {
      final Inflater var1 = new Inflater(true);
      SequenceInputStream var2 = new SequenceInputStream(var0, new ByteArrayInputStream(new byte[1]));
      return new InflaterInputStream(var2, var1, 65536) {
         @Override
         public void close() throws IOException {
            try {
               super.close();
            } finally {
               var1.end();
            }
         }
      };
   }

   record Entry(String name, int method, long compressed, long size, long header) {
   }

   interface Source {
      long size() throws IOException, InterruptedException;

      InputStream range(long var1, long var3) throws IOException, InterruptedException;
   }
}
