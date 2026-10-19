#version 330












layout(location = 0) in vec3 aPos;
layout(location = 1) in vec3 aNormal;
layout(location = 2) in vec4 aWeights;
layout(location = 3) in vec4 aBones;
layout(location = 4) in vec2 aUv;
layout(location = 5) in vec2 aUv2;
layout(location = 6) in int aDraw;
#include "constants.glsl"
#include "lib/far_relief.glsl"
uniform mat4 uViewProjection;
uniform samplerBuffer uPalettes;
uniform samplerBuffer uDraws;
#include "lib/edge_light.glsl"
uniform int uShadowPalette;
uniform int uMotionPass;
uniform mat4 uPlainViewProjection;
uniform mat4 uPrevViewProjection;
uniform vec3 uEye;
out vec2 vUv;
out vec2 vUv2;
out vec3 vNormal;
out vec3 vPos;
out vec4 vClip;
out vec4 vPrevClip;
flat out vec4 vTint;
flat out vec4 vLight;
flat out int vMotion;
flat out int vTexture;
flat out vec2 vFade;
flat out int vClear;
flat out int vGlass;
const int RECORD = 17, MATERIAL = 4, LIGHT = 5, UV = 7, PREVIOUS = 9, INDICES = 13, FADE = 14, STAND = 15, EDGE = 16, MOTION = 8,
          SELF = 64, SOLID = 128, GLASS = 1024, UPRIGHT = 1, PLACED = 2;

void skin(int palette, out vec4 r0, out vec4 r1, out vec4 r2) {
  r0 = vec4(0.0); r1 = vec4(0.0); r2 = vec4(0.0);
  for (int k = 0; k < 4; k++) {
    float w = aWeights[k];
    if (w > 0.0) {
      int at = (palette + int(aBones[k])) * 3;
      r0 += texelFetch(uPalettes, at) * w;
      r1 += texelFetch(uPalettes, at + 1) * w;
      r2 += texelFetch(uPalettes, at + 2) * w;
    }
  }
}
mat4 matrixAt(int at) {
  return mat4(texelFetch(uDraws, at), texelFetch(uDraws, at + 1), texelFetch(uDraws, at + 2), texelFetch(uDraws, at + 3));
}
void main() {
  int record = (aDraw & ((1 << MODEL_SLOT_SHIFT) - 1)) * RECORD;
  vTexture = aDraw >> MODEL_SLOT_SHIFT;
  vec4 indices = texelFetch(uDraws, record + INDICES);
  int palette = int(uShadowPalette == 1 ? indices.y : indices.x);
  bool skinned = dot(aWeights, vec4(1.0)) > 0.0;
  mat4 model = matrixAt(record);
  vec4 p = vec4(aPos, 1.0);
  vec3 n = aNormal;
  if (palette >= 0 && skinned) {
    vec4 r0, r1, r2;
    skin(palette, r0, r1, r2);
    p = vec4(dot(r0, p), dot(r1, p), dot(r2, p), 1.0);
    n = vec3(dot(r0.xyz, n), dot(r1.xyz, n), dot(r2.xyz, n));
  }
  vec4 stand = texelFetch(uDraws, record + STAND);
  bool upright = int(stand.w) == UPRIGHT, placed = int(stand.w) == PLACED;
  vec4 world = vec4(reliefStood((model * p).xyz, stand.xyz, upright, placed), 1.0);
  vec4 uv = texelFetch(uDraws, record + UV);
  vPos = world.xyz;
  vNormal = upright || placed ? mat3(model) * n : reliefNormal(mat3(model) * n, world.xyz);
  vUv = uv.xy + aUv * uv.zw;
  vUv2 = aUv2;
  vTint = texelFetch(uDraws, record + MATERIAL);
  vLight = texelFetch(uDraws, record + LIGHT);
  if ((int(indices.w) & EDGE_LIT) != 0) vLight = edgeLight(record + EDGE, int(indices.w), vPos, normalize(vNormal), uEye, vLight);
  vFade = texelFetch(uDraws, record + FADE).xy;
  vClear = (int(indices.w) & SOLID) != 0 ? 2 : (int(indices.w) & SELF) != 0 ? 1 : 0;
  vGlass = (int(indices.w) & GLASS) != 0 ? 1 : 0;
  gl_Position = uViewProjection * world;
  vMotion = uMotionPass == 1 && (int(indices.w) & MOTION) != 0 ? 1 : 0;
  vClip = vec4(0.0, 0.0, 0.0, 1.0);
  vPrevClip = vClip;
  if (vMotion == 1) {
    int previous = int(indices.z);
    vec4 q = vec4(aPos, 1.0);
    if (previous >= 0 && skinned) {
      vec4 r0, r1, r2;
      skin(previous, r0, r1, r2);
      q = vec4(dot(r0, q), dot(r1, q), dot(r2, q), 1.0);
    }
    vClip = uPlainViewProjection * world;
    vPrevClip = uPrevViewProjection * vec4(reliefStood((matrixAt(record + PREVIOUS) * q).xyz, stand.xyz, upright, placed), 1.0);
  }
}
