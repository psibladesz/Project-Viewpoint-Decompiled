



#ifndef VIVID_CLOUD_BLOCKS_DRAW
#define VIVID_CLOUD_BLOCKS_DRAW
#include "lib/cloud_blocks.glsl"

const int BLOCK_STEPS = 48;

const float BLOCK_OPACITY = 0.85;


const float BLOCK_UNDERSIDE = 0.75;
const float BLOCK_SIDE = 1.0;
const float BLOCK_SUN = 1.2;
const float BLOCK_THROUGH = 0.3;
vec3 blockLight(vec3 n) {
  vec3 sunLight = uSunColor * max(uSunStrength, 0.0) * mix(1.0, 0.25, uNight);
  vec3 ambient = mix(uHorizon, uZenith, 0.5) * (n.y < 0.0 ? BLOCK_UNDERSIDE : BLOCK_SIDE);
  return ambient + sunLight * (BLOCK_SUN * max(dot(n, uSunDir), 0.0) + BLOCK_THROUGH);
}

vec4 blockClouds(vec3 r) {
  if (r.y < 0.015) return vec4(0.0);
  float t0 = BLOCK_FLOOR / r.y, t1 = BLOCK_ROOF / r.y;
  vec2 g = blockGrid(r * t0), cell = floor(g);
  vec2 perT = r.xz / BLOCK_WIDTH;
  vec2 towards = sign(perT);
  vec2 across = 1.0 / max(abs(perT), vec2(1.0e-6));
  vec2 next = mix(vec2(1.0e9), (towards * (cell - g) + max(towards, 0.0)) * across, abs(towards));
  float t = t0;
  int crossed = -1;
  for (int i = 0; i < BLOCK_STEPS; i++) {
    if (blockFull(cell) > 0.5) {
      vec3 n = crossed < 0 ? vec3(0.0, -1.0, 0.0) : crossed == 0 ? vec3(-towards.x, 0.0, 0.0) : vec3(0.0, 0.0, -towards.y);
      float fade = smoothstep(0.015, 0.12, r.y) * exp(-t / 9000.0);
      return vec4(blockLight(n), 1.0) * BLOCK_OPACITY * fade;
    }
    crossed = next.x < next.y ? 0 : 1;
    t = t0 + min(next.x, next.y);
    if (t > t1) break;
    if (crossed == 0) { cell.x += towards.x; next.x += across.x; }
    else { cell.y += towards.y; next.y += across.y; }
  }
  return vec4(0.0);
}
#endif
