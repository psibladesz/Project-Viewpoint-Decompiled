#version 330


#define SMAA_INCLUDE_VS 0
#include "lib/smaa_setup.glsl"
in vec2 vUv;
in vec4 vOffset[3];
uniform sampler2D uColor;
out vec4 fragColor;
void main() { fragColor = vec4(SMAALumaEdgeDetectionPS(vUv, vOffset, uColor), 0.0, 0.0); }
