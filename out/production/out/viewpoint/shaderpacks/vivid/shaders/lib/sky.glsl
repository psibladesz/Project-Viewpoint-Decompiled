




#ifndef VIVID_SKY
#define VIVID_SKY
#include "lib/common.glsl"


const float SKY_BRIGHTNESS = 1.6;

const float ZENITH_RISE = 2.4;
const float BELOW_FALL = 6.0;

const vec3 BELOW = vec3(0.55, 0.52, 0.48);


const float SUNSET_HEIGHT = 0.12;
const float SUNSET_GATHER = 3.0;
const float SUNSET_STRENGTH = 0.35;
const vec3 SUNSET_TINT = vec3(1.0, 0.72, 0.5);
const float SUNSET_GONE = 0.45;

const float GLARE_WIDE_G = 0.7;
const float GLARE_WIDE = 0.3;
const float GLARE_TIGHT_G = 0.9;
const float GLARE_TIGHT = 0.03;
vec3 skyBands(vec3 r, vec3 horizon, vec3 zenith) {
  if (r.y >= 0.0) return mix(horizon, zenith, 1.0 - exp(-r.y * ZENITH_RISE));
  return mix(horizon, horizon * BELOW, 1.0 - exp(r.y * BELOW_FALL));
}


vec3 skyColor(vec3 r, vec3 horizon, vec3 zenith, vec3 sunDir, vec3 sunColor, float glow) {
  vec3 sky = skyBands(r, horizon, zenith);
  float mu = dot(r, sunDir);
  sky += sunColor * glow * (GLARE_WIDE * hg(mu, GLARE_WIDE_G) + GLARE_TIGHT * hg(mu, GLARE_TIGHT_G));

  vec2 across = r.xz / max(length(r.xz), 1.0e-4), toSun = sunDir.xz / max(length(sunDir.xz), 1.0e-4);

  float side = pow(max(0.5 + 0.5 * dot(across, toSun), 0.0), SUNSET_GATHER);
  float low = 1.0 - smoothstep(0.0, SUNSET_GONE, sunDir.y);
  sky += sunColor * SUNSET_TINT * (SUNSET_STRENGTH * glow * low * side * exp(-abs(r.y) / SUNSET_HEIGHT));
  return sky;
}
#endif
