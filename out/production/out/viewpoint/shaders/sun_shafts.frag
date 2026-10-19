#version 330








in vec2 vUv;
uniform sampler2D uSource;
uniform vec2 uBodyUv;
uniform float uStep;
uniform float uDecay;
uniform float uFalloff;
uniform float uAspect;
uniform int uFrame;
out vec4 fragColor;
#include "lib/common.glsl"

const int TAPS = 8;

void main() {
  vec2 toBody = uBodyUv - vUv;
  float jitter = ign(gl_FragCoord.xy + 5.588238 * float(uFrame & 63));
  vec3 sum = vec3(0.0);
  float weight = 0.0, w = 1.0;
  for (int i = 0; i < TAPS; i++) {
    float t = (float(i) + jitter) * uStep;
    vec2 uv = vUv + toBody * t;
    bool on = t <= 1.0 && all(greaterThanEqual(uv, vec2(0.0))) && all(lessThanEqual(uv, vec2(1.0)));
    if (on) sum += texture(uSource, uv).rgb * w;
    weight += w;
    w *= uDecay;
  }
  float fade = uFalloff > 0.0 ? exp(-length(toBody * vec2(uAspect, 1.0)) / uFalloff) : 1.0;
  fragColor = vec4(sum / weight * fade, 1.0);
}
