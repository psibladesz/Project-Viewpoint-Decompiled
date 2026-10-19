package viewpoint.platform;

import jdk.jfr.Category;
import jdk.jfr.Event;
import jdk.jfr.Label;
import jdk.jfr.Name;
import jdk.jfr.StackTrace;

final class CaptureEvents {
   private CaptureEvents() {
   }

   @Name("Viewpoint.GpuFrame")
   @Label("Viewpoint GPU frame")
   @Category("Viewpoint")
   @StackTrace(false)
   static final class GpuFrame extends Event {
      @Label("Ours (ms)")
      float oursMs;
      @Label("Floor bakes")
      float floors;
      @Label("Shadow")
      float shadow;
      @Label("G-buffer")
      float gbuffer;
      @Label("Sky and light")
      float light;
      @Label("Far world")
      float far;
      @Label("Outlines")
      float outlines;
      @Label("Glass")
      float glass;
      @Label("Bounce light")
      float bounce;
      @Label("Weather")
      float weather;
      @Label("Air")
      float air;
      @Label("Bloom")
      float bloom;
      @Label("Post and TAA")
      float post;
   }

   @Name("Viewpoint.Memory")
   @Label("Viewpoint memory")
   @Category("Viewpoint")
   @StackTrace(false)
   static final class Memory extends Event {
      @Label("Video memory free (MiB)")
      int videoFreeMiB;
      @Label("Video memory (MiB)")
      int videoTotalMiB;
      @Label("Video memory evictions, since the driver loaded")
      int videoEvictions;
      @Label("Process committed (MiB)")
      long committedMiB;
      @Label("Heap used (MiB)")
      long heapUsedMiB;
      @Label("Heap committed (MiB)")
      long heapCommittedMiB;
   }

   @Name("Viewpoint.Pool")
   @Label("Viewpoint pool")
   @Category("Viewpoint")
   @StackTrace(false)
   static final class PoolUse extends Event {
      @Label("Pool")
      String pool;
      @Label("Used (MiB)")
      float usedMiB;
      @Label("Cap (MiB)")
      float capMiB;
   }

   @Name("Viewpoint.UiTime")
   @Label("Viewpoint UI element time")
   @Category("Viewpoint")
   @StackTrace(false)
   static final class UiTime extends Event {
      @Label("Element")
      String element;
      @Label("Phase")
      String phase;
      @Label("Off the screen (hidden, or inside a hidden element)")
      boolean hidden;
      @Label("Calls")
      int calls;
      @Label("Own time (ms)")
      float ms;
   }
}
