package viewpoint.platform;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;
import zombie.ZomboidFileSystem;
import zombie.core.Core;

public final class CrashReport {
   static final String DISCORD = "https://discord.gg/6hXWv7Usgk";
   static final int CONSOLE_TAIL = 524288;
   static final int SHOWN_TAIL = 131072;
   private static final String CONSOLE = "console.txt";
   private static volatile Throwable error;
   private static volatile String where;
   private static volatile long when;
   private static volatile boolean shown;

   public static synchronized void report(String var0, Throwable var1) {
      if (error == null) {
         where = var0;
         when = System.currentTimeMillis();
         error = var1;
         shown = true;
         System.out.println("[Viewpoint] crashed while " + var0 + ": the crash window shows the report to send");
      }
   }

   static boolean shown() {
      return shown;
   }

   static void close() {
      shown = false;
   }

   static String text() {
      return header() + "\n--- console.txt (its last 512 KB) ---\n" + console(524288);
   }

   static String console(int var0) {
      File var1 = new File(ZomboidFileSystem.instance.getCacheDir(), "console.txt");

      try (RandomAccessFile var2 = new RandomAccessFile(var1, "r")) {
         long var3 = var2.length();
         long var5 = Math.max(0L, var3 - var0);
         byte[] var7 = new byte[(int)(var3 - var5)];
         var2.seek(var5);
         var2.readFully(var7);
         return new String(var7, StandardCharsets.UTF_8);
      } catch (IOException var11) {
         return "(" + var1 + " could not be read: " + var11 + ")";
      }
   }

   private static String header() {
      StringWriter var0 = new StringWriter();
      PrintWriter var1 = new PrintWriter(var0);
      Runtime var2 = Runtime.getRuntime();
      Hardware var3 = Onboarding.hardware();
      var1.println("Viewpoint crash report");
      var1.println("When: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(when)));
      var1.println("Crashed while: " + where);
      var1.println(
         "Viewpoint: "
            + Build.VERSION
            + (Build.RELEASE ? ", release" : ", development build")
            + ", setup edition "
            + Build.SETUP_EDITION
            + (Settings.dev ? ", dev mode" : "")
      );
      var1.println(
         "Game: "
            + Core.getInstance().getVersion()
            + ", Java "
            + System.getProperty("java.version")
            + " ("
            + System.getProperty("java.vendor")
            + "), heap at most "
            + (var2.maxMemory() >> 20)
            + " MB"
      );
      var1.println(
         "System: "
            + System.getProperty("os.name")
            + " "
            + System.getProperty("os.version")
            + " "
            + System.getProperty("os.arch")
            + ", "
            + var2.availableProcessors()
            + " threads"
      );
      var1.println("Hardware: " + (var3 != null ? var3.describe() : "not checked this session"));
      var1.println();
      var1.println("--- the error ---");
      Throwable var4 = error;
      if (var4 != null) {
         var4.printStackTrace(var1);
      }

      file(var1, "viewpoint.properties");
      file(var1, "viewpoint-live.properties");
      var1.flush();
      return var0.toString();
   }

   private static void file(PrintWriter var0, String var1) {
      File var2 = new File(ZomboidFileSystem.instance.getCacheDir(), var1);
      var0.println();
      var0.println("--- " + var1 + " ---");

      try {
         var0.println(var2.isFile() ? Files.readString(var2.toPath(), StandardCharsets.ISO_8859_1) : "(none)");
      } catch (IOException var4) {
         var0.println("(could not be read: " + var4 + ")");
      }
   }

   private CrashReport() {
   }
}
