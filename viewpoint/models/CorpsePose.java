package viewpoint.models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.IdentityHashMap;
import org.joml.Matrix4f;
import viewpoint.core.Frame;
import viewpoint.render.CorpseTextures;
import zombie.core.skinnedmodel.ModelManager;
import zombie.core.skinnedmodel.DeadBodyAtlas.BodyParams;
import zombie.core.skinnedmodel.advancedanimation.AnimatedModel;
import zombie.core.skinnedmodel.advancedanimation.AnimatedModel.AnimatedModelInstanceRenderData;
import zombie.core.skinnedmodel.animation.AnimationPlayer;
import zombie.core.skinnedmodel.animation.AnimationTrack;
import zombie.core.skinnedmodel.animation.TwistableBoneTransform;
import zombie.core.skinnedmodel.model.ModelInstance;
import zombie.core.skinnedmodel.model.ModelInstanceTextureCreator;
import zombie.core.skinnedmodel.visual.ItemVisuals;
import zombie.core.textures.Texture;
import zombie.iso.Vector2;
import zombie.iso.objects.IsoDeadBody;

final class CorpsePose {
   private static final long MODEL_BYTES = 65536L;
   final Object look;
   private final AnimatedModel model = new AnimatedModel();
   private final ItemVisuals itemVisuals = new ItemVisuals();
   final float ambientR;
   final float ambientG;
   final float ambientB;
   private boolean built;
   ModelInstance[] parts;
   int[] paletteOf;
   Matrix4f[] attachments;
   float[][] palettes;
   int[] paletteBones;
   float angle;
   float scale;
   CorpseTextures textures;
   long lastUsed;
   private long bytes;

   CorpsePose(IsoDeadBody var1, Object var2) {
      this.look = var2;
      BodyParams var3 = new BodyParams();
      var3.init(var1);
      this.ambientR = var3.ambient.r;
      this.ambientG = var3.ambient.g;
      this.ambientB = var3.ambient.b;
      this.model.setAnimate(false);
      this.model.setAnimSetName(var3.animSetName);
      this.model.setState(var3.stateName);
      this.model.setPrimaryHandModelName(var3.primaryHandItem);
      this.model.setSecondaryHandModelName(var3.secondaryHandItem);
      this.model.setAttachedModelNames(var3.attachedModelNames);
      this.model.setVariable("FallOnFront", var3.fallOnFront);
      this.model.setVariable("KilledByFall", var3.killedByFall);
      var3.variables.forEach((var1x, var2x) -> this.model.setVariable(var1x, var2x));
      this.itemVisuals.addAll(var3.itemVisuals);
      this.model.setModelData(var3.baseVisual, var3.itemVisuals);
      this.model.setAngle(new Vector2().setLengthAndDirection(var3.angle, 1.0F));
      this.deathPose(var3.diedBoneTransforms);
      this.model.setTrackTime(var3.trackTime);
      this.model.setGrappleable(var3.grappleable);
      this.model.update();
   }

   private void deathPose(TwistableBoneTransform[] var1) {
      AnimationPlayer var2 = this.model.getAnimationPlayer();
      if (var1 != null && var1.length != 0 && !var2.isBoneCountMismatched(var1)) {
         var2.stopAll();
         AnimationTrack var3 = var2.play("DeathPose", true, true, -1.0F);
         if (var3 != null) {
            var3.setBlendWeight(1.0F);
            var3.initRagdollTransforms(var1);
         }
      }
   }

