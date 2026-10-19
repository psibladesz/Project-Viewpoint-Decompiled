




#ifndef VIEWPOINT_FROXELS
#define VIEWPOINT_FROXELS
uniform vec3 uFroxels;
uniform float uFroxelRange;
const int FROXEL_COLUMNS = 8;


float froxelDistance(float slices) {
  float u = slices / uFroxels.z;
  return uFroxelRange * u * u;
}


float froxelSlices(float distance) {
  return sqrt(max(distance, 0.0) / uFroxelRange) * uFroxels.z;
}


ivec3 froxelAt(ivec2 texel) {
  ivec2 size = ivec2(uFroxels.xy), tile = texel / size;
  return ivec3(texel - tile * size, tile.y * FROXEL_COLUMNS + tile.x);
}


ivec2 froxelTexel(ivec3 f) {
  ivec2 size = ivec2(uFroxels.xy);
  return ivec2(f.z % FROXEL_COLUMNS, f.z / FROXEL_COLUMNS) * size + f.xy;
}


vec4 froxelSlice(sampler2D atlas, vec2 xy, int slice) {
  vec2 size = uFroxels.xy;
  vec2 inTile = clamp(xy, vec2(0.5), size - 0.5);
  vec2 corner = vec2(float(slice % FROXEL_COLUMNS), float(slice / FROXEL_COLUMNS)) * size;
  return texture(atlas, (corner + inTile) / vec2(textureSize(atlas, 0)));
}



vec4 froxelValue(sampler2D atlas, vec3 place) {
  float z = clamp(place.z - 0.5, 0.0, uFroxels.z - 1.0);
  int s0 = int(floor(z)), s1 = min(s0 + 1, int(uFroxels.z) - 1);
  return mix(froxelSlice(atlas, place.xy, s0), froxelSlice(atlas, place.xy, s1), z - float(s0));
}
#endif
