#version 330




in vec2 vUv;
uniform float uRange;
out vec4 fragColor;
#include "lib/camera.glsl"
#include "lib/epipolar.glsl"

void main() {
  ivec2 place = ivec2(gl_FragCoord.xy);
  vec4 line = epiLine(place.y);
  vec2 uv = mix(line.xy, line.zw, float(place.x) / float(EPI_SAMPLES - 1));
  bool off = distance(line.xy, line.zw) < 1.0e-6 || any(lessThan(uv, vec2(0.0))) || any(greaterThan(uv, vec2(1.0)));
  if (off) {
    fragColor = vec4(uv, -1.0, 0.0);
    return;
  }
  float depth = texture(uDepth, uv).r;
  fragColor = vec4(uv, depth >= 1.0 ? uRange : min(distance(scenePos(uv, depth), uEye), uRange), 1.0);
}
