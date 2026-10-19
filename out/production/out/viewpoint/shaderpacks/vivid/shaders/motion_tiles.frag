#version 330


in vec2 vUv;
uniform vec2 uTexel;
out vec4 fragColor;
#include "lib/motion_blur.glsl"
void main() {
  vec2 frameSize = vec2(textureSize(uDepth, 0));
  vec2 corner = vUv - 0.5 * uTexel;
  vec2 longest = vec2(0.0);
  for (int y = 0; y < 8; y++) {
    for (int x = 0; x < 8; x++) {
      vec2 uv = corner + (vec2(x, y) * 2.0 + 1.0) / 16.0 * uTexel;
      vec2 m = motionAt(uv, frameSize);
      if (dot(m, m) > dot(longest, longest)) longest = m;
    }
  }
  fragColor = vec4(longest, 0.0, 1.0);
}
