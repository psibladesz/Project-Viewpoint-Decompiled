

#ifndef VIEWPOINT_CUBE_SHADOW
#define VIEWPOINT_CUBE_SHADOW
const vec3 CUBE_FORWARD[6] = vec3[6](vec3(1.0, 0.0, 0.0), vec3(-1.0, 0.0, 0.0), vec3(0.0, 1.0, 0.0),
                                     vec3(0.0, -1.0, 0.0), vec3(0.0, 0.0, 1.0), vec3(0.0, 0.0, -1.0));
const vec3 CUBE_UP[6] = vec3[6](vec3(0.0, 1.0, 0.0), vec3(0.0, 1.0, 0.0), vec3(0.0, 0.0, 1.0),
                                vec3(0.0, 0.0, 1.0), vec3(0.0, 1.0, 0.0), vec3(0.0, 1.0, 0.0));


bool cubeTexel(vec3 v, float near, float far, out int face, out vec2 uv, out float depth) {
  vec3 a = abs(v);
  face = a.x >= a.y && a.x >= a.z ? (v.x > 0.0 ? 0 : 1) : a.y >= a.z ? (v.y > 0.0 ? 2 : 3) : (v.z > 0.0 ? 4 : 5);
  vec3 forward = CUBE_FORWARD[face], right = cross(forward, CUBE_UP[face]), up = cross(right, forward);
  float along = dot(v, forward);
  if (along <= near) return false;
  depth = 0.5 + 0.5 * ((far + near) / (far - near) - 2.0 * far * near / ((far - near) * along));
  uv = 0.5 + 0.5 * vec2(dot(v, right), dot(v, up)) / along;
  return true;
}
#endif
