#version 330



uniform vec4 uRect;
uniform vec4 uUv;
out vec2 vUv;
const vec2 CORNERS[6] = vec2[6](vec2(0.0, 0.0), vec2(1.0, 0.0), vec2(1.0, 1.0), vec2(0.0, 0.0), vec2(1.0, 1.0), vec2(0.0, 1.0));
void main() {
  vec2 c = CORNERS[gl_VertexID];
  vUv = mix(uUv.xy, uUv.zw, c);
  gl_Position = vec4(mix(uRect.xy, uRect.zw, c) * 2.0 - 1.0, 0.0, 1.0);
}
