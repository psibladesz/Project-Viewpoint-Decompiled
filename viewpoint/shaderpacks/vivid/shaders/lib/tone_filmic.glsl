


#ifndef VIVID_TONE_FILMIC
#define VIVID_TONE_FILMIC
#ifndef OPTION_CONTRAST
#define OPTION_CONTRAST 1.25
#endif
const vec3 TONE_LUMA = vec3(0.2126, 0.7152, 0.0722);

const float TONE_SHOULDER = 0.9;
const float TONE_WHITE = 2.0;
const float TONE_MID_IN = 0.18;
const float TONE_MID_OUT = 0.19;

const float DARK_KEEP = 0.5;
const float DARK_END = 0.15;

const float DARK_DESATURATION = 0.35;
const float DARK_COLOUR_END = 0.06;
const float WHITE_DRIFT = 0.5;
const float WHITE_START = 0.9;

vec3 toneCurve(vec3 x) {
  float a = OPTION_CONTRAST, ad = OPTION_CONTRAST * TONE_SHOULDER;
  float midA = pow(TONE_MID_IN, a), midAd = pow(TONE_MID_IN, ad);
  float whiteA = pow(TONE_WHITE, a), whiteAd = pow(TONE_WHITE, ad);
  float span = (whiteAd - midAd) * TONE_MID_OUT;
  float b = (whiteA * TONE_MID_OUT - midA) / span;
  float c = (whiteAd * midA - whiteA * midAd * TONE_MID_OUT) / span;
  x = min(x, vec3(TONE_WHITE));
  return pow(x, vec3(a)) / (pow(x, vec3(ad)) * b + c);
}

vec3 toneFilmic(vec3 x, out float luma) {
  vec3 toned = toneCurve(x);
  vec3 linear = x * (TONE_MID_OUT / TONE_MID_IN);
  vec3 color = mix(toned, linear, DARK_KEEP * (1.0 - smoothstep(0.0, DARK_END, x)));
  luma = dot(color, TONE_LUMA);
  color = mix(vec3(luma), color, 1.0 - DARK_DESATURATION * (1.0 - smoothstep(0.0, DARK_COLOUR_END, luma)));
  return mix(color, vec3(luma), WHITE_DRIFT * smoothstep(WHITE_START, 1.0, luma));
}
#endif
