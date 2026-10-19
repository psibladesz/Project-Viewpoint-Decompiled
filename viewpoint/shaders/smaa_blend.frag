#version 330

#define SMAA_INCLUDE_VS 0
#include "lib/smaa_setup.glsl"
in vec2 vUv;
in vec4 vOffset;
uniform sampler2D uColor;
uniform sampler2D uWeights;
out vec4 fragColor;
void main() { fragColor = vec4(SMAANeighborhoodBlendingPS(vUv, vOffset, uColor, uWeights).rgb, 1.0); }
