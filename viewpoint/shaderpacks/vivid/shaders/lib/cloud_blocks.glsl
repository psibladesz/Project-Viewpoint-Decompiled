



#ifndef VIVID_CLOUD_BLOCKS
#define VIVID_CLOUD_BLOCKS
#include "lib/cloud_shape.glsl"

#ifndef OPTION_CLOUD_STYLE
#define OPTION_CLOUD_STYLE 0
#endif
#ifndef OPTION_CLOUD_STYLE_LAYERED
#define OPTION_CLOUD_STYLE_LAYERED 1
#endif


const float BLOCK_FLOOR = 420.0;
const float BLOCK_ROOF = 470.0;
const float BLOCK_WIDTH = 40.0;

vec2 blockGrid(vec3 p) { return (p.xz + uCloudOffset) / BLOCK_WIDTH; }

float blockFull(vec2 cell) {
  vec3 p = vec3((cell.x + 0.5) * BLOCK_WIDTH - uCloudOffset.x, 0.5 * (BLOCK_FLOOR + BLOCK_ROOF), (cell.y + 0.5) * BLOCK_WIDTH - uCloudOffset.y);
  float n = textureLod(uCloudNoise, cloudSpace(p), max(log2(BLOCK_WIDTH / CLOUD_TEXEL), 0.0)).r;
  return step(1.0 - weatherAt(p).r, n);
}


const float BLOCK_SHADOW_FLOOR = 0.3;
const float BLOCK_SHADOW_EDGE = 0.08;
float blockShadow(vec3 pos) {
  vec3 at = pos + uSunDir * ((0.5 * (BLOCK_FLOOR + BLOCK_ROOF) - pos.y) / uSunDir.y);
  vec2 g = blockGrid(at) - 0.5;
  vec2 cell = floor(g), f = smoothstep(0.5 - BLOCK_SHADOW_EDGE, 0.5 + BLOCK_SHADOW_EDGE, fract(g));
  float full = mix(mix(blockFull(cell), blockFull(cell + vec2(1.0, 0.0)), f.x),
                   mix(blockFull(cell + vec2(0.0, 1.0)), blockFull(cell + vec2(1.0, 1.0)), f.x), f.y);
  return mix(1.0, BLOCK_SHADOW_FLOOR, full);
}
#endif
