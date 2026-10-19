


#ifndef VIVID_TONE
#define VIVID_TONE
#include "lib/tone_filmic.glsl"
vec3 tone(vec3 x, out float luma) {
  return toneFilmic(x, luma);
}
#endif
