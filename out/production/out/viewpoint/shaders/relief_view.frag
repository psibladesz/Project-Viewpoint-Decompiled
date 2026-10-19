#version 330





in vec2 vUv;
uniform int uReliefView;
uniform vec4 uReliefBands;
uniform float uReliefCycle;
out vec4 fragColor;
#include "lib/camera.glsl"
#include "lib/far_relief.glsl"
const float CONTOUR_MAJOR = 5.0;
const float TINT = 0.6;
vec3 hue(float t) {
  return clamp(abs(fract(t + vec3(0.0, 2.0 / 3.0, 1.0 / 3.0)) * 6.0 - 3.0) - 1.0, 0.0, 1.0);
}


float contour(float metres, float every) {
  float t = metres / every, w = fwidth(t);
  if (w > 0.3) return 0.0;
  return 1.0 - smoothstep(0.5 * w, 1.5 * w, abs(fract(t + 0.5) - 0.5));
}
vec3 band(float slope) {
  if (slope < uReliefBands.x) return vec3(0.15, 0.45, 1.0);
  if (slope < uReliefBands.y) return vec3(0.1, 0.85, 0.25);
  if (slope < uReliefBands.z) return vec3(0.95, 0.9, 0.1);
  if (slope < uReliefBands.w) return vec3(1.0, 0.5, 0.05);
  return vec3(0.95, 0.05, 0.05);
}
void main() {
  float depth = texture(uDepth, vUv).r;
  vec2 world = reliefWorld(scenePos(vUv, depth));
  float metres = relief(world) / max(uReliefGrid.z, 1.0e-6) + uReliefGrid.w;
  float lines = max(contour(metres, 1.0) * 0.5, contour(metres, CONTOUR_MAJOR));
  if (depth >= 1.0 || uReliefGrid.z <= 0.0) discard;
  vec3 tint = uReliefView == 1 ? hue(metres / uReliefCycle) : band(length(reliefSlope(world)));
  fragColor = vec4(mix(tint, vec3(0.0), lines), mix(TINT, 1.0, lines));
}
