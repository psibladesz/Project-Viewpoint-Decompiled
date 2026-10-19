







#include "lib/lanczos.glsl"
uniform sampler2DArray uFloorPages;
uniform sampler2DArray uFloorSurfaces;
uniform int uFloorFilter;
bool floorLow(float record) { return record < 0.0; }


float floorLayer(float record) { return floorLow(record) ? -1.0 - round(record) : round(record); }
float floorShare(float record) { return 1.0 - abs(record - round(record)) / 0.4; }
vec4 lanczosFloor(vec2 uv, float layer) {
  vec2 size = vec2(textureSize(uFloorPages, 0).xy);
  vec2 p = uv * size - 0.5, f = fract(p);
  ivec2 base = ivec2(floor(p)) - 1, last = ivec2(size) - 1;
  vec4 sum = vec4(0.0), lo = vec4(1.0), hi = vec4(0.0);
  float total = 0.0;
  for (int j = 0; j < 4; j++) {
    float wy = lanczos2(float(j - 1) - f.y);
    for (int i = 0; i < 4; i++) {
      float w = lanczos2(float(i - 1) - f.x) * wy;
      vec4 t = texelFetch(uFloorPages, ivec3(clamp(base + ivec2(i, j), ivec2(0), last), int(layer)), 0);
      sum += t * w;
      total += w;
      if (i == 1 || i == 2) if (j == 1 || j == 2) { lo = min(lo, t); hi = max(hi, t); }
    }
  }
  return clamp(sum / total, lo, hi);
}






vec3 floorSquare(vec2 uv, float record, vec2 world) {
  vec2 size = vec2(textureSize(uFloorPages, 0).xy), wx = dFdx(world), wy = dFdy(world);
  bool across = dot(wx, wx) >= dot(wy, wy);
  float squares = max(length(across ? wx : wy), 1.0e-6);
  float texels = length((across ? dFdx(uv) : dFdy(uv)) * size) / squares;
  vec4 c = textureLod(uFloorPages, vec3(uv, floorLayer(record)), log2(max(texels, 1.0)));
  return c.a > 0.0 ? c.rgb / c.a : vec3(0.0);
}



vec4 floorSurface(vec2 uv, float record) {
  vec4 s = texture(uFloorSurfaces, vec3(uv, floorLayer(record)));
  vec2 eastSouth = s.xy * 2.0 - 1.0;
  vec3 n = vec3(-eastSouth.x, sqrt(max(1.0 - dot(eastSouth, eastSouth), 0.0)), -eastSouth.y);
  return vec4(normalize(n), s.z);
}

vec4 sampleFloor(vec2 uv, float record) {
  float layer = floorLayer(record);
  vec2 size = vec2(textureSize(uFloorPages, 0).xy), dx = dFdx(uv), dy = dFdy(uv);
  vec4 c = uFloorFilter == FILTER_LANCZOS && magnified(size, dx, dy) ? lanczosFloor(uv, layer)
      : uFloorFilter == FILTER_SHARP ? textureGrad(uFloorPages, vec3(sharpUv(uv, size, dx, dy), layer), dx, dy)
      : texture(uFloorPages, vec3(uv, layer));
  if (uWireframe != 0) return vec4(uWireframe == 2 || c.a < 0.3 ? vec3(0.8) : c.rgb / c.a, 1.0);
  if (c.a < 0.3) discard;
  return vec4(c.rgb / c.a, 1.0);
}
