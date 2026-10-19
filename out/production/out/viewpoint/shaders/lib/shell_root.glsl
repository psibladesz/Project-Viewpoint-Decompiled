


#include "constants.glsl"
vec2 maskedSquare(vec3 local, int flags) {
  if ((flags & FLAG_ROOTED) == 0) return local.xz;
  return vec2((flags >> ROOT_X_SHIFT) & 255, (flags >> ROOT_Y_SHIFT) & 255) + 0.5;
}
