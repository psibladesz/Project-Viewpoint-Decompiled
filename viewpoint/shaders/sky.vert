#version 330

uniform vec3 uForward;
uniform vec3 uRight;
uniform vec3 uUp;
out vec3 vRay;
out vec2 vUv;
void main() {
  vec2 p = vec2((gl_VertexID << 1) & 2, gl_VertexID & 2) * 2.0 - 1.0;
  vRay = uForward + uRight * p.x + uUp * p.y;
  vUv = p * 0.5 + 0.5;
  gl_Position = vec4(p, 0.0, 1.0);
}
