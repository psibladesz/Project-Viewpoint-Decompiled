package viewpoint.render;

import imgui.ImGui;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL42;
import org.lwjgl.opengl.GL43;

final class FloorPreview {
   private static final float SHOWN = 320.0F;
   private final FloorBakes bakes;
   private int texture;
   private int size;

   FloorPreview(FloorBakes var1) {
      this.bakes = var1;
   }

   void draw() {
      ImGui.textUnformatted(this.bakes.figures());
      FloorPage var1 = this.bakes.nearestPage();
      int var2 = this.bakes.nearestSlice();
      if (var1 != null && var2 >= 0) {
         boolean var3 = (var2 & 1048576) != 0;
         int var4 = this.bakes.tierSize(var2);
         if (this.texture == 0 || this.size != var4) {
            if (this.texture != 0) {
               GL11.glDeleteTextures(this.texture);
            }

            this.texture = GL11.glGenTextures();
            this.size = var4;
            int var5 = GL11.glGetInteger(32873);
            GL11.glBindTexture(3553, this.texture);
            GL42.glTexStorage2D(3553, 1, 32856, this.size, this.size);
            GL11.glTexParameteri(3553, 10241, 9729);
            GL11.glBindTexture(3553, var5);
         }

         GL43.glCopyImageSubData(this.bakes.texture(var2), 35866, 0, 0, 0, FloorBakes.layer(var2), this.texture, 3553, 0, 0, 0, 0, this.size, this.size, 1);
         ImGui.textUnformatted(
            String.format(
               "Chunk %d, %d, storey %d: %s, %d texels a side (north at the top)", var1.wx, var1.wy, var1.level, var3 ? "distant tier" : "near tier", this.size
            )
         );
         ImGui.image(this.texture, 320.0F, 320.0F, 0.0F, 0.0F, 1.0F, 1.0F);
      } else {
         ImGui.textUnformatted("No floor baked in view yet.");
      }
   }
}
