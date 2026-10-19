





#ifndef VIEWPOINT_FLOOR_MATERIALS
#define VIEWPOINT_FLOOR_MATERIALS
const int FAMILY_UNKNOWN = 0, FAMILY_NATURAL = 1, FAMILY_GRASS = 2, FAMILY_STREET = 3, FAMILY_PAVING = 4;
const int FAMILY_INTERIOR = 5, FAMILY_TILES = 6, FAMILY_WOOD = 7, FAMILY_CARPET = 8, FAMILY_BURNT = 9;
const int FAMILY_DECAL = 10;




const vec4 KIND_GRASS = vec4(0.85, 0.8, 0.15, 0.0), KIND_DIRT = vec4(0.95, 0.6, 0.5, 0.15);
const vec4 KIND_SAND = vec4(0.9, 0.3, 0.6, 0.0), KIND_GRAVEL = vec4(0.9, 0.4, 0.2, 0.9);
const vec4 KIND_ASPHALT = vec4(0.85, 0.3, 1.0, 0.8), KIND_CONCRETE = vec4(0.75, 0.15, 0.12, 0.0);
const vec4 KIND_PAVING = vec4(0.7, 1.0, 0.2, 0.05), KIND_WOOD = vec4(0.55, 0.6, 0.1, 0.0);
const vec4 KIND_TILE = vec4(0.25, 0.4, 0.0, 0.0), KIND_CARPET = vec4(0.95, 0.2, 0.5, 0.0);
const vec4 KIND_BURNT = vec4(0.95, 0.5, 0.4, 0.3), KIND_DECAL = vec4(0.4, 0.0, 0.0, 0.0);
const vec3 FLOOR_LUMA = vec3(0.2126, 0.7152, 0.0722);


float floorWarmth(vec3 c) {
  float most = max(c.r, max(c.g, c.b)), saturation = (most - min(c.r, min(c.g, c.b))) / max(most, 1.0e-4);
  return smoothstep(0.08, 0.2, c.r - c.b) * smoothstep(0.3, 0.5, saturation);
}




float jointHeight(vec3 c, float brick) {
  return mix(dot(c, FLOOR_LUMA), clamp(2.0 * (c.r - c.b), 0.0, 1.0), brick);
}



vec4 naturalKind(vec3 c) {
  float most = max(c.r, max(c.g, c.b)), least = min(c.r, min(c.g, c.b));
  float saturation = (most - least) / max(most, 1.0e-4);
  float green = smoothstep(0.0, 0.06, c.g - 0.95 * max(c.r, c.b));
  float grey = (1.0 - green) * (1.0 - smoothstep(0.08, 0.2, saturation));
  float earth = 1.0 - green - grey;
  return green * KIND_GRASS + grey * KIND_GRAVEL
      + earth * mix(KIND_DIRT, KIND_SAND, smoothstep(0.35, 0.55, dot(c, FLOOR_LUMA)));
}


vec4 floorKind(int family, vec3 c) {
  if (family == FAMILY_GRASS) return KIND_GRASS;
  if (family == FAMILY_STREET) return mix(KIND_ASPHALT, KIND_CONCRETE, smoothstep(0.3, 0.45, dot(c, FLOOR_LUMA)));
  if (family == FAMILY_PAVING) return KIND_PAVING;
  if (family == FAMILY_TILES) return KIND_TILE;
  if (family == FAMILY_WOOD) return KIND_WOOD;
  if (family == FAMILY_CARPET) return KIND_CARPET;
  if (family == FAMILY_BURNT) return KIND_BURNT;
  if (family == FAMILY_DECAL) return KIND_DECAL;
  if (family == FAMILY_INTERIOR) {
    float most = max(c.r, max(c.g, c.b)), saturation = (most - min(c.r, min(c.g, c.b))) / max(most, 1.0e-4);
    return mix(KIND_TILE, KIND_WOOD, smoothstep(0.04, 0.12, c.r - c.b) * smoothstep(0.15, 0.3, saturation));
  }

  return naturalKind(c);
}
#endif
