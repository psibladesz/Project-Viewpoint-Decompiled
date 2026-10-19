package viewpoint.models;

import org.joml.Matrix4f;
import viewpoint.core.Frame;
import viewpoint.light.OwnedLight;
import viewpoint.render.ModelDraws;
import zombie.core.skinnedmodel.model.ModelInstance;
import zombie.iso.IsoGridSquare;
import zombie.iso.objects.IsoDeadBody;
import zombie.scripting.objects.ModelScript;

final class CorpseCapture {
   static final float MIDDLE = 0.2F;
   static final float RADIUS = 1.2F;
   private static final float[] WHOLE = new float[]{0.3F, 1.0F, 0.3F};
   private static final float[] DISSOLVING = new float[]{1.0F, 1.0F, 0.2F};
   private static final float[] UNTINTED = new float[]{1.0F, 1.0F, 1.0F};
   private static final Matrix4f place = new Matrix4f();
   private static final Matrix4f model = new Matrix4f();
   private static final Matrix4f attachment = new Matrix4f();
   private static int[] firstBones = new int[8];

   static void draw(Frame var0, IsoDeadBody var1, CorpsePose var2, float var3, int var4, boolean var5) {
      ModelDraws var6 = var0.scene.models;
      int var7 = var6.count();
      float[] var8 = !var5 ? UNTINTED : (var3 < 1.0F ? DISSOLVING : WHOLE);
      float var9 = var1.getX();
      float var10 = var1.getY();
      float var11 = var1.getZ();
      place.translation(-(var9 - var0.camX), (var11 - var0.camZ) * 2.4494896F, -(var10 - var0.camY))
         .scale(-1.5F, 1.5F, 1.5F)
         .rotateY(var2.angle + (float) Math.PI)
         .scale(var2.scale);
      float var12 = place.m30();
      float var13 = place.m31();
      float var14 = place.m32();
      IsoGridSquare var15 = var1.getSquare();
      int var16 = var15 == null ? -1 : OwnedLight.modelLightAt(var15.x, var15.y, var15.z);
      boolean var17 = var15 != null && var15.isOutside() && !var15.haveRoof;
      float var18 = var15 == null ? 0.0F : OwnedLight.modelSky(var15.x, var15.y, var15.z, var17);
      firstBones = firstBones.length >= var2.palettes.length ? firstBones : new int[var2.palettes.length * 2];

      for (int var19 = 0; var19 < var2.palettes.length; var19++) {
         firstBones[var19] = var6.palette(var2.palettes[var19], var2.paletteBones[var19]);
      }

      for (int var24 = 0; var24 < var2.parts.length; var24++) {
         ModelInstance var20 = var2.parts[var24];
         if (ModelCapture.loaded(var20.model)) {
            int var21 = var2.paletteOf[var24] < 0 ? -1 : firstBones[var2.paletteOf[var24]];
            Matrix4f var22 = var21 < 0 ? model.set(place).mul(attachment.set(var2.attachments[var24]).transpose()) : place;
            int var23 = var6.add(var20.model.mesh, var20, var20.model.tex, var22, var21, var21, cull(var20), var4);
            var6.material(var23, var20.tintR * var8[0], var20.tintG * var8[1], var20.tintB * var8[2], var20.hue);
            var6.light(
               var23,
               var16 < 0 ? Math.min(1.0F, var2.ambientR) : (var16 & 0xFF) / 255.0F,
               var16 < 0 ? Math.min(1.0F, var2.ambientG) : (var16 >> 8 & 0xFF) / 255.0F,
               var16 < 0 ? Math.min(1.0F, var2.ambientB) : (var16 >> 16 & 0xFF) / 255.0F,
               var18
            );
            var6.bounds(var23, var12, var13 + 0.2F, var14, 1.2F * Math.max(1.0F, var2.scale));
            var6.fade(var23, 0.0F, var3);
         }
      }

      if (var15 != null && var15 == var0.lootSquare) {
         var6.target(var7);
      }
   }

   private static int cull(ModelInstance var0) {
      ModelScript var1 = var0.modelScript;
      return var1 != null && var1.cullFace != -1 ? var1.cullFace : 1028;
   }

   private CorpseCapture() {
   }
}
