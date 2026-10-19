

uniform int uLodView;
const int LOD_NEAR = 0, LOD_FLOOR_LOW = 1, LOD_SHELL_FULL = 2, LOD_SHELL_LITE = 3, LOD_BOXES = 4, LOD_INSIDES = 5;
const vec3 LOD_COLORS[6] = vec3[6](
    vec3(0.1, 0.85, 0.2),
    vec3(0.05, 0.8, 0.75),
    vec3(0.95, 0.85, 0.05),
    vec3(1.0, 0.45, 0.05),
    vec3(0.9, 0.05, 0.05),
    vec3(0.8, 0.0, 0.8));
vec3 lodView(vec3 color, int lod) {
  if (uLodView == 0) return color;
  return uLodView == 2 ? LOD_COLORS[lod] : mix(color, LOD_COLORS[lod], 0.55);
}
