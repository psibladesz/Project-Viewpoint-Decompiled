package viewpoint.render;

import java.nio.FloatBuffer;
import java.util.Arrays;
import org.joml.Vector3f;
import org.lwjgl.util.vector.Matrix4f;
import zombie.core.skinnedmodel.model.ItemModelRenderer;
import zombie.core.skinnedmodel.model.ModelInstance;
import zombie.core.skinnedmodel.model.ModelMesh;
import zombie.core.skinnedmodel.model.ModelSlotRenderData;
import zombie.core.textures.Texture;

public final class ModelDraws {
   public static final int COLOUR = 1;
   public static final int SHADOW = 2;
   public static final int OUTLINE_BEHIND = 4;
   public static final int MOTION = 8;
   public static final int TARGET = 16;
   public static final int STILL = 32;
   public static final int SELF = 64;
   public static final int SOLID = 128;
   public static final int EDGE_LIT = 256;
   public static final int EDGE_ACROSS_X = 512;
   public static final int GLASS = 1024;
   public static final int BONE_FLOATS = 12;
   static final int VALUES = 68;
   static final int MATERIAL = 16;
   static final int LIGHT = 20;
   static final int SPHERE = 24;
   static final int UV = 28;
   static final int OUTLINE = 32;
   static final int PREVIOUS = 36;
   static final int INDICES = 52;
   static final int FADE = 56;
   static final int STAND = 60;
   static final int EDGE = 64;
   public static final int EACH_VERTEX = 0;
   public static final int UPRIGHT = 1;
   public static final int PLACED = 2;
   private static final float WHOLE_LO = 0.0F;
   private static final float WHOLE_HI = 2.0F;
   ModelMesh[] meshes = new ModelMesh[256];
   ModelInstance[] instances = new ModelInstance[256];
   Texture[] textures = new Texture[256];
   ItemModelRenderer[] items = new ItemModelRenderer[256];
   CharacterTextures.Composited[] slots = new CharacterTextures.Composited[64];
   int[] slotFirst = new int[64];
   int[] slotDraws = new int[64];
   float[] slotDistance = new float[64];
   int slotCount;
   float[] values = new float[17408];
   int[] palette = new int[256];
   int[] shadowPalette = new int[256];
   int[] cull = new int[256];
   int[] flags = new int[256];
   int[] material = new int[256];
   Object[] keys = new Object[256];
   int[] previous = new int[256];
   public final VehicleMaterials vehicles = new VehicleMaterials();
   FarRelief.Lift relief;
   float[] palettes = new float[49152];
   int count;
   int bones;
   private int[] copiedTo = new int[4096];
   private int[] copiedStamp = new int[4096];
   private int stamp = 1;
   boolean outlined;
   boolean targeted;
   private final Vector3f scratch = new Vector3f();

   void reset() {
      Arrays.fill(this.meshes, 0, this.count, null);
      Arrays.fill(this.instances, 0, this.count, null);
      Arrays.fill(this.textures, 0, this.count, null);
      Arrays.fill(this.items, 0, this.count, null);
      Arrays.fill(this.keys, 0, this.count, null);
      Arrays.fill(this.slots, 0, this.slotCount, null);
      this.count = 0;
      this.slotCount = 0;
      this.bones = 0;
      this.stamp++;
      this.outlined = false;
      this.targeted = false;
      this.relief = null;
      this.vehicles.reset();
   }

   public void slot(ModelSlotRenderData var1, float var2) {
      this.look(new CharacterTextures.Snapshot(var1), var1.modelData.size(), var2);
   }

   public void look(CorpseTextures var1, float var2) {
      this.look(var1, 0, var2);
   }

   private void look(CharacterTextures.Composited var1, int var2, float var3) {
      if (this.slotCount == this.slots.length) {
         this.slots = Arrays.copyOf(this.slots, this.slotCount * 2);
         this.slotFirst = Arrays.copyOf(this.slotFirst, this.slotCount * 2);
         this.slotDraws = Arrays.copyOf(this.slotDraws, this.slotCount * 2);
         this.slotDistance = Arrays.copyOf(this.slotDistance, this.slotCount * 2);
      }

      this.slotFirst[this.slotCount] = this.count;
      this.slotDraws[this.slotCount] = var2;
      this.slotDistance[this.slotCount] = var3;
      this.slots[this.slotCount++] = var1;
   }

