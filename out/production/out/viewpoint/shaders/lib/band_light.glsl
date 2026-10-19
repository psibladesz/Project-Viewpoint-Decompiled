





#include "lib/light_grid.glsl"
#include "lib/far_light.glsl"
uniform isampler3D uBandLight;
vec4 bandLight(vec2 square, float height, vec3 n) {
  vec2 at = square + vec2(-n.x, -n.z) * 0.3;
  vec2 chunk = floor(at / 8.0);
  int level = int(floor(height / SQRT6 + n.y * 0.05));
  if (level < 0 || level >= BAND_LEVELS) return farLight(square, height, n);
  ivec2 texel = ivec2(mod(chunk, float(BAND_WINDOW)));
  int slot = texelFetch(uBandLight, ivec3(texel, level), 0).r - 1;
  if (slot < 0) return farLight(square, height, n);
  vec2 origin = vec2(mod(float(slot), LIGHT_COLUMNS), floor(float(slot) / LIGHT_COLUMNS)) * LIGHT_GRID;
  return gridLight(origin, clamp(at - chunk * 8.0, 0.01, 7.99) + 1.0);
}
