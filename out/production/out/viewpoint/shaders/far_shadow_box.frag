#version 330



in vec2 vLocal;
uniform vec2 uCellWorld;
#include "lib/far_caster.glsl"
void main() {
  if (nearBuilt(uCellWorld + vLocal)) discard;
}
