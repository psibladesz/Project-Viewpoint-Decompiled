#version 330


in vec2 vUv;
uniform sampler2D uLayer;
out vec4 fragColor;
void main() { fragColor = texture(uLayer, vUv); }
