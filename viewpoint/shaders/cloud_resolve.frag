#version 330










in vec2 vUv;
uniform sampler2D uTrace;
uniform sampler2D uHistory;
uniform vec3 uForward;
uniform vec3 uRight;
uniform vec3 uUp;
uniform vec3 uEye;
uniform mat4 uReprojection;
uniform vec2 uCloudPick;
uniform float uHistoryValid;
uniform float uStretch;
out vec4 fragColor;
#include "lib/cloud_depth.glsl"
const int BLOCK = 4;
const float TRACE_WEIGHT = 0.2;

void main() {
  ivec2 pixel = ivec2(gl_FragCoord.xy);
  ivec2 block = pixel / BLOCK;
  bool tracedHere = pixel - block * BLOCK == ivec2(uCloudPick);
  vec3 r = normalize(uForward + uRight * (vUv.x * 2.0 - 1.0) + uUp * (vUv.y * 2.0 - 1.0));
  vec4 clip = uReprojection * vec4(uEye + r * cloudDepth(r), 1.0);
  vec2 last = clip.xy / clip.w * 0.5 + 0.5;
  bool carried = uHistoryValid > 0.5 && clip.w > 0.0 && all(greaterThanEqual(last, vec2(0.0)))
      && all(lessThanEqual(last, vec2(1.0)));

  vec2 traced = (gl_FragCoord.xy - 0.5 - uCloudPick) / float(BLOCK) + 0.5;
  vec4 stretched = textureLod(uTrace, traced / vec2(textureSize(uTrace, 0)), 0.0);
  if (!carried) {
    fragColor = stretched;
    return;
  }
  ivec2 corner = textureSize(uTrace, 0) - 1;
  vec4 least = vec4(1.0e30), most = vec4(-1.0e30);
  for (int y = -1; y <= 1; y++) {
    for (int x = -1; x <= 1; x++) {
      vec4 near = texelFetch(uTrace, clamp(block + ivec2(x, y), ivec2(0), corner), 0);
      least = min(least, near);
      most = max(most, near);
    }
  }
  vec4 history = clamp(textureLod(uHistory, last, 0.0), least, most);
  vec4 resolved = tracedHere ? mix(history, texelFetch(uTrace, block, 0), TRACE_WEIGHT) : history;
  fragColor = mix(resolved, stretched, uStretch);
}
