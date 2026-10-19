package viewpoint.game;

import java.util.HashSet;
import java.util.Stack;
import viewpoint.platform.LiveSettings;
import zombie.characters.IsoPlayer;

public final class SpottedStack<E> extends Stack<E> {
   public static final LiveSettings.Toggle ON = LiveSettings.toggle(
      "game.spottedStack", "Characters seen lately found at once (line of sight)", "Debug/Fixes", true
   );
   private static final int STALE = -1;
   private HashSet<Object> held = new HashSet<>();
   private int indexed = -1;

   public static void use(IsoPlayer var0) {
      Stack var1 = var0.getLastSpotted();
      boolean var2 = ON.get();
      if (var2 != (var1 instanceof SpottedStack)) {
         Stack var3 = var2 ? new SpottedStack() : new Stack();
         var3.addAll(var1);
         var0.setLastSpotted(var3);
      }
   }

   @Override
   public synchronized boolean contains(Object var1) {
      if (this.indexed != this.modCount) {
         this.held.clear();
         this.held.addAll(this);
         this.indexed = this.modCount;
      }

      return this.held.contains(var1);
   }

   @Override
   public synchronized boolean add(E var1) {
      boolean var2 = this.indexed == this.modCount;
      super.add((E)var1);
      if (var2) {
         this.held.add(var1);
         this.indexed = this.modCount;
      }

      return true;
   }

   @Override
   public synchronized E set(int var1, E var2) {
      this.indexed = -1;
      return super.set(var1, (E)var2);
   }

   @Override
   public synchronized void setElementAt(E var1, int var2) {
      this.indexed = -1;
      super.setElementAt((E)var1, var2);
   }

   @Override
   public synchronized Object clone() {
      SpottedStack var1 = (SpottedStack)super.clone();
      var1.held = new HashSet<>();
      var1.indexed = -1;
      return var1;
   }
}
