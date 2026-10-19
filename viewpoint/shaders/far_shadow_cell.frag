#version 330



in vec3 vPos;
in vec2 vLocal;
in float vHeight;
flat in int vFace;
uniform vec2 uCellWorld;
#include "lib/far_caster.glsl"
void main() {
  if (vFace == 0 && vHeight < 0.01) discard;
  vec2 world = uCellWorld + vLocal;
  if (withinNear(vPos) || nearBuilt(world) || shellStands(world)) discard;
}
