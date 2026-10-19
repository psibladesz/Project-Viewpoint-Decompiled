package viewpoint.platform;

import java.util.Objects;

public final class PadBind extends LiveSettings.Setting {
   final PadChord fallback;
   public final boolean hold;
   private volatile PadChord chord;

   PadBind(String var1, String var2, String var3, String var4, boolean var5) {
      super(var1, var2, var3);
      this.fallback = PadChord.parse(var4);
      this.hold = var5;
      this.chord = this.fallback;
   }

   public boolean pressed() {
      return PadInput.pressed(this);
   }

   public boolean held() {
      return PadInput.held(this);
   }

   public PadChord chord() {
      return this.chord;
   }

   public boolean bound() {
      return this.chord != null;
   }

   void set(PadChord var1) {
      this.chord = var1;
   }

   @Override
   String text() {
      PadChord var1 = this.chord;
      return var1 == null ? "" : var1.text();
   }

   @Override
   void read(String var1) {
      try {
         this.set(PadChord.parse(var1));
      } catch (IllegalArgumentException var3) {
         System.out.println("[Viewpoint] pad " + this.key + ": " + var3.getMessage() + " in \"" + var1 + "\"; kept " + this.text());
      }
   }

   @Override
   boolean matches(String var1) {
      try {
         return Objects.equals(PadChord.parse(var1), this.chord);
      } catch (IllegalArgumentException var3) {
         return false;
      }
   }
}
