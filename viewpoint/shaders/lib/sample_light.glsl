


#include "lib/light_grid.glsl"
#ifndef LIGHT_ORIGIN_GIVEN
flat in vec2 vLightOrigin;
#endif
vec4 sampleLight(vec3 local, vec3 n) {
  vec2 p = clamp(local.xz + vec2(-n.x, -n.z) * 0.3, -0.99, 8.99) + 1.0;
  return gridLight(vLightOrigin, p);
}

float sampleSky(vec3 local, vec3 n) {
  return gridSky(vLightOrigin, clamp(local.xz + vec2(-n.x, -n.z) * 0.3, -0.99, 8.99) + 1.0);
}

bool openSky(vec3 local) {
  return gridOpenSky(vLightOrigin, clamp(local.xz, -0.99, 8.99) + 1.0);
}
