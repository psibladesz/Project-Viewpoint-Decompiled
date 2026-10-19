#version 330


#define SMAA_INCLUDE_VS 0
#include "lib/smaa_setup.glsl"
in vec2 vUv;
in vec2 vPixel;
in vec4 vOffset[3];
uniform sampler2D uEdges;
uniform sampler2D uArea;
uniform sampler2D uSearch;
out vec4 fragColor;
void main() { fragColor = SMAABlendingWeightCalculationPS(vUv, vPixel, vOffset, uEdges, uArea, uSearch, vec4(0.0)); }
