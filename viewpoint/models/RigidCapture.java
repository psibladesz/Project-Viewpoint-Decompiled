package viewpoint.models;

import java.lang.reflect.Field;
import java.nio.FloatBuffer;
import java.util.List;
import org.joml.Matrix4f;
import viewpoint.core.Frame;
import viewpoint.light.OwnedLight;
import viewpoint.render.ModelDraws;
import zombie.core.skinnedmodel.model.IsoObjectModelDrawer;
import zombie.core.skinnedmodel.model.ItemModelRenderer;
import zombie.core.skinnedmodel.model.Model;
import zombie.core.skinnedmodel.model.ModelMesh;
import zombie.core.skinnedmodel.model.WorldItemModelDrawer;
import zombie.core.textures.Texture;
import zombie.inventory.InventoryItem;
import zombie.iso.IsoCell;
import zombie.iso.IsoGridSquare;
import zombie.iso.IsoObject;
import zombie.iso.objects.IsoDoor;
import zombie.iso.objects.IsoThumpable;
import zombie.scripting.objects.ModelScript;

final class RigidCapture {
   private static boolean reached = true;
   private static final Field OBJECT_MODEL = field(IsoObjectModelDrawer.class, "model");
   private static final Field OBJECT_SCRIPT = field(IsoObjectModelDrawer.class, "modelScript");
   private static final Field OBJECT_TEXTURE = field(IsoObjectModelDrawer.class, "texture");
   private static final Field OBJECT_PALETTE = field(IsoObjectModelDrawer.class, "matrixPalette");
   private static final Field OBJECT_TRANSFORM = field(IsoObjectModelDrawer.class, "transform");
   private static final Field[] OBJECT_XYZ = fields(IsoObjectModelDrawer.class, "x", "y", "z");
   private static final Field[] OBJECT_COLOUR = fields(IsoObjectModelDrawer.class, "tintR", "tintG", "tintB", "hue");
   private static final Field[] OBJECT_AMBIENT = fields(IsoObjectModelDrawer.class, "ambientR", "ambientG", "ambientB");
   private static final Field ITEM_RENDERER = field(WorldItemModelDrawer.class, "renderer");
   private static final Field ITEM_MODEL = field(ItemModelRenderer.class, "model");
   private static final Field ITEM_PARTS = field(ItemModelRenderer.class, "weaponParts");
   private static final Field ITEM_TRANSFORM = field(ItemModelRenderer.class, "transform");
   private static final Field ITEM_CULL = field(ItemModelRenderer.class, "cullFace");
   private static final Field ITEM_MASK = field(ItemModelRenderer.class, "tintMask");
   private static final Field[] ITEM_COLOUR = fields(ItemModelRenderer.class, "tintR", "tintG", "tintB", "hue");
   private static final Field[] ITEM_AMBIENT = fields(ItemModelRenderer.class, "ambientR", "ambientG", "ambientB");
   private static final Field PART_MODEL = field(part(), "model");
   private static final Field PART_TRANSFORM = field(part(), "transform");
   private static final Matrix4f place = new Matrix4f();
   private static final Matrix4f model = new Matrix4f();
   private static final float[] colour = new float[4];
   private static final float[] ambient = new float[3];

