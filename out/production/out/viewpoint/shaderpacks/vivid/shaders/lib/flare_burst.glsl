


const float RAYS = 7.0, BURST_FALL = 9.0;

float flareBurst(vec2 d) {
  float r = length(d), angle = atan(d.y, d.x);
  float rays = pow(abs(cos(angle * RAYS * 0.5)), 24.0) * 0.6 + pow(abs(cos(angle * RAYS * 0.5 + 0.9)), 48.0) * 0.4;

  return exp(-r * BURST_FALL) * (0.35 + rays) + exp(-r * 2.5) * 0.08;
}
