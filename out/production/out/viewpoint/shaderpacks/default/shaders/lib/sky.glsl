







#ifndef VIVID_SKY
#define VIVID_SKY
#include "lib/common.glsl"
#include "lib/atmosphere.glsl"
uniform vec3 uSkyBaseZenith;
uniform vec3 uSkyBaseHorizon;
uniform vec3 uSunTrue;
uniform float uOvercast;


const float SKY_BRIGHTNESS = 1.6;

const float ZENITH_RISE = 2.4;
const float BELOW_FALL = 6.0;

const vec3 BELOW = vec3(0.55, 0.52, 0.48);

const float GLARE_WIDE_G = 0.7;
const float GLARE_WIDE = 0.3;
const float GLARE_TIGHT_G = 0.9;
const float GLARE_TIGHT = 0.03;
vec3 skyBands(vec3 r, vec3 horizon, vec3 zenith) {
  if (r.y >= 0.0) return mix(horizon, zenith, 1.0 - exp(-r.y * ZENITH_RISE));
  return mix(horizon, horizon * BELOW, 1.0 - exp(r.y * BELOW_FALL));
}

vec3 skyPlain(vec3 r) {
  return skyBands(r, uSkyBaseHorizon, uSkyBaseZenith) + airSky(r, uSunTrue) * (1.0 - uOvercast);
}


vec3 skyColor(vec3 r, vec3 horizon, vec3 zenith, vec3 sunDir, vec3 sunColor, float glow) {

  float moon = dot(sunDir, uSunTrue) < 0.9999 ? 1.0 : 0.0, mu = dot(r, sunDir);
  vec3 glare = sunColor * glow * (GLARE_WIDE * hg(mu, GLARE_WIDE_G) + GLARE_TIGHT * hg(mu, GLARE_TIGHT_G));
  return skyPlain(r) + moon * glare;
}
#endif
