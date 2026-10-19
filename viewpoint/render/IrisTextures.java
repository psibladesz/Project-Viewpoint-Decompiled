package viewpoint.render;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL30;
import viewpoint.iris.PackSource;
import viewpoint.iris.ShadersProperties;
import zombie.core.textures.PNGDecoder;
import zombie.core.textures.PNGDecoder.Format;

final class IrisTextures {
   private static final int IDAT = 1229209940;
   final Map<String, IrisTextures.Texture> byStage = new HashMap<>();
   final Map<String, IrisTextures.Texture> byName = new HashMap<>();
   final List<String> notes = new ArrayList<>();
   IrisTextures.Texture noise;
   IrisTextures.Texture normals;
   IrisTextures.Texture specular;
   IrisTextures.Texture lightmap;
   IrisTextures.Texture white;
   IrisTextures.Texture clouds;
   long bytes;

   void load(PackSource var1, ShadersProperties var2, int var3) {
      GL13.glActiveTexture(34079);
      this.white = solid(255, 255, 255, 255);
      this.normals = solid(128, 128, 255, 255);
      this.specular = solid(0, 0, 0, 0);
      this.lightmap = this.lightmapTexture();
      this.clouds = this.cloudMask();
      String var4 = var2.flag("texture.noise", null);
      this.noise = var4 == null ? this.randomNoise(var3) : this.read(var1, var4, "texture.noise");

      for (Entry<String, String> var6 : var2.textures.entrySet()) {
         String var7 = ((String)var6.getKey()).replaceAll("\\.\\d+$", "");
         this.byStage.put(var7, this.read(var1, (String)var6.getValue(), "texture." + (String)var6.getKey()));
      }

      for (Entry<String, String> var9 : var2.customTextures.entrySet()) {
         this.byName.put((String)var9.getKey(), this.read(var1, (String)var9.getValue(), "customTexture." + (String)var9.getKey()));
      }

      GL13.glActiveTexture(33984);
   }

   IrisTextures.Texture custom(String var1, String var2) {
      IrisTextures.Texture var3 = this.byStage.get(var1 + "." + var2);
      return var3 != null ? var3 : this.byName.get(var2);
   }

   private IrisTextures.Texture read(PackSource var1, String var2, String var3) {
      List<String> var4 = ShadersProperties.words(var2);

      try {
         if (var4.isEmpty()) {
            throw new IOException("no path");
         }

         String var5 = (String)var4.get(0);
         if (var5.startsWith("minecraft:")) {
            return var5.endsWith("clouds.png") ? this.clouds : this.white;
         }

         byte[] var6 = var1.readBytes(var5.startsWith("/") ? var5.substring(1) : var5);
         if (var6 == null) {
            throw new IOException("no file " + var5);
         }

         IrisTextures.Texture var7 = var4.size() > 1 ? this.raw(var6, var4) : this.png(var6);
         filtering(var7, var1.readBytes((var5.startsWith("/") ? var5.substring(1) : var5) + ".mcmeta"));
         return var7;
      } catch (IOException | RuntimeException var8) {
         this.notes.add(var3 + ": " + var8.getMessage() + " (reads white)");
         return this.white;
      }
   }

   private static void filtering(IrisTextures.Texture var0, byte[] var1) {
      String var2 = var1 == null ? "" : new String(var1, StandardCharsets.UTF_8).replaceAll("\\s", "");
      int var3 = var2.contains("\"blur\":true") ? 9729 : 9728;
      int var4 = var2.contains("\"clamp\":true") ? 33071 : 10497;
      GL11.glBindTexture(var0.target(), var0.id());
      GL11.glTexParameteri(var0.target(), 10241, var3);
      GL11.glTexParameteri(var0.target(), 10240, var3);
      GL11.glTexParameteri(var0.target(), 10242, var4);
      GL11.glTexParameteri(var0.target(), 10243, var4);
      GL11.glTexParameteri(var0.target(), 32882, var4);
      GL11.glBindTexture(var0.target(), 0);
   }

   private IrisTextures.Texture png(byte[] var1) throws IOException {
      PNGDecoder var2 = new PNGDecoder(new ByteArrayInputStream(var1), false);
      int var3 = var2.getWidth();
      int var4 = var2.getHeight();
      ByteBuffer var5 = BufferUtils.createByteBuffer(var3 * var4 * 4);
      var2.decode(var5, var3 * 4, var4, Format.RGBA, 1229209940);
      var5.flip();
      int var6 = texture2D(32856, var3, var4, 6408, 5121, var5, 10497);
      this.bytes += (long)var3 * var4 * 4L;
      return new IrisTextures.Texture(3553, var6);
   }

