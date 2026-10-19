#version 330
#extension GL_ARB_gpu_shader5 : enable




in vec2 vUv;
in vec3 vLocal;
flat in vec4 vRect;
flat in int vFlags;
in vec3 vNormal;
flat in vec2 vCellWorld;
flat in int vAtlas;
#include "constants.glsl"
uniform sampler2D uAtlases[ATLAS_SLOTS];
uniform sampler2D uTreeAtlas;
#include "lib/sample_sprite.glsl"
#include "lib/crown_shadow.glsl"
#include "lib/shell_root.glsl"
#include "lib/far_caster.glsl"
void main() {
  if (nearBuilt(vCellWorld + maskedSquare(vLocal, vFlags))) discard;
  if ((vFlags & FLAG_BILLBOARD) != 0) crownShadow(uTreeAtlas, vUv, vRect);
  else if (dot(vNormal, vNormal) < 0.6) crownShadow(uAtlases[vAtlas], vUv, vRect);
  else sampleSprite(uAtlases[vAtlas], vUv, vRect, vFlags);
}
