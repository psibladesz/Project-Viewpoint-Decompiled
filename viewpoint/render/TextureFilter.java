package viewpoint.render;

import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL33;
import viewpoint.platform.GlProgram;
import viewpoint.platform.LiveSettings;
import viewpoint.platform.LongMap;

final class TextureFilter {
   static final int NEAREST = 0;
   static final int NEAREST_MIPMAPPED = 1;
   static final int BILINEAR = 2;
   static final int TRILINEAR = 3;
   static final int ANISOTROPIC = 4;
   static final int LANCZOS = 5;
   static final int SHARP = 6;
   private static final String[] OPTIONS = new String[]{"Nearest", "Nearest, mipmapped", "Bilinear", "Trilinear", "Anisotropic", "Lanczos", "Sharp"};
   private static final float MAX_ANISOTROPY = 16.0F;
   private static final float ALL_LEVELS = 1000.0F;
   private static final float ATLAS_LEVELS = 3.0F;
   private static final String SECTION = "World/Texture filtering";
   static final TextureFilter FLOORS = new TextureFilter("floors.filter", "Floors", "World/Texture filtering", "uFloorFilter", 6, 1000.0F, 33071);
   static final TextureFilter SPRITES = new TextureFilter("sprites.filter", "Walls and objects", "World/Texture filtering", "uSpriteFilter", 6, 3.0F, 33071);
   static final TextureFilter MODELS = new TextureFilter("models.filter", "3D models", "World/Texture filtering", "uModelFilter", 6, 1000.0F, 10497);
   private static final LongMap<Boolean> hasMips = new LongMap<>();
   private static float levelBias;
   private final LiveSettings.Choice choice;
   private final String uniform;
   private final float maxLevel;
   private final int wrap;
   private int[] mipped;
   private int[] flat;

   private TextureFilter(String var1, String var2, String var3, String var4, int var5, float var6, int var7) {
      this.choice = LiveSettings.choice(var1, var2, var3, OPTIONS, var5);
      this.uniform = var4;
      this.maxLevel = var6;
      this.wrap = var7;
   }

   int get() {
      return this.choice.get();
   }

   void apply(GlProgram var1) {
      if (var1.has(this.uniform)) {
         var1.setInt(this.uniform, this.get());
      }
   }

   static void bias(float var0) {
      if (var0 != levelBias) {
         levelBias = var0;

         for (TextureFilter var4 : new TextureFilter[]{FLOORS, SPRITES, MODELS}) {
            for (int var5 = 0; var4.mipped != null && var5 < var4.mipped.length; var5++) {
               GL33.glSamplerParameterf(var4.mipped[var5], 34049, var0);
            }
         }
      }
   }

   int sampler(int var1) {
      return this.sampler(hasMips(var1));
   }

   int sampler(boolean var1) {
      if (this.mipped == null) {
         this.mipped = new int[]{
            this.make(9728, 9728, false),
            this.make(9986, 9728, false),
            this.make(9729, 9729, false),
            this.make(9987, 9729, false),
            this.make(9987, 9729, true),
            this.make(9987, 9729, true),
            this.make(9987, 9729, true)
         };
         int var2 = this.make(9728, 9728, false);
         int var3 = this.make(9729, 9729, false);
         this.flat = new int[]{var2, var2, var3, var3, var3, var3, var3};
      }

      return (var1 ? this.mipped : this.flat)[this.get()];
   }

   private int make(int var1, int var2, boolean var3) {
      int var4 = GL33.glGenSamplers();
      GL33.glSamplerParameteri(var4, 10241, var1);
      GL33.glSamplerParameteri(var4, 10240, var2);
      GL33.glSamplerParameteri(var4, 10242, this.wrap);
      GL33.glSamplerParameteri(var4, 10243, this.wrap);
      if (this.maxLevel < 1000.0F) {
         GL33.glSamplerParameterf(var4, 33083, this.maxLevel);
      }

      GL33.glSamplerParameterf(var4, 34049, levelBias);
      if (var3 && GL.getCapabilities().GL_EXT_texture_filter_anisotropic) {
         float var5 = GL11.glGetFloat(34047);
         GL33.glSamplerParameterf(var4, 34046, Math.min(16.0F, var5));
      }

      return var4;
   }

   private static boolean hasMips(int var0) {
      Boolean var1 = hasMips.get(var0);
      if (var1 == null) {
         int var2 = Math.max(GL11.glGetTexLevelParameteri(3553, 0, 4096), GL11.glGetTexLevelParameteri(3553, 0, 4097));
         int var3 = Math.min(31 - Integer.numberOfLeadingZeros(Math.max(var2, 1)), GL11.glGetTexParameteri(3553, 33085));
         var1 = var3 > 0 && GL11.glGetTexLevelParameteri(3553, var3, 4096) > 0;
         hasMips.put(var0, var1);
      }

      return var1;
   }
}
