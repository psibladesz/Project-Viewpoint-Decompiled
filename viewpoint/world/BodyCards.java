package viewpoint.world;

import java.util.ArrayList;
import viewpoint.models.Corpses;
import viewpoint.render.SceneData;
import zombie.core.textures.TextureID;
import zombie.iso.objects.IsoDeadBody;

final class BodyCards {
   private static final ArrayList<IsoDeadBody> gatheredBodies = new ArrayList<>();
   private static final ArrayList<float[]> gatheredCards = new ArrayList<>();
   private static final ArrayList<TextureID> gatheredPages = new ArrayList<>();
   private static final ArrayList<Object> gatheredLooks = new ArrayList<>();
   private final IsoDeadBody[] bodies;
   private final float[] cards;
   private final TextureID[] pages;
   private final Object[] looks;

   private BodyCards(IsoDeadBody[] var1, float[] var2, TextureID[] var3, Object[] var4) {
      this.bodies = var1;
      this.cards = var2;
      this.pages = var3;
      this.looks = var4;
   }

   static void begin() {
      gatheredBodies.clear();
      gatheredCards.clear();
      gatheredPages.clear();
      gatheredLooks.clear();
   }

   static void add(IsoDeadBody var0, float[] var1, TextureID var2, Object var3) {
      gatheredBodies.add(var0);
      gatheredCards.add(var1);
      gatheredPages.add(var2);
      gatheredLooks.add(var3);
   }

   static BodyCards end() {
      int var0 = gatheredBodies.size();
      if (var0 == 0) {
         return null;
      }

      float[] var1 = new float[var0 * 28];

      for (int var2 = 0; var2 < var0; var2++) {
         float[] var3 = gatheredCards.get(var2);
         if (var3 != null) {
            System.arraycopy(var3, 0, var1, var2 * 28, 28);
         }
      }

      BodyCards var4 = new BodyCards(gatheredBodies.toArray(new IsoDeadBody[0]), var1, gatheredPages.toArray(new TextureID[0]), gatheredLooks.toArray());
      begin();
      return var4;
   }

   void list(float var1, boolean var2) {
      for (int var3 = 0; var3 < this.bodies.length; var3++) {
         Corpses.listed(this.bodies[var3], this.looks[var3], var1, var2);
      }
   }

   void into(SceneData var1, int var2) {
      for (int var3 = 0; var3 < this.bodies.length; var3++) {
         if (this.pages[var3] != null && this.bodies[var3].getDoRender()) {
            var1.addCard(var2, this.cards, var3 * 28, this.pages[var3], Corpses.share(this.bodies[var3]));
         }
      }
   }
}
