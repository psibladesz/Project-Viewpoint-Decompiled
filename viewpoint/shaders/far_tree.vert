#version 330










#include "constants.glsl"
#include "lib/far_relief.glsl"
uniform usamplerBuffer uTrees;
uniform samplerBuffer uKinds;
uniform mat4 uViewProjection;
uniform vec3 uOrigin;
uniform vec3 uEye;
uniform float uGrow;
uniform float uPixel;
uniform vec2 uFade;
uniform vec2 uTreeFade;
uniform vec2 uCellWorld;
uniform vec2 uTreeVariation;
uniform float uDisplays[100];
uniform float uBareCrown;
flat out vec2 vFade;
flat out float vCover;
flat out float vKeep;
out vec3 vPos;
out vec2 vCorner;
flat out vec2 vLocal;
flat out vec3 vColour;
const float MIN_PIXELS = 2.0;
const int KIND_TEXELS = 6;
const uint TURN_SHIFT = 8u;
const vec2 CORNERS[6] = vec2[6](vec2(-1.0, 0.0), vec2(1.0, 0.0), vec2(1.0, 1.0), vec2(-1.0, 0.0), vec2(1.0, 1.0), vec2(-1.0, 1.0));

vec2 squareNoise(uvec2 square) {
  uint h = (square.x * 0x27D4EB2Du) ^ (square.y * 0x165667B1u);
  h ^= h >> 15u;
  h *= 0x85EBCA6Bu;
  h ^= h >> 13u;
  return vec2(float(h & 0xFFFFu), float(h >> 16u)) / 32767.5 - 1.0;
}
void main() {
  vFade = uFade;
  uvec4 tree = texelFetch(uTrees, gl_VertexID / 6);
  int kind = int(tree.w);
  uint level = tree.z & ((1u << TURN_SHIFT) - 1u);
  int display = int(uDisplays[tree.z >> TURN_SHIFT]);
  vec2 stray = squareNoise(uvec2(ivec2(uCellWorld) + ivec2(tree.xy))) * uTreeVariation.x;
  vec4 look = texelFetch(uKinds, kind * KIND_TEXELS + 1 + display);
  vColour = look.rgb * (1.0 + stray.x) * vec3(1.0 + 0.5 * stray.y, 1.0, 1.0 - 0.5 * stray.y);
  vCover = mix(1.0, uBareCrown, look.a);
  vec2 size = texelFetch(uKinds, kind * KIND_TEXELS).xy;
  vLocal = vec2(tree.xy) + 0.5;
  vec3 foot = uOrigin + vec3(-vLocal.x, (float(level) + reliefCoarse(uCellWorld + vLocal)) * SQRT6, -vLocal.y);
  vKeep = clamp((uTreeFade.y - length(foot.xz - uEye.xz)) / max(uTreeFade.y - uTreeFade.x, 1.0), 0.0, 1.0);
  float grow = max(uGrow, MIN_PIXELS * 0.5 * uPixel * length(uEye - foot) / max(size.x, 0.1));
  vec2 toEye = uEye.xz - foot.xz;
  vec2 right = normalize(vec2(toEye.y, -toEye.x) + vec2(1.0e-6, 0.0));
  vCorner = CORNERS[gl_VertexID % 6];
  vec2 across = right * vCorner.x * size.x * grow;
  vPos = foot + vec3(across.x, vCorner.y * size.y * grow, across.y);
  gl_Position = uViewProjection * vec4(vPos, 1.0);
}
