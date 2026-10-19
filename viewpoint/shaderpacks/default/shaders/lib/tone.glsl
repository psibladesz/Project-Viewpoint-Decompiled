












#ifndef DEFAULT_TONE
#define DEFAULT_TONE
#include "lib/tone_filmic.glsl"
#ifndef OPTION_TONE_CURVE
#define OPTION_TONE_CURVE 0
#define OPTION_TONE_CURVE_FILMIC 0
#define OPTION_TONE_CURVE_NEUTRAL 1
#define OPTION_TONE_CURVE_TONY 2
#define OPTION_TONE_CURVE_UNCHARTED 3
#endif
uniform sampler3D uToneLut;

const float NEUTRAL_START = 0.8 - 0.04, NEUTRAL_DESATURATION = 0.15;

const float TONY_SIZE = 48.0;



const float HABLE_A = 0.15, HABLE_B = 0.50, HABLE_C = 0.10, HABLE_D = 0.20, HABLE_E = 0.02, HABLE_F = 0.30;
const float HABLE_WHITE = 11.2, HABLE_EXPOSURE = 3.11;

vec3 toneNeutral(vec3 color) {
  float x = min(color.r, min(color.g, color.b));
  float offset = x < 0.08 ? x - 6.25 * x * x : 0.04;
  color -= offset;
  float peak = max(color.r, max(color.g, color.b));
  if (peak < NEUTRAL_START) return color;
  float d = 1.0 - NEUTRAL_START;
  float newPeak = 1.0 - d * d / (peak + d - NEUTRAL_START);
  color *= newPeak / peak;
  float g = 1.0 - 1.0 / (NEUTRAL_DESATURATION * (peak - newPeak) + 1.0);
  return mix(color, vec3(newPeak), g);
}



vec3 toneTony(vec3 x) {
  vec3 encoded = x / (x + 1.0);
  return texture(uToneLut, encoded * ((TONY_SIZE - 1.0) / TONY_SIZE) + 0.5 / TONY_SIZE).rgb;
}

vec3 hable(vec3 x) {
  return (x * (HABLE_A * x + HABLE_C * HABLE_B) + HABLE_D * HABLE_E) / (x * (HABLE_A * x + HABLE_B) + HABLE_D * HABLE_F)
      - HABLE_E / HABLE_F;
}

vec3 toneUncharted(vec3 x) { return hable(x * HABLE_EXPOSURE) / hable(vec3(HABLE_WHITE)); }

vec3 tone(vec3 x, out float luma) {
#if OPTION_TONE_CURVE == OPTION_TONE_CURVE_NEUTRAL
  vec3 color = toneNeutral(x);
#elif OPTION_TONE_CURVE == OPTION_TONE_CURVE_TONY
  vec3 color = toneTony(x);
#elif OPTION_TONE_CURVE == OPTION_TONE_CURVE_UNCHARTED
  vec3 color = toneUncharted(x);
#else
  return toneFilmic(x, luma);
#endif
#if OPTION_TONE_CURVE != OPTION_TONE_CURVE_FILMIC
  luma = dot(color, TONE_LUMA);
  return color;
#endif
}
#endif
