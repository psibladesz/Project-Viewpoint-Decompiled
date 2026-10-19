




#ifndef OPTION_FACE_SHADING
#define OPTION_FACE_SHADING 1.0
#endif
uniform vec3 uFogColor;

vec3 lamps(vec3 pos, vec3 n) {
  return vec3(0.0);
}
#include "lib/torches.glsl"

float faceShade(vec3 n) {
  vec3 a = abs(n);
  float side = (a.x * 0.6 + a.z * 0.8) / max(a.x + a.z, 1.0e-4);
  float shade = n.y >= 0.0 ? mix(side, 1.0, n.y) : mix(side, 0.5, -n.y);
  return mix(1.0, shade, OPTION_FACE_SHADING);
}
vec3 shade(vec3 albedo, vec4 light, vec3 n, float lit) {
  return albedo * light.rgb * faceShade(n);
}
