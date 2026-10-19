




#ifndef VIEWPOINT_SKY_LEVEL
#define VIEWPOINT_SKY_LEVEL
uniform vec3 uSkyLevel;
#endif

const float BOUNCE_SUN = 1.35;
const float BOUNCE_SUN_FULL = 0.35;

vec3 bounceIrradiance(float sky, float lit, vec3 n) {
  float sun = BOUNCE_SUN * smoothstep(0.0, BOUNCE_SUN_FULL, uSunStrength) * lit * max(dot(n, uSunDir), 0.0);
  return uSkyLevel * (sky * (0.5 + 0.5 * n.y) + uSunColor * sun);
}
