




#include "constants.glsl"
uniform sampler2DArray uCellLight;
uniform isampler2D uCellTable;
uniform vec3 uDaylight;

const vec3 ROOM_LIGHT = vec3(229.0, 216.0, 209.0) / 255.0;
int cellLayer(vec2 square) {
  return texelFetch(uCellTable, ivec2(mod(floor(square / 256.0), float(CELL_WINDOW))), 0).r - 1;
}



vec4 farLight(vec2 square, float height, vec3 n) {
  vec2 at = square + vec2(-n.x, -n.z) * 0.3;
  int layer = cellLayer(at);
  if (layer < 0) return vec4(uDaylight, 1.0);
  vec3 lamps = texture(uCellLight, vec3(fract(at / 256.0), float(layer))).rgb;
  float level = max(height / SQRT6 + n.y * 0.05, 0.0);
  return vec4(max(uDaylight, lamps * clamp(1.0 - level * 0.3, 0.0, 1.0)), 1.0);
}

bool litRoom(vec2 square, float level) {
  int layer = cellLayer(square);
  if (layer < 0 || level < 0.0 || level >= 8.0) return false;
  ivec2 texel = ivec2(fract(square / 256.0) * float(textureSize(uCellLight, 0).x));
  int bits = int(texelFetch(uCellLight, ivec3(texel, layer), 0).a * 255.0 + 0.5);
  return (bits >> int(level) & 1) != 0;
}

vec3 roomGlow(float glow) {
  return max(ROOM_LIGHT - uDaylight, vec3(0.0)) * glow;
}
