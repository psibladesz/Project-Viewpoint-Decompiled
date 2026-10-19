#version 330




#include "lib/far_relief.glsl"
#include "lib/plant.glsl"
#include "lib/pack_model.glsl"
#include "lib/corpse_card.glsl"
layout(location = 0) in vec3 aPos;
layout(location = 1) in vec2 aUv;
layout(location = 2) in vec4 aRect;
layout(location = 3) in vec3 aNormal;
layout(location = 5) in int aFlags;
layout(location = 6) in vec4 aMesh;
layout(location = 7) in vec4 aMeshFade;
uniform mat4 uViewProjection;
uniform vec3 uEye;


uniform float uOwnFixture;
out vec2 vUv;
flat out vec4 vRect;
flat out vec2 vFade;
flat out float vFoliage;
flat out int vFlags;
out vec3 vAnchor;
void main() {
  vec3 pos = aPos, offset = aMesh.xyz, foot = vec3(0.0);
  bool standing = false;
  vUv = aUv; vRect = aRect; vFade = aMeshFade.xy; vFlags = aFlags;
  vFoliage = dot(aNormal, aNormal) < 0.6 ? 1.0 : 0.0;
  if (uPlants != 0) {
    PlantVertex plant = plantVertex(aMesh.xyz, uEye);
    pos = plant.pos; vUv = plant.uv; vRect = plant.rect; vFlags = plant.flags; vFoliage = 1.0;
    foot = plant.foot; standing = true;
  }
  if (uModels != 0) {
    ModelVertex model = modelVertex(aPos, aUv, aNormal);
    bool own = uOwnFixture > 0.0 && length((model.foot + model.mesh.xyz).xz - uEye.xz) < uOwnFixture;
    if (model.hidden || own) { gl_Position = vec4(2.0, 2.0, 2.0, 1.0); return; }
    pos = model.pos; vUv = model.uv; vRect = model.rect; vFlags = model.flags; vFoliage = 0.0;
    offset = model.mesh.xyz; vFade = model.fade.xy;
    foot = model.foot; standing = true;
  }
  if (uCards != 0) {
    CardVertex card = cardVertex();
    pos = card.pos; vUv = card.uv; vRect = card.rect; vFlags = card.flags; vFoliage = 0.0;
    offset = card.mesh.xyz; vFade = card.fade.xy;
  }
  bool onPlane = false;
  if (!standing && (vFlags & FLAG_STANDING) != 0) {
    standing = true;
    onPlane = true;
    foot = vec3(-(float((vFlags >> ROOT_X_SHIFT) & 255) + 0.5), aMesh.w,
                -(float((vFlags >> ROOT_Y_SHIFT) & 255) + 0.5));
  }
  vAnchor = pos;

  vec3 lifted = onPlane ? reliefOnPlane(pos + offset, foot + offset, RELIEF_ROOT)
      : standing ? reliefUpright(pos + offset, foot + offset, RELIEF_ROOT)
      : (vFlags & FLAG_WATER) != 0 ? reliefWaterLifted(pos + offset) : reliefLifted(pos + offset);
  gl_Position = uViewProjection * vec4(lifted, 1.0);
}
