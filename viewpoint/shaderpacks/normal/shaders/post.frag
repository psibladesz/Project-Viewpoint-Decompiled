#version 330



in vec2 vUv;
uniform sampler2D uColor;
uniform int uDebugView;
uniform float uExposure;
uniform vec3 uFogColor;
uniform vec2 uFogRange;
uniform vec2 uHaze;
out vec4 fragColor;
#include "lib/camera.glsl"
#include "lib/common.glsl"
#include "lib/fog_amount.glsl"
void main() {
  vec3 color = texture(uColor, vUv).rgb;
  if (uDebugView != 0) { fragColor = vec4(color, 1.0); return; }

  if (any(isnan(color)) || any(isinf(color))) color = vec3(0.0);
  float depth = texture(uDepth, vUv).r;
  if (depth < 1.0) {
    float fog = max(fogAt(linearDepth(depth), uFogRange), hazeAt(distance(scenePos(vUv, depth), uEye), uHaze));
    color = mix(color, uFogColor, fog);
  }
  color = shoulder(max(color, vec3(0.0)) * uExposure);
  color += (ign(gl_FragCoord.xy + 17.0) - 0.5) / 255.0;
  fragColor = vec4(color, 1.0);
}
