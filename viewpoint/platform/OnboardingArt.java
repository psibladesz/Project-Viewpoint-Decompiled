package viewpoint.platform;

import java.util.concurrent.atomic.AtomicBoolean;

final class OnboardingArt {
   private static final String[] FILES = new String[]{
      "/viewpoint/onboarding/banner.jpg", "/viewpoint/onboarding/logo.png", "/viewpoint/onboarding/look-default.jpg", "/viewpoint/onboarding/look-vivid.jpg"
   };
   private static volatile Pictures.Texels[] read;
   private static final AtomicBoolean started = new AtomicBoolean();
   private static Pictures.Picture banner;
   private static Pictures.Picture logo;
   private static Pictures.Picture lookDefault;
   private static Pictures.Picture lookVivid;

   static void preload() {
      if (started.compareAndSet(false, true)) {
         Thread var0 = new Thread(OnboardingArt::readAll, "Viewpoint setup pictures");
         var0.setDaemon(true);
         var0.start();
      }
   }

   static Pictures.Picture banner() {
      ready();
      return banner;
   }

   static Pictures.Picture logo() {
      ready();
      return logo;
   }

   static Pictures.Picture look(boolean var0) {
      ready();
      return var0 ? lookDefault : lookVivid;
   }

   private static void ready() {
      if (banner == null) {
         preload();
         Pictures.Texels[] var0 = read;
         if (var0 != null && var0.length == FILES.length) {
            banner = Pictures.upload(var0[0]);
            logo = Pictures.upload(var0[1]);
            lookDefault = Pictures.upload(var0[2]);
            lookVivid = Pictures.upload(var0[3]);
         }
      }
   }

   private static void readAll() {
      try {
         Pictures.Texels[] var0 = new Pictures.Texels[FILES.length];

         for (int var1 = 0; var1 < FILES.length; var1++) {
            var0[var1] = Pictures.decode(FILES[var1]);
         }

         read = var0;
      } catch (Exception var2) {
         read = new Pictures.Texels[0];
         System.out.println("[Viewpoint] setup's pictures not read: " + var2);
      }
   }

   private OnboardingArt() {
   }
}
