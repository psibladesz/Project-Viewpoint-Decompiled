#version 330



in vec2 vUv;
uniform sampler2D uDepth;
uniform sampler2D uFocusPrevious;
uniform vec4 uCamera;
uniform float uFrameSeconds;
uniform float uPreviousValid;
out vec4 fragColor;
#ifndef OPTION_FOCUS_SPEED
#define OPTION_FOCUS_SPEED 0.3
#endif
float viewDepth(vec2 uv) {
  float z = texture(uDepth, uv).r * 2.0 - 1.0;
  return 2.0 * uCamera.x * uCamera.y / (uCamera.y + uCamera.x - z * (uCamera.y - uCamera.x));
}
void main() {
  float d = viewDepth(vec2(0.5));
  for (int i = 0; i < 4; i++) {
    vec2 o = vec2(i == 0 ? 0.01 : i == 1 ? -0.01 : 0.0, i == 2 ? 0.015 : i == 3 ? -0.015 : 0.0);
    d = min(d, viewDepth(0.5 + o));
  }
  float was = texture(uFocusPrevious, vec2(0.5)).r;
  float ease = was > 0.0 && uPreviousValid > 0.5 ? 1.0 - exp(-uFrameSeconds / OPTION_FOCUS_SPEED) : 1.0;
  fragColor = vec4(mix(was, d, ease), 0.0, 0.0, 1.0);
}
