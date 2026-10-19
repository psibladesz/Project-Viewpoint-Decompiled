#version 330



in vec2 vUv;
uniform sampler2D uMarched;
out vec4 fragColor;
#include "lib/epipolar.glsl"

void main() {
  ivec2 place = ivec2(gl_FragCoord.xy);
  vec4 here = texelFetch(uMarched, place, 0);
  if (here.a > 0.5) {
    fragColor = vec4(here.rgb, 1.0);
    return;
  }
  int before = -1, after = -1;
  vec3 a = vec3(0.0), b = vec3(0.0);
  for (int k = 1; k <= EPI_STRIDE && before < 0 && place.x - k >= 0; k++) {
    vec4 m = texelFetch(uMarched, ivec2(place.x - k, place.y), 0);
    if (m.a > 0.5) {
      before = place.x - k;
      a = m.rgb;
    }
  }
  for (int k = 1; k <= EPI_STRIDE && after < 0 && place.x + k < EPI_SAMPLES; k++) {
    vec4 m = texelFetch(uMarched, ivec2(place.x + k, place.y), 0);
    if (m.a > 0.5) {
      after = place.x + k;
      b = m.rgb;
    }
  }
  fragColor = before < 0 || after < 0 ? vec4(0.0, 0.0, 0.0, 1.0)
      : vec4(mix(a, b, float(place.x - before) / float(after - before)), 1.0);
}
