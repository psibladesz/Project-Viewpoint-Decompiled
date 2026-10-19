

#ifndef VIEWPOINT_WET
#define VIEWPOINT_WET
const float WET_DARK = 0.3;

vec3 wetGround(vec3 color, vec3 n, float outdoors, float wet) {
  return color * (1.0 - WET_DARK * wet * step(0.9, n.y) * step(0.5, outdoors));
}
#endif
