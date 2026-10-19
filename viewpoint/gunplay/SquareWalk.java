package viewpoint.gunplay;

final class SquareWalk {
   static final int NONE = 0;
   static final int WEST = 1;
   static final int NORTH = 2;
   int x;
   int y;
   double in;
   double out;
   int edge;
   int edgeX;
   int edgeY;
   private int stepX;
   private int stepY;
   private double nextX;
   private double nextY;
   private double deltaX;
   private double deltaY;
   private double end;
   private boolean first;

   void start(double var1, double var3, double var5, double var7, double var9, double var11) {
      double var13 = var1 + var5 * var9;
      double var15 = var3 + var7 * var9;
      this.x = (int)Math.floor(var13);
      this.y = (int)Math.floor(var15);
      this.stepX = var5 > 0.0 ? 1 : -1;
      this.stepY = var7 > 0.0 ? 1 : -1;
      this.deltaX = var5 == 0.0 ? Double.POSITIVE_INFINITY : 1.0 / Math.abs(var5);
      this.deltaY = var7 == 0.0 ? Double.POSITIVE_INFINITY : 1.0 / Math.abs(var7);
      this.nextX = var5 == 0.0 ? Double.POSITIVE_INFINITY : var9 + ((var5 > 0.0 ? this.x + 1 : this.x) - var13) / var5;
      this.nextY = var7 == 0.0 ? Double.POSITIVE_INFINITY : var9 + ((var7 > 0.0 ? this.y + 1 : this.y) - var15) / var7;
      this.in = var9;
      this.out = var9;
      this.end = var11;
      this.edge = 0;
      this.first = true;
   }

   boolean next() {
      if (this.first) {
         this.first = false;
         this.out = Math.min(this.end, Math.min(this.nextX, this.nextY));
         return true;
      }

      if (this.out >= this.end) {
         return false;
      }

      if (this.nextX < this.nextY) {
         int var1 = this.x;
         this.x = this.x + this.stepX;
         this.edge = 1;
         this.edgeX = Math.max(var1, this.x);
         this.edgeY = this.y;
         this.in = this.nextX;
         this.nextX = this.nextX + this.deltaX;
      } else {
         int var2 = this.y;
         this.y = this.y + this.stepY;
         this.edge = 2;
         this.edgeX = this.x;
         this.edgeY = Math.max(var2, this.y);
         this.in = this.nextY;
         this.nextY = this.nextY + this.deltaY;
      }

      this.out = Math.min(this.end, Math.min(this.nextX, this.nextY));
      return true;
   }
}
