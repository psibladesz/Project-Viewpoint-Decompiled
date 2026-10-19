



const float RAYS = 12.0, BURST_FALL = 30.0, BURST = 0.12;

float flareBurst(vec2 d) {
  float r = length(d), angle = atan(d.y, d.x);
  float rays = pow(abs(cos(angle * RAYS * 0.5)), 48.0) * 0.6 + pow(abs(cos(angle * RAYS * 0.5 + 0.9)), 96.0) * 0.4;

  return exp(-r * BURST_FALL) * BURST * (0.5 + rays);
}
