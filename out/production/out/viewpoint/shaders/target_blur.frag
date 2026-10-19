#version 330


in vec2 vUv;
uniform sampler2D uMask;
uniform vec2 uStep;
uniform float uRadius;
out vec4 fragColor;
void main() {
  int taps = int(ceil(uRadius));
  float sigma = max(uRadius * 0.5, 0.5), sum = 0.0, weight = 0.0;
  for (int i = -taps; i <= taps; i++) {
    float w = exp(-0.5 * float(i * i) / (sigma * sigma));
    sum += texture(uMask, vUv + uStep * float(i)).r * w;
    weight += w;
  }
  fragColor = vec4(sum / weight);
}
