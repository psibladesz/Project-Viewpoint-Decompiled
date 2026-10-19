#version 330


in vec3 vRay;
in vec2 vUv;
uniform vec3 uHorizon;
uniform vec3 uZenith;
out vec4 fragColor;
void main() {
  vec3 r = normalize(vRay);
  float h = max(r.y, 0.0);
  fragColor = vec4(mix(uHorizon, uZenith, 1.0 - exp(-h * 3.0)), 1.0);
}
