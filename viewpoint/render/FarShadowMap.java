package viewpoint.render;

import java.util.Arrays;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL33;
import viewpoint.platform.Gl;

final class FarShadowMap {
   static final int NEAR = 0;
   static final int SHELL = 1;
   static final int INSIDES = 2;
   static final int BOXES = 3;
   private static final String[] GROUPS = new String[]{"near", "shell", "insides", "boxes"};
   private final int size;
   private final int unit;
   private final int format;
   final float recentre;
   final Matrix4f matrix = new Matrix4f();
   final Matrix4f now = new Matrix4f();
   float centreSceneX;
   float centreSceneY;
   float centreSceneZ;
   int age = Integer.MAX_VALUE;
   boolean valid;
   long drawnFar;
   long drawnNear;
   long drawnRelief;
   float reach;
   float depth;
   private final Matrix4f lightView = new Matrix4f();
   private int fbo;
   private int texture;
   private int centreX;
   private int centreY;
   private float sunX;
   private float sunY;
   private float sunZ;
   private double originX;
   private double originY;
   private double originZ;
   private final int[] pieces = new int[GROUPS.length];
   private final int[] queries = new int[GROUPS.length];
   private final long[] vertices = new long[GROUPS.length];
   private final boolean[] asked = new boolean[GROUPS.length];

   FarShadowMap(int var1, float var2, float var3, float var4, int var5, int var6) {
      this.size = var1;
      this.reach = var2;
      this.depth = var3;
      this.recentre = var4;
      this.unit = var5;
      this.format = var6;
   }

   void span(float var1, float var2) {
      if (var1 != this.reach || var2 != this.depth) {
         this.reach = var1;
         this.depth = var2;
         this.valid = false;
      }
   }

   boolean moved(SceneData var1, double var2, double var4) {
      boolean var6 = Math.abs(var2 - this.centreX) > this.recentre || Math.abs(var4 - this.centreY) > this.recentre;
      return var6 || var1.sunX * this.sunX + var1.sunY * this.sunY + var1.sunZ * this.sunZ < 0.99995F;
   }

   void begin(SceneData var1, double var2, double var4) {
      if (this.fbo == 0) {
         this.makeTarget();
      }

      this.age = 0;
      this.valid = true;
      this.centreX = (int)Math.round(var2 / this.recentre) * (int)this.recentre;
      this.centreY = (int)Math.round(var4 / this.recentre) * (int)this.recentre;
      this.sunX = var1.sunX;
      this.sunY = var1.sunY;
      this.sunZ = var1.sunZ;
      this.originX = var1.originX;
      this.originY = var1.originY;
      this.originZ = var1.originZ;
      this.centreSceneX = (float)(var2 - this.centreX);
      this.centreSceneY = (float)(-var1.originY);
      this.centreSceneZ = (float)(var4 - this.centreY);
      boolean var6 = Math.abs(var1.sunY) > 0.99F;
      this.lightView
         .setLookAt(
            this.centreSceneX + var1.sunX * this.depth * 0.5F,
            this.centreSceneY + var1.sunY * this.depth * 0.5F,
            this.centreSceneZ + var1.sunZ * this.depth * 0.5F,
            this.centreSceneX,
            this.centreSceneY,
            this.centreSceneZ,
            0.0F,
            var6 ? 0.0F : 1.0F,
            var6 ? 1.0F : 0.0F
         );
      this.matrix.setOrtho(-this.reach, this.reach, -this.reach, this.reach, 0.0F, this.depth).mul(this.lightView);
      GL30.glBindFramebuffer(36160, this.fbo);
      GL11.glViewport(0, 0, this.size, this.size);
      GL11.glEnable(2929);
      GL11.glDepthFunc(513);
      GL11.glDepthMask(true);
      GL11.glClear(256);
      GL11.glEnable(32823);
      GL11.glPolygonOffset(1.5F, 2.0F);
      Arrays.fill(this.pieces, 0);
      Arrays.fill(this.vertices, 0L);
      Arrays.fill(this.asked, false);
   }

   void count(int var1) {
      if (this.queries[0] == 0) {
         GL15.glGenQueries(this.queries);
      }

      GL15.glBeginQuery(35092, this.queries[var1]);
      this.asked[var1] = true;
   }

   void counted(int var1, int var2, long var3) {
      GL15.glEndQuery(35092);
      this.pieces[var1] = this.pieces[var1] + var2;
      this.vertices[var1] = this.vertices[var1] + var3;
   }

   String report() {
      if (!this.valid) {
         return "none";
      }

      StringBuilder var1 = new StringBuilder(String.format("about %d,%d, %.0f squares each way", this.centreX, this.centreY, this.reach));

      for (int var2 = 0; var2 < GROUPS.length; var2++) {
         var1.append(
            String.format(", %s %d (%.2fM vertices, %s samples)", GROUPS[var2], this.pieces[var2], this.vertices[var2] / 1000000.0, this.samples(var2))
         );
      }

      return var1.toString();
   }

   private String samples(int var1) {
      if (!this.asked[var1]) {
         return "no";
      } else {
         return GL15.glGetQueryObjecti(this.queries[var1], 34919) == 0
            ? "pending"
            : String.format("%.2fM", GL33.glGetQueryObjecti64(this.queries[var1], 34918) / 1000000.0);
      }
   }

   void end() {
      GL30.glBindVertexArray(0);
      GL11.glDisable(32823);
   }

   void carry(SceneData var1) {
      this.now.set(this.matrix).translate((float)(var1.originX - this.originX), (float)(var1.originY - this.originY), (float)(var1.originZ - this.originZ));
   }

   Matrix4f textureMatrix(Matrix4f var1) {
      return var1.translation(0.5F, 0.5F, 0.5F).scale(0.5F).mul(this.now);
   }

   float texelSquares() {
      return this.valid ? 2.0F * this.reach / this.size : 0.0F;
   }

   float texelStep() {
      return 1.0F / this.size;
   }

   void bind(boolean var1) {
      Gl.bind(this.unit, var1 ? this.texture : 0);
   }

   private void makeTarget() {
      GL13.glActiveTexture(33984 + this.unit);
      this.texture = Gl.texture(this.format, this.size, this.size, 6402, 5126, 9729);
      GL11.glTexParameteri(3553, 34892, 34894);
      GL11.glTexParameteri(3553, 34893, 515);
      this.fbo = GL30.glGenFramebuffers();
      GL30.glBindFramebuffer(36160, this.fbo);
      GL30.glFramebufferTexture2D(36160, 36096, 3553, this.texture, 0);
      GL11.glDrawBuffer(0);
      GL11.glReadBuffer(0);
      GL13.glActiveTexture(33984);
   }
}
