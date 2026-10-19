#version 330






in vec2 vUv;
uniform sampler2D uFarDepth;
uniform sampler2D uVelocity;
uniform mat4 uUnproject;
uniform mat4 uPrevious;
uniform mat4 uFarUnproject;
uniform vec2 uFarRange;
layout(location = 0) out float fragDepth;
layout(location = 1) out vec2 fragMotion;
#include "lib/camera.glsl"

float farLinear(float z) { z = z * 2.0 - 1.0; return 2.0 * uFarRange.x * uFarRange.y / (uFarRange.y + uFarRange.x - z * (uFarRange.y - uFarRange.x)); }


float directDepth(float d) {
  return clamp(uCamera.y * (d - uCamera.x) / (d * (uCamera.y - uCamera.x)), 0.0, 1.0);
}

void main() {
  float near = texture(uDepth, vUv).r;
  float far = uFarRange.x > 0.0 ? texture(uFarDepth, vUv).r : 1.0;
  bool farWorld = near >= 1.0 && far < 1.0;
  fragDepth = near >= 1.0 && !farWorld ? 1.0 : directDepth(farWorld ? farLinear(far) : linearDepth(near));
  vec4 motion = texture(uVelocity, vUv);
  if (motion.w > 0.5) {
    fragMotion = -motion.xy;
    return;
  }
  vec4 scene = farWorld ? uFarUnproject * vec4(vUv * 2.0 - 1.0, far * 2.0 - 1.0, 1.0)
                        : uUnproject * vec4(vUv * 2.0 - 1.0, min(near, 1.0) * 2.0 - 1.0, 1.0);
  vec4 previous = uPrevious * vec4(scene.xyz / scene.w, 1.0);
  fragMotion = previous.xy / previous.w * 0.5 + 0.5 - vUv;
}
