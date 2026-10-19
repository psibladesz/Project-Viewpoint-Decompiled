



#ifndef VIEWPOINT_DISTANT_RAIN
#define VIEWPOINT_DISTANT_RAIN
const float DISTANT_NEAR = 30.0, DISTANT_FAR = 16000.0;
const int DISTANT_ROWS = 64;

float distantDistance(float u) { return DISTANT_NEAR + (DISTANT_FAR - DISTANT_NEAR) * u * u; }

float distantRainAt(sampler2D table, float turn, float ground) {
  float u = sqrt(clamp((ground - DISTANT_NEAR) / (DISTANT_FAR - DISTANT_NEAR), 0.0, 1.0));
  return textureLod(table, vec2(turn, (u * float(DISTANT_ROWS - 1) + 0.5) / float(DISTANT_ROWS)), 0.0).r;
}
#endif
