package viewpoint.render;

import zombie.core.skinnedmodel.model.ModelInstanceTextureCreator;

public final class CorpseTextures implements CharacterTextures.Composited {
   private final ModelInstanceTextureCreator creator;
   private volatile boolean made;

   public CorpseTextures(ModelInstanceTextureCreator var1) {
      this.creator = var1;
   }

   public boolean made() {
      return this.made;
   }

   @Override
   public boolean composites() {
      return !this.creator.isRendered();
   }

   @Override
   public void ready() {
      this.creator.render();
      this.made = this.creator.isRendered();
   }
}
