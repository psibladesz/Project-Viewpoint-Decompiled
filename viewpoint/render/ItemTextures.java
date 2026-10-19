package viewpoint.render;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import zombie.core.skinnedmodel.model.ItemModelRenderer;
import zombie.core.textures.SmartTexture;
import zombie.core.textures.Texture;
import zombie.core.textures.TextureCombiner;

final class ItemTextures {
   private static final Method CHECK = method();
   private static final Field NAME = field("modelTextureName");
   private static final Field SMART = field("smartTexture");
   private final ArrayList<ItemModelRenderer> made = new ArrayList<>();
   private final HashSet<String> failed = new HashSet<>();

   Texture make(ItemModelRenderer var1) {
      if (CHECK != null && NAME != null && SMART != null) {
         try {
            String var2 = (String)NAME.get(var1);
            if (!(Boolean)CHECK.invoke(var1, var2)) {
               return null;
            }

            this.made.add(var1);
            Texture var3 = ((SmartTexture)SMART.get(var1)).result;
            if (var3 == null && this.failed.add(var2)) {
               System.out.println("[Viewpoint] models: the game could not composite " + var2 + " (its reason is above), drawn with its own art");
            }

            return var3;
         } catch (ReflectiveOperationException | RuntimeException var4) {
            return null;
         }
      } else {
         return null;
      }
   }

   void release() {
      for (int var1 = 0; var1 < this.made.size(); var1++) {
         try {
            SmartTexture var2 = (SmartTexture)SMART.get(this.made.get(var1));
            if (var2.result != null) {
               TextureCombiner.instance.releaseTexture(var2.result);
               var2.result = null;
            }

            var2.clear();
         } catch (ReflectiveOperationException | RuntimeException var3) {
         }
      }

      this.made.clear();
   }

   private static Method method() {
      try {
         Method var0 = ItemModelRenderer.class.getDeclaredMethod("checkSmartTexture", String.class);
         var0.setAccessible(true);
         return var0;
      } catch (ReflectiveOperationException | RuntimeException var1) {
         System.out.println("[Viewpoint] models: items keep their plain textures: " + var1);
         return null;
      }
   }

   private static Field field(String var0) {
      try {
         Field var1 = ItemModelRenderer.class.getDeclaredField(var0);
         var1.setAccessible(true);
         return var1;
      } catch (ReflectiveOperationException | RuntimeException var2) {
         System.out.println("[Viewpoint] models: items keep their plain textures: " + var2);
         return null;
      }
   }
}
