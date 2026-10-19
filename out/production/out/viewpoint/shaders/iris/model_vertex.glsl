



#include "constants.glsl"
#include "lib/far_relief.glsl"
layout(location = 0) in vec3 aPos;
layout(location = 1) in vec3 aNormal;
layout(location = 2) in vec4 aWeights;
layout(location = 3) in vec4 aBones;
layout(location = 4) in vec2 aUv;
layout(location = 5) in vec2 aUv2;
layout(location = 6) in int aDraw;
uniform samplerBuffer uPalettes;
uniform samplerBuffer uDraws;
#include "lib/edge_light.glsl"
uniform int uShadowPalette;
uniform vec3 uIrisEye;
uniform int uIrisEntity;
uniform int uIrisUntinted;
vec4 vp_Vertex;
vec3 vp_Normal;
vec4 vp_Color;
vec4 vp_MultiTexCoord0;
vec4 vp_MultiTexCoord1;
const vec4 vp_MultiTexCoordZero = vec4(0.0, 0.0, 0.0, 1.0);
vec4 vp_entity;
vec4 vp_midTexCoord;
vec4 vp_tangent;
vec4 vp_midBlock;
vec3 vp_velocity;
out vec2 vUv;
out vec2 vUv2;
out vec3 vPos;
out vec3 vNormal;
flat out vec4 vTint;
flat out vec4 vLight;
flat out int vTexture;
flat out vec2 vFade;
flat out int vClear;
flat out int vGlass;
out vec2 vLevels;
bool hiddenVertex;
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

vec3 toMinecraft(vec3 sceneDir) { return vec3(-sceneDir.x, sceneDir.y, -sceneDir.z); }


vec4 posed(int palette, bool skinned, out vec3 normal) {
  vec4 p = vec4(aPos, 1.0);
  normal = aNormal;
  if (palette >= 0 && skinned) {
    vec4 r0, r1, r2;
    skin(palette, r0, r1, r2);
    p = vec4(dot(r0, p), dot(r1, p), dot(r2, p), 1.0);
    normal = vec3(dot(r0.xyz, normal), dot(r1.xyz, normal), dot(r2.xyz, normal));
  }
  return p;
}

void fetchModel() {
  int record = (aDraw & ((1 << MODEL_SLOT_SHIFT) - 1)) * RECORD;
  vTexture = aDraw >> MODEL_SLOT_SHIFT;
  vec4 indices = texelFetch(uDraws, record + INDICES);
  bool skinned = dot(aWeights, vec4(1.0)) > 0.0;
  mat4 model = matrixAt(record);
  vec3 n;
  vec4 world = model * posed(int(uShadowPalette == 1 ? indices.y : indices.x), skinned, n);
  vec3 before;
  vec4 previous = matrixAt(record + PREVIOUS) * posed(int(indices.z), skinned, before);
  vec4 stand = texelFetch(uDraws, record + STAND);
  bool upright = int(stand.w) == UPRIGHT, placed = int(stand.w) == PLACED;
  world.xyz = reliefStood(world.xyz, stand.xyz, upright, placed);
  previous.xyz = reliefStood(previous.xyz, stand.xyz, upright, placed);
  vec4 uv = texelFetch(uDraws, record + UV);
  vPos = world.xyz;
  vNormal = normalize(upright || placed ? mat3(model) * n : reliefNormal(mat3(model) * n, world.xyz));
  vUv = uv.xy + aUv * uv.zw;
  vUv2 = aUv2;
  vTint = texelFetch(uDraws, record + MATERIAL);
  vLight = texelFetch(uDraws, record + LIGHT);
  if ((int(indices.w) & EDGE_LIT) != 0) vLight = edgeLight(record + EDGE, int(indices.w), vPos, vNormal, uIrisEye, vLight);
  vFade = texelFetch(uDraws, record + FADE).xy;
  vClear = (int(indices.w) & SOLID) != 0 ? 2 : (int(indices.w) & SELF) != 0 ? 1 : 0;
  vGlass = (int(indices.w) & GLASS) != 0 ? 1 : 0;
  hiddenVertex = false;
  vLevels = vec2(max(vLight.r, max(vLight.g, vLight.b)), vLight.a) * 15.0;
  vec3 tangent = abs(vNormal.y) > 0.9 ? vec3(-1.0, 0.0, 0.0) : normalize(cross(vec3(0.0, 1.0, 0.0), vNormal));
  vp_Vertex = vec4(toMinecraft(vPos - uIrisEye), 1.0);
  vp_Normal = toMinecraft(vNormal);
  vp_Color = uIrisUntinted == 1 ? vec4(1.0) : vec4(vTint.rgb, 1.0);
  vp_MultiTexCoord0 = vec4(vUv, 0.0, 1.0);
  vp_MultiTexCoord1 = vec4(vLevels * 16.0, 0.0, 1.0);
  vp_entity = vec4(float(uIrisEntity), -1.0, 0.0, 0.0);
  vp_midTexCoord = vec4(vUv, 0.0, 1.0);
  vp_tangent = vec4(toMinecraft(tangent), 1.0);
  vp_midBlock = vec4(0.0);
  vp_velocity = (int(indices.w) & MOTION) != 0 ? toMinecraft(world.xyz - previous.xyz) : vec3(0.0);
}
