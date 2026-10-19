


#ifndef VIEWPOINT_PANINI
#define VIEWPOINT_PANINI
uniform vec3 uPanini;
uniform vec2 uPaniniTan;
vec2 paniniFrame(vec2 uv) {
  float d = uPanini.x;
  if (d <= 0.0) return uv;
  vec2 p = (uv * 2.0 - 1.0) * uPanini.yz;
  float q = p.x / (d + 1.0), qq = q * q;
  float c = (sqrt(1.0 + qq * (1.0 - d * d)) - qq * d) / (1.0 + qq);
  return p * ((d + c) / ((d + 1.0) * c)) / uPaniniTan * 0.5 + 0.5;
}
#endif
