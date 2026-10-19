package viewpoint.platform;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import zombie.core.Core;

public final class BuildPin {
   public static final String BUILD = "Build 42.21.0";
   public static final String SHA256 = "E1A69EB743EDE60B213A0FE7F8B83D4FCAB773036D256CC4543A336F3B058A33";
   private static Boolean supported;
   private static boolean refusalReported;

   public static synchronized boolean supported() {
      if (supported == null) {
         supported = check(gameJar());
      }

      return supported;
   }

   public static void reportRefused() {
      if (!refusalReported) {
         refusalReported = true;
         System.out.println("[Viewpoint] first person stays off: this is not the game build the mod supports (see the line at load)");
      }
   }

   static boolean check(File var0) {
      try {
         String var1 = sha256(var0);
         if ("E1A69EB743EDE60B213A0FE7F8B83D4FCAB773036D256CC4543A336F3B058A33".equals(var1)) {
            System.out.println("[Viewpoint] game build: Build 42.21.0, as pinned");
            return true;
         } else {
            System.out
               .println(
                  "[Viewpoint] unsupported game build: "
                     + var0
                     + " has SHA-256 "
                     + var1
                     + ", the mod supports E1A69EB743EDE60B213A0FE7F8B83D4FCAB773036D256CC4543A336F3B058A33 (Build 42.21.0); first person stays off"
               );
            return false;
         }
      } catch (IOException var2) {
         System.out.println("[Viewpoint] cannot read the game's JAR " + var0 + " to check its build (" + var2 + "); first person stays off");
         return false;
      }
   }

   static String sha256(File var0) throws IOException {
      MessageDigest var1 = sha256Digest();

      try (InputStream var2 = Files.newInputStream(var0.toPath())) {
         byte[] var3 = new byte[65536];

         for (int var4 = var2.read(var3); var4 > 0; var4 = var2.read(var3)) {
            var1.update(var3, 0, var4);
         }
      }

      return HexFormat.of().withUpperCase().formatHex(var1.digest());
   }

   static File gameJar() {
      try {
         return new File(Core.class.getProtectionDomain().getCodeSource().getLocation().toURI());
      } catch (Exception | LinkageError var1) {
         return new File("projectzomboid.jar");
      }
   }

   private static MessageDigest sha256Digest() {
      try {
         return MessageDigest.getInstance("SHA-256");
      } catch (NoSuchAlgorithmException var1) {
         throw new IllegalStateException("every Java runtime has SHA-256", var1);
      }
   }

   private BuildPin() {
   }
}
