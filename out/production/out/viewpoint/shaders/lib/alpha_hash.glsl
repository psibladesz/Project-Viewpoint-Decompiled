






#ifndef VIEWPOINT_ALPHA_HASH
#define VIEWPOINT_ALPHA_HASH
uniform int uAlphaHash;
uniform float uAlphaHashShift;

float alphaHash2(vec2 p) {
  return fract(1.0e4 * sin(17.0 * p.x + 0.1 * p.y) * (0.1 + abs(sin(13.0 * p.y + p.x))));
}

float alphaHash3(vec3 p) {
  return alphaHash2(vec2(alphaHash2(p.xy), p.z));
}



float alphaHashThreshold(vec3 anchor, vec3 dx, vec3 dy) {
  float level = log2(1.0 / max(max(length(dx), length(dy)), 1.0e-7));
  vec2 scales = exp2(vec2(floor(level), ceil(level)));
  float t = fract(level);
  float x = mix(alphaHash3(floor(scales.x * anchor)), alphaHash3(floor(scales.y * anchor)), t);
  float a = min(t, 1.0 - t);
  float u = x < a ? x * x / (2.0 * a * (1.0 - a))
      : x < 1.0 - a ? (x - 0.5 * a) / (1.0 - a)
      : 1.0 - (1.0 - x) * (1.0 - x) / (2.0 * a * (1.0 - a));
  float shifted = clamp(u, 1.0e-5, 1.0) + uAlphaHashShift;
  return max(shifted > 1.0 ? shifted - 1.0 : shifted, 1.0e-5);
}
#endif
