





#ifndef VIEWPOINT_EPIPOLAR
#define VIEWPOINT_EPIPOLAR
uniform vec2 uEpipole;



const int EPI_SLICES = 512, EPI_SAMPLES = 256, EPI_STRIDE = 16;
const float EPI_BREAK = 0.1;



vec2 epiEdgePoint(float place) {
  float p = fract(place) * 4.0;
  if (p < 1.0) return vec2(p, 0.0);
  if (p < 2.0) return vec2(1.0, p - 1.0);
  if (p < 3.0) return vec2(3.0 - p, 1.0);
  return vec2(0.0, 4.0 - p);
}


vec2 epiBox(vec2 o, vec2 d) {
  vec2 safe = vec2(abs(d.x) < 1.0e-9 ? 1.0e-9 : d.x, abs(d.y) < 1.0e-9 ? 1.0e-9 : d.y);
  vec2 a = (vec2(0.0) - o) / safe, b = (vec2(1.0) - o) / safe;
  vec2 lo = min(a, b), hi = max(a, b);
  return vec2(max(lo.x, lo.y), min(hi.x, hi.y));
}


float epiExitPlace(vec2 uv) {
  vec2 d = uv - uEpipole;
  vec2 e = clamp(uEpipole + d * epiBox(uEpipole, d).y, 0.0, 1.0);
  vec2 off = min(e, 1.0 - e);
  if (off.y <= off.x) return e.y < 0.5 ? e.x / 4.0 : (3.0 - e.x) / 4.0;
  return e.x > 0.5 ? (1.0 + e.y) / 4.0 : fract((4.0 - e.y) / 4.0);
}


vec4 epiLine(int slice) {
  vec2 end = epiEdgePoint((float(slice) + 0.5) / float(EPI_SLICES)), d = end - uEpipole;
  bool inside = all(greaterThanEqual(uEpipole, vec2(0.0))) && all(lessThanEqual(uEpipole, vec2(1.0)));
  vec2 t = epiBox(uEpipole, d);
  vec2 start = inside ? uEpipole : uEpipole + d * clamp(t.x, 0.0, 1.0);
  return t.x > t.y ? vec4(end, end) : vec4(start, end);
}





vec4 epiUnwarp(sampler2D coords, sampler2D light, vec2 uv, float along) {
  float f = epiExitPlace(uv) * float(EPI_SLICES) - 0.5, first = floor(f);
  vec4 sum = vec4(0.0);
  for (int i = 0; i < 2; i++) {
    int slice = int(mod(first + float(i), float(EPI_SLICES)));
    vec4 line = epiLine(slice);
    vec2 d = line.zw - line.xy;
    float length2 = dot(d, d);
    if (length2 < 1.0e-12) continue;
    float k = clamp(dot(uv - line.xy, d) / length2, 0.0, 1.0) * float(EPI_SAMPLES - 1), k0 = floor(k);
    for (int j = 0; j < 2; j++) {
      ivec2 place = ivec2(min(int(k0) + j, EPI_SAMPLES - 1), slice);
      float seen = texelFetch(coords, place, 0).z;
      bool near = seen >= 0.0 && abs(seen - along) <= EPI_BREAK * min(seen, along);
      float w = (i == 0 ? 1.0 - (f - first) : f - first) * (j == 0 ? 1.0 - (k - k0) : k - k0) * (near ? 1.0 : 0.0);
      sum += vec4(texelFetch(light, place, 0).rgb, 1.0) * w;
    }
  }
  return sum;
}


vec2 epiPlace(int slice, int sample) {
  vec4 line = epiLine(slice);
  return mix(line.xy, line.zw, float(sample) / float(EPI_SAMPLES - 1));
}
#endif
