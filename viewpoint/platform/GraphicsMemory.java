package viewpoint.platform;

import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import oshi.SystemInfoFFM;
import oshi.hardware.GraphicsCard;

public final class GraphicsMemory {
   private static final long MIB = 1048576L;
   private static final Pattern MODEL = Pattern.compile("\\b([a-z]{2,4}) ?(\\d{3,4})\\b");
   private static volatile List<GraphicsMemory.Card> cards;
   private static Thread reading;

   public static synchronized void start() {
      if (reading == null) {
         reading = new Thread(GraphicsMemory::readCards, "Viewpoint graphics memory");
         reading.setDaemon(true);
         reading.start();
      }
   }

   static boolean done() {
      return cards != null;
   }

   static int total(String var0) {
      List<GraphicsMemory.Card> var1 = cards;
      return var1 == null ? 0 : match(var0, var1);
   }

   static int match(String var0, List<GraphicsMemory.Card> var1) {
      String var2 = model(var0);
      long var3 = 0L;
      long var5 = 0L;

      for (GraphicsMemory.Card var8 : var1) {
         var3 = Math.max(var3, var8.bytes());
         if (!var2.isEmpty() && var8.name() != null && var8.name().toLowerCase(Locale.ROOT).contains(var2)) {
            var5 = Math.max(var5, var8.bytes());
         }
      }

      return (int)((var5 > 0L ? var5 : var3) / 1048576L);
   }

   static String model(String var0) {
      String var1 = var0 == null ? "" : var0.replaceAll("/.*", "").toLowerCase(Locale.ROOT);
      Matcher var2 = MODEL.matcher(var1);
      return var2.find() ? var2.group(1) + " " + var2.group(2) : "";
   }

   private static void readCards() {
      long var0 = System.nanoTime();

      try {
         List<GraphicsCard> var2 = new SystemInfoFFM().getHardware().getGraphicsCards();
         List<GraphicsMemory.Card> var3 = var2.stream().map(var0x -> new GraphicsMemory.Card(var0x.getName(), var0x.getVRam())).toList();
         StringBuilder var4 = new StringBuilder();
         var3.forEach(var1 -> var4.append(", ").append(var1.name()).append(' ').append(var1.bytes() / 1048576L).append(" MiB"));
         cards = var3;
         System.out
                 .println(
                         String.format(
                                 Locale.ROOT,
                                 "[Viewpoint] setup: graphics cards as the system tells them: %s (%.0f ms)",
                                 var4.length() == 0 ? "none" : var4.substring(2),
                                 (System.nanoTime() - var0) * 1.0E-6
                         )
                 );
      } catch (RuntimeException | LinkageError var5) {
         cards = List.of();
         System.out.println("[Viewpoint] setup: the graphics cards' memory could not be read: " + var5);
      }
   }

   private GraphicsMemory() {
   }

   record Card(String name, long bytes) {
   }
}