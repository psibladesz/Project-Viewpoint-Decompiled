package viewpoint.platform;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;
import java.util.function.IntPredicate;

public final class KeyBind extends LiveSettings.Setting {
   private static final AtomicInteger changes = new AtomicInteger();
   final KeyBind.Kind kind;
   final boolean dev;
   final List<KeyChord> fallback;
   private volatile List<KeyChord> chords;
   private volatile BooleanSupplier usable = () -> true;

   KeyBind(String var1, String var2, String var3, KeyBind.Kind var4, boolean var5, String var6) {
      super(var1, var2, var3);
      this.kind = var4;
      this.dev = var5;
      this.fallback = KeyChord.parse(var6);
      this.chords = this.fallback;
   }

   public boolean pressed() {
      return KeyInput.pressed(this);
   }

   public boolean down(IntPredicate var1) {
      if (!this.usable()) {
         return false;
      }

      for (KeyChord var3 : this.chords) {
         if (var1.test(var3.key()) && var3.heldDown(var1)) {
            return true;
         }
      }

      return false;
   }

   KeyBind usableWhen(BooleanSupplier var1) {
      this.usable = var1;
      return this;
   }

   boolean usable() {
      return this.usable.getAsBoolean();
   }

   public List<KeyChord> chords() {
      return this.chords;
   }

   public boolean bound() {
      return !this.chords.isEmpty();
   }

   public String display() {
      return KeyChord.display(this.chords, KeyChord::gameName);
   }

   String windowText() {
      return KeyChord.display(this.chords, KeyChord::latinName);
   }

   void set(List<KeyChord> var1) {
      this.chords = List.copyOf(var1);
      changes.incrementAndGet();
   }

   static int changes() {
      return changes.get();
   }

   @Override
   String text() {
      return KeyChord.text(this.chords);
   }

   @Override
   void read(String var1) {
      try {
         this.set(KeyChord.parse(var1));
      } catch (IllegalArgumentException var3) {
         System.out.println("[Viewpoint] key " + this.key + ": " + var3.getMessage() + " in \"" + var1 + "\"; kept " + this.text());
      }
   }

   @Override
   boolean matches(String var1) {
      try {
         return KeyChord.parse(var1).equals(this.chords);
      } catch (IllegalArgumentException var3) {
         return false;
      }
   }

   public enum Kind {
      PRESS,
      MENU,
      HOLD;
   }
}
