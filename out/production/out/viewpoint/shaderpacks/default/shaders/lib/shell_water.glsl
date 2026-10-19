







#ifndef VIEWPOINT_SHELL_WATER
#define VIEWPOINT_SHELL_WATER
#include "lib/water_kinds.glsl"
#include "lib/sky.glsl"

const float SHELL_WATER_ABOVE = 0.5;

const float SHELL_WATER_F0 = 0.02;


bool farWaterAt(vec3 art, vec2 world) {
  if (art.b < art.g || art.b <= art.r + 12.0 / 255.0) return false;
  return relief(world) <= reliefWater(world) + SHELL_WATER_ABOVE * uReliefGrid.z;
}

bool shellWater(vec3 art, vec2 world) {
#if OPTION_WATER_REACH == OPTION_WATER_REACH_LOADED
  return false;
#else
  return farWaterAt(art, world);
#endif
}

bool farWater(vec3 art, vec2 world) {
#if OPTION_WATER_REACH == OPTION_WATER_REACH_ALL
  return farWaterAt(art, world);
#else
  return false;
#endif
}

vec3 shellWaterShade(vec3 shaded, vec3 white, vec3 horizon, vec3 pos, vec3 eye) {
  vec3 v = normalize(eye - pos);
  float m = 1.0 - max(v.y, 0.0), m2 = m * m, fresnel = SHELL_WATER_F0 + (1.0 - SHELL_WATER_F0) * m2 * m2 * m;
  WaterBody b = waterBody(uReliefGrid.x > 0.0 ? reliefFlow(reliefWorld(pos)).z : 2.0);
  vec3 sky = skyPlain(normalize(vec3(-v.x, max(v.y, 1.0e-3), -v.z))) * SKY_BRIGHTNESS;
  return mix(b.scatter * white, sky, fresnel);
}
#endif
