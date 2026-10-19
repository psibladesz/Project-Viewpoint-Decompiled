

#ifndef VIEWPOINT_SPOTS_AIR
#define VIEWPOINT_SPOTS_AIR
#include "lib/common.glsl"
#include "lib/spots.glsl"
uniform float uSpotAirOn;



vec3 spotAir(Spot s, vec3 p, vec3 dir) {
  vec3 d = p - s.pos;
  float dist = length(d);
  if (dist >= s.reach) return vec3(0.0);
  vec3 l = d / max(dist, 1.0e-4);
  float beam = spotBeam(s, l);
  if (beam <= 0.0) return vec3(0.0);
  float mu = -dot(l, dir);
  return s.colour * s.air * beam * spotFalloff(s, dist) * mix(hg(mu, 0.2), hg(mu, 0.65), 0.4)
    * spotShadow(s, p, vec3(0.0), dist, false);
}

vec3 spotsAir(vec3 p, vec3 dir) {
  if (uSpotAirOn < 0.5) return vec3(0.0);
  vec3 sum = vec3(0.0);
  for (int i = 0; i < uSpotCount; i++) sum += spotAir(spotAt(i), p, dir);
  return sum;
}
#endif