   private IrisTextures.Texture raw(byte[] var1, List<String> var2) throws IOException {
      String var3 = ((String)var2.get(1)).toUpperCase(Locale.ROOT);
      IrisFormats.Format var4 = IrisFormats.format((String)var2.get(2));
      int var5 = var3.equals("TEXTURE_3D") ? 3 : (var3.equals("TEXTURE_1D") ? 1 : 2);
      if (var4 != null && var2.size() >= 3 + var5 + 2) {
         int var6 = Integer.parseInt((String)var2.get(3));
         int var7 = var5 > 1 ? Integer.parseInt((String)var2.get(4)) : 1;
         int var8 = var5 > 2 ? Integer.parseInt((String)var2.get(5)) : 1;
         int var9 = IrisFormats.pixelFormat((String)var2.get(3 + var5));
         int var10 = IrisFormats.pixelType((String)var2.get(4 + var5));
         ByteBuffer var11 = BufferUtils.createByteBuffer(var1.length).put(var1).flip();
         int var12 = GL11.glGenTextures();
         int var13 = var5 == 3 ? 32879 : (var5 == 1 ? 3552 : 3553);
         GL11.glBindTexture(var13, var12);
         GL11.glPixelStorei(3317, 1);
         if (var5 == 3) {
            GL12.glTexImage3D(var13, 0, var4.internal(), var6, var7, var8, 0, var9, var10, var11);
         } else if (var5 == 1) {
            GL11.glTexImage1D(var13, 0, var4.internal(), var6, 0, var9, var10, var11);
         } else {
            GL11.glTexImage2D(var13, 0, var4.internal(), var6, var7, 0, var9, var10, var11);
         }

         GL11.glPixelStorei(3317, 4);
         GL11.glTexParameteri(var13, 10241, 9729);
         GL11.glTexParameteri(var13, 10240, 9729);
         GL11.glTexParameteri(var13, 10242, 10497);
         GL11.glTexParameteri(var13, 10243, 10497);
         GL11.glTexParameteri(var13, 32882, 10497);
         GL11.glBindTexture(var13, 0);
         this.bytes += var1.length;
         return new IrisTextures.Texture(var13, var12);
      } else {
         throw new IOException("cannot read the raw texture's line " + String.join(" ", var2));
      }
   }

   private IrisTextures.Texture randomNoise(int var1) {
      Random var2 = new Random(1L);
      ByteBuffer var3 = BufferUtils.createByteBuffer(var1 * var1 * 4);

      for (int var4 = 0; var4 < var1 * var1 * 4; var4++) {
         var3.put((byte)var2.nextInt(256));
      }

      var3.flip();
      this.bytes += (long)var1 * var1 * 4L;
      return new IrisTextures.Texture(3553, texture2D(32856, var1, var1, 6408, 5121, var3, 10497));
   }

   private IrisTextures.Texture lightmapTexture() {
      ByteBuffer var1 = BufferUtils.createByteBuffer(1024);

      for (int var2 = 0; var2 < 16; var2++) {
         for (int var3 = 0; var3 < 16; var3++) {
            float var4 = var3 / 15.0F;
            float var5 = var2 / 15.0F;
            var1.put((byte)(255.0F * Math.max(var4, var5)))
                    .put((byte)(255.0F * Math.max(var4 * 0.85F, var5)))
                    .put((byte)(255.0F * Math.max(var4 * 0.7F, var5)))
                    .put((byte)-1);
         }
      }

      var1.flip();
      return new IrisTextures.Texture(3553, texture2D(32856, 16, 16, 6408, 5121, var1, 33071));
   }

   private IrisTextures.Texture cloudMask() {
      short var1 = 256;
      ByteBuffer var2 = BufferUtils.createByteBuffer(var1 * var1 * 4);
      Random var3 = new Random(7L);
      float[] var4 = new float[256];

      for (int var5 = 0; var5 < var4.length; var5++) {
         var4[var5] = var3.nextFloat();
      }

      for (int var9 = 0; var9 < var1; var9++) {
         for (int var6 = 0; var6 < var1; var6++) {
            float var7 = var4[var9 / 16 * 16 + var6 / 16];
            byte var8 = (byte)(var7 > 0.55F ? 255 : 0);
            var2.put(var8).put(var8).put(var8).put(var8);
         }
      }

      var2.flip();
      return new IrisTextures.Texture(3553, texture2D(32856, var1, var1, 6408, 5121, var2, 10497));
   }

   private static IrisTextures.Texture solid(int var0, int var1, int var2, int var3) {
      ByteBuffer var4 = BufferUtils.createByteBuffer(4).put((byte)var0).put((byte)var1).put((byte)var2).put((byte)var3);
      var4.flip();
      return new IrisTextures.Texture(3553, texture2D(32856, 1, 1, 6408, 5121, var4, 10497));
   }

   private static int texture2D(int var0, int var1, int var2, int var3, int var4, ByteBuffer var5, int var6) {
      int var7 = GL11.glGenTextures();
      GL11.glBindTexture(3553, var7);
      GL11.glTexImage2D(3553, 0, var0, var1, var2, 0, var3, var4, var5);
      GL30.glGenerateMipmap(3553);
      GL11.glTexParameteri(3553, 10241, 9987);
      GL11.glTexParameteri(3553, 10240, 9729);
      GL11.glTexParameteri(3553, 10242, var6);
      GL11.glTexParameteri(3553, 10243, var6);
      GL11.glBindTexture(3553, 0);
      return var7;
   }

   void release() {
      HashSet<Integer> var1 = new HashSet<>();

      for (IrisTextures.Texture var5 : new IrisTextures.Texture[]{this.noise, this.normals, this.specular, this.lightmap, this.white, this.clouds}) {
         if (var5 != null) {
            var1.add(var5.id());
         }
      }

      this.byStage.values().forEach(var1x -> var1.add(var1x.id()));
      this.byName.values().forEach(var1x -> var1.add(var1x.id()));
      var1.forEach(GL11::glDeleteTextures);
      this.byStage.clear();
      this.byName.clear();
      this.bytes = 0L;
   }

   record Texture(int target, int id) {
   }
}