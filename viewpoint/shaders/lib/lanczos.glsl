







#ifndef VIEWPOINT_LANCZOS
#define VIEWPOINT_LANCZOS
const int FILTER_LANCZOS = 5;
const int FILTER_SHARP = 6;

vec2 sharpUv(vec2 uv, vec2 size, vec2 dx, vec2 dy) {
  vec2 p = uv * size, corner = floor(p + 0.5);
  vec2 pixel = max((abs(dx) + abs(dy)) * size, vec2(1.0e-5));
  return (corner + clamp((p - corner) / pixel, -0.5, 0.5)) / size;
}
float lanczos2(float x) {
  x = abs(x);
  if (x < 1.0e-4) return 1.0;
  if (x >= 2.0) return 0.0;
  float px = 3.14159265 * x;
  return 2.0 * sin(px) * sin(px * 0.5) / (px * px);
}

bool magnified(vec2 size, vec2 dx, vec2 dy) {
  return max(length(dx * size), length(dy * size)) < 1.0;
}


vec4 lanczos(sampler2D tex, vec2 uv, ivec2 lo, ivec2 hi, bool wrap) {
  vec2 p = uv * vec2(textureSize(tex, 0)) - 0.5, f = fract(p);
  ivec2 base = ivec2(floor(p)) - 1, span = hi - lo + 1;
  vec4 sum = vec4(0.0), low = vec4(1.0), high = vec4(0.0);
  float total = 0.0;
  for (int j = 0; j < 4; j++) {
    float wy = lanczos2(float(j - 1) - f.y);
    for (int i = 0; i < 4; i++) {
      float w = lanczos2(float(i - 1) - f.x) * wy;
      ivec2 at = base + ivec2(i, j);
      at = wrap ? at - span * ivec2(floor(vec2(at - lo) / vec2(span))) : clamp(at, lo, hi);
      vec4 t = texelFetch(tex, at, 0);
      sum += t * w;
      total += w;
      if (i == 1 || i == 2) if (j == 1 || j == 2) { low = min(low, t); high = max(high, t); }
    }
  }
  return clamp(sum / total, low, high);
}
#endif
