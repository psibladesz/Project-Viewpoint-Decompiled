






#ifndef VIEWPOINT_BOUNCE_CASCADES
#define VIEWPOINT_BOUNCE_CASCADES
const int CASCADE_SQUARES = 64;
const int CASCADE_SIDE = 128;
const int CASCADES = 4;
const float CASCADE_FIRST = 1.0;
const float CASCADE_TAU = 6.28318531;


int cascadeSpacing(int i) { return 1 << i; }
int cascadeBlock(int i) { return 2 << i; }


float cascadeStart(int i) {
  return CASCADE_FIRST * float((1 << (2 * i)) - 1) / 3.0;
}


vec2 cascadeDirection(int i, int d) {
  int b = cascadeBlock(i);
  float angle = CASCADE_TAU * (float(d) + 0.5) / float(b * b);
  return vec2(cos(angle), sin(angle));
}


vec2 cascadeProbe(int i, ivec2 p) {
  return (vec2(p) + 0.5) * float(cascadeSpacing(i));
}


ivec2 cascadeTexel(int i, ivec2 p, int d) {
  int b = cascadeBlock(i);
  return p * b + ivec2(d % b, d / b);
}
#endif
