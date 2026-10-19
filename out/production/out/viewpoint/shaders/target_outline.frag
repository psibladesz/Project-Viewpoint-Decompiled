#version 330



in vec2 vUv;
uniform sampler2D uGlow;
uniform sampler2D uMask;
uniform vec3 uColour;
uniform float uOpacity;
uniform float uEdge;
out vec4 fragColor;
#include "lib/panini.glsl"
void main() {
  vec2 at = paniniFrame(vUv);
  float glow = clamp(texture(uGlow, at).r * uEdge, 0.0, 1.0);
  float a = glow * (1.0 - texture(uMask, at).r) * uOpacity;
  if (a <= 0.0) discard;
  fragColor = vec4(uColour, a);
}
