#version 330


in vec2 vUv;
uniform sampler2D uColor;
uniform sampler2D uFlare;
out vec4 fragColor;
#ifndef OPTION_LENS_FLARE_STRENGTH
#define OPTION_LENS_FLARE_STRENGTH 1.0
#endif
void main() {
  vec3 c = texture(uColor, vUv).rgb;
  vec3 flare = 1.0 - exp(-texture(uFlare, vUv).rgb * OPTION_LENS_FLARE_STRENGTH);
  fragColor = vec4(c + flare * (1.0 - c), 1.0);
}
