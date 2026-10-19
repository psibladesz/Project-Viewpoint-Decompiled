

#ifndef VIEWPOINT_GTAO
#define VIEWPOINT_GTAO
const float GTAO_FALLOFF_RANGE = 0.615;
const float GTAO_DISTRIBUTION_POWER = 2.0;
const float GTAO_FINAL_POWER = 2.2;
const float GTAO_MIP_OFFSET = 3.30;
const float GTAO_TERM_SCALE = 1.5;
const float GTAO_DEPTH_MOST = 65504.0;
const int GTAO_LEVELS = 5;
const float GTAO_PI = 3.14159265, GTAO_HALF_PI = 1.57079633;



const float GTAO_PLANT_BLOCKS = 0.35;


vec3 gtaoViewPos(vec2 uv, float depth, vec2 tanHalf) {
  return vec3((uv * 2.0 - 1.0) * tanHalf * depth, -depth);
}


float gtaoAcos(float x) {
  float a = abs(x), r = (-0.156583 * a + GTAO_HALF_PI) * sqrt(max(1.0 - a, 0.0));
  return x >= 0.0 ? r : GTAO_PI - r;
}
#endif
