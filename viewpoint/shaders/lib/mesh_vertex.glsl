







#include "lib/far_relief.glsl"
#include "lib/plant.glsl"
#include "lib/pack_model.glsl"
#include "lib/corpse_card.glsl"
layout(location = 0) in vec3 aPos;
layout(location = 1) in vec2 aUv;
layout(location = 2) in vec4 aRect;
layout(location = 3) in vec3 aNormal;
layout(location = 4) in float aFill;
layout(location = 5) in int aFlags;
layout(location = 6) in vec4 aMesh;
layout(location = 7) in vec4 aMeshFade;
layout(location = 8) in vec4 aMeshBelow;
struct MeshVertex {
  vec3 pos;
  vec3 local;
  vec2 uv;
  vec4 rect;
  vec3 normal;
  float fill;
  int flags;
  vec4 mesh;
  vec4 fade;
  vec4 below;
  bool hidden;
  float bend;
};





MeshVertex meshVertex(vec3 eye) {
  MeshVertex v;
  v.pos = aPos; v.flags = aFlags;
  v.mesh = aMesh; v.fade = aMeshFade; v.below = aMeshBelow;
  v.uv = aUv; v.rect = aRect; v.normal = aNormal; v.fill = aFill; v.hidden = false;
  vec3 foot = vec3(0.0);
  bool standing = false, onPlane = false;
  if (uPlants != 0) {
    PlantVertex plant = plantVertex(v.mesh.xyz, eye);
    v.pos = plant.pos; v.flags = plant.flags;
    v.uv = plant.uv; v.rect = plant.rect; v.normal = vec3(0.0, 0.5, 0.0); v.fill = 0.0;
    foot = plant.foot; standing = true;
  }
  if (uModels != 0) {
    ModelVertex model = modelVertex(aPos, aUv, aNormal);
    v.hidden = model.hidden;
    v.pos = model.pos; v.flags = model.flags;
    v.uv = model.uv; v.rect = model.rect; v.normal = model.normal; v.fill = 0.0;
    v.mesh = model.mesh; v.fade = model.fade; v.below = model.below;
    foot = model.foot; standing = true;
  }
  if (uCards != 0) {
    CardVertex card = cardVertex();
    v.pos = card.pos; v.flags = card.flags;
    v.uv = card.uv; v.rect = card.rect; v.normal = vec3(0.0, 1.0, 0.0); v.fill = 0.0;
    v.mesh = card.mesh; v.fade = card.fade; v.below = card.below;
  }
  if (!standing && (v.flags & FLAG_STANDING) != 0) {
    standing = true;
    onPlane = true;
    foot = vec3(-(float((v.flags >> ROOT_X_SHIFT) & 255) + 0.5), v.mesh.w,
                -(float((v.flags >> ROOT_Y_SHIFT) & 255) + 0.5));
  }
  v.local = vec3(-v.pos.x, v.pos.y - v.mesh.w, -v.pos.z);
  vec3 scene = v.pos + v.mesh.xyz;
  bool water = !standing && (v.flags & FLAG_WATER) != 0;
  v.bend = onPlane ? 1.0 : standing ? reliefBend(v.pos.y - foot.y, RELIEF_ROOT) : water ? 0.0 : 1.0;
  v.pos = onPlane ? reliefOnPlane(scene, foot + v.mesh.xyz, RELIEF_ROOT)
      : standing ? reliefUpright(scene, foot + v.mesh.xyz, RELIEF_ROOT)
      : water ? reliefWaterLifted(scene) : reliefLifted(scene);
  return v;
}



vec3 meshNormal(MeshVertex v) {
  return mix(v.normal, reliefNormal(v.normal, v.pos), v.bend);
}





vec2 lightOrigin(MeshVertex v, float eyeY) {
  bool ceiling = eyeY < v.mesh.y + v.mesh.w && abs(v.normal.y) > 0.5;
  bool lifted = (v.flags & FLAG_LIFTED) == FLAG_LIFTED && eyeY > v.pos.y;
  float slot = ceiling ? v.below.x : lifted ? v.below.y : v.fade.z;
  return vec2(mod(slot, LIGHT_COLUMNS), floor(slot / LIGHT_COLUMNS)) * LIGHT_GRID;
}











vec3 pulled(MeshVertex v, vec3 eye) {
  bool floorTile = (v.flags & (FLAG_SOLID_FLOOR | FLAG_WATER)) == FLAG_SOLID_FLOOR;
  bool artFace = (v.flags & (FLAG_SINGLE_SIDED | FLAG_COVER)) == FLAG_SINGLE_SIDED && uModels == 0;
  float pull = float((v.flags & FLAG_DECAL) != 0) + 1.5 * float((v.flags & FLAG_COVER) != 0)
             + 2.0 * float((v.flags >> FLAG_LAYER_SHIFT) & 3) + 0.5 * float(floorTile) + 0.25 * float(artFace);
  if (!floorTile && abs(v.normal.y) > 0.9 && eye.y < v.pos.y) pull = 0.0;
  if (pull <= 0.0) return v.pos;
  vec3 toEye = eye - v.pos;
  float d = length(toEye);
  return v.pos + toEye / max(d, 0.001) * min((0.001 + d * d * 6.0e-6) * pull, 0.5 * d);
}