   public int palette(float[] var1, int var2) {
      return this.palette(var1, 0, var2);
   }

   public int palette(FloatBuffer var1, int var2) {
      if ((this.bones + var2) * 12 > this.palettes.length) {
         this.palettes = Arrays.copyOf(this.palettes, Math.max(this.palettes.length * 2, (this.bones + var2) * 12));
      }

      int var3 = this.bones;

      for (int var4 = 0; var4 < var2; var4++) {
         var1.get(var4 * 16, this.palettes, (var3 + var4) * 12, 12);
      }

      this.bones += var2;
      return var3;
   }

   public int palette(Matrix4f[] var1) {
      int var2 = var1.length;
      if ((this.bones + var2) * 12 > this.palettes.length) {
         this.palettes = Arrays.copyOf(this.palettes, Math.max(this.palettes.length * 2, (this.bones + var2) * 12));
      }

      int var3 = this.bones;
      float[] var4 = this.palettes;
      int var5 = 0;

      for (int var6 = var3 * 12; var5 < var2; var6 += 12) {
         Matrix4f var7 = var1[var5];
         var4[var6] = var7.m00;
         var4[var6 + 1] = var7.m01;
         var4[var6 + 2] = var7.m02;
         var4[var6 + 3] = var7.m03;
         var4[var6 + 4] = var7.m10;
         var4[var6 + 5] = var7.m11;
         var4[var6 + 6] = var7.m12;
         var4[var6 + 7] = var7.m13;
         var4[var6 + 8] = var7.m20;
         var4[var6 + 9] = var7.m21;
         var4[var6 + 10] = var7.m22;
         var4[var6 + 11] = var7.m23;
         var5++;
      }

      this.bones += var2;
      return var3;
   }

   public int lastPose(ModelDraws var1, int var2, int var3) {
      int var4 = var1.palette[var2];
      if (var4 < 0 || var4 + var3 > var1.bones) {
         return -1;
      }

      if (var4 < this.copiedTo.length && this.copiedStamp[var4] == this.stamp) {
         return this.copiedTo[var4];
      }

      int var5 = this.palette(var1.palettes, var4, var3);
      if (var4 >= this.copiedTo.length) {
         this.copiedTo = Arrays.copyOf(this.copiedTo, Math.max(var4 + 1, this.copiedTo.length * 2));
         this.copiedStamp = Arrays.copyOf(this.copiedStamp, this.copiedTo.length);
      }

      this.copiedTo[var4] = var5;
      this.copiedStamp[var4] = this.stamp;
      return var5;
   }

   private int palette(float[] var1, int var2, int var3) {
      if ((this.bones + var3) * 12 > this.palettes.length) {
         this.palettes = Arrays.copyOf(this.palettes, Math.max(this.palettes.length * 2, (this.bones + var3) * 12));
      }

      int var4 = this.bones;
      System.arraycopy(var1, var2 * 12, this.palettes, var4 * 12, var3 * 12);
      this.bones += var3;
      return var4;
   }

   private void indices(int var1) {
      int var2 = var1 * 68 + 52;
      this.values[var2] = this.palette[var1];
      this.values[var2 + 1] = this.shadowPalette[var1];
      this.values[var2 + 2] = this.previous[var1];
      this.values[var2 + 3] = this.flags[var1];
   }

   public void key(int var1, Object var2) {
      this.keys[var1] = var2;
   }

   public Object key(int var1) {
      return this.keys[var1];
   }

   public int flags(int var1) {
      return this.flags[var1];
   }

