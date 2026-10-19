package viewpoint.far;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class FlowPoints {
   static final String OBJECTS = "objects.lua";
   static final FlowPoints NONE = new FlowPoints(new float[0]);
   private static final int X = 0;
   private static final int Y = 1;
   private static final int WAY_X = 2;
   private static final int WAY_Y = 3;
   private static final int SPEED = 4;
   private static final int FIELDS = 5;
   private static final Pattern TYPE = Pattern.compile("\\btype\\s*=\\s*\"WaterFlow\"");
   private final float[] points;

   private FlowPoints(float[] var1) {
      this.points = var1;
   }

   static FlowPoints read(File var0, List<File> var1) throws IOException {
      ArrayList<float[]> var2 = new ArrayList<>();

      for (File var4 : files(var0, var1)) {
         parse(Files.readString(var4.toPath(), StandardCharsets.UTF_8), var2);
      }

      return of(var2);
   }

   static List<File> files(File var0, List<File> var1) {
      LinkedHashSet<File> var2 = new LinkedHashSet<>();
      var2.add(var0.getAbsoluteFile());

      for (File var4 : var1) {
         var2.add(var4.getAbsoluteFile().getParentFile());
      }

      ArrayList<File> var7 = new ArrayList<>();

      for (File var5 : var2) {
         File var6 = new File(var5, "objects.lua");
         if (var6.isFile()) {
            var7.add(var6);
         }
      }

      return var7;
   }

   static FlowPoints of(List<float[]> var0) {
      float[] var1 = new float[var0.size() * 5];

      for (int var2 = 0; var2 < var0.size(); var2++) {
         float[] var3 = var0.get(var2);
         double var4 = Math.toRadians(var3[2]);
         var1[var2 * 5 + 0] = var3[0];
         var1[var2 * 5 + 1] = var3[1];
         var1[var2 * 5 + 2] = (float)(-Math.sin(var4));
         var1[var2 * 5 + 3] = (float)Math.cos(var4);
         var1[var2 * 5 + 4] = Math.max(0.0F, var3[3]);
      }

      return new FlowPoints(var1);
   }

   static void parse(String var0, List<float[]> var1) {
      int var2 = 0;
      int var3 = -1;

      for (int var4 = 0; var4 < var0.length(); var4++) {
         char var5 = var0.charAt(var4);
         if (var5 == '"') {
            var4 = var0.indexOf(34, var4 + 1);
            if (var4 < 0) {
               return;
            }
         } else if (var5 == '{') {
            var2++;
            var3 = var2 == 2 ? var4 : var3;
         } else if (var5 == '}') {
            if (var2 == 2 && var3 >= 0) {
               entry(var0.substring(var3, var4 + 1), var1);
               var3 = -1;
            }

            var2--;
         }
      }
   }

   private static void entry(String var0, List<float[]> var1) {
      if (TYPE.matcher(var0).find()) {
         float var2 = number(var0, "x");
         float var3 = number(var0, "y");
         float var4 = number(var0, "WaterDirection");
         float var5 = number(var0, "WaterSpeed");
         if (!Float.isNaN(var2) && !Float.isNaN(var3) && !Float.isNaN(var4) && !Float.isNaN(var5)) {
            var1.add(new float[]{var2, var3, var4, var5});
         }
      }
   }

   private static float number(String var0, String var1) {
      Matcher var2 = Pattern.compile("\\b" + var1 + "\\s*=\\s*(-?[0-9]*\\.?[0-9]+)").matcher(var0);
      return var2.find() ? Float.parseFloat(var2.group(1)) : Float.NaN;
   }

   int size() {
      return this.points.length / 5;
   }

   float x(int var1) {
      return this.points[var1 * 5 + 0];
   }

   float y(int var1) {
      return this.points[var1 * 5 + 1];
   }

   float wayX(int var1) {
      return this.points[var1 * 5 + 2];
   }

   float wayY(int var1) {
      return this.points[var1 * 5 + 3];
   }

   float speed(int var1) {
      return this.points[var1 * 5 + 4];
   }
}