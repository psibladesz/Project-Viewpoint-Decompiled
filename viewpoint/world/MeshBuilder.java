package viewpoint.world;

import java.util.Arrays;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import zombie.tileDepth.TileGeometryFile.Box;
import zombie.tileDepth.TileGeometryFile.Cylinder;
import zombie.tileDepth.TileGeometryFile.Geometry;
import zombie.tileDepth.TileGeometryFile.Polygon;

public final class MeshBuilder {
   private static final int CYLINDER_SEGMENTS = 12;
   private static final Vector3f TO_ISO_CAMERA = new Vector3f(1.0F, 0.8164966F, 1.0F).normalize();
   private static final float UV_INSET = 1.0F;
   private static final float ON_FLOOR = 0.002F;
   private float[] data = new float[288];
   private int floats;
   private final Matrix4f m = new Matrix4f();
   private final Vector3f normal = new Vector3f();
   private boolean inset;
   private float insetX;
   private float insetY;
   private final Vector3f scratch = new Vector3f();
   private final Vector3f[] p = newVectors(8);
   private final Vector3f[] q = newVectors(8);

   TileMesh build() {
      return new TileMesh(Arrays.copyOf(this.data, this.floats));
   }

   void add(Geometry var1) {
      if (var1.isBox()) {
         this.box(var1.asBox());
      } else if (var1.isCylinder()) {
         this.cylinder(var1.asCylinder());
      } else if (var1.isPolygon()) {
         this.polygon(var1.asPolygon());
      }
   }

   void box(Box var1) {
      this.transform(var1.translate, var1.rotate);
      this.box(var1.min, var1.max);
   }

   void box(Vector3f var1, Vector3f var2) {
      for (int var3 = 0; var3 < 3; var3++) {
         for (int var4 = 0; var4 < 2; var4++) {
            this.normal.set(0.0F).setComponent(var3, var4 == 0 ? -1.0F : 1.0F);
            boolean var5 = this.face(false);
            int var6 = 1 << (var3 + 1) % 3;
            int var7 = 1 << (var3 + 2) % 3;
            int var8 = var4 == 0 ? 0 : 1 << var3;
            int[] var9 = new int[]{var8, var8 | var6, var8 | var6 | var7, var8 | var7};

            for (int var10 = 0; var10 < 4; var10++) {
               this.corner(var9[var10], var1, var2, this.p[var10]);
               this.corner(var5 ? var9[var10] ^ 1 << var3 : var9[var10], var1, var2, this.q[var10]);
            }

            if (!this.restsOnFloor(4)) {
               this.insetAround(4);
               this.quad();
            }
         }
      }
   }

   private boolean restsOnFloor(int var1) {
      if (this.normal.y > -0.99F) {
         return false;
      }

      for (int var2 = 0; var2 < var1; var2++) {
         if (Math.abs(this.p[var2].y) > 0.002F) {
            return false;
         }
      }

      return true;
   }

   void cylinder(Cylinder var1) {
      this.transform(var1.translate, var1.rotate);
      float var2 = var1.height / 2.0F;
      float var3 = var1.radius1;

      for (int var4 = 0; var4 < 12; var4++) {
         double var5 = (Math.PI * 2) * var4 / 12.0;
         double var7 = (Math.PI * 2) * (var4 + 1) / 12.0;
         float var9 = var3 * (float)Math.cos(var5);
         float var10 = var3 * (float)Math.sin(var5);
         float var11 = var3 * (float)Math.cos(var7);
         float var12 = var3 * (float)Math.sin(var7);
         this.normal.set(var9 + var11, var10 + var12, 0.0F).normalize();
         float var13 = this.face(false) ? -1.0F : 1.0F;
         this.local(this.p[0], this.q[0], var9, var10, -var2, var13, var13, 1.0F);
         this.local(this.p[1], this.q[1], var11, var12, -var2, var13, var13, 1.0F);
         this.local(this.p[2], this.q[2], var11, var12, var2, var13, var13, 1.0F);
         this.local(this.p[3], this.q[3], var9, var10, var2, var13, var13, 1.0F);
         this.inset = false;
         this.quad();

         for (int var14 = -1; var14 <= 1; var14 += 2) {
            this.normal.set(0.0F, 0.0F, var14);
            var13 = this.face(false) ? -1.0F : 1.0F;
            this.local(this.p[0], this.q[0], 0.0F, 0.0F, var14 * var2, 1.0F, 1.0F, var13);
            this.local(this.p[1], this.q[1], var9, var10, var14 * var2, 1.0F, 1.0F, var13);
            this.local(this.p[2], this.q[2], var11, var12, var14 * var2, 1.0F, 1.0F, var13);
            if (!this.restsOnFloor(3)) {
               this.insetAround(1);
               this.triangle(0, 1, 2);
            }
         }
      }
   }

