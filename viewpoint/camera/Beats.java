package viewpoint.camera;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

final class Beats {
   static final int DISTANCE = 0;
   static final int HEIGHT = 1;
   static final int SHOULDER = 2;
   static final int PITCH = 3;
   static final int YAW = 4;
   static final int ROLL = 5;
   static final int FOV = 6;
   static final int VALUES = 7;
   private static final String PLACE = "/viewpoint/camera/";
   static final List<Beats.Beat> ALL = load();

   static Beats.Beat forState(String var0) {
      for (Beats.Beat var2 : ALL) {
         if (var2.playsFor(var0)) {
            return var2;
         }
      }

      return null;
   }

   static Beats.Beat parse(String var0, List<String> var1) {
      String[] var2 = new String[0];
      float var3 = 0.2F;
      float var4 = 0.3F;
      boolean var5 = false;
      ArrayList var6 = new ArrayList();
      int var7 = 0;

      for (String var9 : var1) {
         var7++;
         String var10 = var9.strip();
         if (!var10.isEmpty() && !var10.startsWith("#")) {
            String[] var11 = var10.split("\\s+");

            try {
               switch (var11[0]) {
                  case "states":
                     var2 = Arrays.copyOfRange(var11, 1, var11.length);
                     break;
                  case "in":
                     var3 = Float.parseFloat(var11[1]);
                     break;
                  case "out":
                     var4 = Float.parseFloat(var11[1]);
                     break;
                  case "hold":
                     var5 = true;
                     break;
                  default:
                     row(var11, var6);
               }
            } catch (RuntimeException var14) {
               throw new IllegalArgumentException(var0 + " line " + var7 + ": " + var10 + " (" + var14.getMessage() + ")");
            }
         }
      }

      if (var2.length != 0 && !var6.isEmpty()) {
         float[] var15 = new float[var6.size()];

         for (int var16 = 0; var16 < var15.length; var16++) {
            var15[var16] = (Float)var6.get(var16);
         }

         return new Beats.Beat(var0, var2, var3, var4, var5, var15);
      } else {
         throw new IllegalArgumentException(var0 + ": a beat needs its states and at least one row");
      }
   }

   private static void row(String[] var0, List<Float> var1) {
      if (var0.length != 8) {
         throw new IllegalArgumentException("a row is a time and 7 values");
      }

      float var2 = Float.parseFloat(var0[0]);
      if (!var1.isEmpty() && var2 < (Float)var1.get(var1.size() - 7 - 1)) {
         throw new IllegalArgumentException("rows go forward in time");
      }

      for (String var6 : var0) {
         var1.add(Float.parseFloat(var6));
      }
   }

   private static List<Beats.Beat> load() {
      ArrayList var0 = new ArrayList();

      for (String var2 : lines("beats.txt")) {
         String var3 = var2.strip();
         if (!var3.isEmpty() && !var3.startsWith("#")) {
            var0.add(parse(var3, lines(var3)));
         }
      }

      return List.copyOf(var0);
   }

   private static List<String> lines(String var0) {
      try (InputStream var1 = Beats.class.getResourceAsStream("/viewpoint/camera/" + var0)) {
         if (var1 == null) {
            throw new IllegalStateException("the camera's beat /viewpoint/camera/" + var0 + " is not in the JAR");
         }

         ArrayList var2 = new ArrayList();
         BufferedReader var3 = new BufferedReader(new InputStreamReader(var1, StandardCharsets.UTF_8));

         for (String var4 = var3.readLine(); var4 != null; var4 = var3.readLine()) {
            var2.add(var4);
         }

         return var2;
      } catch (IOException var7) {
         throw new IllegalStateException("the camera's beat " + var0 + " does not read", var7);
      }
   }

   private Beats() {
   }

   record Beat(String name, String[] states, float in, float out, boolean hold, float[] rows) {
      int count() {
         return this.rows.length / 8;
      }

      float end() {
         return this.rows[(this.count() - 1) * 8];
      }

      boolean playsFor(String var1) {
         for (String var5 : this.states) {
            if (var5.equals(var1)) {
               return true;
            }
         }

         return false;
      }

      void at(float var1, float[] var2) {
         byte var3 = 8;
         int var4 = this.count() - 1;
         int var5 = 0;

         while (var5 < var4 && this.rows[(var5 + 1) * var3] <= var1) {
            var5++;
         }

         float var6 = this.rows[var5 * var3];
         float var7 = var5 < var4 ? this.rows[(var5 + 1) * var3] : var6;
         float var8 = var7 > var6 ? Math.max(0.0F, Math.min(1.0F, (var1 - var6) / (var7 - var6))) : 0.0F;
         var8 = var8 * var8 * (3.0F - 2.0F * var8);

         for (int var9 = 0; var9 < 7; var9++) {
            float var10 = this.rows[var5 * var3 + 1 + var9];
            float var11 = var5 < var4 ? this.rows[(var5 + 1) * var3 + 1 + var9] : var10;
            var2[var9] = var10 + (var11 - var10) * var8;
         }
      }
   }

   static final class Player {
      final float[] values = new float[7];
      private final float[] row = new float[7];
      private Beats.Beat playing;
      private String state = "";
      private float time;
      private float weight;

      void stop() {
         this.playing = null;
         this.weight = 0.0F;
         Arrays.fill(this.values, 0.0F);
      }

      void play(Beats.Beat var1) {
         this.playing = var1;
         this.time = 0.0F;
      }

      void update(String var1, float var2) {
         if (!var1.equals(this.state)) {
            this.state = var1;
            Beats.Beat var3 = Beats.forState(var1);
            if (var3 != null) {
               this.play(var3);
            }
         }

         if (this.playing == null) {
            Arrays.fill(this.values, 0.0F);
         } else {
            this.time += var2;
            boolean var6 = this.playing.playsFor(this.state) && (this.playing.hold() || this.time <= this.playing.end());
            float var4 = var6 ? this.playing.in() : this.playing.out();
            this.weight = this.weight + ((var6 ? 1.0F : 0.0F) - this.weight) * (1.0F - (float)Math.exp(-3.0F * var2 / var4));
            this.playing.at(this.time, this.row);

            for (int var5 = 0; var5 < 7; var5++) {
               this.values[var5] = this.row[var5] * this.weight;
            }

            if (!var6 && this.weight < 0.001F) {
               this.stop();
            }
         }
      }
   }
}
