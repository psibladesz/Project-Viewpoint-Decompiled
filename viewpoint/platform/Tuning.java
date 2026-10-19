package viewpoint.platform;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Properties;
import zombie.ZomboidFileSystem;

public final class Tuning {
   private static final String FILE = "viewpoint-tuning.properties";
   public static float volumeSun = 1.5F;
   public static float airDensity = 0.0028F;
   public static float mistDensity = 0.02F;
   public static float dust = 20.0F;
   public static float dustSun = 30.0F;
   public static float ambientHaze = 0.85F;
   public static float lampHalo = 8.0F;
   public static float shaftLobe = 100.0F;
   public static float shaftCap = 4.0F;
   public static float shaftDecay = 0.96F;
   public static float shaftFalloff = 0.25F;
   public static float shaftStrength = 0.35F;
   public static float reflectionTolerance = 1.5F;
   public static float farHaze = 4000.0F;
   public static float farTreeGrowth = 0.25F;
   public static float farTreeVariation = 0.18F;
   public static float farTreeRelief = 0.3F;
   public static float farBareCrown = 0.35F;
   public static float farWindowGlow = 1.2F;
   public static float farLampGlow = 3.0F;
   public static float farLampSize = 0.6F;
   public static float farLampPixels = 2.5F;
   public static float reliefSquaresPerMetre = 0.82F;
   public static float reliefViewCycle = 40.0F;
   public static float softCloudStretch = 0.5F;
   public static float lampShadowFloor = 0.3F;
   public static float bloomStrength = 0.1F;
   public static float exposure = 1.0F;
   public static float taaFeedback = 0.1F;
   public static float sharpenStops = 2.0F;
   public static float upscaleSharpenStops = 0.25F;
   public static float eyeKey = -1.4F;
   public static float eyeDayWindow = 1.0F;
   public static float eyeNightWindow = 0.25F;
   public static float eyeOpening = 1.0F;
   public static float eyeClosing = 3.0F;
   public static float occlusionRadius = 1.0F;
   public static float lampHeight = 0.55F;
   public static float lampAcross = 0.75F;
   public static float lampGlass = 0.1F;
   public static float beamReach = 1.4F;
   public static float beamAim = 1.5F;
   public static float beamWidth = 20.0F;
   public static float headlampIntensity = 300.0F;
   public static float tailIntensity = 0.5F;
   public static float brakeIntensity = 1.2F;
   public static float tailReach = 1.4F;
   public static float beamShadowReach = 40.0F;
   public static float mapNear = 15.0F;
   public static float mapMiddle = 28.0F;
   public static float lampMerge = 30.0F;
   public static float lensGlow = 24.0F;
   public static float coronaStrength = 0.8F;
   public static float coronaSize = 0.35F;
   public static float flashIntensity = 13.0F;
   public static float flashHot = 0.25F;
   public static float flashScatter = 2.5F;
   public static float handRight = 0.14F;
   public static float handDown = 0.25F;
   public static float handForward = 0.05F;
   public static float flashAim = 5.0F;
   public static float emberReach = 1.5F;
   public static float emberLight = 0.3F;
   public static float emberR = 1.0F;
   public static float emberG = 0.42F;
   public static float emberB = 0.12F;
   public static float rainFall = 6.1F;
   public static float snowFall = 1.9F;
   public static float rainExposure = 0.09F;
   public static float rainLampGain = 0.6F;
   public static float rainSky = 0.6F;
   public static float rainDensity = 0.006F;
   public static float flameGlow = 3.0F;
   public static float roofEastArt = 0.7F;
   public static float vehicleGlass = 0.4F;
   public static float targetOutlineEdge = 2.0F;
   public static float deviceTextHeight = 1.2F;
   public static float vehicleTextHeight = 1.8F;
   public static float headTextHeight = 2.0F;
   public static float subtitleRise = 0.04F;
   public static float progressBarDrop = 0.06F;
   public static float progressBarScale = 2.0F;
   private static long nextPoll;
   private static long fileStamp = -1L;

   public static void poll(long var0) {
      if (Settings.dev && var0 >= nextPoll) {
         nextPoll = var0 + 1000000000L;
         File var2 = new File(ZomboidFileSystem.instance.getCacheDir(), "viewpoint-tuning.properties");
         if (!var2.isFile()) {
            write(var2);
         }

         long var3 = var2.lastModified();
         if (var3 != fileStamp) {
            fileStamp = var3;
            Properties var5 = new Properties();

            try (FileInputStream var6 = new FileInputStream(var2)) {
               var5.load(var6);
            } catch (Exception var17) {
               System.out.println("[Viewpoint] tuning: could not read " + var2 + ": " + var17);
               return;
            }

            StringBuilder var18 = new StringBuilder();

            for (Field var10 : fields()) {
               String var11 = var5.getProperty(var10.getName());
               if (var11 != null) {
                  try {
                     float var12 = Float.parseFloat(var11.trim());
                     float var13 = var10.getFloat(null);
                     if (var12 != var13) {
                        var10.setFloat(null, var12);
                        var18.append(' ').append(var10.getName()).append(' ').append(var13).append(" -> ").append(var12);
                     }
                  } catch (NumberFormatException | IllegalAccessException var15) {
                     System.out.println("[Viewpoint] tuning: " + var10.getName() + " = " + var11 + " is not a number");
                  }
               }
            }

            if (var18.length() > 0) {
               System.out.println("[Viewpoint] tuning:" + var18);
            }
         }
      }
   }

   private static void write(File var0) {
      Properties var1 = new Properties();

      for (Field var5 : fields()) {
         try {
            var1.setProperty(var5.getName(), Float.toString(var5.getFloat(null)));
         } catch (IllegalAccessException var10) {
            return;
         }
      }

      try (FileOutputStream var11 = new FileOutputStream(var0)) {
         var1.store(var11, "Viewpoint look constants (dev mode). Saved changes apply within a second; see Tuning.java for what each does.");
      } catch (Exception var9) {
         System.out.println("[Viewpoint] tuning: could not write " + var0 + ": " + var9);
      }
   }

   private static Field[] fields() {
      return Arrays.stream(Tuning.class.getDeclaredFields())
         .filter(var0 -> var0.getType() == float.class && Modifier.isStatic(var0.getModifiers()) && !Modifier.isFinal(var0.getModifiers()))
         .toArray(Field[]::new);
   }

   private Tuning() {
   }
}
