




#include "constants.glsl"
#include "lib/far_relief.glsl"
layout(location = 0) in vec4 aVertex;
uniform vec3 uOrigin;
uniform vec2 uCellWorld;
uniform vec2 uFade;
uniform sampler2D uTop;
uniform sampler2D uSide;
uniform vec3 uIrisEye;
uniform usamplerBuffer uTrees;
uniform samplerBuffer uKinds;
uniform float uGrow;
uniform float uPixel;
uniform vec2 uTreeFade;
uniform vec2 uTreeVariation;
uniform float uDisplays[100];
uniform float uBareCrown;
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
out vec2 vWorld;
out float vEyeDistance;
flat out vec2 vFade;
flat out int vFace;
out float vHeight;
out vec2 vCorner;
flat out float vCover;
bool hiddenVertex;
const vec3 NORMALS[5] = vec3[5](vec3(0.0, 1.0, 0.0), vec3(1.0, 0.0, 0.0), vec3(-1.0, 0.0, 0.0), vec3(0.0, 0.0, 1.0), vec3(0.0, 0.0, -1.0));
const vec2 INSIDE[5] = vec2[5](vec2(0.0), vec2(0.5, 0.0), vec2(-0.5, 0.0), vec2(0.0, 0.5), vec2(0.0, -0.5));
const float DH_LEAVES = 1.0, DH_STONE = 2.0, DH_GRASS = 13.0;
const float MIN_PIXELS = 2.0;
const int KIND_TEXELS = 6;
const uint TURN_SHIFT = 8u;
const vec2 CORNERS[6] = vec2[6](vec2(-1.0, 0.0), vec2(1.0, 0.0), vec2(1.0, 1.0), vec2(-1.0, 0.0), vec2(1.0, 1.0), vec2(-1.0, 1.0));

vec3 toMinecraft(vec3 sceneDir) { return vec3(-sceneDir.x, sceneDir.y, -sceneDir.z); }

void fetchFarBox() {
  int face = int(aVertex.w + 0.5);
  vec2 local = aVertex.xy;
  vec3 pos = uOrigin + vec3(-aVertex.x, (aVertex.z + reliefCoarse(uCellWorld + local)) * SQRT6, -aVertex.y);
  vec3 colour = face == 0 ? texture(uTop, local / 256.0).rgb : texture(uSide, (local + INSIDE[face]) / 256.0).rgb;
  bool ground = face == 0 && aVertex.z < 0.01;
  vWorld = uCellWorld + local;
  vFade = uFade;
  vFace = face;
  vHeight = aVertex.z;
  vCorner = vec2(0.0, 2.0);
  vCover = 1.0;
  hiddenVertex = false;
  vp_Vertex = vec4(toMinecraft(pos - uIrisEye), 1.0);
  vEyeDistance = length(pos - uIrisEye);
  vp_Normal = toMinecraft(reliefNormalAt(NORMALS[face], uCellWorld + local));
  vp_Color = vec4(colour, 1.0);
  vp_MultiTexCoord0 = vec4(0.0, 0.0, 0.0, 1.0);
  vp_MultiTexCoord1 = vec4(0.0, face == 0 ? 240.0 : 200.0, 0.0, 1.0);
  vp_entity = vec4(ground ? DH_GRASS : DH_STONE, -1.0, 0.0, 0.0);
  vp_midTexCoord = vec4(0.0, 0.0, 0.0, 1.0);
  vp_tangent = vec4(toMinecraft(abs(NORMALS[face].y) > 0.9 ? vec3(-1.0, 0.0, 0.0) : cross(vec3(0.0, 1.0, 0.0), NORMALS[face])), 1.0);
  vp_midBlock = vec4(0.0);
  vp_velocity = vec3(0.0);
}

vec2 squareNoise(uvec2 square) {
  uint h = (square.x * 0x27D4EB2Du) ^ (square.y * 0x165667B1u);
  h ^= h >> 15u;
  h *= 0x85EBCA6Bu;
  h ^= h >> 13u;
  return vec2(float(h & 0xFFFFu), float(h >> 16u)) / 32767.5 - 1.0;
}


void fetchFarTree() {
  uvec4 tree = texelFetch(uTrees, gl_VertexID / 6);
  int kind = int(tree.w);
  uint level = tree.z & ((1u << TURN_SHIFT) - 1u);
  int display = int(uDisplays[tree.z >> TURN_SHIFT]);
  vec2 stray = squareNoise(uvec2(ivec2(uCellWorld) + ivec2(tree.xy))) * uTreeVariation.x;
  vec4 look = texelFetch(uKinds, kind * KIND_TEXELS + 1 + display);
  vec3 colour = look.rgb * (1.0 + stray.x) * vec3(1.0 + 0.5 * stray.y, 1.0, 1.0 - 0.5 * stray.y);
  vCover = mix(1.0, uBareCrown, look.a);
  vec2 size = texelFetch(uKinds, kind * KIND_TEXELS).xy;
  vec2 local = vec2(tree.xy) + 0.5;
  vec3 foot = uOrigin + vec3(-local.x, (float(level) + reliefCoarse(uCellWorld + local)) * SQRT6, -local.y);
  hiddenVertex = length(foot.xz - uIrisEye.xz) >= uTreeFade.y;
  float grow = max(uGrow, MIN_PIXELS * 0.5 * uPixel * length(uIrisEye - foot) / max(size.x, 0.1));
  vec2 toEye = uIrisEye.xz - foot.xz;
  vec2 right = normalize(vec2(toEye.y, -toEye.x) + vec2(1.0e-6, 0.0));
  vCorner = CORNERS[gl_VertexID % 6];
  vec2 across = right * vCorner.x * size.x * grow;
  vec3 pos = foot + vec3(across.x, vCorner.y * size.y * grow, across.y);
  vWorld = uCellWorld + local;
  vFade = uFade;
  vFace = -1;
  vHeight = float(level);
  vec3 facing = normalize(vec3(toEye.x, 0.0, toEye.y) + vec3(1.0e-6, 0.0, 0.0));
  vp_Vertex = vec4(toMinecraft(pos - uIrisEye), 1.0);
  vEyeDistance = length(pos - uIrisEye);
  vp_Normal = toMinecraft(facing);
  vp_Color = vec4(colour, 1.0);
  vp_MultiTexCoord0 = vec4(vCorner * 0.5 + 0.5, 0.0, 1.0);
  vp_MultiTexCoord1 = vec4(0.0, 240.0, 0.0, 1.0);
  vp_entity = vec4(DH_LEAVES, -1.0, 0.0, 0.0);
  vp_midTexCoord = vec4(0.5, 0.5, 0.0, 1.0);
  vp_tangent = vec4(toMinecraft(vec3(right.x, 0.0, right.y)), 1.0);
  vp_midBlock = vec4(0.0);
  vp_velocity = vec3(0.0);
}
