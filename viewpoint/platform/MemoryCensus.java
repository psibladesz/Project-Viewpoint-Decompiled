package viewpoint.platform;

import java.lang.management.ManagementFactory;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.management.JMException;
import javax.management.ObjectName;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.Configuration;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.MemoryUtil.MemoryAllocationReport.Aggregate;
import org.lwjgl.system.jemalloc.JEmalloc;
import zombie.core.textures.Texture;
import zombie.core.textures.TextureID;
import zombie.core.utils.DirectBufferAllocator;

public final class MemoryCensus {
   private static final long MIB = 1048576L;
   private static final long EVERY_NANOS = 60000000000L;
   private static final int TOP = 8;
   private static final Pattern TOTAL = Pattern.compile("Total: reserved=(\\d+)MB, committed=(\\d+)MB");
   private static final Pattern CATEGORY = Pattern.compile("-\\s+(.+?) \\(reserved=(\\d+)MB, committed=(\\d+)MB\\)");
   private static final String[] JEMALLOC = new String[]{"allocated", "active", "metadata", "resident", "mapped", "retained"};
   private static long lastAt;
   private static boolean failed;

   private MemoryCensus() {
   }

   public static void renderFrame(long var0) {
      if (!Build.RELEASE && !failed && var0 - lastAt >= 60000000000L) {
         lastAt = var0;

         try {
            ProcessMemory.sample(var0);
            System.out.println(line(ProcessMemory.committed() / 1048576L));
         } catch (RuntimeException | LinkageError var3) {
            failed = true;
            System.out.println("[Viewpoint] memory census stopped: " + var3);
         }
      }
   }

   static String line(long var0) {
      StringBuilder var2 = new StringBuilder("[Viewpoint] memory census MiB: committed ").append(var0);
      long var3 = jvm(var2);
      long var5 = jemalloc(var2);
      var2.append(String.format(" | the rest (driver, the game's native libraries) %d", var0 - var3 - var5));
      lwjgl(var2);
      var2.append(
              String.format(
                      " | game textures made %d, uploaded %d MiB in all, image buffers %d MiB",
                      Texture.totalTextureID,
                      (long)TextureID.totalMemUsed / 1048576L,
                      DirectBufferAllocator.getBytesAllocated() / 1048576L
              )
      );
      return var2.toString();
   }

   private static long jvm(StringBuilder var0) {
      String var1;
      try {
         var1 = (String)ManagementFactory.getPlatformMBeanServer()
                 .invoke(
                         new ObjectName("com.sun.management:type=DiagnosticCommand"),
                         "vmNativeMemory",
                         new Object[]{new String[]{"summary", "scale=MB"}},
                         new String[]{String[].class.getName()}
                 );
      } catch (JMException var9) {
         var0.append(" | JVM unknown (").append(var9.getMessage()).append(')');
         return 0L;
      }

      Matcher var2 = TOTAL.matcher(var1);
      if (!var2.find()) {
         var0.append(" | JVM not tracked (-XX:NativeMemoryTracking=summary)");
         return 0L;
      }

      long var3 = Long.parseLong(var2.group(2));
      var0.append(" | JVM ").append(var3).append(" (");
      Matcher var5 = CATEGORY.matcher(var1);
      ArrayList<String> var6 = new ArrayList<>();

      while (var5.find()) {
         long var7 = Long.parseLong(var5.group(3));
         if (var7 >= 16L) {
            var6.add(var5.group(1).trim() + " " + var7);
         }
      }

      var0.append(String.join(", ", var6)).append(')');
      return var3;
   }

   private static long jemalloc(StringBuilder var0) {
      String var1 = MemoryUtil.getAllocator(false).getClass().getName();
      if (!var1.contains("JEmalloc")) {
         var0.append(" | MemoryUtil's allocator ").append(var1);
         return 0L;
      }

      long var2 = 0L;
      var0.append(" | jemalloc");
      MemoryStack var4 = MemoryStack.stackPush();

      label65: {
         long var6;
         try {
            ByteBuffer var5 = var4.malloc(1);
            if (JEmalloc.je_mallctl("config.stats", var5, var4.mallocPointer(1).put(0, 1L), null) == 0 && var5.get(0) != 0) {
               JEmalloc.je_mallctl("epoch", null, null, var4.malloc(8).putLong(0, 1L));
               String[] var17 = JEMALLOC;
               int var7 = var17.length;
               int var8 = 0;

               while (true) {
                  if (var8 >= var7) {
                     break label65;
                  }

                  String var9 = var17[var8];
                  ByteBuffer var10 = var4.malloc(8);
                  PointerBuffer var11 = var4.mallocPointer(1).put(0, 8L);
                  int var12 = JEmalloc.je_mallctl("stats." + var9, var10, var11, null);
                  long var13 = var12 == 0 ? var10.getLong(0) : -1048576L;
                  var0.append(' ').append(var9).append(' ').append(var13 / 1048576L);
                  var2 = var9.equals("mapped") ? var13 : var2;
                  var8++;
               }
            }

            var0.append(" (the game's build keeps no statistics: -SystemAllocator to compare without it)");
            var6 = 0L;
         } catch (Throwable var16) {
            if (var4 != null) {
               try {
                  var4.close();
               } catch (Throwable var15) {
                  var16.addSuppressed(var15);
               }
            }

            throw var16;
         }

         if (var4 != null) {
            var4.close();
         }

         return var6;
      }

      if (var4 != null) {
         var4.close();
      }

      return Math.max(0L, var2 / 1048576L);
   }

   private static void lwjgl(StringBuilder var0) {
      if ((Boolean)Configuration.DEBUG_MEMORY_ALLOCATOR.get(false)) {
         HashMap<String, Long> var1 = new HashMap<>();
         MemoryUtil.memReport(
                 (var1x, var3, var5, var7, var8) -> var1.merge(var8.length == 0 ? "?" : var8[0].getClassName() + "." + var8[0].getMethodName(), var3, Long::sum),
                 Aggregate.GROUP_BY_METHOD,
                 false
         );
         long var2 = var1.values().stream().mapToLong(Long::longValue).sum();
         var0.append(" | MemoryUtil live ").append(var2 / 1048576L).append(':');
         var1.entrySet()
                 .stream()
                 .sorted((var0x, var1x) -> Long.compare((Long)var1x.getValue(), (Long)var0x.getValue()))
                 .limit(8L)
                 .forEach(var1x -> var0.append(' ').append((String)var1x.getKey()).append(' ').append((Long)var1x.getValue() / 1048576L).append(','));
         var0.setLength(var0.length() - 1);
      }
   }
}