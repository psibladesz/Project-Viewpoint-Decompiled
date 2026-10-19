package viewpoint.render;

import org.lwjgl.opengl.GL33;
import viewpoint.platform.GlProgram;

final class FloorFilter {
   static final int BAKE_NEAREST = 0;
   static final int BAKE_LINEAR = 1;
   static final int BAKE_LANCZOS = 2;

   static int bakeFilter() {
      int var0 = TextureFilter.FLOORS.get();
      return var0 != 0 && var0 != 1 && var0 != 6 ? (var0 == 5 ? 2 : 1) : 0;
   }

   static void apply(GlProgram var0) {
      TextureFilter.FLOORS.apply(var0);
   }

   static void bind(int var0) {
      GL33.glBindSampler(41, var0 == 0 ? 0 : TextureFilter.FLOORS.sampler(true));
   }

   private FloorFilter() {
   }
}
