package viewpoint.render;

import java.lang.reflect.Field;
import java.util.Arrays;
import viewpoint.platform.Profile;
import zombie.core.skinnedmodel.model.ModelInstanceRenderData;
import zombie.core.skinnedmodel.model.ModelInstanceTextureInitializer;
import zombie.core.skinnedmodel.model.ModelSlotRenderData;

final class CharacterTextures {
   private static final float ALWAYS = 6.0F;
   private static final int PER_FRAME = 4;
   private long[] waiting = new long[64];
   private int waitingCount;

   void ready(ModelDraws var1) {
      this.waiting = this.waiting.length >= var1.slotCount ? this.waiting : new long[var1.slotCount * 2];
      this.waitingCount = 0;
      byte var2 = 0;

      for (int var3 = 0; var3 < var1.slotCount; var3++) {
         CharacterTextures.Composited var4 = var1.slots[var3];
         boolean var5 = var4.composites();
         if (var5 && !(var1.slotDistance[var3] <= 6.0F)) {
            this.waiting[this.waitingCount++] = (long)Float.floatToIntBits(var1.slotDistance[var3]) << 32 | var3;
         } else {
            var2 += var5 ? 1 : 0;
            var4.ready();
         }
      }

      Arrays.sort(this.waiting, 0, this.waitingCount);
      int var6 = Math.min(4, this.waitingCount);

      for (int var7 = 0; var7 < var6; var7++) {
         var1.slots[(int)this.waiting[var7]].ready();
      }

      Profile.modelTexturesMade = var2 + var6;
      Profile.modelTexturesWaiting = this.waitingCount - var6;
   }

   void leaveOut(ModelDraws var1, ModelMeshes.Held[] var2) {
      for (int var3 = Math.min(4, this.waitingCount); var3 < this.waitingCount; var3++) {
         int var4 = (int)this.waiting[var3];
         int var5 = var1.slotFirst[var4];
         Arrays.fill(var2, var5, var5 + var1.slotDraws[var4], null);
      }
   }

   interface Composited {
      boolean composites();

      void ready();
   }

   static final class Snapshot implements CharacterTextures.Composited {
      private static final Field RENDERED = rendered();
      private final ModelSlotRenderData data;

      Snapshot(ModelSlotRenderData var1) {
         this.data = var1;
      }

      @Override
      public boolean composites() {
         if (this.data.textureCreator != null && !this.data.textureCreator.isRendered()) {
            return true;
         }

         for (int var1 = 0; var1 < this.data.modelData.size(); var1++) {
            ModelInstanceRenderData var2 = (ModelInstanceRenderData)this.data.modelData.get(var1);
            ModelInstanceTextureInitializer var3 = var2.modelInstance.getTextureInitializer();
            if (var3 != null && !var3.isRendered()) {
               return true;
            }
         }

         return false;
      }

      @Override
      public void ready() {
         boolean var1 = this.data.checkReady();
         if (RENDERED != null) {
            try {
               RENDERED.setBoolean(this.data, var1);
            } catch (ReflectiveOperationException | RuntimeException var3) {
            }
         }
      }

      private static Field rendered() {
         try {
            Field var0 = ModelSlotRenderData.class.getDeclaredField("rendered");
            var0.setAccessible(true);
            return var0;
         } catch (ReflectiveOperationException | RuntimeException var1) {
            System.out.println("[Viewpoint] models: rendered cannot be set: " + var1);
            return null;
         }
      }
   }
}
