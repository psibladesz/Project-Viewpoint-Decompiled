





#ifndef FADE_GIVEN
flat in vec2 vFade;
#endif
uniform float uFadeNoise;
float fadeNoise() {
  return fract(52.9829189 * fract(dot(gl_FragCoord.xy + uFadeNoise, vec2(0.06711056, 0.00583715))));
}
void fadeDither() {
  if (vFade.x > 0.0 || vFade.y <= 1.0) {
    float n = fadeNoise();
    if (n < vFade.x || n >= vFade.y) discard;
  }
}


bool builtOver(float share) {
  return fadeNoise() < share;
}


float shellMaskValue(vec3 texel) {
  vec3 m = texel * 255.0;
  return fadeNoise() * 255.0 < m.g ? m.r : m.b;
}
