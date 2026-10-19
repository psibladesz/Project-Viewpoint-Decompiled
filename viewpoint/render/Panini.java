package viewpoint.render;

import org.joml.Matrix4f;
import viewpoint.platform.GlProgram;
import viewpoint.platform.IrisPacks;
import viewpoint.platform.LiveSettings;

public final class Panini {
   private static final String SECTION = "Controls/View";
   private static final LiveSettings.Toggle ON = LiveSettings.toggle("view.panini", "Panini projection", "Controls/View", false);
   private static final LiveSettings.Number STRENGTH = LiveSettings.number("view.paniniStrength", "Panini strength", "Controls/View", 0.1F, 1.0F, 0.05F, 0.5F);
   public static final Panini NONE = new Panini(0.0, 1.0, 1.0);
   private final double d;
   private final double tanX;
   private final double tanY;
   private final double halfX;
   private final double halfY;

   private Panini(double var1, double var3, double var5) {
      this.d = var1;
      this.tanX = var3;
      this.tanY = var5;
      double var7 = Math.atan(var3);
      this.halfX = var1 == 0.0 ? var3 : (var1 + 1.0) * Math.sin(var7) / (var1 + Math.cos(var7));
      this.halfY = this.halfX * var5 / var3;
   }

   public static float strength() {
      return ON.get() && IrisPacks.active() == null ? STRENGTH.get() : 0.0F;
   }

   public static Panini of(Matrix4f var0, float var1) {
      return of(1.0F / var0.m00(), 1.0F / var0.m11(), var1);
   }

   public static Panini ofView(float var0, float var1, float var2) {
      float var3 = (float)Math.tan(var0 / 2.0);
      return of(var3 * var1, var3, var2);
   }

   public static Panini of(float var0, float var1, float var2) {
      return var2 <= 0.0F ? NONE : new Panini(var2, var0, var1);
   }

   public boolean on() {
      return this.d > 0.0;
   }

   public float centreScale() {
      return (float)(this.tanX / this.halfX);
   }

   public void toScreen(float var1, float var2, float[] var3) {
      if (!this.on()) {
         var3[0] = var1;
         var3[1] = var2;
      } else {
         double var4 = var1 * this.tanX;
         double var6 = var2 * this.tanY;
         double var8 = 1.0 / Math.sqrt(1.0 + var4 * var4);
         double var10 = (this.d + 1.0) * var8 / (this.d + var8);
         var3[0] = (float)(var4 * var10 / this.halfX);
         var3[1] = (float)(var6 * var10 / this.halfY);
      }
   }

   public void toFrame(float var1, float var2, float[] var3) {
      if (!this.on()) {
         var3[0] = var1;
         var3[1] = var2;
      } else {
         double var4 = var1 * this.halfX;
         double var6 = var2 * this.halfY;
         double var8 = var4 / (this.d + 1.0);
         double var10 = var8 * var8;
         double var12 = (Math.sqrt(1.0 + var10 * (1.0 - this.d * this.d)) - var10 * this.d) / (1.0 + var10);
         double var14 = (this.d + var12) / ((this.d + 1.0) * var12);
         var3[0] = (float)(var4 * var14 / this.tanX);
         var3[1] = (float)(var6 * var14 / this.tanY);
      }
   }

   void uniforms(GlProgram var1) {
      var1.set("uPanini", (float)this.d, (float)this.halfX, (float)this.halfY);
      var1.set("uPaniniTan", (float)this.tanX, (float)this.tanY);
   }

   static {
      ON.describe("Draws the sides of a wide view closer to their true size: upright lines stay straight, the middle is drawn larger.");
      STRENGTH.describe("How far the view is taken from flat (0.1) towards the full Panini projection (1).");
   }
}
