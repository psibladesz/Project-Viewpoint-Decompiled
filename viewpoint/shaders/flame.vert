#version 330



uniform mat4 uViewProjection;
uniform vec3 uEye;
uniform vec3 uRight;
uniform vec4 uCards[240];
out vec2 vUv;
out float vFromEye;
flat out vec4 vTint;
flat out float vGlow;
const vec2 CORNERS[6] = vec2[6](vec2(0.0, 0.0), vec2(1.0, 0.0), vec2(1.0, 1.0), vec2(0.0, 0.0), vec2(1.0, 1.0),
                                vec2(0.0, 1.0));
void main() {
  int at = gl_VertexID / 6 * 5;
  vec2 q = CORNERS[gl_VertexID % 6];
  vec4 foot = uCards[at], rect = uCards[at + 1], uv = uCards[at + 2];
  vec3 toEye = vec3(uEye.x - foot.x, 0.0, uEye.z - foot.z);
  vec3 towards = dot(toEye, toEye) > 1.0e-6 ? normalize(toEye) : vec3(0.0);
  vec3 p = foot.xyz + towards * foot.w + uRight * mix(rect.x, rect.z, q.x) + vec3(0.0, mix(rect.y, rect.w, q.y), 0.0);
  vUv = vec2(mix(uv.x, uv.z, q.x), mix(uv.w, uv.y, q.y));
  vTint = uCards[at + 3];
  vGlow = uCards[at + 4].x;
  vFromEye = distance(p, uEye);
  gl_Position = uViewProjection * vec4(p, 1.0);
}
