




#ifndef VIVID_MOTION
#define VIVID_MOTION
uniform sampler2D uDepth;
uniform sampler2D uVelocity;
uniform mat4 uInvPlainViewProjection;
uniform mat4 uPrevious;
uniform float uPreviousValid;
#ifndef OPTION_MOTION_BLUR_STRENGTH
#define OPTION_MOTION_BLUR_STRENGTH 0.5
#endif
const float MOTION_MAX = 32.0;

const float MOTION_TILE = 16.0;
vec2 motionAt(vec2 uv, vec2 frameSize) {
  vec4 model = texture(uVelocity, uv);
  vec2 moved;
  if (model.w > 0.5) {
    moved = model.xy;
  } else {
    vec4 p = uInvPlainViewProjection * vec4(uv * 2.0 - 1.0, texture(uDepth, uv).r * 2.0 - 1.0, 1.0);
    vec4 was = uPrevious * vec4(p.xyz / p.w, 1.0);
    moved = was.w > 0.0 ? uv - (was.xy / was.w * 0.5 + 0.5) : vec2(0.0);
  }
  vec2 px = moved * frameSize * OPTION_MOTION_BLUR_STRENGTH * uPreviousValid;
  float len = length(px);
  return len > MOTION_MAX ? px * (MOTION_MAX / len) : px;
}
#endif
