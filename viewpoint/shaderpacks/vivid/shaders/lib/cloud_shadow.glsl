




#include "lib/cloud_blocks.glsl"


const int SHADOW_SAMPLES = 3;
const float SHADOW_FOOTPRINT = 24.0;
const float SHADOW_EXTINCTION = 0.007;

const float SHADOW_FLOOR = 0.3;
float cloudShadow(vec3 pos) {
#if !PIPELINE_CLOUDS
  return 1.0;
#else
  if (uSunDir.y <= 0.0) return 1.0;
#if OPTION_CLOUD_STYLE == OPTION_CLOUD_STYLE_LAYERED
  return blockShadow(pos);
#else
  vec2 layer = cloudLayer();
  float span = (layer.y - layer.x) / float(SHADOW_SAMPLES), density = 0.0;
  for (int i = 0; i < SHADOW_SAMPLES; i++) {
    float h = layer.x + (float(i) + 0.5) * span;
    density += cloudShape(pos + uSunDir * ((h - pos.y) / uSunDir.y), SHADOW_FOOTPRINT, false);
  }
  return mix(SHADOW_FLOOR, 1.0, exp(-density * span / uSunDir.y * SHADOW_EXTINCTION));
#endif
#endif
}
