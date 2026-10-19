




uniform float uShadowPhase;
#include "lib/common.glsl"
#ifndef OPTION_SHADOW_SAMPLES
#define OPTION_SHADOW_SAMPLES 8
#endif

const float DISC_CASCADE = 1.5;
const float DISC_FAR = 1.0;
const float GOLDEN_ANGLE = 2.39996323;
float sunFilter(int c, vec2 uv, float z) {
  float radius = (c >= FIRST_FAR_MAP ? DISC_FAR : DISC_CASCADE) * uShadowMaps[c].z;
#if PIPELINE_TAA
  float spin = 6.2831853 * fract(ign(gl_FragCoord.xy) + uShadowPhase * 0.618034);
#else
  float spin = 0.0;
#endif
  float lit = 0.0;
  for (int i = 0; i < OPTION_SHADOW_SAMPLES; i++) {
    float r = sqrt((float(i) + 0.5) / float(OPTION_SHADOW_SAMPLES)) * radius;
    float a = float(i) * GOLDEN_ANGLE + spin;
    lit += shadowTap(c, vec3(uv + vec2(cos(a), sin(a)) * r, z));
  }
  return lit / float(OPTION_SHADOW_SAMPLES);
}
