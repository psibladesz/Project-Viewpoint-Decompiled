#version 330


in vec2 vCorner;
in vec3 vColour;
in float vStreak;
in float vLong;
out vec4 fragColor;
void main() {
  float r2 = dot(vCorner, vCorner);
  float glow = r2 < 1.0 ? exp(-r2 * 4.0) + 0.3 * exp(-r2 * 1.2) * (1.0 - r2) : 0.0;
  float streak = vStreak * exp(-vCorner.y * vCorner.y * 60.0) * max(1.0 - abs(vCorner.x) / vLong, 0.0);
  fragColor = vec4(vColour * (glow + streak), 0.0);
}
