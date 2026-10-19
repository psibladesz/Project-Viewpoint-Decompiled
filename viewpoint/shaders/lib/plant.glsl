






#ifndef VIEWPOINT_PLANT
#define VIEWPOINT_PLANT
#include "constants.glsl"
uniform int uPlants;
uniform samplerBuffer uPlantSlots;

struct PlantVertex {
  vec3 pos;
  vec3 foot;
  vec2 uv;
  vec4 rect;
  int flags;
};

const float PLANT_PI = 3.14159265;
const vec2 PLANT_CORNERS[6] = vec2[6](vec2(0.0, 0.0), vec2(1.0, 0.0), vec2(1.0, 1.0),
                                      vec2(0.0, 0.0), vec2(1.0, 1.0), vec2(0.0, 1.0));



PlantVertex plantVertex(vec3 origin, vec3 eye) {
  int slot = gl_VertexID / PLANT_VERTICES;
  vec2 c = PLANT_CORNERS[gl_VertexID - slot * PLANT_VERTICES];
  int t = slot * PLANT_TEXELS;
  vec2 f0 = texelFetch(uPlantSlots, t).xy, f1 = texelFetch(uPlantSlots, t + 1).xy;
  vec4 rect = vec4(texelFetch(uPlantSlots, t + 2).xy, texelFetch(uPlantSlots, t + 3).xy);
  vec4 card = vec4(texelFetch(uPlantSlots, t + 4).xy, texelFetch(uPlantSlots, t + 5).xy);
  vec3 foot = vec3(f0.x, f0.y, f1.x);
  int flags = floatBitsToInt(f1.y);
  vec3 side;
  if ((flags & FLAG_FACING) != 0) {
    vec2 toEye = eye.xz - (foot.xz + origin.xz);
    vec2 across = normalize(vec2(toEye.y, -toEye.x) + vec2(1.0e-6, 0.0));
    side = vec3(across.x, 0.0, across.y);
  } else {
    int plane = (flags >> PLANE_SHIFT) & PLANE_MASK, planes = (flags >> (PLANE_SHIFT + PLANE_BITS)) & PLANE_MASK;
    float angle = -PLANT_PI / 4.0 + float(plane) * PLANT_PI / float(max(planes, 1));
    side = vec3(-cos(angle), 0.0, -sin(angle));
  }
  PlantVertex v;
  v.pos = foot + side * mix(card.x, card.y, c.x) + vec3(0.0, mix(card.z, card.w, c.y), 0.0);
  v.foot = foot;
  v.uv = vec2(mix(rect.x, rect.z, c.x), mix(rect.w, rect.y, c.y));
  v.rect = rect;
  v.flags = flags;
  return v;
}
#endif
