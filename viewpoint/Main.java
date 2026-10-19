package viewpoint;

import viewpoint.game.FrameCaps;
import viewpoint.platform.BuildPin;
import viewpoint.platform.Collector;
import viewpoint.platform.GraphicsMemory;
import viewpoint.platform.HotReload;

public class Main {
   public static void main(String[] var0) {
      System.out.println("[Viewpoint] loaded");
      BuildPin.supported();
      if (FrameCaps.ON) {
         FrameCaps.startup();
      }

      GraphicsMemory.start();
      Collector.start();
      HotReload.start();
   }
}
