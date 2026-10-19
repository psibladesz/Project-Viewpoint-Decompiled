








#ifndef VIEWPOINT_AIR_POINT
#define VIEWPOINT_AIR_POINT
uniform float uFloorY;
uniform float uDensity;
uniform float uClearDensity;
uniform float uDust;
uniform float uDustSun;
uniform float uTime;
uniform vec3 uNoiseOrigin;
uniform sampler3D uNoise;
uniform int uLightCount;
uniform vec4 uLightPos[48];
uniform vec3 uLightColor[48];
uniform float uLightLevel[48];
uniform float uLampHalo;
uniform sampler2D uLampFields;

#ifndef OPTION_GODRAYS
#define OPTION_GODRAYS 1
#endif
#include "constants.glsl"
#include "lib/weather.glsl"
#include "lib/sun.glsl"
#include "lib/common.glsl"
#include "lib/spots_air.glsl"
#include "lib/air_sun.glsl"
#include "lib/floor_plan.glsl"
#include "lib/lamp_field.glsl"

const float LAMP_CORE = 0.08, LAMP_G = 0.2;
const float FOUR_PI = 12.566371;

const int SUN_AIR_STEPS = 32;

int storeyOf(vec3 p) {
  return planLevelAt(p);
}


float densityAt(vec3 p, float indoor) {
  float h = max(planHeightAt(p) - uSceneOrigin.y - uFloorY, 0.0);
  float d = mix(uDensity, uClearDensity, indoor) * (0.35 + 0.65 * exp(-h / 10.0));
  float n = textureLod(uNoise, (p + uNoiseOrigin + vec3(uWind.x, 0.0, uWind.y) * uTime * 0.25) / 48.0, 0.0).r;
  return d * mix(0.5, 1.5, n) * (1.0 + indoor);
}


vec3 lampsAir(vec3 p, vec3 dir) {
  int storey = storeyOf(p);
  vec3 sum = vec3(0.0);
  for (int i = 0; i < uLightCount; i++) {
    if (int(uLightLevel[i]) != storey) continue;
    vec3 m = p - uLightPos[i].xyz;
    float squared = dot(m, m), radius = uLightPos[i].w;
    if (squared >= radius * radius) continue;
    if (planIndoorsAt(uLightPos[i].xyz) && !planInteriorAt(uLightPos[i].xyz)) continue;
    float d = sqrt(squared), reach = 1.0 - smoothstep(0.3 * radius, radius, d);
    vec2 texel = lampFieldTexel(i, -(uSceneOrigin.xz + p.xz));
    float through = texture(uLampFields, texel / vec2(textureSize(uLampFields, 0))).r;
    float phase = FOUR_PI * hg(-dot(m / max(d, 1.0e-4), dir), LAMP_G);
    sum += uLightColor[i] * (reach * through * phase / (squared + LAMP_CORE));
  }
  return sum * uLampHalo;
}



vec4 airAt(vec3 p, vec3 dir) {
  float indoor = planInteriorAt(p) ? 1.0 : 0.0;
  return vec4(spotsAir(p, dir) * mix(1.0, uDust, indoor) + lampsAir(p, dir), densityAt(p, indoor));
}



vec3 sunAirAt(vec3 p, vec3 dir, float indoor) {
#if OPTION_GODRAYS && PIPELINE_SHADOWS
  float sun = uSunStrength > 0.0 ? sunAt(p) : 0.0;
#else
  float sun = 0.0;
#endif
  return airSunLight(dir) * sun * mix(1.0, uDust * uDustSun, indoor);
}



vec3 sunAirMarch(vec3 dir, float d, float jitter) {
  if (uSunStrength <= 0.0 || d <= 0.0) return vec3(0.0);
  vec3 light = vec3(0.0);
  float through = 1.0, last = 0.0;
  for (int i = 0; i < SUN_AIR_STEPS; i++) {
    float u = (float(i) + jitter) / float(SUN_AIR_STEPS), at = d * u * u;
    float step = d * (float(i) + 1.0) * (float(i) + 1.0) / float(SUN_AIR_STEPS * SUN_AIR_STEPS) - last;
    vec3 p = uEye + dir * at;
    float indoor = planInteriorAt(p) ? 1.0 : 0.0;
    float t = exp(-densityAt(p, indoor) * step);
    light += through * sunAirAt(p, dir, indoor) * (1.0 - t);
    through *= t;
    last += step;
  }
  return light;
}
#endif
