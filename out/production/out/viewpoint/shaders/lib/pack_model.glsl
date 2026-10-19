







#ifndef VIEWPOINT_PACK_MODEL
#define VIEWPOINT_PACK_MODEL
#include "constants.glsl"
#include "lib/plant.glsl"
uniform int uModels;
uniform samplerBuffer uPackTable;
uniform samplerBuffer uMeshRecords;
layout(location = 9) in ivec4 aRun;

struct ModelVertex {
  vec3 pos;
  vec3 foot;
  vec2 uv;
  vec4 rect;
  vec3 normal;
  int flags;
  vec4 mesh;
  vec4 fade;
  vec4 below;
  bool hidden;
};

float modelSlot(int t, int i) {
  vec2 pair = texelFetch(uPlantSlots, t + i / 2).xy;
  return i % 2 == 0 ? pair.x : pair.y;
}

ModelVertex modelVertex(vec3 pos, vec2 uv, vec3 normal) {
  int t = (aRun.y + gl_InstanceID) * PLANT_TEXELS;
  vec3 foot = vec3(modelSlot(t, 0), modelSlot(t, 1), modelSlot(t, 2));
  float c = modelSlot(t, 3), s = modelSlot(t, 4), scale = modelSlot(t, 5);
  int square = floatBitsToInt(modelSlot(t, MODEL_SQUARE)), model = floatBitsToInt(modelSlot(t, MODEL_INDEX));
  vec4 art = texelFetch(uPackTable, model * 2), far = texelFetch(uPackTable, model * 2 + 1);
  vec3 p = pos * scale;
  ModelVertex v;
  v.foot = foot;
  v.pos = foot + vec3(p.x * c - p.z * s, p.y, p.x * s + p.z * c);
  v.normal = vec3(normal.x * c - normal.z * s, normal.y, normal.x * s + normal.z * c);
  v.uv = art.xy + uv * art.zw;
  v.rect = vec4(art.xy, far.xy);
  v.flags = floatBitsToInt(modelSlot(t, MODEL_FLAGS)) | FLAG_SINGLE_SIDED;
  v.mesh = texelFetch(uMeshRecords, aRun.x * 3);
  v.fade = texelFetch(uMeshRecords, aRun.x * 3 + 1);
  v.below = texelFetch(uMeshRecords, aRun.x * 3 + 2);
  int word = square < 32 ? aRun.z : aRun.w;
  v.hidden = square < NO_SQUARE && ((word >> (square & 31)) & 1) == 0;
  return v;
}
#endif
