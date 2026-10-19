


#ifndef VIEWPOINT_FOG_AMOUNT
#define VIEWPOINT_FOG_AMOUNT


float fogAt(float d, vec2 range) {
  float t = clamp((d - range.x) / (range.y - range.x), 0.0, 1.0);
  return t * t * (3.0 - 2.0 * t);
}


float hazeAt(float h, vec2 haze) {
  return 1.0 - exp(-max(h - haze.y, 0.0) / haze.x);
}
#endif
