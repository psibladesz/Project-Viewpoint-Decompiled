


#include "lib/land_light.glsl"

vec3 bounceIrradiance(float sky, float lit, vec3 n) {
  return uSkyLevel * sky * landSky(n) + landBody() * (max(dot(n, uSunDir), 0.0) * lit);
}