   public void motion(int var1, ModelDraws var2, int var3, int var4, boolean var5) {
      if (var4 > 0) {
         int var6 = var5 ? this.palette[var1] : this.lastPose(var2, var3, var4);
         if (var6 < 0) {
            return;
         }

         this.previous[var1] = var6;
      }

      System.arraycopy(var2.values, var3 * 68, this.values, var1 * 68 + 36, 16);
      if (this.values[var1 * 68 + 60 + 3] == 2.0F) {
         this.values[var1 * 68 + 36 + 13] = this.values[var1 * 68 + 36 + 13] - (float)(FarRelief.sink(this.relief) - FarRelief.sink(var2.relief));
      }

      this.flags[var1] = this.flags[var1] | 8;
      this.indices(var1);
   }

   public void move(int var1, org.joml.Matrix4f var2) {
      float[] var3 = this.palettes;
      int var4 = var1 * 12;

      for (int var5 = 0; var5 < 4; var5++) {
         float var6 = var3[var4 + var5];
         float var7 = var3[var4 + 4 + var5];
         float var8 = var3[var4 + 8 + var5];
         float var9 = var5 == 3 ? 1.0F : 0.0F;
         var3[var4 + var5] = var2.m00() * var6 + var2.m10() * var7 + var2.m20() * var8 + var2.m30() * var9;
         var3[var4 + 4 + var5] = var2.m01() * var6 + var2.m11() * var7 + var2.m21() * var8 + var2.m31() * var9;
         var3[var4 + 8 + var5] = var2.m02() * var6 + var2.m12() * var7 + var2.m22() * var8 + var2.m32() * var9;
      }
   }

   public void collapse(int var1, float var2, float var3, float var4) {
      int var5 = var1 * 12;
      Arrays.fill(this.palettes, var5, var5 + 12, 0.0F);
      this.palettes[var5 + 3] = var2;
      this.palettes[var5 + 7] = var3;
      this.palettes[var5 + 11] = var4;
   }

   public int add(ModelMesh var1, ModelInstance var2, Texture var3, org.joml.Matrix4f var4, int var5, int var6, int var7, int var8) {
      if (this.count == this.meshes.length) {
         this.meshes = Arrays.copyOf(this.meshes, this.count * 2);
         this.instances = Arrays.copyOf(this.instances, this.count * 2);
         this.textures = Arrays.copyOf(this.textures, this.count * 2);
         this.items = Arrays.copyOf(this.items, this.count * 2);
         this.values = Arrays.copyOf(this.values, this.count * 2 * 68);
         this.palette = Arrays.copyOf(this.palette, this.count * 2);
         this.shadowPalette = Arrays.copyOf(this.shadowPalette, this.count * 2);
         this.cull = Arrays.copyOf(this.cull, this.count * 2);
         this.flags = Arrays.copyOf(this.flags, this.count * 2);
         this.material = Arrays.copyOf(this.material, this.count * 2);
         this.keys = Arrays.copyOf(this.keys, this.count * 2);
         this.previous = Arrays.copyOf(this.previous, this.count * 2);
      }

      int var9 = this.count++;
      this.meshes[var9] = var1;
      this.instances[var9] = var2;
      this.textures[var9] = var3;
      var4.get(this.values, var9 * 68);
      this.uv(var9, 0.0F, 0.0F, 1.0F, 1.0F);
      this.fade(var9, 0.0F, 2.0F);
      this.stand(var9, 0, 0.0F, 0.0F, 0.0F);
      this.values[var9 * 68 + 32 + 3] = 0.0F;
      this.palette[var9] = var5;
      this.shadowPalette[var9] = var6;
      this.cull[var9] = var7;
      this.flags[var9] = var8;
      this.material[var9] = -1;
      this.previous[var9] = -1;
      this.indices(var9);
      return var9;
   }

   public void material(int var1, float var2, float var3, float var4, float var5) {
      int var6 = var1 * 68 + 16;
      this.values[var6] = var2;
      this.values[var6 + 1] = var3;
      this.values[var6 + 2] = var4;
      this.values[var6 + 3] = var5;
   }

   public void light(int var1, float var2, float var3, float var4, float var5) {
      int var6 = var1 * 68 + 20;
      this.values[var6] = var2;
      this.values[var6 + 1] = var3;
      this.values[var6 + 2] = var4;
      this.values[var6 + 3] = var5;
   }

