#version 330


in vec2 vUv;
flat in vec4 vRect;
flat in int vFlags;
uniform sampler2D uTexture;
#include "lib/sample_sprite.glsl"
out vec4 fragColor;
void main() {
  sampleSprite(uTexture, vUv, vRect, vFlags);
  fragColor = vec4(1.0);
}
