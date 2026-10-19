







#ifndef VIEWPOINT_SPOTS
#define VIEWPOINT_SPOTS
#include "constants.glsl"
uniform int uSpotCount;
uniform sampler2D uSpots;
uniform mat4 uSpotMaps[SPOT_MAPS];
uniform float uSpotTexel[SPOT_MAPS];
uniform float uSpotLayer[SPOT_MAPS];
uniform vec4 uSpotTiles[SPOT_MAPS];
uniform sampler2DArrayShadow uSpotShadow;
uniform float uLensGlow;
struct Spot {
  vec3 pos;
  float reach;
  vec3 axis;
  int kind;
  vec3 colour;
  int map;
  vec3 up;
  float cone;
  vec3 facing;
  float lens;
  float hot;
  float wear;
  float air;
  bool outside;
};


float spotOpen = 1.0;
Spot spotAt(int i) {
  vec4 t0 = texelFetch(uSpots, ivec2(0, i), 0), t1 = texelFetch(uSpots, ivec2(1, i), 0);
  vec4 t2 = texelFetch(uSpots, ivec2(2, i), 0), t3 = texelFetch(uSpots, ivec2(3, i), 0);
  vec4 t4 = texelFetch(uSpots, ivec2(4, i), 0), t5 = texelFetch(uSpots, ivec2(5, i), 0);
  return Spot(
      t0.xyz, t0.w, t1.xyz, int(t1.w), t2.rgb, int(t2.w), t3.xyz, t3.w, t4.xyz, t4.w, t5.x, t5.y, t5.z, t5.w > 0.5);
}
float torchBeam(Spot s, float c) {
  if (c <= s.cone) return 0.0;
  float a = acos(min(c, 1.0)), hot = s.hot;
  float rim = (a - hot * 1.15) / (hot * 0.3);
  float spill = smoothstep(s.cone, mix(s.cone, 1.0, 0.35), c);
  return exp(-a * a / (hot * hot)) + 0.1 * exp(-rim * rim) + 0.18 * spill;
}






float lowBeam(Spot s, vec3 l) {
  float along = dot(l, s.axis);
  if (along <= 0.05) return 0.0;
  float x = dot(l, cross(s.axis, s.up)) / along, y = dot(l, s.up) / along;
  float spread = s.cone * (1.0 + 0.5 * s.wear), blur = 1.0 + s.wear;
  if (abs(x) > 3.0 * spread || y < -0.7 || y > 0.25) return 0.0;
  vec2 h = vec2((x - 0.035) / (0.07 * blur), (y + 0.026) / (0.025 * blur));
  float hot = exp(-dot(h, h)) * (1.0 - 0.6 * s.wear);
  float wide = x / spread, low = (y + 0.03) / 0.06;
  float spill = exp(-wide * wide) * (0.2 * exp(-low * low) + 0.05 * smoothstep(-0.7, -0.15, y));
  float edge = 0.01 + 0.02 * clamp(x / 0.1, 0.0, 1.0);
  return (hot + spill) * mix(0.03, 1.0, 1.0 - smoothstep(edge - 0.01, edge + 0.02, y));
}

float rearBeam(Spot s, vec3 l) {
  return smoothstep(-0.3, 0.6, dot(l, s.axis));
}

float spotBeam(Spot s, vec3 l) {
  if (s.kind == SPOT_LOW_BEAM) return lowBeam(s, l);
  if (s.kind == SPOT_REAR) return rearBeam(s, l);
  return torchBeam(s, dot(l, s.axis));
}


float spotNose(Spot s, vec3 d) {
  if (s.kind == SPOT_TORCH) return 1.0;
  return smoothstep(0.1, 0.6, dot(d, s.axis)) * (s.outside ? smoothstep(0.0, 0.1, spotOpen) : 1.0);
}
float spotFalloff(Spot s, float d) {
  float x = d / s.reach;
  float window = clamp(1.0 - x * x * x * x, 0.0, 1.0);
  return window * window / (d * d + 0.25);
}
float spotShadow(Spot s, vec3 pos, vec3 n, float d, bool filtered) {
  if (s.map < 0) return 1.0;
  vec4 sc = uSpotMaps[s.map] * vec4(pos + n * (0.01 + uSpotTexel[s.map] * d * 2.0), 1.0);
  if (sc.w <= 0.0) return 0.0;
  sc.xyz /= sc.w;
  float layer = uSpotLayer[s.map];
  vec4 tile = uSpotTiles[s.map];
  if (!filtered) return texture(uSpotShadow, vec4(clamp(sc.xy, tile.xy, tile.zw), layer, sc.z));
  vec2 texel = 1.0 / vec2(textureSize(uSpotShadow, 0).xy);
  float lit = 0.0;
  for (int y = -1; y <= 1; y++)
    for (int x = -1; x <= 1; x++)
      lit += texture(uSpotShadow, vec4(clamp(sc.xy + vec2(x, y) * texel, tile.xy, tile.zw), layer, sc.z));
  return lit / 9.0;
}

vec3 spotLight(Spot s, vec3 pos, vec3 n, bool foliage) {
  vec3 d = pos - s.pos;
  float dist = length(d);
  if (dist >= s.reach) return vec3(0.0);
  vec3 l = d / max(dist, 1.0e-4);
  float beam = spotBeam(s, l);
  float facing = foliage ? 0.6 : dot(n, -l);
  if (beam <= 0.0 || facing <= 0.0) return vec3(0.0);
  return s.colour * beam * facing * spotFalloff(s, dist) * spotShadow(s, pos, foliage ? -l : n, dist, true)
      * spotNose(s, d);
}


float spotSeen(float c) {
  float ahead = max(c, 0.0);
  return 0.3 * smoothstep(-0.35, 0.05, c) + 0.7 * ahead * ahead * ahead * ahead;
}



float spotLens(vec3 pos, vec3 eye) {
  float nearest = 1.0e9;
  int found = -1;
  for (int i = 0; i < uSpotCount; i++) {
    float d = distance(pos, texelFetch(uSpots, ivec2(0, i), 0).xyz);
    if (d < nearest) {
      nearest = d;
      found = i;
    }
  }
  if (found < 0) return 1.0;
  Spot s = spotAt(found);
  if (s.kind == SPOT_TORCH || nearest > s.lens * 3.0 + 0.3) return 1.0;
  float shows = spotSeen(dot(normalize(eye - s.pos), s.facing));
  return 1.0 + (uLensGlow - 1.0) * shows * (s.kind == SPOT_REAR ? 0.2 : 1.0);
}

vec3 spots(vec3 pos, vec3 n, bool foliage) {
  vec3 sum = vec3(0.0);
  for (int i = 0; i < uSpotCount; i++) sum += spotLight(spotAt(i), pos, n, foliage);
  return sum;
}
#endif
