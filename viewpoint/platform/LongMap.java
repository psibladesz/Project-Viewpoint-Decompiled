package viewpoint.platform;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

public final class LongMap<V> {
   private static final Object TOMBSTONE = new Object();
   private long[] keys;
   private Object[] values;
   private int size;
   private int used;
   private int mask;

   public LongMap() {
      this(16);
   }

   public LongMap(int var1) {
      int var2 = Integer.highestOneBit(Math.max(8, var1 * 2 - 1)) << 1;
      this.keys = new long[var2];
      this.values = new Object[var2];
      this.mask = var2 - 1;
   }

   public int size() {
      return this.size;
   }

   public boolean isEmpty() {
      return this.size == 0;
   }

   public Iterable<V> values() {
      return () -> new Iterator<V>() {
         private int next = LongMap.this.advance(0);
         private int last = -1;

         @Override
         public boolean hasNext() {
            return this.next < LongMap.this.keys.length;
         }

         @Override
         public V next() {
            if (this.next >= LongMap.this.keys.length) {
               throw new NoSuchElementException();
            }

            this.last = this.next;
            this.next = LongMap.this.advance(this.next + 1);
            return (V)LongMap.this.valueAt(this.last);
         }

         @Override
         public void remove() {
            LongMap.this.removeAt(this.last);
         }
      };
   }

   public void removeIf(Predicate<V> var1) {
      for (int var2 = 0; var2 < this.keys.length; var2++) {
         V var3 = this.valueAt(var2);
         if (var3 != null && var1.test(var3)) {
            this.removeAt(var2);
         }
      }
   }

   private int advance(int var1) {
      while (var1 < this.keys.length && this.valueAt(var1) == null) {
         var1++;
      }

      return var1;
   }

   public V get(long var1) {
      int var3 = this.index(var1);

      while (true) {
         Object var4 = this.values[var3];
         if (var4 == null) {
            return null;
         }

         if (var4 != TOMBSTONE && this.keys[var3] == var1) {
            return (V)var4;
         }

         var3 = var3 + 1 & this.mask;
      }
   }

   public V put(long var1, V var3) {
      if (var3 == null) {
         throw new IllegalArgumentException("null value");
      }

      int var4 = -1;
      int var5 = this.index(var1);

      while (true) {
         Object var6 = this.values[var5];
         if (var6 == null) {
            if (var4 < 0) {
               var4 = var5;
               this.used++;
            }

            this.keys[var4] = var1;
            this.values[var4] = var3;
            this.size++;
            if (this.used * 2 > this.keys.length) {
               this.rehash(this.size * 2 > this.keys.length / 2 ? this.keys.length * 2 : this.keys.length);
            }

            return null;
         }

         if (var6 == TOMBSTONE) {
            var4 = var4 < 0 ? var5 : var4;
         } else if (this.keys[var5] == var1) {
            this.values[var5] = var3;
            return (V)var6;
         }

         var5 = var5 + 1 & this.mask;
      }
   }

   public V remove(long var1) {
      int var3 = this.index(var1);

      while (true) {
         Object var4 = this.values[var3];
         if (var4 == null) {
            return null;
         }

         if (var4 != TOMBSTONE && this.keys[var3] == var1) {
            this.values[var3] = TOMBSTONE;
            this.size--;
            return (V)var4;
         }

         var3 = var3 + 1 & this.mask;
      }
   }

   public void clear() {
      Arrays.fill(this.values, null);
      this.size = this.used = 0;
   }

   public int capacity() {
      return this.keys.length;
   }

   public V valueAt(int var1) {
      Object var2 = this.values[var1];
      return (V)(var2 == TOMBSTONE ? null : var2);
   }

   public long keyAt(int var1) {
      return this.keys[var1];
   }

   public void removeAt(int var1) {
      if (this.values[var1] != null && this.values[var1] != TOMBSTONE) {
         this.values[var1] = TOMBSTONE;
         this.size--;
      }
   }

   private int index(long var1) {
      long var3 = var1;
      var3 ^= var3 >>> 33;
      var3 *= -49064778989728563L;
      var3 ^= var3 >>> 33;
      var3 *= -4265267296055464877L;
      var3 ^= var3 >>> 33;
      return (int)var3 & this.mask;
   }

   private void rehash(int var1) {
      long[] var2 = this.keys;
      Object[] var3 = this.values;
      this.keys = new long[var1];
      this.values = new Object[var1];
      this.mask = var1 - 1;
      this.size = this.used = 0;

      for (int var4 = 0; var4 < var2.length; var4++) {
         if (var3[var4] != null && var3[var4] != TOMBSTONE) {
            this.insertFresh(var2[var4], var3[var4]);
         }
      }
   }

   private void insertFresh(long var1, Object var3) {
      int var4 = this.index(var1);

      while (this.values[var4] != null) {
         var4 = var4 + 1 & this.mask;
      }

      this.keys[var4] = var1;
      this.values[var4] = var3;
      this.size++;
      this.used++;
   }
}