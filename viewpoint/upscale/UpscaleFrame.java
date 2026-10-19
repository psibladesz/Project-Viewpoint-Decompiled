package viewpoint.upscale;

public record UpscaleFrame(
   float jitterX,
   float jitterY,
   float motionScaleX,
   float motionScaleY,
   boolean reset,
   float frameMs,
   float near,
   float far,
   float fovY,
   float metresPerUnit,
   float[] matrices,
   float[] camera
) {
}
