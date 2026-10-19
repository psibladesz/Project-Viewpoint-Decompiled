#version 330


uniform vec4 uRects[64];
uniform float uIds[64];
flat out vec4 vRect;
const vec2 CORNERS[6] = vec2[6](vec2(0.0, 0.0), vec2(1.0, 0.0), vec2(1.0, 1.0), vec2(0.0, 0.0), vec2(1.0, 1.0),
    vec2(0.0, 1.0));
const int SIDE = 256;

void main() {
  int tile = gl_VertexID / 6, id = int(uIds[tile] + 0.5);
  vec2 texel = vec2(float(id % SIDE), float(id / SIDE)) + CORNERS[gl_VertexID % 6];
  gl_Position = vec4(texel / float(SIDE) * 2.0 - 1.0, 0.0, 1.0);
  vRect = uRects[tile];
}