   static boolean object(Frame var0, IsoObjectModelDrawer var1, IsoGridSquare var2, IsoObject var3) {
      if (!reached) {
         return false;
      }

      try {
         Model var4 = (Model)OBJECT_MODEL.get(var1);
         FloatBuffer var5 = (FloatBuffer)OBJECT_PALETTE.get(var1);
         if (ModelCapture.loaded(var4) && (var4.isStatic || var5 != null && var5.limit() >= 16)) {
            ModelScript var6 = (ModelScript)OBJECT_SCRIPT.get(var1);
            Texture var7 = (Texture)OBJECT_TEXTURE.get(var1);
            Texture var8 = var7 != null ? var7 : var4.tex;
            boolean var9 = var6 != null && var6.getShaderName().contains("door");
            int var10 = var9 ? 1028 : (var6 != null && var6.cullFace > 0 ? var6.cullFace : 0);
            ModelDraws var11 = var0.scene.models;
            placed(var0, OBJECT_XYZ[0].getFloat(var1), OBJECT_XYZ[1].getFloat(var1), OBJECT_XYZ[2].getFloat(var1));
            place.rotateY((float) Math.PI);
            model.set(place).mul((Matrix4f)OBJECT_TRANSFORM.get(var1));
            int var12 = var4.isStatic ? -1 : var11.palette(var5, var5.limit() / 16);
            int var13 = 3 | (var9 ? 1152 : 0);
            int var14 = var11.add(var4.mesh, null, var8, model, var12, var12, var10, var13);
            read(var1, OBJECT_COLOUR, colour);
            read(var1, OBJECT_AMBIENT, ambient);
            var11.material(var14, colour[0], colour[1], colour[2], colour[3]);
            if (var9 && var8 != null) {
               doorUv(var11, var14, var8);
            }

            finish(var11, var14, var2, var4.mesh, !var4.isStatic);
            if (var9 && var2 != null) {
               doorLight(var11, var14, var2, var3);
            }

            return true;
         } else {
            return false;
         }
      } catch (ReflectiveOperationException | RuntimeException var15) {
         return false;
      }
   }

   static boolean item(Frame var0, WorldItemModelDrawer var1, IsoGridSquare var2, InventoryItem var3) {
      if (!reached) {
         return false;
      }

      try {
         ItemModelRenderer var4 = (ItemModelRenderer)ITEM_RENDERER.get(var1);
         Model var5 = (Model)ITEM_MODEL.get(var4);
         List var6 = (List)ITEM_PARTS.get(var4);
         if (rigid(var5) && partsRigid(var6)) {
            ModelDraws var7 = var0.scene.models;
            int var8 = Math.max(0, ITEM_CULL.getInt(var4));
            boolean var9 = ITEM_MASK.get(var4) != null;
            read(var4, ITEM_COLOUR, colour);
            read(var4, ITEM_AMBIENT, ambient);
            placed(var0, var4.x, var4.y, var4.z);
            place.rotateX((float)Math.toRadians(var3.worldXRotation))
               .rotateY((float)Math.toRadians(var3.worldZRotation))
               .rotateZ((float)Math.toRadians(var3.worldYRotation));
            model.set(place).mul((Matrix4f)ITEM_TRANSFORM.get(var4));
            int var10 = var7.add(var5.mesh, null, var5.tex, model, -1, -1, var8, 3);
            var7.item(var10, var4);
            var7.material(var10, var9 ? 1.0F : colour[0], var9 ? 1.0F : colour[1], var9 ? 1.0F : colour[2], colour[3]);
            finish(var7, var10, var2, var5.mesh, false);

            for (int var11 = 0; var6 != null && var11 < var6.size(); var11++) {
               Model var12 = (Model)PART_MODEL.get(var6.get(var11));
               model.set(place).mul((Matrix4f)PART_TRANSFORM.get(var6.get(var11)));
               int var13 = var7.add(var12.mesh, null, var12.tex, model, -1, -1, var8, 3);
               var7.material(var13, var9 ? 1.0F : colour[0], var9 ? 1.0F : colour[1], var9 ? 1.0F : colour[2], colour[3]);
               finish(var7, var13, var2, var12.mesh, false);
            }

            return true;
         } else {
            return false;
         }
      } catch (ReflectiveOperationException | RuntimeException var14) {
         return false;
      }
   }

   private static boolean rigid(Model var0) {
      return ModelCapture.loaded(var0) && var0.isStatic;
   }

   private static boolean partsRigid(List<?> var0) throws ReflectiveOperationException {
      for (int var1 = 0; var0 != null && var1 < var0.size(); var1++) {
         if (!rigid((Model)PART_MODEL.get(var0.get(var1)))) {
            return false;
         }
      }

      return true;
   }

