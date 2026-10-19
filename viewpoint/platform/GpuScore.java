package viewpoint.platform;

import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class GpuScore {
   private static final double LAPTOP = 0.8;
   private static final Pattern NVIDIA = Pattern.compile("\\b(RTX|GTX|GT|MX)\\s*(\\d{3,4})\\s*(TI|SUPER)?");
   private static final Pattern RADEON = Pattern.compile("\\bRX\\s*(\\d{3,4})\\s*(XTX|XT|GRE)?\\s*(M)?\\b");
   private static final Pattern ARC = Pattern.compile("\\bARC\\S*\\s*([AB]\\d{3})\\b");
   private static final Pattern APU = Pattern.compile("\\b(\\d{3})M\\b");
   private static final Map<String, Double> GTX = Map.ofEntries(
      Map.entry("GTX 750", 0.12),
      Map.entry("GTX 750 TI", 0.14),
      Map.entry("GTX 760", 0.17),
      Map.entry("GTX 770", 0.22),
      Map.entry("GTX 780", 0.28),
      Map.entry("GTX 780 TI", 0.33),
      Map.entry("GTX 950", 0.2),
      Map.entry("GTX 960", 0.24),
      Map.entry("GTX 970", 0.36),
      Map.entry("GTX 980", 0.43),
      Map.entry("GTX 980 TI", 0.55),
      Map.entry("GTX 1050", 0.2),
      Map.entry("GTX 1050 TI", 0.25),
      Map.entry("GTX 1060", 0.4),
      Map.entry("GTX 1070", 0.55),
      Map.entry("GTX 1070 TI", 0.62),
      Map.entry("GTX 1080", 0.68),
      Map.entry("GTX 1080 TI", 0.88),
      Map.entry("GTX 1630", 0.18),
      Map.entry("GTX 1650", 0.42),
      Map.entry("GTX 1650 SUPER", 0.55),
      Map.entry("GTX 1660", 0.6),
      Map.entry("GTX 1660 SUPER", 0.68),
      Map.entry("GTX 1660 TI", 0.68),
      Map.entry("GT 1030", 0.12),
      Map.entry("RTX 2050", 0.35),
      Map.entry("RTX 3050", 0.7),
      Map.entry("RTX 3060 TI", 1.25)
   );
   private static final Map<Integer, Double> RTX_GENERATION = Map.of(20, 0.85, 30, 1.0, 40, 1.2, 50, 1.4);
   private static final Map<Integer, Double> RTX_TIER = Map.of(50, 0.7, 60, 1.0, 70, 1.45, 80, 1.9, 90, 2.4);
   private static final Map<String, Double> RX = Map.ofEntries(
      Map.entry("460", 0.2),
      Map.entry("470", 0.36),
      Map.entry("480", 0.4),
      Map.entry("550", 0.13),
      Map.entry("560", 0.2),
      Map.entry("570", 0.38),
      Map.entry("580", 0.42),
      Map.entry("590", 0.47),
      Map.entry("5500 XT", 0.45),
      Map.entry("5600 XT", 0.7),
      Map.entry("5700", 0.8),
      Map.entry("5700 XT", 0.9),
      Map.entry("6400", 0.3),
      Map.entry("6500 XT", 0.35),
      Map.entry("6600", 0.9),
      Map.entry("6600 XT", 1.05),
      Map.entry("6650 XT", 1.1),
      Map.entry("6700", 1.2),
      Map.entry("6700 XT", 1.3),
      Map.entry("6750 XT", 1.4),
      Map.entry("6800", 1.65),
      Map.entry("6800 XT", 1.9),
      Map.entry("6900 XT", 2.05),
      Map.entry("6950 XT", 2.2),
      Map.entry("7600", 1.1),
      Map.entry("7600 XT", 1.15),
      Map.entry("7700 XT", 1.6),
      Map.entry("7800 XT", 1.9),
      Map.entry("7900 GRE", 2.1),
      Map.entry("7900 XT", 2.5),
      Map.entry("7900 XTX", 2.9),
      Map.entry("9060 XT", 1.6),
      Map.entry("9070", 2.5),
      Map.entry("9070 XT", 2.8)
   );
   private static final Map<String, Double> INTEL_ARC = Map.of("A310", 0.25, "A380", 0.35, "A580", 0.7, "A750", 0.85, "A770", 0.95, "B570", 0.95, "B580", 1.1);
   private static final Map<String, Double> RADEON_APU = Map.of("660", 0.2, "680", 0.25, "740", 0.18, "760", 0.25, "780", 0.3, "880", 0.35, "890", 0.4);

   static double of(String var0) {
      String var1 = normal(var0);
      double var2 = nvidia(var1);
      if (Double.isNaN(var2)) {
         var2 = amd(var1);
      }

      if (Double.isNaN(var2)) {
         var2 = intel(var1);
      }

      boolean var4 = var1.contains("LAPTOP") || var1.contains("MAX-Q") || var1.contains("MOBILE");
      return var4 ? var2 * 0.8 : var2;
   }

   static boolean integrated(String var0) {
      String var1 = normal(var0);
      boolean var2 = var1.contains("INTEL") && !ARC.matcher(var1).find();
      boolean var3 = var1.contains("RADEON")
         && !var1.contains("RX")
         && (var1.contains("RADEON GRAPHICS") || var1.contains("VEGA ") || APU.matcher(var1).find());
      return var2 || var3;
   }

   private static String normal(String var0) {
      return var0 == null ? "" : var0.toUpperCase(Locale.ROOT).replace("(R)", "").replace("(TM)", "").replaceAll("/.*", "").replaceAll("\\s+", " ").trim();
   }

   private static double nvidia(String var0) {
      Matcher var1 = NVIDIA.matcher(var0);
      if (!var1.find()) {
         return Double.NaN;
      } else {
         String var2 = var1.group(1);
         String var3 = var1.group(3) == null ? "" : " " + var1.group(3);
         int var4 = Integer.parseInt(var1.group(2));
         Double var5 = GTX.get(var2 + " " + var4 + var3);
         if (var5 != null) {
            return var5;
         } else if (var2.equals("RTX") && var4 >= 2000) {
            Double var6 = RTX_GENERATION.get(var4 / 100);
            Double var7 = RTX_TIER.get(var4 % 100);
            return var6 != null && var7 != null ? var6 * var7 * (var3.equals(" TI") ? 1.15 : (var3.equals(" SUPER") ? 1.1 : 1.0)) : Double.NaN;
         } else {
            return var2.equals("MX") ? 0.18 : (var2.equals("GT") ? 0.06 : 0.12);
         }
      }
   }

   private static double amd(String var0) {
      Matcher var1 = RADEON.matcher(var0);
      if (var1.find()) {
         String var4 = var1.group(1) + (var1.group(2) == null ? "" : " " + var1.group(2));
         Double var3 = RX.getOrDefault(var4, RX.get(var1.group(1)));
         return var3 == null ? Double.NaN : (var1.group(3) != null ? var3 * 0.8 : var3);
      } else if (!var0.contains("RADEON")) {
         return Double.NaN;
      } else {
         Matcher var2 = APU.matcher(var0);
         if (var2.find() && RADEON_APU.containsKey(var2.group(1))) {
            return RADEON_APU.get(var2.group(1));
         } else {
            return var0.contains("VEGA 56") ? 0.6 : (var0.contains("VEGA 64") ? 0.66 : (integrated(var0) ? 0.15 : Double.NaN));
         }
      }
   }

   private static double intel(String var0) {
      if (!var0.contains("INTEL")) {
         return Double.NaN;
      } else {
         Matcher var1 = ARC.matcher(var0);
         if (var1.find()) {
            return INTEL_ARC.getOrDefault(var1.group(1), Double.NaN);
         } else if (var0.contains("ARC")) {
            return 0.3;
         } else {
            return var0.contains("IRIS XE") ? 0.14 : (var0.contains("IRIS") ? 0.09 : 0.06);
         }
      }
   }

   private GpuScore() {
   }
}
