package viewpoint.far;

import java.nio.ByteBuffer;
import java.util.zip.CRC32C;
import viewpoint.platform.Tuning;

record ReliefRules(
   int smoothingSquares,
   int townSmoothingSquares,
   float height,
   float crossSlope,
   float grade,
   float gradeChange,
   float bank,
   float padMargin,
   float driveway,
   float freeboard,
   float creekFall,
   float townSlope,
   int lotSquares
) {
   static final float GRADE_SPAN = 8.0F;
   static final ReliefRules DEFAULTS = new ReliefRules(32, 176, 0.5F, 0.02F, 0.08F, 0.01F, 0.5F, 2.0F, 0.12F, 0.5F, 0.02F, 0.03F, 16);

   ReliefRules smoothed(int var1) {
      return new ReliefRules(
         var1,
         this.townSmoothingSquares,
         this.height,
         this.crossSlope,
         this.grade,
         this.gradeChange,
         this.bank,
         this.padMargin,
         this.driveway,
         this.freeboard,
         this.creekFall,
         this.townSlope,
         this.lotSquares
      );
   }

   long key() {
      CRC32C var1 = new CRC32C();
      ByteBuffer var2 = ByteBuffer.allocate(52)
         .putInt(this.smoothingSquares)
         .putInt(this.townSmoothingSquares)
         .putFloat(this.height)
         .putFloat(this.crossSlope)
         .putFloat(this.grade)
         .putFloat(this.gradeChange)
         .putFloat(this.bank)
         .putFloat(this.padMargin)
         .putFloat(this.driveway)
         .putFloat(this.freeboard)
         .putFloat(this.creekFall)
         .putFloat(this.townSlope)
         .putInt(this.lotSquares);
      var1.update(var2.flip());
      return var1.getValue();
   }

   float metresASquare(float var1) {
      return var1 / (Tuning.reliefSquaresPerMetre * this.height);
   }
}
