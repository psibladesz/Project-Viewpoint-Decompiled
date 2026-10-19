#version 330



in vec2 vUv;
uniform sampler2D uVolume;
uniform vec2 uStep;
out vec4 fragColor;
#include "lib/camera.glsl"
void main() {
  float d0 = linearDepth(texture(uDepth, vUv).r);
  vec4 sum = vec4(0.0);
  float weight = 0.0;
  for (int i = -4; i <= 4; i++) {
    vec2 uv = vUv + uStep * float(i);
    float d = linearDepth(texture(uDepth, uv).r);
    float w = exp(-float(i * i) / 8.0) / (1.0 + abs(d - d0) * 8.0 / max(d0, 0.5));
    sum += texture(uVolume, uv) * w;
    weight += w;
  }
  fragColor = sum / max(weight, 1.0e-4);
}
