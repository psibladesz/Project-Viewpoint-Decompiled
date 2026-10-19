





#include "lib/clouds.glsl"


const int SHADOW_SAMPLES = 4;
const float SHADOW_FOOTPRINT = 40.0;
const float SHADOW_EXTINCTION = 0.02;

const float SHADOW_FLOOR = 0.25;
float cloudShadowRay(vec3 pos) {
#if OPTION_CLOUD_STYLE == OPTION_CLOUD_STYLE_LAYERED
  return 1.0;
#else
  float alt = pos.y + uSceneOrigin.y;

  vec3 middle = pos + uSunDir * ((KIND_SPAN[2].x + KIND_SPAN[2].y) * 0.5 - alt) / uSunDir.y;
  vec4 w = weatherAt(middle);
  vec2 span, shape;
  kindOf(w.b, w.a, span, shape);
  float lowest = lowestBase(span), stride = (span.y - lowest) / float(SHADOW_SAMPLES), depth = 0.0;
  for (int i = 0; i < SHADOW_SAMPLES; i++) {
    float height = lowest + (float(i) + 0.5) * stride;
    vec3 at = pos + uSunDir * ((height - alt) / uSunDir.y);
    depth += lowDensity(at, height, lowWeather(at, height), SHADOW_FOOTPRINT, 0.0) * stride;
  }
  return mix(SHADOW_FLOOR, 1.0, exp(-depth / uSunDir.y * SHADOW_EXTINCTION));
#endif
}