   void polygon(Polygon var1) {
      this.transform(var1.translate, var1.rotate);
      int var2 = var1.points.size() / 2;
      if (var2 >= 3) {
         float[] var3 = new float[var2 * 2];

         for (int var4 = 0; var4 < var3.length; var4++) {
            var3[var4] = var1.points.getQuick(var4);
         }

         this.normal.set(0.0F, 0.0F, 1.0F);
         this.face(true);
         this.insetX = this.insetY = 0.0F;

         for (int var8 = 0; var8 < var2; var8++) {
            this.m.transformPosition(this.scratch.set(var3[var8 * 2], var3[var8 * 2 + 1], 0.0F));
            this.insetX = this.insetX + frameX(this.scratch) / var2;
            this.insetY = this.insetY + frameY(this.scratch) / var2;
         }

         this.inset = true;
         int[] var9 = EarClip.triangulate(var3);

         for (int var5 = 0; var5 < var9.length; var5 += 3) {
            for (int var6 = 0; var6 < 3; var6++) {
               int var7 = var9[var5 + var6];
               this.local(this.p[var6], this.q[var6], var3[var7 * 2], var3[var7 * 2 + 1], 0.0F, 1.0F, 1.0F, 1.0F);
            }

            this.triangle(0, 1, 2);
         }
      }
   }

   void floor() {
      this.m.identity();
      this.normal.set(0.0F, 1.0F, 0.0F);
      this.face(true);
      this.local(this.p[0], this.q[0], -0.5F, 0.0F, -0.5F, 1.0F, 1.0F, 1.0F);
      this.local(this.p[1], this.q[1], 0.5F, 0.0F, -0.5F, 1.0F, 1.0F, 1.0F);
      this.local(this.p[2], this.q[2], 0.5F, 0.0F, 0.5F, 1.0F, 1.0F, 1.0F);
      this.local(this.p[3], this.q[3], -0.5F, 0.0F, 0.5F, 1.0F, 1.0F, 1.0F);
      this.insetAround(4);
      this.quad();
   }

   void crossed(float var1, float var2, int var3) {
      this.m.identity();
      this.normal.set(0.0F, 0.5F, 0.0F);
      this.inset = false;
      float var4 = var1 * 0.70710677F;

      for (int var5 = 0; var5 < var3; var5++) {
         double var6 = (-Math.PI / 4) + var5 * Math.PI / var3;
         float var8 = (float)Math.cos(var6) * var1;
         float var9 = (float)Math.sin(var6) * var1;
         this.p[0].set(-var8, 0.0F, -var9);
         this.p[1].set(var8, 0.0F, var9);
         this.p[2].set(var8, var2, var9);
         this.p[3].set(-var8, var2, -var9);
         this.q[0].set(-var4, 0.0F, var4);
         this.q[1].set(var4, 0.0F, -var4);
         this.q[2].set(var4, var2, -var4);
         this.q[3].set(-var4, var2, var4);
         this.quad();
      }
   }

