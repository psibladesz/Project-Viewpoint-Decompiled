package viewpoint.render;

public record PackData(float[] vertices, int[] indices, int artWidth, int artHeight, int width, int height, byte[][] levels) {
   public static final int VERTEX_FLOATS = 8;
   public static final int LEVELS = 4;
   public static final int ALIGN = 8;

   public long bytes() {
      long var1 = 0L;

      for (byte[] var6 : this.levels) {
         var1 += var6.length;
      }

      return this.vertices.length * 4L + this.indices.length * 4L + var1;
   }
}
