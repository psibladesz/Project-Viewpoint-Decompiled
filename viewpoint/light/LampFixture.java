package viewpoint.light;

public record LampFixture(float x, float y, float rise) {
   static final float WALL_GAP = 0.15F;
   static final float WALL_RISE = 2.0F;
   static final float STREET_RISE = 3.6F;
   public static final LampFixture MIDDLE = new LampFixture(0.5F, 0.5F, 2.1F);

   static LampFixture of(boolean var0, boolean var1, boolean var2, boolean var3, boolean var4) {
      if (var0) {
         return new LampFixture(0.5F, 0.5F, 3.6F);
      } else if (var1) {
         return new LampFixture(0.5F, 0.15F, 2.0F);
      } else if (var2) {
         return new LampFixture(0.5F, 0.85F, 2.0F);
      } else if (var3) {
         return new LampFixture(0.15F, 0.5F, 2.0F);
      } else {
         return var4 ? new LampFixture(0.85F, 0.5F, 2.0F) : MIDDLE;
      }
   }
}