   void edgeQuad(int var1, float var2) {
      this.normal.set(0.0F).setComponent(var1, 1.0F);
      int var3 = var1 == 2 ? 0 : 2;

      for (int var4 = 0; var4 < 4; var4++) {
         this.p[var4].set(0.0F, var4 < 2 ? 0.0F : 2.4494896F, 0.0F).setComponent(var3, var4 != 0 && var4 != 3 ? 0.5F : -0.5F).setComponent(var1, -0.5F);
         this.q[var4].set(this.p[var4]).setComponent(var1, var2);
      }

      this.insetAround(4);
      this.quad();
   }

   private boolean face(boolean var1) {
      this.m.transformDirection(this.normal).normalize();
      return this.normal.dot(TO_ISO_CAMERA) <= 0.0F;
   }

   private void transform(Vector3f var1, Vector3f var2) {
      this.m.translation(var1).rotateXYZ((float)Math.toRadians(var2.x), (float)Math.toRadians(var2.y), (float)Math.toRadians(var2.z));
   }

   private void corner(int var1, Vector3f var2, Vector3f var3, Vector3f var4) {
      var4.set((var1 & 1) == 0 ? var2.x : var3.x, (var1 & 2) == 0 ? var2.y : var3.y, (var1 & 4) == 0 ? var2.z : var3.z);
      this.m.transformPosition(var4);
   }

   private void local(Vector3f var1, Vector3f var2, float var3, float var4, float var5, float var6, float var7, float var8) {
      this.m.transformPosition(var1.set(var3, var4, var5));
      this.m.transformPosition(var2.set(var3 * var6, var4 * var7, var5 * var8));
   }

   private void insetAround(int var1) {
      this.insetX = this.insetY = 0.0F;

      for (int var2 = 0; var2 < var1; var2++) {
         this.insetX = this.insetX + frameX(this.q[var2]) / var1;
         this.insetY = this.insetY + frameY(this.q[var2]) / var1;
      }

      this.inset = true;
   }

   private static float frameX(Vector3f var0) {
      return 32.0F * (var0.x - var0.z + 1.0F);
   }

   private static float frameY(Vector3f var0) {
      return 112.0F + 16.0F * (var0.x + var0.z) - 39.191833F * var0.y;
   }

   private void quad() {
      this.triangle(0, 1, 2);
      this.triangle(0, 2, 3);
   }

   private void triangle(int var1, int var2, int var3) {
      this.vertex(this.p[var1], this.q[var1]);
      this.vertex(this.p[var2], this.q[var2]);
      this.vertex(this.p[var3], this.q[var3]);
   }

   private void vertex(Vector3f var1, Vector3f var2) {
      if (this.floats + 8 > this.data.length) {
         this.data = Arrays.copyOf(this.data, this.data.length * 2);
      }

      this.data[this.floats++] = var1.x;
      this.data[this.floats++] = var1.y;
      this.data[this.floats++] = var1.z;
      float var3 = frameX(var2);
      float var4 = frameY(var2);
      if (this.inset) {
         float var5 = this.insetX - var3;
         float var6 = this.insetY - var4;
         float var7 = (float)Math.sqrt(var5 * var5 + var6 * var6);
         if (var7 > 1.0E-4F) {
            float var8 = Math.min(1.0F, var7 * 0.5F) / var7;
            var3 += var5 * var8;
            var4 += var6 * var8;
         }
      }

      this.data[this.floats++] = var3;
      this.data[this.floats++] = var4;
      this.data[this.floats++] = this.normal.x;
      this.data[this.floats++] = this.normal.y;
      this.data[this.floats++] = this.normal.z;
   }

   private static Vector3f[] newVectors(int var0) {
      Vector3f[] var1 = new Vector3f[var0];

      for (int var2 = 0; var2 < var0; var2++) {
         var1[var2] = new Vector3f();
      }

      return var1;
   }
}
