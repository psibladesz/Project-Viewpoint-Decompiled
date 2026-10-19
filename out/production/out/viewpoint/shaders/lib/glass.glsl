




#include "constants.glsl"
uniform int uGlassFar;
uniform vec4 uGlassRange;
uniform vec3 uGlassEye;
uniform vec3 uGlassOrigin;
float outsideOf(float v, float lo, float hi) { return max(max(lo - v, v - hi), 0.0); }

float glassFar(vec3 pos, vec3 normal) {
  if (uGlassFar == 0) return 0.0;
  vec3 p = pos + uGlassOrigin, e = uGlassEye + uGlassOrigin;
  bool north = abs(normal.z) > abs(normal.x);
  float along = floor(north ? p.x : p.z), plane = round(north ? p.z : p.x);
  float storey = floor(p.y / SQRT6) * SQRT6;
  float dt = outsideOf(north ? e.x : e.z, along, along + 1.0);
  float dh = outsideOf(e.y, storey, storey + SQRT6);
  float dn = plane - (north ? e.z : e.x);
  float byDistance = smoothstep(uGlassRange.x, uGlassRange.y, sqrt(dt * dt + dh * dh + dn * dn));
  return max(byDistance, smoothstep(uGlassRange.z - 1.0, uGlassRange.z, dh / SQRT6));
}

bool glassSolid(int flags, vec3 pos, vec3 normal) {
  return (flags & FLAG_GLASS) != 0 && glassFar(pos, normal) >= 1.0;
}
