






#ifndef VIEWPOINT_BOUNCE
#define VIEWPOINT_BOUNCE
#include "constants.glsl"
#include "lib/grids.glsl"
#include "lib/floor_plan.glsl"
#include "lib/bounce_field.glsl"
uniform sampler2D uBounce;
uniform float uBounceFloor;
uniform sampler2D uFlashBounce;

#ifndef OPTION_BOUNCE_STRENGTH
#define OPTION_BOUNCE_STRENGTH 1.0
#endif
#ifndef OPTION_FLASH_BOUNCE
#define OPTION_FLASH_BOUNCE 1.0
#endif


ivec2 bounceWorld(ivec2 cell) {
  vec2 middle = uExposureA - (vec2(cell) + 0.5);
  return planSquareAt(vec3(middle.x, 0.0, middle.y));
}


vec3 bounceAt(vec3 pos, vec3 n) {
  vec2 uv = gridUv(pos.xz + n.xz * 0.5);
  float fade = 1.0 - smoothstep(0.35, 0.5, max(abs(uv.x - 0.5), abs(uv.y - 0.5)));
  float height = planHeightAt(pos) - uSceneOrigin.y;
  int storey = int(floor((height + n.y * 0.05 - uBounceFloor) / SQRT6)) + BOUNCE_STOREYS / 2;
  if (fade <= 0.0 || storey < 0 || storey >= BOUNCE_STOREYS) return vec3(0.0);
  int size = int(uExposureSize), row = storey * size;
  int level = int(floor(uSceneOrigin.y / SQRT6 + 1.0e-4)) + storey - BOUNCE_STOREYS / 2;
  vec2 p = uv * uExposureSize, f = p - 0.5 - floor(p - 0.5);
  ivec2 base = ivec2(floor(p - 0.5)), own = clamp(ivec2(floor(p)), ivec2(0), ivec2(size - 1));



  ivec2 toward = ivec2(base.x == own.x ? 1 : -1, base.y == own.y ? 1 : -1), square = bounceWorld(own);
  uvec2 here = planSquare(square, level), across = planSquare(square + ivec2(toward.x, 0), level);
  uvec2 along = planSquare(square + ivec2(0, toward.y), level), corner = planSquare(square + toward, level);
  int alongX = toward.x > 0 ? PLAN_EAST : PLAN_WEST, alongY = toward.y > 0 ? PLAN_SOUTH : PLAN_NORTH;
  float openX = planStepOf(here, across, alongX), openY = planStepOf(here, along, alongY);
  float openCorner = max(openX * planStepOf(across, corner, alongY), openY * planStepOf(along, corner, alongX));
  vec3 squared = n * n;
  int tileX = n.x >= 0.0 ? 0 : 1, tileY = n.y >= 0.0 ? 2 : 3, tileZ = n.z >= 0.0 ? 4 : 5;
  vec3 e = vec3(0.0);
  float weights = 0.0;
  for (int k = 0; k < 4; k++) {
    ivec2 o = ivec2(k & 1, k >> 1), cell = clamp(base + o, ivec2(0), ivec2(size - 1)), d = cell - own;
    float w = (o.x == 1 ? f.x : 1.0 - f.x) * (o.y == 1 ? f.y : 1.0 - f.y);
    if (w <= 0.0) continue;
    w *= d == ivec2(0) ? 1.0 : d.y == 0 ? openX : d.x == 0 ? openY : openCorner;
    if (w <= 0.0) continue;
    e += w * (squared.x * texelFetch(uBounce, ivec2(tileX * size + cell.x, row + cell.y), 0).rgb
        + squared.y * texelFetch(uBounce, ivec2(tileY * size + cell.x, row + cell.y), 0).rgb
        + squared.z * texelFetch(uBounce, ivec2(tileZ * size + cell.x, row + cell.y), 0).rgb);
    weights += w;
  }
  return weights > 1.0e-4 ? e * (fade * float(OPTION_BOUNCE_STRENGTH) / weights) : vec3(0.0);
}





vec3 flashBounceAt(vec3 pos, vec3 n) {
  if (float(OPTION_FLASH_BOUNCE) <= 0.0) return vec3(0.0);
  if (texelFetch(uFlashBounce, ivec2(0, 1), 0).w < 0.5) return vec3(0.0);
  vec3 sum = vec3(0.0);
  for (int i = 0; i < FLASH_LIGHTS; i++) {
    vec4 place = texelFetch(uFlashBounce, ivec2(i, 0), 0);
    if (place.w < 0.5) continue;
    vec3 face = texelFetch(uFlashBounce, ivec2(i, 1), 0).xyz;
    vec4 light = texelFetch(uFlashBounce, ivec2(i, 2), 0);
    vec3 d = pos - place.xyz;
    float squared = dot(d, d);
    vec3 way = d * inversesqrt(max(squared, 1.0e-8));
    float spread = light.w / 3.14159265 + 0.05;
    sum += light.rgb * (max(dot(face, way), 0.0) * max(-dot(n, way), 0.0) / (squared + spread));
  }
  return sum * (float(OPTION_FLASH_BOUNCE) / 3.14159265);
}
#endif
