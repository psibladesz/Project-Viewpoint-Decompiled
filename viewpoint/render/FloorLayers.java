package viewpoint.render;

import zombie.core.textures.TextureID;

public interface FloorLayers {
   int first(int var1);

   int end(int var1);

   TextureID atlas(int var1);

   float number(int var1, int var2);

   boolean has(int var1, int var2);

   default int material(int var1) {
      return 0;
   }
}
