





#ifndef VIEWPOINT_WALL_SURFACE
#define VIEWPOINT_WALL_SURFACE
const int WALL_UNKNOWN = 1, WALL_EXTERIOR = 2, WALL_WOOD = 3, WALL_COMMERCIAL = 4, WALL_GARAGE = 5;
const int WALL_INTERIOR = 6, WALL_TILED = 7, WALL_BURNT = 8;




const vec2 WALL_MASONRY = vec2(0.85, 1.0), WALL_PAINTED = vec2(0.7, 0.8), WALL_TIMBER = vec2(0.7, 0.9);
const vec2 WALL_METAL = vec2(0.45, 0.8), WALL_PLASTER = vec2(0.8, 0.3), WALL_PANEL = vec2(0.55, 0.7);
const vec2 WALL_GLAZED = vec2(0.25, 0.5), WALL_CHARRED = vec2(0.95, 0.8);

const float WALL_RELIEF = 0.05;

const float WALL_LEVELS = 3.0;
const vec3 WALL_LUMA = vec3(0.2126, 0.7152, 0.0722);


float wallWarmth(vec3 c) {
  float most = max(c.r, max(c.g, c.b)), saturation = (most - min(c.r, min(c.g, c.b))) / max(most, 1.0e-4);
  return smoothstep(0.08, 0.2, c.r - c.b) * smoothstep(0.3, 0.5, saturation);
}


vec2 wallKind(int family, vec3 c) {
  if (family == WALL_WOOD) return WALL_TIMBER;
  if (family == WALL_COMMERCIAL) return WALL_MASONRY;
  if (family == WALL_GARAGE) return WALL_METAL;
  if (family == WALL_TILED) return WALL_GLAZED;
  if (family == WALL_BURNT) return WALL_CHARRED;
  if (family == WALL_INTERIOR) {
    return mix(WALL_PLASTER, WALL_PANEL, wallWarmth(c) * (1.0 - smoothstep(0.25, 0.45, dot(c, WALL_LUMA))));
  }

  return mix(WALL_PAINTED, WALL_MASONRY, wallWarmth(c));
}




float wallHeight(vec3 c, float brick) {
  return mix(dot(c, WALL_LUMA), clamp(2.0 * (c.r - c.b), 0.0, 1.0), brick);
}


float wallLight(sampler2D tex, vec2 uv, float level, vec4 inside, float brick, float here) {
  vec4 c = textureLod(tex, clamp(uv, inside.xy, inside.zw), level);
  return c.a >= 0.3 ? wallHeight(c.rgb / c.a, brick) : here;
}




vec4 wallSurface(sampler2D tex, vec2 uv, vec4 rect, int family, vec3 n, vec2 uvDx, vec2 uvDy, vec3 posDx, vec3 posDy) {
  vec2 size = vec2(textureSize(tex, 0));
  float level = clamp(log2(max(max(length(uvDx * size), length(uvDy * size)), 1.0)), 0.0, WALL_LEVELS);
  vec4 c = textureLod(tex, uv, level);
  if (c.a < 0.9) return vec4(n, -1.0);
  vec3 colour = c.rgb / c.a;
  vec2 kind = wallKind(family, colour), texel = exp2(level) / size;
  vec4 inside = vec4(rect.xy + 0.5 * texel, rect.zw - 0.5 * texel);
  vec4 around = textureLod(tex, clamp(uv, inside.xy, inside.zw), WALL_LEVELS);
  float brick = around.a > 0.0 ? wallWarmth(around.rgb / around.a) : 0.0, here = wallHeight(colour, brick);
  float alongU = wallLight(tex, uv + vec2(texel.x, 0.0), level, inside, brick, here)
      - wallLight(tex, uv - vec2(texel.x, 0.0), level, inside, brick, here);
  float alongV = wallLight(tex, uv + vec2(0.0, texel.y), level, inside, brick, here)
      - wallLight(tex, uv - vec2(0.0, texel.y), level, inside, brick, here);
  vec2 slope = vec2(alongU, alongV) / (2.0 * texel) * (WALL_RELIEF * kind.y);
  vec3 r1 = cross(posDy, n), r2 = cross(n, posDx);
  float det = dot(posDx, r1);
  if (abs(det) < 1.0e-14) return vec4(n, kind.x);
  vec3 gradient = (slope.x * (r1 * uvDx.x + r2 * uvDy.x) + slope.y * (r1 * uvDx.y + r2 * uvDy.y)) / det;
  return vec4(normalize(n - gradient), kind.x);
}
#endif
