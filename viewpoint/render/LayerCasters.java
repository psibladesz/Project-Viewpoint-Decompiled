package viewpoint.render;

import java.util.Arrays;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

final class LayerCasters {
   static final int REDRAW_DRAWINGS = 12;
   private final Set<ChunkMeshData> held = Collections.newSetFromMap(new IdentityHashMap<>());
   private long farKey;
   private int age;
   private boolean[] caster = new boolean[0];
   private boolean[] settled = new boolean[0];
   private boolean[] extra = new boolean[0];
   private int extras;
   private boolean lost;
   private boolean arrived;

   void sort(SceneData var1, byte var2) {
      if (this.caster.length < var1.meshCount) {
         int var3 = Math.max(var1.meshCount, this.caster.length * 2);
         this.caster = new boolean[var3];
         this.settled = new boolean[var3];
         this.extra = new boolean[var3];
      }

      this.lost = false;
      this.arrived = false;
      this.extras = 0;
      this.age++;

      for (int var5 = 0; var5 < var1.meshCount; var5++) {
         this.caster[var5] = Meshes.drawn(var1, var5, var2, Integer.MAX_VALUE);
         this.settled[var5] = this.caster[var5] && var1.meshFade[var5 * 2] == 0.0F && var1.meshFade[var5 * 2 + 1] > 1.0F && !var1.meshes[var5].retired();
         boolean var4 = this.caster[var5] && this.held.contains(var1.meshes[var5]);
         this.extra[var5] = this.caster[var5] && !var4;
         this.lost = this.lost | (var4 && !this.settled[var5]);
         this.extras = this.extras + (this.extra[var5] ? 1 : 0);
         this.arrived = this.arrived | (this.extra[var5] && this.settled[var5]);
      }

      for (ChunkMeshData var7 : this.held) {
         this.lost = this.lost | var7.retired();
      }
   }

   boolean lost() {
      return this.lost;
   }

   boolean waited() {
      return this.age >= 12;
   }

   boolean arrived() {
      return this.arrived;
   }

   boolean farSwitched(boolean var1) {
      return var1 != (this.farKey != 0L);
   }

   boolean farChanged(long var1) {
      return var1 != this.farKey;
   }

   boolean[] take(SceneData var1, long var2) {
      this.held.clear();
      this.extras = 0;

      for (int var4 = 0; var4 < var1.meshCount; var4++) {
         if (this.settled[var4]) {
            this.held.add(var1.meshes[var4]);
         }

         this.extra[var4] = this.caster[var4] && !this.settled[var4];
         this.extras = this.extras + (this.extra[var4] ? 1 : 0);
      }

      this.farKey = var2;
      this.age = 0;
      this.lost = false;
      this.arrived = false;
      return this.settled;
   }

   boolean[] extra() {
      return this.extra;
   }

   int extras() {
      return this.extras;
   }

   static int share(byte var0) {
      int var1 = var0 & 255;
      return var1 == 0 ? 0 : (var1 == 255 ? 2 : 1);
   }

   static long place(long var0, long var2, long var4) {
      long var6 = (var0 * -7046029254386353131L + var2) * -4417276706812531889L + var4 * 1609587929392839161L;
      return var6 ^ var6 >>> 29;
   }

   void clear() {
      this.held.clear();
      Arrays.fill(this.extra, false);
      this.extras = 0;
      this.lost = false;
      this.arrived = false;
   }
}
