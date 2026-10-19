package viewpoint.upscale;

import org.lwjgl.system.MemoryUtil;

final class NativeStruct implements AutoCloseable {
   final long address;
   private final int size;

   NativeStruct(int var1) {
      this.size = var1;
      this.address = MemoryUtil.nmemCallocChecked(1L, var1);
   }

   NativeStruct u64(int var1, long var2) {
      MemoryUtil.memPutLong(this.at(var1, 8), var2);
      return this;
   }

   NativeStruct u32(int var1, int var2) {
      MemoryUtil.memPutInt(this.at(var1, 4), var2);
      return this;
   }

   NativeStruct f32(int var1, float var2) {
      MemoryUtil.memPutFloat(this.at(var1, 4), var2);
      return this;
   }

   NativeStruct bool(int var1, boolean var2) {
      MemoryUtil.memPutByte(this.at(var1, 1), (byte)(var2 ? 1 : 0));
      return this;
   }

   long getU64(int var1) {
      return MemoryUtil.memGetLong(this.at(var1, 8));
   }

   int getU32(int var1) {
      return MemoryUtil.memGetInt(this.at(var1, 4));
   }

   float getF32(int var1) {
      return MemoryUtil.memGetFloat(this.at(var1, 4));
   }

   static void header(long var0, int var2, int var3, int var4, long var5, int var7) {
      MemoryUtil.memPutLong(var0, 0L);
      MemoryUtil.memPutInt(var0 + 8L, var2);
      MemoryUtil.memPutShort(var0 + 12L, (short)var3);
      MemoryUtil.memPutShort(var0 + 14L, (short)var4);
      MemoryUtil.memPutLong(var0 + 16L, Long.reverseBytes(var5));
      MemoryUtil.memPutLong(var0 + 24L, var7);
   }

   static NativeStruct streamline(int var0, int var1, int var2, int var3, long var4, int var6) {
      NativeStruct var7 = new NativeStruct(var0);
      header(var7.address, var1, var2, var3, var4, var6);
      return var7;
   }

   void clear() {
      MemoryUtil.memSet(this.address, 0, this.size);
   }

   @Override
   public void close() {
      MemoryUtil.nmemFree(this.address);
   }

   private long at(int var1, int var2) {
      if (var1 >= 0 && var1 + var2 <= this.size) {
         return this.address + var1;
      } else {
         throw new IndexOutOfBoundsException("field at " + var1 + " of " + var2 + " bytes in a " + this.size + "-byte struct");
      }
   }
}
