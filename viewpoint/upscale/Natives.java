package viewpoint.upscale;

import java.io.File;
import org.lwjgl.system.Library;
import org.lwjgl.system.SharedLibrary;

final class Natives {
   private Natives() {
   }

   static SharedLibrary load(String var0) {
      File var1 = NativeFiles.file(var0);
      if (!var1.isFile()) {
         throw new IllegalStateException(var0 + " is missing from " + var1.getParent() + ": download its upscaler's files again in the settings (Graphics)");
      } else {
         return Library.loadNative(Natives.class, "viewpoint", var1.getAbsolutePath());
      }
   }
}
