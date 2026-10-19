




#ifndef VIEWPOINT_LAMP_FIELD
#define VIEWPOINT_LAMP_FIELD
const int FIELD_COLUMNS = 8, FIELD_TEXELS = 84;
const float FIELD_PER_SQUARE = 2.0, FIELD_REACH = 21.0;

const int FIELD_STEPS = 86;


vec2 lampFieldLight(int i) {
  return -(uSceneOrigin.xz + uLightPos[i].xz);
}



vec2 lampFieldTexel(int i, vec2 world) {
  vec2 corner = floor(lampFieldLight(i)) - FIELD_REACH;
  vec2 square = floor(world);
  vec2 local = clamp(world, square + 0.25, square + 0.75) - corner;
  ivec2 tile = ivec2(i % FIELD_COLUMNS, i / FIELD_COLUMNS);
  return vec2(tile * FIELD_TEXELS) + clamp(local * FIELD_PER_SQUARE, vec2(0.5), vec2(float(FIELD_TEXELS) - 0.5));
}
#endif
