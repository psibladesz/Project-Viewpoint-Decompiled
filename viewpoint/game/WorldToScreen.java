package viewpoint.game;

import org.joml.Matrix4f;
import org.joml.Vector4f;
import viewpoint.camera.CameraView;
import viewpoint.core.Frame;
import viewpoint.input.Camera;
import viewpoint.input.FreeCam;
import viewpoint.input.SeatedView;
import viewpoint.render.FarRelief;
import viewpoint.render.Panini;

final class WorldToScreen {
   private final Matrix4f view = new Matrix4f();
   private final Matrix4f viewProjection = new Matrix4f();
   private final Vector4f clip = new Vector4f();
   private final float[] eye = new float[3];
   private final float[] dir = new float[3];
   private final float[] up = new float[3];
   private final float[] onScreen = new float[2];
   private float originX;
   private float originY;
   private float originZ;
   private float width;
   private float height;
   private FarRelief.Lift lift;
   private Panini panini = Panini.NONE;

   boolean set(Frame var1, FreeCam.Place var2, float var3, float var4) {
      boolean var5 = false;
      this.up[0] = 0.0F;
      this.up[1] = 1.0F;
      this.up[2] = 0.0F;
      if (var2 != null) {
         Camera.at(var1, var2, this.eye, this.dir);
      } else {
         Camera.direction(var1.viewYaw, var1.viewPitch, this.dir);
         var5 = !CameraView.place(var1, var1.viewYaw, var1.viewPitch, this.eye);
         if (var5) {
            Camera.eye(var1, var1.viewYaw, this.eye);
            SeatedView.turn(var1, this.dir);
            SeatedView.turn(var1, this.up);
         } else {
            CameraView.direction(var1.cameraShot, var1.viewYaw, var1.viewPitch, this.dir, this.up);
         }
      }

      this.view
         .setLookAt(
            this.eye[0],
            this.eye[1],
            this.eye[2],
            this.eye[0] + this.dir[0],
            this.eye[1] + this.dir[1],
            this.eye[2] + this.dir[2],
            this.up[0],
            this.up[1],
            this.up[2]
         );
      var1.scene.projection.mul(this.view, this.viewProjection);
      this.originX = var1.camX;
      this.originY = var1.camY;
      this.originZ = var1.camZ;
      this.lift = var1.scene.relief;
      this.panini = Panini.of(var1.scene.projection, var1.scene.panini);
      this.width = var3;
      this.height = var4;
      return var5;
   }

   boolean pixel(float var1, float var2, float var3, float var4, float[] var5) {
      float var6 = (var3 - this.originZ) * 2.4494896F + var4 + (this.lift == null ? 0.0F : this.lift.scene(var1, var2));
      this.viewProjection.transform(-(var1 - this.originX), var6, -(var2 - this.originY), 1.0F, this.clip);
      if (this.clip.w < 0.05F) {
         return false;
      }

      this.panini.toScreen(this.clip.x / this.clip.w, this.clip.y / this.clip.w, this.onScreen);
      var5[0] = (0.5F + 0.5F * this.onScreen[0]) * this.width;
      var5[1] = (0.5F - 0.5F * this.onScreen[1]) * this.height;
      return true;
   }
}
