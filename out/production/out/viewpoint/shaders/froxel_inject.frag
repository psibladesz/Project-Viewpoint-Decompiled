#version 330






in vec2 vUv;
uniform mat4 uFroxelInverse;
uniform mat4 uReprojection;
uniform float uHistoryOn;
uniform sampler2D uHistory;
uniform vec3 uJitter;
out vec4 fragColor;
#include "lib/camera.glsl"
#include "lib/air_point.glsl"
#include "lib/froxels.glsl"

const float HISTORY = 0.9;


vec3 froxelRay(vec2 xy) {
  vec4 far = uFroxelInverse * vec4(xy / uFroxels.xy * 2.0 - 1.0, 1.0, 1.0);
  return normalize(far.xyz / far.w - uEye);
}

void main() {
  ivec3 f = froxelAt(ivec2(gl_FragCoord.xy));
  if (f.z >= int(uFroxels.z)) {
    fragColor = vec4(0.0);
    return;
  }
  vec3 cell = vec3(f) + 0.5;
  vec4 history = vec4(0.0);
  float keep = 0.0;
  if (uHistoryOn > 0.5) {
    vec3 centre = uEye + froxelRay(cell.xy) * froxelDistance(cell.z);
    vec4 clip = uReprojection * vec4(centre, 1.0);
    if (clip.w > 1.0e-4) {
      vec2 ndc = clip.xy / clip.w;
      float along = clip.w * length(vec3(ndc * uCamera.zw, 1.0));
      vec3 last = vec3((ndc * 0.5 + 0.5) * uFroxels.xy, froxelSlices(along));
      if (all(greaterThan(last, vec3(0.0))) && all(lessThan(last, uFroxels))) {
        history = froxelValue(uHistory, last);
        keep = HISTORY;
        cell = vec3(f) + uJitter;
      }
    }
  }
  vec3 dir = froxelRay(cell.xy);
  vec4 now = airAt(uEye + dir * froxelDistance(cell.z), dir);
  if (any(isnan(now)) || any(isinf(now))) now = history;
  fragColor = mix(now, history, keep);
}
