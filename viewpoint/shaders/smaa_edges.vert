#version 330

#define SMAA_INCLUDE_PS 0
#include "lib/smaa_setup.glsl"
out vec2 vUv;
out vec4 vOffset[3];
void main() {
  vec2 p = vec2((gl_VertexID << 1) & 2, gl_VertexID & 2);
  vUv = p;
  SMAAEdgeDetectionVS(vUv, vOffset);
  gl_Position = vec4(p * 2.0 - 1.0, 0.0, 1.0);
}
