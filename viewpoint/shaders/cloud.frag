#version 330



in vec2 vUv;
uniform vec3 uForward;
uniform vec3 uRight;
uniform vec3 uUp;
out vec4 fragColor;
#include "lib/cloud_march.glsl"
void main() {
  vec2 ndc = vUv * 2.0 - 1.0;
  vec3 r = normalize(uForward + uRight * ndc.x + uUp * ndc.y);
  fragColor = marchClouds(r);
}
