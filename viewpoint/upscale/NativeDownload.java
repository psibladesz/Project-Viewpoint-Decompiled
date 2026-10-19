package viewpoint.upscale;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.function.LongConsumer;

final class NativeDownload {
   static final int CONNECT_MS = 20000;
   static final int STALL_MS = 30000;
   private static final int BUFFER = 65536;
   private static final int MS_A_SECOND = 1000;
   private static final int OK = 200;
   private static final int PARTIAL = 206;
   private static final String PART = ".part";
   private static final String AGENT = "Viewpoint (Project Zomboid mod)";

   private NativeDownload() {
   }

   static void fetch(NativeFiles.Library var0, Path var1, LongConsumer var2) throws IOException, InterruptedException {
      fetch(var0, var1, var2, 30000);
   }

   static void fetch(NativeFiles.Library var0, Path var1, LongConsumer var2, int var3) throws IOException, InterruptedException {
      Files.createDirectories(var1);
      Path var4 = var1.resolve(var0.name());
      Path var5 = var1.resolve(var0.name() + ".part");
      String var6 = URI.create(var0.url()).getHost();
      MessageDigest var7 = sha256();
      long var8 = 0L;

      try (
         InputStream var10 = open(var0, var3);
         OutputStream var11 = Files.newOutputStream(var5);
      ) {
         byte[] var12 = new byte[65536];

         for (int var13 = var10.read(var12); var13 >= 0 && var8 <= var0.size(); var13 = var10.read(var12)) {
            var11.write(var12, 0, var13);
            var7.update(var12, 0, var13);
            var8 += var13;
            var2.accept(var13);
         }
      } catch (SocketTimeoutException var18) {
         Files.deleteIfExists(var5);
         throw new IOException(var6 + " sent nothing for " + var3 / 1000 + " s", var18);
      } catch (IOException | InterruptedException | RuntimeException var19) {
         Files.deleteIfExists(var5);
         throw var19;
      }

      String var20 = HexFormat.of().formatHex(var7.digest());
      if (var8 == var0.size() && var20.equals(var0.sha256())) {
         Files.move(var5, var4, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
      } else {
         Files.deleteIfExists(var5);
         throw new IOException(var0.name() + " from " + var6 + " is not the version Viewpoint was made for (" + var8 + " bytes, SHA-256 " + var20 + ")");
      }
   }

   private static InputStream open(NativeFiles.Library var0, int var1) throws IOException, InterruptedException {
      if (var0.entry() == null) {
         return body(connect(var0.url(), var1, "GET", null), 200);
      }

      ZipRanges var2 = new ZipRanges(new NativeDownload.Ranges(var0.url(), var1));
      return var2.open(var2.find(var0.entry()));
   }

   private static HttpURLConnection connect(String var0, int var1, String var2, String var3) throws IOException {
      HttpURLConnection var4 = (HttpURLConnection)URI.create(var0).toURL().openConnection();
      var4.setConnectTimeout(20000);
      var4.setReadTimeout(var1);
      var4.setRequestMethod(var2);
      var4.setRequestProperty("User-Agent", "Viewpoint (Project Zomboid mod)");
      if (var3 != null) {
         var4.setRequestProperty("Range", var3);
      }

      return var4;
   }

   private static InputStream body(HttpURLConnection var0, int var1) throws IOException {
      int var2 = var0.getResponseCode();
      if (var2 != var1) {
         var0.disconnect();
         throw new IOException("HTTP " + var2 + " from " + var0.getURL().getHost() + " for " + var0.getURL().getPath());
      } else {
         return var0.getInputStream();
      }
   }

   private static MessageDigest sha256() {
      try {
         return MessageDigest.getInstance("SHA-256");
      } catch (NoSuchAlgorithmException var1) {
         throw new IllegalStateException("this Java has no SHA-256", var1);
      }
   }

   private record Ranges(String url, int stallMs) implements ZipRanges.Source {
      @Override
      public long size() throws IOException {
         HttpURLConnection var1 = NativeDownload.connect(this.url, this.stallMs, "HEAD", null);
         NativeDownload.body(var1, 200).close();
         long var2 = var1.getContentLengthLong();
         if (var2 < 0L) {
            throw new IOException(var1.getURL().getHost() + " gave no size for " + this.url);
         } else {
            return var2;
         }
      }

      @Override
      public InputStream range(long var1, long var3) throws IOException {
         String var5 = "bytes=" + var1 + "-" + (var1 + var3 - 1L);
         return NativeDownload.body(NativeDownload.connect(this.url, this.stallMs, "GET", var5), 206);
      }
   }
}
