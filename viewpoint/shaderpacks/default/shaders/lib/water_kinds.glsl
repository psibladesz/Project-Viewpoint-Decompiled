


#ifndef DEFAULT_WATER_KINDS
#define DEFAULT_WATER_KINDS
struct WaterBody {
  float depth;
  float bank;
  vec3 absorb;
  vec3 scatter;
  vec3 bed;
};





WaterBody waterBody(float kind) {
  if (kind < 0.5) {
    return WaterBody(1.8, 0.05, vec3(0.35, 0.06, 0.03), vec3(0.03, 0.09, 0.11), vec3(0.55, 0.74, 0.8));
  }
  if (kind > 3.5) {
    return WaterBody(6.0, 8.0, vec3(1.3, 1.0, 1.15), vec3(0.08, 0.075, 0.05), vec3(0.3, 0.26, 0.19));
  }
  if (kind > 2.5) {
    return WaterBody(0.7, 2.0, vec3(0.7, 0.4, 0.45), vec3(0.03, 0.045, 0.035), vec3(0.42, 0.39, 0.32));
  }
  if (kind > 1.5) {
    return WaterBody(4.0, 5.0, vec3(1.0, 0.65, 0.85), vec3(0.04, 0.06, 0.04), vec3(0.34, 0.31, 0.23));
  }
  return WaterBody(1.5, 3.0, vec3(1.4, 0.9, 1.3), vec3(0.05, 0.075, 0.03), vec3(0.3, 0.28, 0.2));
}
#endif
