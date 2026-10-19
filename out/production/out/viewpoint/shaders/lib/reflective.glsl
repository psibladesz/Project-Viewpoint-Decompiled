


#ifndef VIEWPOINT_REFLECTIVE
#define VIEWPOINT_REFLECTIVE
#include "lib/materials.glsl"


bool reflects(vec2 uv, vec3 pos, vec4 albedo, vec4 normal, vec4 engine, out vec3 n, out float rough) {
  n = normalize(normal.xyz);
  rough = 0.0;
  if (isWater(albedo.a)) {
    rough = 0.05;
    return true;
  }
  if (isPaint(albedo.a)) {
    rough = 0.1;
    return true;
  }
  return false;
}
#endif
