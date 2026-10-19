












#ifndef DEFAULT_DISTANCE
#define DEFAULT_DISTANCE
#include "lib/fog.glsl"
#include "lib/fog_amount.glsl"
#include "lib/mist.glsl"

const float DISTANCE_KM = 0.001;


vec3 airThrough(float d) {
  vec3 ground = AIR_RAYLEIGH * exp(-AIR_GROUND / AIR_RAYLEIGH_HEIGHT)
      + vec3(AIR_MIE_EXTINCTION * exp(-AIR_GROUND / AIR_MIE_HEIGHT));
  return exp(-ground * d * DISTANCE_KM);
}


vec3 airHorizon(vec3 horizon, vec3 view) {
  vec2 across = view.xz;
  float len = length(across);
  return fogColor(horizon, len > 1.0e-4 ? vec3(across.x / len, 0.0, across.y / len) : vec3(1.0, 0.0, 0.0));
}

float distanceShare(vec3 pos, float d, float fogDepth, vec2 fogRange, vec2 haze) {
  vec3 view = (pos - uEye) / max(d, 1.0e-4);
  float air = dot(airThrough(d), vec3(0.2126, 0.7152, 0.0722));
  float mist = exp(-mistDepth(mistHeight(uEye.y), view.y, d) * mistOpen(uEye, pos));
  return max(1.0 - air * mist, fogAt(fogDepth, fogRange));
}

vec3 distanceFog(vec3 color, vec3 pos, vec3 view, float d, float fogDepth, vec2 fogRange, vec2 haze, vec3 horizon) {
  float h0 = mistHeight(uEye.y);
  vec3 air = airThrough(d);
  float mist = exp(-mistDepth(h0, view.y, d) * mistOpen(uEye, pos));
  vec3 sky = airHorizon(horizon, view);
  vec3 seen = (color * air + sky * (1.0 - air)) * mist + mistLight(view, uSkySun, sky) * (1.0 - mist);
  return mix(seen, mistedBeyond(fogColor(horizon, view), view, h0, uSkySun), fogAt(fogDepth, fogRange));
}
#endif
