#version 330






in vec2 vUv;
uniform sampler2D uColor;
uniform sampler2D uMotionTiles;
uniform vec2 uTexel;
uniform vec4 uCamera;
uniform int uFrame;
out vec4 fragColor;
#include "lib/motion_blur.glsl"
#ifndef OPTION_MOTION_BLUR_SAMPLES
#define OPTION_MOTION_BLUR_SAMPLES 10
#endif

const float SOFT_DEPTH = 0.3;
float viewDepth(vec2 uv) {
  float z = texture(uDepth, uv).r * 2.0 - 1.0;
  return 2.0 * uCamera.x * uCamera.y / (uCamera.y + uCamera.x - z * (uCamera.y - uCamera.x));
}

float nearer(float a, float b) {
  return clamp(1.0 - (a - b) / SOFT_DEPTH, 0.0, 1.0);
}

float cone(float away, float len) {
  return clamp(1.0 - away / max(0.5 * len, 0.5), 0.0, 1.0);
}
float cylinder(float away, float len) {
  float reach = max(0.5 * len, 0.5);
  return 1.0 - smoothstep(0.95 * reach, 1.05 * reach, away);
}
void main() {
  vec3 colour = texture(uColor, vUv).rgb;
  ivec2 tiles = textureSize(uMotionTiles, 0), tile = ivec2(vUv * vec2(tiles));
  vec2 widest = vec2(0.0);
  for (int y = -1; y <= 1; y++) {
    for (int x = -1; x <= 1; x++) {
      vec2 m = texelFetch(uMotionTiles, clamp(tile + ivec2(x, y), ivec2(0), tiles - 1), 0).xy;
      if (dot(m, m) > dot(widest, widest)) widest = m;
    }
  }
  if (dot(widest, widest) < 0.25) {
    fragColor = vec4(colour, 1.0);
    return;
  }
  vec2 frameSize = 1.0 / uTexel;
  float here = length(motionAt(vUv, frameSize)), depth = viewDepth(vUv);
  float weight = 1.0 / max(here, 1.0);
  vec3 sum = colour * weight;
  vec2 p = gl_FragCoord.xy + 5.588238 * float(uFrame & 63);
  float jitter = fract(52.9829189 * fract(dot(p, vec2(0.06711056, 0.00583715)))) - 0.5;
  for (int i = 0; i < OPTION_MOTION_BLUR_SAMPLES; i++) {
    float t = (float(i) + 0.5 + jitter) / float(OPTION_MOTION_BLUR_SAMPLES) - 0.5;
    if (abs(t) < 0.5 / float(OPTION_MOTION_BLUR_SAMPLES)) continue;
    vec2 offset = widest * t, uv = vUv + offset * uTexel;
    float away = length(offset), there = length(motionAt(uv, frameSize)), depthThere = viewDepth(uv);
    float w = nearer(depthThere, depth) * cone(away, there)
            + nearer(depth, depthThere) * cone(away, here)
            + cylinder(away, there) * cylinder(away, here) * 2.0;
    sum += texture(uColor, uv).rgb * w;
    weight += w;
  }
  fragColor = vec4(sum / weight, 1.0);
}
