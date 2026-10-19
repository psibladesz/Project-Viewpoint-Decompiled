package viewpoint.render;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL42;
import org.lwjgl.opengl.GL43;
import org.lwjgl.opengl.GL44;
import viewpoint.iris.ShadersProperties;

final class IrisImages {
   final Map<String, IrisImages.Image> images = new LinkedHashMap<>();
   final Map<Integer, Integer> buffers = new LinkedHashMap<>();
   final List<String> notes = new ArrayList<>();
   long bytes;

   void load(ShadersProperties var1, int var2, int var3) {
      GL13.glActiveTexture(34079);

      for (Entry var5 : var1.images.entrySet()) {
         try {
            this.image((String)var5.getKey(), ShadersProperties.words((String)var5.getValue()), var2, var3);
         } catch (RuntimeException var10) {
            this.notes.add("image." + (String)var5.getKey() + ": " + var10.getMessage());
         }
      }

      for (Entry var12 : var1.bufferObjects.entrySet()) {
         List var6 = ShadersProperties.words((String)var12.getValue());
         long var7 = var6.isEmpty() ? 0L : Long.parseLong((String)var6.get(0));
         if (var6.size() >= 4 && ((String)var6.get(1)).equalsIgnoreCase("true")) {
            var7 = (long)((float)var7 * Float.parseFloat((String)var6.get(2)) * var2 * Float.parseFloat((String)var6.get(3)) * var3);
         }

         int var9 = GL15.glGenBuffers();
         GL15.glBindBuffer(37074, var9);
         GL15.glBufferData(37074, var7, 35050);
         GL43.glClearBufferData(37074, 33330, 36244, 5121, (ByteBuffer)null);
         GL15.glBindBuffer(37074, 0);
         this.buffers.put((Integer)var12.getKey(), var9);
         this.bytes += var7;
      }

      GL13.glActiveTexture(33984);
   }

   private void image(String var1, List<String> var2, int var3, int var4) {
      IrisFormats.Format var5 = IrisFormats.format((String)var2.get(2));
      if (var5 != null && var2.size() >= 7) {
         boolean var6 = ((String)var2.get(5)).equalsIgnoreCase("true");
         int var7 = var2.size() - 6;
         int[] var8 = new int[3];

         for (int var9 = 0; var9 < 3; var9++) {
            float var10 = var9 < var7 ? Float.parseFloat((String)var2.get(6 + var9)) : 1.0F;
            var8[var9] = var6 && var9 < 2 ? Math.max(1, Math.round(var10 * (var9 == 0 ? var3 : var4))) : (int)var10;
         }

         int var15 = var7 >= 3 ? 32879 : (var7 == 2 ? 3553 : 3552);
         int var16 = GL11.glGenTextures();
         GL11.glBindTexture(var15, var16);
         int var11 = IrisFormats.pixelFormat((String)var2.get(1));
         int var12 = IrisFormats.pixelType((String)var2.get(3));
         if (var15 == 32879) {
            GL42.glTexStorage3D(var15, 1, var5.internal(), var8[0], var8[1], var8[2]);
         } else if (var15 == 3553) {
            GL42.glTexStorage2D(var15, 1, var5.internal(), var8[0], var8[1]);
         } else {
            GL42.glTexStorage1D(var15, 1, var5.internal(), var8[0]);
         }

         int var13 = var5.integer() ? 9728 : 9729;
         GL11.glTexParameteri(var15, 10241, var13);
         GL11.glTexParameteri(var15, 10240, var13);
         GL11.glTexParameteri(var15, 10242, 33071);
         GL11.glTexParameteri(var15, 10243, 33071);
         GL11.glTexParameteri(var15, 32882, 33071);
         GL11.glBindTexture(var15, 0);
         IrisImages.Image var14 = new IrisImages.Image(
            var1, (String)var2.get(0), var16, var15, var5, var8, ((String)var2.get(4)).equalsIgnoreCase("true"), var11, var12
         );
         this.images.put(var1, var14);
         GL44.glClearTexImage(var16, 0, var11, var12, (ByteBuffer)null);
         this.bytes = this.bytes + (long)var8[0] * var8[1] * var8[2] * var5.bytes();
      } else {
         throw new IllegalArgumentException("cannot read " + String.join(" ", var2));
      }
   }

   void frame() {
      for (IrisImages.Image var2 : this.images.values()) {
         if (var2.clear()) {
            GL44.glClearTexImage(var2.texture(), 0, var2.pixels(), var2.type(), (ByteBuffer)null);
         }
      }

      for (Entry var4 : this.buffers.entrySet()) {
         GL30.glBindBufferBase(37074, (Integer)var4.getKey(), (Integer)var4.getValue());
      }
   }

   void bind(IrisProgram var1) {
      for (Entry var3 : var1.images.entrySet()) {
         IrisImages.Image var4 = this.images.get(var3.getKey());
         if (var4 != null) {
            GL42.glBindImageTexture((Integer)var3.getValue(), var4.texture(), 0, var4.target() == 32879, 0, 35002, var4.format().internal());
         }
      }
   }

   int sampler(String var1, int var2) {
      for (IrisImages.Image var4 : this.images.values()) {
         if (var4.sampler().equals(var1) && var4.target() == var2) {
            return var4.texture();
         }
      }

      return 0;
   }

   void release() {
      this.images.values().forEach(var0 -> GL11.glDeleteTextures(var0.texture()));
      this.buffers.values().forEach(GL15::glDeleteBuffers);
      this.images.clear();
      this.buffers.clear();
      this.bytes = 0L;
   }

   record Image(String name, String sampler, int texture, int target, IrisFormats.Format format, int[] size, boolean clear, int pixels, int type) {
   }
}
