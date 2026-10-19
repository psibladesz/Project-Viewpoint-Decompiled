




uniform sampler2D uMask;
uniform sampler2D uShellMask;
uniform vec3 uMaskGrid;
uniform vec3 uShellGrid;
uniform float uNearReach;

bool withinNear(vec3 pos) {
  return dot(pos.xz, pos.xz) < uNearReach * uNearReach;
}
bool nearBuilt(vec2 world) {
  vec2 chunk = floor(world / 8.0) - uMaskGrid.xy;
  return all(greaterThanEqual(chunk, vec2(0.0))) && all(lessThan(chunk, vec2(uMaskGrid.z)))
      && texelFetch(uMask, ivec2(chunk), 0).r >= 0.5;
}
bool shellStands(vec2 world) {
  vec2 block = floor(world / 64.0) - uShellGrid.xy;
  if (any(lessThan(block, vec2(0.0))) || any(greaterThanEqual(block, vec2(uShellGrid.z)))) return false;
  vec3 m = texelFetch(uShellMask, ivec2(block), 0).rgb * 255.0;
  return max(m.r, m.b) > 0.5;
}
