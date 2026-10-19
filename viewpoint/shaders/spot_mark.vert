#version 330




#include "lib/spots.glsl"
uniform mat4 uViewProjection;
uniform vec2 uPixel;
out vec3 vColour;
const float AXIS = 12.0;
void main() {
  int i = gl_VertexID / 18, k = gl_VertexID - i * 18, corner = k % 6;
  Spot s = spotAt(i);
  bool glass = k >= 12;
  vec4 from = uViewProjection * vec4(s.pos, 1.0);
  vec4 to = uViewProjection * vec4(s.pos + (glass ? s.facing * 0.5 : s.axis * AXIS), 1.0);
  vColour = glass ? vec3(0.2, 0.9, 1.0) : s.kind == SPOT_LOW_BEAM ? vec3(1.0, 0.9, 0.2) : vec3(1.0, 0.15, 0.1);
  if (s.kind == SPOT_TORCH || from.w <= 0.0 || k >= 6 && to.w <= 0.0) {
    gl_Position = vec4(2.0, 2.0, 2.0, 1.0);
    return;
  }
  vec2 p = from.xy / from.w, q = to.xy / to.w;
  vec2 c = vec2(
      corner == 1 || corner == 2 || corner == 4 ? 1.0 : -1.0, corner == 2 || corner == 4 || corner == 5 ? 1.0 : -1.0);
  vec2 at = p + c * uPixel * 4.0;
  if (k >= 6) {
    vec2 d = (q - p) / uPixel;
    vec2 across = length(d) > 1.0e-3 ? normalize(vec2(-d.y, d.x)) : vec2(0.0, 1.0);
    at = mix(p, q, c.x * 0.5 + 0.5) + across * uPixel * c.y;
  }
  gl_Position = vec4(at, 0.0, 1.0);
}
