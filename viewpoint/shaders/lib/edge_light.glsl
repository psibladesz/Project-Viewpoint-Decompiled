






#ifndef VIEWPOINT_EDGE_LIGHT
#define VIEWPOINT_EDGE_LIGHT
const int EDGE_LIT = 256, EDGE_ACROSS_X = 512;
const vec2 EDGE_SQUARELY = vec2(0.25, 0.75);


vec4 edgeLight(int beyond, int flags, vec3 pos, vec3 n, vec3 eye, vec4 own) {
  if (dot(n, eye - pos) < 0.0) n = -n;
  float across = (flags & EDGE_ACROSS_X) != 0 ? n.x : n.z;
  return mix(own, texelFetch(uDraws, beyond), smoothstep(EDGE_SQUARELY.x, EDGE_SQUARELY.y, across));
}
#endif
