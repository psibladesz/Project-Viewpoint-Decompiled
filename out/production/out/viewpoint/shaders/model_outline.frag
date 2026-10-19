#version 330
#extension GL_ARB_gpu_shader5 : enable


in vec2 vUv;
#include "constants.glsl"
uniform sampler2D uTextures[MODEL_TEXTURES];
flat in int vTexture;
uniform vec3 uColour;
out vec4 fragColor;
void main() {
  if (texture(uTextures[vTexture], vUv).a < 0.5) discard;
  fragColor = vec4(uColour, 1.0);
}
