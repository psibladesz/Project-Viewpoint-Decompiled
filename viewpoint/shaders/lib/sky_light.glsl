




#ifndef VIEWPOINT_SKY_LIGHT
#define VIEWPOINT_SKY_LIGHT
uniform sampler2D uSkyLight;

vec3 skyIrradiance(vec3 n) {
  const float A0 = 3.141593, A1 = 2.094395, A2 = 0.785398;
  vec3 e = A0 * 0.282095 * texelFetch(uSkyLight, ivec2(0, 0), 0).rgb;
  e += A1 * 0.488603 * (n.y * texelFetch(uSkyLight, ivec2(1, 0), 0).rgb
      + n.z * texelFetch(uSkyLight, ivec2(2, 0), 0).rgb + n.x * texelFetch(uSkyLight, ivec2(3, 0), 0).rgb);
  e += A2 * (1.092548 * (n.x * n.z * texelFetch(uSkyLight, ivec2(4, 0), 0).rgb
      + n.y * n.z * texelFetch(uSkyLight, ivec2(5, 0), 0).rgb + n.x * n.y * texelFetch(uSkyLight, ivec2(7, 0), 0).rgb)
      + 0.315392 * (3.0 * n.y * n.y - 1.0) * texelFetch(uSkyLight, ivec2(6, 0), 0).rgb
      + 0.546274 * (n.x * n.x - n.z * n.z) * texelFetch(uSkyLight, ivec2(8, 0), 0).rgb);
  return max(e, vec3(0.0));
}

vec3 skyBodyIrradiance() {
  return texelFetch(uSkyLight, ivec2(9, 0), 0).rgb;
}

float skyLevelGround() {
  return texelFetch(uSkyLight, ivec2(9, 0), 0).a;
}
#endif
