



#ifndef VIEWPOINT_FOG_AMOUNT
#define VIEWPOINT_FOG_AMOUNT
const float FOG_START = 0.3, HAZE = 4.0;
float fogAt(float d, vec2 range) {
  float start = min(range.x, range.y * FOG_START);
  float t = clamp((d - start) / (range.y - start), 0.0, 1.0);
  return t * t * (3.0 - 2.0 * t);
}
float hazeAt(float h, vec2 haze) {
  return 1.0 - exp(-max(h - haze.y, 0.0) * HAZE / haze.x);
}
#endif
