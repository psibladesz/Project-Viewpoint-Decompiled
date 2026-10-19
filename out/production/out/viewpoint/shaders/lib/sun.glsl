











uniform sampler2DShadow uShadowMap0;
uniform sampler2DShadow uShadowMap1;
uniform sampler2DShadow uShadowMap2;
uniform sampler2DShadow uShadowMap3;
uniform sampler2DShadow uShadowMap4;
uniform mat4 uShadowMatrix[5];


uniform vec4 uShadowMaps[5];
uniform vec3 uSunDir;
uniform vec3 uSunColor;
uniform float uSunStrength;
uniform float uShadowsOff;
#include "lib/cloud_shadow.glsl"
const int SUN_MAPS = 5, FIRST_FAR_MAP = 3;

bool sunMap(int c) {
#if !PIPELINE_SHADOWS
  if (c < FIRST_FAR_MAP) return false;
#endif
#if !PIPELINE_FAR_SHADOWS
  if (c >= FIRST_FAR_MAP) return false;
#endif
  return uShadowMaps[c].x > 0.0;
}
float shadowTap(int c, vec3 sc) {
  if (c == 0) return texture(uShadowMap0, sc);
  if (c == 1) return texture(uShadowMap1, sc);
  if (c == 2) return texture(uShadowMap2, sc);
  if (c == 3) return texture(uShadowMap3, sc);
  return texture(uShadowMap4, sc);
}
#include "lib/sun_filter.glsl"



float cascadeLit(int c, vec3 pos, vec3 n, float grazing) {
  float texel = uShadowMaps[c].x;
  vec4 sc = uShadowMatrix[c] * vec4(pos + n * (0.01 + texel * (1.5 + 4.0 * grazing)), 1.0);
  float z = sc.z - (0.01 + texel * (1.0 + grazing)) / uShadowMaps[c].y;
  if (z >= 1.0) return 1.0;
  return sunFilter(c, sc.xy, z);
}

const float BLEND_START = 0.8;


float servedEdge(int c, vec3 pos) {
  return uShadowMaps[c].w > 0.0 ? max(abs(pos.x), abs(pos.z)) / uShadowMaps[c].w : 0.0;
}

float cascadeEdge(int c, vec3 pos) {
  vec2 d = abs((uShadowMatrix[c] * vec4(pos, 1.0)).xy * 2.0 - 1.0);
  return max(max(d.x, d.y), servedEdge(c, pos));
}


float nextMapLit(int c, vec3 pos, vec3 n, float grazing) {
  for (int m = c + 1; m < SUN_MAPS; m++) {
    if (sunMap(m) && cascadeEdge(m, pos) < 1.0 && servedEdge(m, pos) < BLEND_START) {
      return cascadeLit(m, pos, n, grazing);
    }
  }
  return 1.0;
}
float sunVisibility(vec3 pos, vec3 n, float facing) {
  float grazing = 1.0 - facing;
  for (int c = 0; c < SUN_MAPS; c++) {
    if (!sunMap(c)) continue;
    float e = cascadeEdge(c, pos);
    if (e < 0.985) {
      float lit = cascadeLit(c, pos, n, grazing);
      float blend = smoothstep(BLEND_START, 0.985, e);
      if (blend > 0.0) lit = mix(lit, nextMapLit(c, pos, n, grazing), blend);
      return lit;
    }
  }
  return 1.0;
}

float sunAt(vec3 p) {
  float clouds = cloudShadow(p);
  for (int c = 0; c < SUN_MAPS; c++) {
    if (!sunMap(c)) continue;
    vec4 sc = uShadowMatrix[c] * vec4(p, 1.0);
    vec2 d = abs(sc.xy * 2.0 - 1.0);
    if (max(max(d.x, d.y), servedEdge(c, p)) < 0.98) {
      float z = sc.z - (0.05 + uShadowMaps[c].x * 2.0) / uShadowMaps[c].y;
      return clouds * (z >= 1.0 ? 1.0 : shadowTap(c, vec3(sc.xy, z)));
    }
  }
  return clouds;
}
float sunlit(vec3 pos, vec3 n, vec3 eye, bool foliage) {
  float facing = dot(n, uSunDir);
  if (uSunStrength <= 0.0 || facing <= 0.0) return 0.0;
#if PIPELINE_SHADOWS || PIPELINE_FAR_SHADOWS
  float lit = uShadowsOff > 0.5 ? 1.0 : sunVisibility(pos, n, facing);
#else
  float lit = 1.0;
#endif
  return lit * smoothstep(0.0, 0.3, facing) * cloudShadow(pos);
}



vec3 sunMapsView(vec3 pos, vec3 n) {
  const vec3 COLOURS[SUN_MAPS] = vec3[SUN_MAPS](vec3(1.0, 0.3, 0.3), vec3(1.0, 0.85, 0.3), vec3(0.35, 1.0, 0.35),
                                                vec3(0.3, 0.9, 1.0), vec3(0.4, 0.45, 1.0));
  float facing = dot(n, uSunDir);
  if (uSunStrength <= 0.0 || facing <= 0.0) return vec3(0.03);
  for (int c = 0; c < SUN_MAPS; c++) {
    if (sunMap(c) && cascadeEdge(c, pos) < 0.985) return COLOURS[c] * (0.15 + 0.85 * cascadeLit(c, pos, n, 1.0 - facing));
  }
  return vec3(0.5);
}
