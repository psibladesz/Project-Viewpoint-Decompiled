#version 330





uniform mat4 uViewProjection;
uniform vec3 uEye;
uniform vec4 uDecals[216];
#include "constants.glsl"
#include "lib/far_relief.glsl"
out vec2 vUv;
flat out vec4 vRect;
flat out vec4 vTint;
const int VEC4S = 6;

const float PULL = 4.0;
const vec2 CORNERS[6] = vec2[6](vec2(0.0, 0.0), vec2(1.0, 0.0), vec2(1.0, 1.0), vec2(0.0, 0.0), vec2(1.0, 1.0),
                                vec2(0.0, 1.0));
void main() {
  int at = gl_VertexID / 6 * VEC4S;
  vec2 q = CORNERS[gl_VertexID % 6];
  vec4 origin = uDecals[at], along = uDecals[at + 1], up = uDecals[at + 2], uv = uDecals[at + 3];
  vec4 more = uDecals[at + 4];
  vec3 normal = normalize(cross(along.xyz, up.xyz)) * more.w;
  vec3 p = reliefLifted(origin.xyz + along.xyz * q.x + up.xyz * q.y);
  vUv = uv.xy + uv.zw * q.x + more.xy * q.y;
  vRect = uDecals[at + 5];
  vTint = vec4(along.w, up.w, more.z, origin.w);
  vec3 toEye = uEye - p;
  float d = length(toEye);
  if (dot(normal, uEye - reliefLifted(origin.xyz)) <= 0.0) {
    gl_Position = vec4(2.0, 2.0, 2.0, 1.0);
    return;
  }
  p += toEye / max(d, 0.001) * min((0.001 + d * d * 6.0e-6) * PULL, 0.5 * d);
  gl_Position = uViewProjection * vec4(p, 1.0);
}
