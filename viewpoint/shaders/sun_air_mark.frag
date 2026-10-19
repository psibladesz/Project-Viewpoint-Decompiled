#version 330










in vec2 vUv;
uniform sampler2D uCoords;
uniform sampler2D uMarched;
uniform int uStride;
out vec4 fragColor;
#include "lib/epipolar.glsl"

const float REFINE = 0.1;

float luminance(vec3 c) {
  return dot(c, vec3(0.2126, 0.7152, 0.0722));
}


bool first(ivec2 place, float here) {
  float before = place.x > 0 ? texelFetch(uCoords, place - ivec2(1, 0), 0).z : -1.0;
  float after = place.x < EPI_SAMPLES - 1 ? texelFetch(uCoords, place + ivec2(1, 0), 0).z : -1.0;
  return here < 0.0 || place.x % EPI_STRIDE == 0 || before < 0.0 || after < 0.0
      || abs(here - 0.5 * (before + after)) > EPI_BREAK * here;
}



bool refines(ivec2 place) {
  if (place.x % uStride != 0 || texelFetch(uMarched, place, 0).a > 0.5) return false;
  int coarse = uStride * 4, from = place.x / coarse * coarse, to = min(from + coarse, EPI_SAMPLES - 1);
  vec4 a = texelFetch(uMarched, ivec2(from, place.y), 0), b = texelFetch(uMarched, ivec2(to, place.y), 0);
  if (a.a < 0.5 || b.a < 0.5) return false;
  float la = luminance(a.rgb), lb = luminance(b.rgb);
  return abs(la - lb) > REFINE * max(la, lb) + 1.0e-6;
}

void main() {
  ivec2 place = ivec2(gl_FragCoord.xy);
  if (uStride == EPI_STRIDE ? !first(place, texelFetch(uCoords, place, 0).z) : !refines(place)) discard;
  fragColor = vec4(0.0);
}