   boolean advance() {
      if (this.built) {
         return true;
      }

      if (!this.model.isReadyToRender()) {
         return false;
      }

      this.model.update();
      ArrayList<ModelInstance> var1 = new ArrayList<>();
      ArrayList<float[]> var2 = new ArrayList<>();
      ArrayList<Integer> var3 = new ArrayList<>();
      ArrayList<Matrix4f> var4 = new ArrayList<>();
      list(this.model.getModelInstance(), null, var1, var2, var3, var4);
      this.parts = var1.toArray(new ModelInstance[0]);
      this.paletteOf = var3.stream().mapToInt(Integer::intValue).toArray();
      this.attachments = var4.toArray(new Matrix4f[0]);
      this.palettes = var2.toArray(new float[0][]);
      this.paletteBones = var2.stream().mapToInt(var0 -> var0.length / 12).toArray();
      this.angle = this.model.getAnimationPlayer().getRenderedAngle();
      this.scale = this.model.getScale();
      ModelInstanceTextureCreator var5 = ModelInstanceTextureCreator.alloc();
      var5.init(this.model.getVisual(), this.itemVisuals, this.model.getModelInstance());
      this.textures = new CorpseTextures(var5);
      this.releasePlayer();
      this.built = true;
      return true;
   }

   private void releasePlayer() {
      forgetPlayer(this.model.getModelInstance());
      this.model.releaseAnimationPlayer();
   }

   private static void forgetPlayer(ModelInstance var0) {
      var0.animPlayer = null;

      for (ModelInstance var2 : var0.sub) {
         forgetPlayer(var2);
      }
   }

   private static void list(
           ModelInstance var0,
           AnimatedModelInstanceRenderData var1,
           ArrayList<ModelInstance> var2,
           ArrayList<float[]> var3,
           ArrayList<Integer> var4,
           ArrayList<Matrix4f> var5
   ) {
      AnimatedModelInstanceRenderData var6 = new AnimatedModelInstanceRenderData();
      var6.initModel(var0, var1);
      var6.transformToParent(var1);
      boolean var7 = var0.model.isStatic;
      if (var0.model.mesh != null && (var7 || var6.matrixPalette != null && var6.matrixPalette.limit() >= 16)) {
         var2.add(var0);
         var4.add(var7 ? -1 : shared(var3, var6.matrixPalette.limit() / 16, var6));
         var5.add(new Matrix4f(var6.xfrm));
      }

      for (ModelInstance var9 : var0.sub) {
         list(var9, var6, var2, var3, var4, var5);
      }
   }

   private static int shared(ArrayList<float[]> var0, int var1, AnimatedModelInstanceRenderData var2) {
      float[] var3 = new float[var1 * 12];

      for (int var4 = 0; var4 < var1; var4++) {
         var2.matrixPalette.get(var4 * 16, var3, var4 * 12, 12);
      }

      for (int var5 = 0; var5 < var0.size(); var5++) {
         if (Arrays.equals((float[])var0.get(var5), var3)) {
            return var5;
         }
      }

      var0.add(var3);
      return var0.size() - 1;
   }

   boolean drawable() {
      return this.built && this.textures.made();
   }

   boolean built() {
      return this.built;
   }

   long bytes() {
      if (this.bytes == 0L && this.drawable()) {
         IdentityHashMap<Texture, Boolean> var1 = new IdentityHashMap<>();
         long var2 = 65536L;

         for (ModelInstance var7 : this.parts) {
            Texture var8 = var7.tex;
            if (var8 != null && var8.isReady() && var1.put(var8, Boolean.TRUE) == null) {
               var2 += (long)var8.getWidth() * var8.getHeight() * 4L * 4L / 3L;
            }
         }

         this.bytes = var2;
      }

      return this.bytes == 0L ? 65536L : this.bytes;
   }

   void hold(Frame var1) {
      hold(var1, this.model.getModelInstance());
   }

   private static void hold(Frame var0, ModelInstance var1) {
      if (var1 != null) {
         var1.renderRefCount++;
         var0.corpseInstances.add(var1);

         for (ModelInstance var3 : var1.sub) {
            hold(var0, var3);
         }
      }
   }

   void letGo() {
      this.reset(this.model.getModelInstance());
      this.model.releaseAnimationPlayer();
   }

   private void reset(ModelInstance var1) {
      if (var1 != null) {
         for (ModelInstance var3 : new ArrayList<>(var1.sub)) {
            this.reset(var3);
         }

         ModelManager.instance.resetModelInstance(var1, this.model);
      }
   }
}