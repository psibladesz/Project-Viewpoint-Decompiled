#version 330


in vec2 vUv;
in float vFromEye;
flat in vec4 vTint;
flat in float vGlow;
uniform sampler2D uArt;
uniform float uFlameGlow;
uniform vec3 uSkyLevel;
#include "lib/translucent.glsl"
const float SMOKE_LEAST = 0.08;
void main() {
  vec4 c = texture(uArt, vUv) * vTint;
  if (c.a < 0.004) discard;
  vec3 colour = vGlow > 0.5 ? c.rgb * uFlameGlow : c.rgb * max(uSkyLevel, vec3(SMOKE_LEAST));
  translucent(colour, c.a, vFromEye);
}
