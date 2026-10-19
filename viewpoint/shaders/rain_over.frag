#version 330


in vec2 vUv;
uniform sampler2D uRain;
out vec4 fragColor;
#include "lib/panini.glsl"
void main() { fragColor = texture(uRain, paniniFrame(vUv)); }
