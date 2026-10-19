#version 330
#extension GL_ARB_gpu_shader5 : enable


in vec2 vUv;
#include "constants.glsl"
uniform sampler2D uTextures[MODEL_TEXTURES];
flat in int vTexture;
#include "lib/fade.glsl"
void main() {
  fadeDither();
  if (texture(uTextures[vTexture], vUv).a < 0.5) discard;
}