   private static void placed(Frame var0, float var1, float var2, float var3) {
      place.translation(-(var1 - var0.camX), (var3 - var0.camZ) * 2.4494896F, -(var2 - var0.camY)).scale(-1.5F, 1.5F, 1.5F);
   }

   private static void doorUv(ModelDraws var0, int var1, Texture var2) {
      float var3 = var2.getWidthHW();
      float var4 = var2.getHeightHW();
      float var5 = var2.xStart * var3 - var2.offsetX;
      float var6 = var2.yStart * var4 - var2.offsetY;
      var0.uv(var1, var5 / var3, var6 / var4, var2.getWidthOrig() / var3, var2.getHeightOrig() / var4);
   }

   private static void finish(ModelDraws var0, int var1, IsoGridSquare var2, ModelMesh var3, boolean var4) {
      int var5 = var2 == null ? -1 : OwnedLight.modelLightAt(var2.x, var2.y, var2.z);
      var0.light(
         var1,
         var5 < 0 ? Math.min(1.0F, ambient[0]) : (var5 & 0xFF) / 255.0F,
         var5 < 0 ? Math.min(1.0F, ambient[1]) : (var5 >> 8 & 0xFF) / 255.0F,
         var5 < 0 ? Math.min(1.0F, ambient[2]) : (var5 >> 16 & 0xFF) / 255.0F,
         var2 == null ? 0.0F : OwnedLight.modelSky(var2.x, var2.y, var2.z, var2.isOutside() && !var2.haveRoof)
      );
      var0.bounds(var1, model, var3.minXyz, var3.maxXyz, var4 ? 2.0F : 1.0F);
   }

   private static void doorLight(ModelDraws var0, int var1, IsoGridSquare var2, IsoObject var3) {
      boolean var4;
      if (var3 instanceof IsoDoor var6) {
         var4 = var6.getNorth() != var6.IsOpen();
      } else {
         if (!(var3 instanceof IsoThumpable var5) || !var5.isDoor()) {
            return;
         }

         var4 = var5.getNorth() != var5.IsOpen();
      }

      IsoCell var9 = var2.getCell();
      IsoGridSquare var7 = var9 == null ? null : var9.getGridSquare(var4 ? var2.x : var2.x - 1, var4 ? var2.y - 1 : var2.y, var2.z);
      int var8 = var7 == null ? -1 : OwnedLight.modelLightAt(var7.x, var7.y, var7.z);
      if (var8 >= 0) {
         var0.edgeLit(
            var1,
            (var8 & 0xFF) / 255.0F,
            (var8 >> 8 & 0xFF) / 255.0F,
            (var8 >> 16 & 0xFF) / 255.0F,
            OwnedLight.modelSky(var7.x, var7.y, var7.z, var7.isOutside() && !var7.haveRoof),
            !var4
         );
      }
   }

   private static void read(Object var0, Field[] var1, float[] var2) throws IllegalAccessException {
      for (int var3 = 0; var3 < var1.length; var3++) {
         var2[var3] = var1[var3].getFloat(var0);
      }
   }

   private static Class<?> part() {
      try {
         return Class.forName("zombie.core.skinnedmodel.model.ItemModelRenderer$WeaponPartParams");
      } catch (ClassNotFoundException | RuntimeException var1) {
         return null;
      }
   }

   private static Field[] fields(Class<?> var0, String... var1) {
      Field[] var2 = new Field[var1.length];

      for (int var3 = 0; var3 < var1.length; var3++) {
         var2[var3] = field(var0, var1[var3]);
      }

      return var2;
   }

   private static Field field(Class<?> var0, String var1) {
      try {
         Field var2 = var0.getDeclaredField(var1);
         var2.setAccessible(true);
         return var2;
      } catch (ReflectiveOperationException | RuntimeException var3) {
         if (reached) {
            System.out.println("[Viewpoint] models: objects and items cannot be drawn, " + var1 + ": " + var3);
         }

         reached = false;
         return null;
      }
   }

   private RigidCapture() {
   }
}
