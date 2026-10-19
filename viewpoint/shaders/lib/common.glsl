
#ifndef VIEWPOINT_COMMON
#define VIEWPOINT_COMMON


float ign(vec2 p) { return fract(52.9829189 * fract(dot(p, vec2(0.06711056, 0.00583715)))); }

float hg(float mu, float g) { return (1.0 - g * g) / (12.566371 * pow(1.0 + g * g - 2.0 * g * mu, 1.5)); }





vec3 shoulder(vec3 x) {
  const float k = 0.8;
  vec3 over = max(x - k, 0.0);
  return min(x, vec3(k)) + (1.0 - k) * (1.0 - exp(-over / (1.0 - k)));
}
#endif
