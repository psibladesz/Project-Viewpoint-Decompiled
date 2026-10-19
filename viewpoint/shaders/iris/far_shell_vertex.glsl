



#include "constants.glsl"
#include "lib/relief_lod.glsl"
layout(location = 0) in vec3 aPos;
layout(location = 1) in vec2 aUv;
layout(location = 2) in vec4 aRect;
layout(location = 3) in vec3 aNormal;
layout(location = 4) in float aFill;
layout(location = 5) in int aFlags;
layout(location = 6) in int aDraw;
uniform samplerBuffer uShellRecords;

uniform vec3 uIrisEye;
uniform int uIrisShadow;
uniform float uPullScale;
uniform vec3 uEye;
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
out vec3 vLocal;
flat out float vFill;
flat out int vFlags;
flat out vec4 vRect;
out vec3 vNormal;
out vec3 vPos;
flat out vec2 vCellWorld;
flat out float vFloorLayer;
flat out vec2 vFade;
bool hiddenVertex;
const float DH_LEAVES = 1.0, DH_STONE = 2.0, DH_GRASS = 13.0;

vec3 toMinecraft(vec3 sceneDir) { return vec3(-sceneDir.x, sceneDir.y, -sceneDir.z); }

void fetchFarShell() {
  int shellRecord = (aDraw & ((1 << ATLAS_SLOT_SHIFT) - 1)) * 2;
  vec4 shellOrigin = texelFetch(uShellRecords, shellRecord), shellCell = texelFetch(uShellRecords, shellRecord + 1);
  vCellWorld = shellCell.xy;
  vFloorLayer = shellOrigin.w;
  vFade = shellCell.zw;
  bool billboard = (aFlags & (FLAG_BILLBOARD | FLAG_FACING)) != 0;
  vUv = aUv; vRect = aRect; vFill = billboard ? 0.0 : aFill; vFlags = aFlags;
  vLocal = vec3(-aPos.x, aPos.y, -aPos.z);
  vPos = aPos + shellOrigin.xyz;
  vPos.y += reliefShell(vCellWorld + vLocal.xz) * SQRT6;
  if (billboard) {
    vec2 toEye = uEye.xz - vPos.xz;
    vPos.xz += normalize(vec2(toEye.y, -toEye.x) + vec2(1.0e-6, 0.0)) * aFill;
  }
  bool floorTile = (aFlags & FLAG_SOLID_FLOOR) != 0;
  bool artFace = (aFlags & (FLAG_SINGLE_SIDED | FLAG_COVER)) == FLAG_SINGLE_SIDED;
  float pull = float((aFlags & FLAG_DECAL) != 0) + 1.5 * float((aFlags & FLAG_COVER) != 0)
             + 2.0 * float((aFlags >> FLAG_LAYER_SHIFT) & 3) + 0.5 * float(floorTile) + 0.25 * float(artFace);
  if (uIrisShadow != 0 || !floorTile && abs(aNormal.y) > 0.9 && uEye.y < vPos.y) pull = 0.0;
  if (pull > 0.0) {
    vec3 toEye = uEye - vPos;
    float d = length(toEye);
    vPos += toEye / max(d, 0.001) * min((0.001 + d * d * 6.0e-6 * uPullScale) * pull, 0.5 * d);
  }
  hiddenVertex = false;
  vNormal = reliefNormalAt(aNormal, vCellWorld + vLocal.xz);
  vec3 n = normalize(dot(vNormal, vNormal) < 0.01 ? vec3(0.0, 1.0, 0.0) : vNormal);
  vp_Vertex = vec4(toMinecraft(vPos - uIrisEye), 1.0);
  vp_Normal = toMinecraft(n);
  vp_Color = vec4(1.0);
  vp_MultiTexCoord0 = vec4(aUv, 0.0, 1.0);
  vp_MultiTexCoord1 = vec4(0.0, 240.0, 0.0, 1.0);
  vp_entity = vec4(billboard ? DH_LEAVES : floorTile ? DH_GRASS : DH_STONE, -1.0, 0.0, 0.0);
  vp_midTexCoord = vec4((aRect.xy + aRect.zw) * 0.5, 0.0, 1.0);
  vp_tangent = vec4(toMinecraft(abs(n.y) > 0.9 ? vec3(-1.0, 0.0, 0.0) : normalize(cross(vec3(0.0, 1.0, 0.0), n))), 1.0);
  vp_midBlock = vec4(0.0);
  vp_velocity = vec3(0.0);
}
