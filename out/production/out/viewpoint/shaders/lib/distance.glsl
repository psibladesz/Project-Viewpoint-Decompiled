




#ifndef VIEWPOINT_DISTANCE
#define VIEWPOINT_DISTANCE
#include "lib/fog.glsl"
#include "lib/fog_amount.glsl"




float distanceShare(vec3 pos, float d, float fogDepth, vec2 fogRange, vec2 haze) {
  return max(fogAt(fogDepth, fogRange), hazeAt(d, haze));
}



vec3 distanceFog(vec3 color, vec3 pos, vec3 view, float d, float fogDepth, vec2 fogRange, vec2 haze, vec3 horizon) {
  return mix(color, fogColor(horizon, view), distanceShare(pos, d, fogDepth, fogRange, haze));
}
#endif
