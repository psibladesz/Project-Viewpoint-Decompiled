











#ifndef VIEWPOINT_LAMP_SHADOW
#define VIEWPOINT_LAMP_SHADOW
#include "constants.glsl"
#include "lib/cube_shadow.glsl"
#if PIPELINE_LAMP_SHADOWS
uniform int uLampShadowCount;
uniform vec4 uLampShadowSlot[PIPELINE_LAMP_SHADOWS];
uniform float uLampShadowFloor;
uniform sampler2DArrayShadow uLampShadow;
uniform float uLightRise[48];
uniform float uLightOutdoors[48];

const float LAMP_LEVEL_COST = 3.0;

const float LAMP_FIELD_OFF = 0.05;
uniform sampler2D uLampFields;
#include "lib/floor_plan.glsl"
#include "lib/lamp_field.glsl"
#endif

float lampShadow(int i, vec3 pos, vec3 n) {
#if PIPELINE_LAMP_SHADOWS
  if (i >= uLampShadowCount) return 1.0;
  vec4 slot = uLampShadowSlot[i];
  vec3 v = pos - uLightPos[i].xyz;
  float m = max(abs(v.x), max(abs(v.y), abs(v.z)));

  v += n * (0.02 + 3.0 * m / float(PIPELINE_LAMP_SHADOW_RESOLUTION));
  int face;
  vec2 uv;
  float depth;
  if (!cubeTexel(v, slot.z, slot.w, face, uv, depth)) return 1.0;
  float texel = 1.0 / float(PIPELINE_LAMP_SHADOW_RESOLUTION), lit = 0.0;
  for (int k = 0; k < 4; k++) {
    lit += texture(uLampShadow, vec4(uv + (vec2(k & 1, k >> 1) - 0.5) * texel, slot.x + float(face), depth));
  }
  return mix(1.0, lit * 0.25, slot.y);
#else
  return 1.0;
#endif
}

float lampStoreys(int i, vec3 pos) {
#if PIPELINE_LAMP_SHADOWS
  return abs(floor((pos.y - uLightPos[i].y + uLightRise[i]) / SQRT6 + 0.02));
#else
  return 0.0;
#endif
}

float lampField(int i, vec3 pos, vec3 n) {
#if PIPELINE_LAMP_SHADOWS
  if (lampStoreys(i, pos) > 0.0) return 1.0;
  vec3 p = pos + n * LAMP_FIELD_OFF;
  vec2 texel = lampFieldTexel(i, -(uSceneOrigin.xz + p.xz));
  return texture(uLampFields, texel / vec2(textureSize(uLampFields, 0))).r;
#else
  return 1.0;
#endif
}

float lampVisibility(int i, vec3 pos, vec3 n) {
  return min(lampField(i, pos, n), lampShadow(i, pos, n));
}


vec2 lampShadowParts(vec3 light, vec3 pos, vec3 n, float outdoors) {
#if PIPELINE_LAMP_SHADOWS
  float grids = max(light.r, max(light.g, light.b));
  if (grids <= 0.0 || uLightCount == 0) return vec2(0.0, 1.0);
  float total = 0.0, through = 0.0;
  for (int i = 0; i < uLightCount; i++) {
    if ((uLightOutdoors[i] > 0.5) != (outdoors > 0.5)) continue;
    vec3 d = pos - uLightPos[i].xyz;
    float cost = length(d.xz) + LAMP_LEVEL_COST * lampStoreys(i, pos), radius = uLightPos[i].w;
    if (cost >= radius) continue;
    vec3 c = uLightColor[i];
    float e = max(c.r, max(c.g, c.b)) * (1.0 - cost / radius);
    total += e;
    through += e * max(lampVisibility(i, pos, n), uLampShadowFloor);
  }
  return total > 0.0 ? vec2(min(total / grids, 1.0), through / total) : vec2(0.0, 1.0);
#else
  return vec2(0.0, 1.0);
#endif
}

float lampShadowKeep(vec3 light, vec3 pos, vec3 n, float outdoors) {
  vec2 parts = lampShadowParts(light, pos, n, outdoors);
  return 1.0 - parts.x * (1.0 - parts.y);
}
vec3 lampShadowed(vec3 light, vec3 pos, vec3 n, float outdoors) {
  return light * lampShadowKeep(light, pos, n, outdoors);
}
#endif