   public void uv(int var1, float var2, float var3, float var4, float var5) {
      int var6 = var1 * 68 + 28;
      this.values[var6] = var2;
      this.values[var6 + 1] = var3;
      this.values[var6 + 2] = var4;
      this.values[var6 + 3] = var5;
   }

   public void fade(int var1, float var2, float var3) {
      int var4 = var1 * 68 + 56;
      this.values[var4] = var2;
      this.values[var4 + 1] = var3;
      this.values[var4 + 2] = 0.0F;
      this.values[var4 + 3] = 0.0F;
   }

   public void stand(int var1, int var2, float var3, float var4, float var5) {
      int var6 = var1 * 68 + 60;
      this.values[var6] = var3;
      this.values[var6 + 1] = var4;
      this.values[var6 + 2] = var5;
      this.values[var6 + 3] = var2;
   }

   public void item(int var1, ItemModelRenderer var2) {
      this.items[var1] = var2;
   }

   public void outline(int var1, float var2, float var3, float var4, float var5, boolean var6) {
      int var7 = var1 * 68 + 32;
      this.values[var7] = var2;
      this.values[var7 + 1] = var3;
      this.values[var7 + 2] = var4;
      this.values[var7 + 3] = Math.max(var5, 0.001F);
      this.flags[var1] = var6 ? this.flags[var1] | 4 : this.flags[var1] & -5;
      this.indices(var1);
      this.outlined = true;
   }

   public void own(int var1) {
      for (int var2 = var1; var2 < this.count; var2++) {
         this.flags[var2] = this.flags[var2] | 64;
         this.indices(var2);
      }
   }

   public void edgeLit(int var1, float var2, float var3, float var4, float var5, boolean var6) {
      int var7 = var1 * 68 + 64;
      this.values[var7] = var2;
      this.values[var7 + 1] = var3;
      this.values[var7 + 2] = var4;
      this.values[var7 + 3] = var5;
      this.flags[var1] = this.flags[var1] | 256 | (var6 ? 512 : 0);
      this.indices(var1);
   }

   public void whole(int var1) {
      for (int var2 = var1; var2 < this.count; var2++) {
         this.flags[var2] = this.flags[var2] | 128;
         this.indices(var2);
      }
   }

   public void target(int var1) {
      for (int var2 = var1; var2 < this.count; var2++) {
         this.flags[var2] = this.flags[var2] | 16;
         this.indices(var2);
         this.targeted = true;
      }
   }

   public void vehicle(int var1, int var2) {
      this.material[var1] = var2;
   }

   public void bounds(int var1, org.joml.Matrix4f var2, Vector3f var3, Vector3f var4, float var5) {
      boolean var6 = var3.x <= var4.x;
      float var7 = var6 ? var3.distance(var4) * 0.5F : 1.0F;
      Vector3f var8 = var6 ? this.scratch.set(var3).add(var4).mul(0.5F) : this.scratch.zero();
      var2.transformPosition(var8);
      float var9 = var8.x;
      float var10 = var8.y;
      float var11 = var8.z;
      var2.getScale(this.scratch);
      this.bounds(var1, var9, var10, var11, var7 * Math.max(this.scratch.x, Math.max(this.scratch.y, this.scratch.z)) * var5);
   }

   public void bounds(int var1, float var2, float var3, float var4, float var5) {
      boolean var6 = this.relief != null && this.values[var1 * 68 + 60 + 3] != 2.0F;
      float var7 = var6 ? this.relief.riseAbout(var2, var4, var5) : 0.0F;
      int var8 = var1 * 68 + 24;
      this.values[var8] = var2;
      this.values[var8 + 1] = var3 + (var6 ? this.relief.sceneAt(var2, var4) : 0.0F);
      this.values[var8 + 2] = var4;
      this.values[var8 + 3] = var5 + var7;
   }

   public int count() {
      return this.count;
   }

   public static boolean refused(ModelMesh var0) {
      return ModelMeshes.refused.contains(var0) || !ModelMeshes.available();
   }
}
