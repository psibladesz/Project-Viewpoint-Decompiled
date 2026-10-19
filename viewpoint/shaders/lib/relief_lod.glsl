




#ifndef VIEWPOINT_RELIEF_LOD
#define VIEWPOINT_RELIEF_LOD
#include "lib/far_relief.glsl"
uniform sampler2D uShellMask;
uniform vec3 uShellGrid;
const float RELIEF_BLEND = 32.0;
const float SHELL_FULL = 2.0;
bool shellFull(ivec2 block) {
  ivec2 at = block - ivec2(uShellGrid.xy);
  if (any(lessThan(at, ivec2(0))) || any(greaterThanEqual(at, ivec2(uShellGrid.z)))) return false;
  vec3 m = texelFetch(uShellMask, at, 0).rgb * 255.0;
  return abs(m.r - SHELL_FULL) < 0.5 || abs(m.b - SHELL_FULL) < 0.5;
}


float reliefCoarseShare(vec2 world) {
  ivec2 own = ivec2(floor(world / 64.0));
  if (!shellFull(own)) return 1.0;

  vec2 local = world - vec2(own) * 64.0;
  ivec2 toward = ivec2(local.x < 32.0 ? -1 : 1, local.y < 32.0 ? -1 : 1);
  float share = 0.0;
  for (int k = 1; k < 4; k++) {
    ivec2 b = own + ivec2(toward.x * (k & 1), toward.y * (k >> 1));
    if (shellFull(b)) continue;
    vec2 lo = vec2(b) * 64.0;
    float d = length(max(max(lo - world, world - (lo + 64.0)), vec2(0.0)));
    share = max(share, 1.0 - smoothstep(0.0, RELIEF_BLEND, d));
  }
  return share;
}

float reliefShell(vec2 world) {
  float share = reliefCoarseShare(world);
  if (share <= 0.0) return relief(world);
  return share >= 1.0 ? reliefCoarse(world) : mix(relief(world), reliefCoarse(world), share);
}
#endif
