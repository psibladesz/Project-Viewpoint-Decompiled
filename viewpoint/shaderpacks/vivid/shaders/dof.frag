#version 330





in vec2 vUv;
uniform sampler2D uColor;
uniform sampler2D uDepth;
uniform sampler2D uFocus;
uniform vec2 uTexel;
uniform vec4 uCamera;
out vec4 fragColor;
#ifndef OPTION_DOF_STRENGTH
#define OPTION_DOF_STRENGTH 0.5
#endif
#ifndef OPTION_DOF_SAMPLES
#define OPTION_DOF_SAMPLES 24
#endif

const float DOF_MAX = 14.0, DOF_PER_DIOPTRE = 60.0;
const float GOLDEN_ANGLE = 2.39996323;
float viewDepth(vec2 uv) {
  float z = texture(uDepth, uv).r * 2.0 - 1.0;
  return 2.0 * uCamera.x * uCamera.y / (uCamera.y + uCamera.x - z * (uCamera.y - uCamera.x));
}

float blur(float depth, float focus) {
  return clamp((1.0 / focus - 1.0 / depth) * DOF_PER_DIOPTRE * OPTION_DOF_STRENGTH, 0.0, DOF_MAX);
}
void main() {
  vec3 colour = texture(uColor, vUv).rgb;
  float focus = max(texture(uFocus, vec2(0.5)).r, uCamera.x), depth = viewDepth(vUv);
  float here = blur(depth, focus);
  if (here < 0.5) {
    fragColor = vec4(colour, 1.0);
    return;
  }
  vec3 sum = colour;
  float total = 1.0;
  for (int i = 0; i < OPTION_DOF_SAMPLES; i++) {
    float r = here * sqrt((float(i) + 0.5) / float(OPTION_DOF_SAMPLES)), a = float(i) * GOLDEN_ANGLE;
    vec2 uv = vUv + vec2(cos(a), sin(a)) * r * uTexel;
    float there = viewDepth(uv), reach = blur(there, focus);
    if (there < depth) reach = min(reach, here);
    float w = smoothstep(r - 0.5, r + 0.5, reach);
    sum += texture(uColor, uv).rgb * w;
    total += w;
  }
  fragColor = vec4(sum / total, 1.0);
}
