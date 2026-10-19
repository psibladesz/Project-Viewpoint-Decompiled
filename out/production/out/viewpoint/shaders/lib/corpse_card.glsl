




#ifndef VIEWPOINT_CORPSE_CARD
#define VIEWPOINT_CORPSE_CARD
#include "constants.glsl"
#include "lib/pack_model.glsl"
uniform int uCards;
uniform samplerBuffer uCardTexels;

struct CardVertex {
  vec3 pos;
  vec2 uv;
  vec4 rect;
  int flags;
  vec4 mesh;
  vec4 fade;
  vec4 below;
};

const int CARD_CORNERS[6] = int[6](0, 1, 2, 0, 2, 3);

CardVertex cardVertex() {
  int card = gl_VertexID / 6;
  int t = card * CARD_TEXELS;
  vec4 corner = texelFetch(uCardTexels, t + CARD_CORNERS[gl_VertexID - card * 6]);
  vec4 info = texelFetch(uCardTexels, t + 4);
  int record = floatBitsToInt(info.z);
  CardVertex v;
  v.pos = vec3(corner.x, info.x, corner.y);
  v.uv = corner.zw;
  v.rect = texelFetch(uCardTexels, t + 5);
  v.flags = floatBitsToInt(info.y);
  v.mesh = texelFetch(uMeshRecords, record * 3);
  v.fade = texelFetch(uMeshRecords, record * 3 + 1);
  v.below = texelFetch(uMeshRecords, record * 3 + 2);
  vec2 own = texelFetch(uCardTexels, t + 6).xy;
  v.fade.xy = vec2(max(v.fade.x, own.x), min(v.fade.y, own.y));
  return v;
}
#endif
