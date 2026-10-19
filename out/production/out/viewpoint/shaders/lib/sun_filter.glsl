




uniform float uShadowPhase;
float sunFilter(int c, vec2 uv, float z) {
  float tap = uShadowMaps[c].z;
  if (c >= FIRST_FAR_MAP) {
    return 0.25 * (shadowTap(c, vec3(uv + vec2(-0.5, -0.5) * tap, z)) + shadowTap(c, vec3(uv + vec2(0.5, -0.5) * tap, z))
                 + shadowTap(c, vec3(uv + vec2(-0.5, 0.5) * tap, z)) + shadowTap(c, vec3(uv + vec2(0.5, 0.5) * tap, z)));
  }
  float lit = 0.0;
  for (int y = -1; y <= 1; y++)
    for (int x = -1; x <= 1; x++)
      lit += shadowTap(c, vec3(uv + vec2(x, y) * tap, z));
  return lit / 9.0;
}
