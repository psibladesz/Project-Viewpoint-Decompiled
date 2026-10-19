




#ifndef DEFAULT_LAND_LIGHT
#define DEFAULT_LAND_LIGHT
#include "constants.glsl"
#include "lib/sky.glsl"
#include "lib/sky_light.glsl"
#ifndef VIEWPOINT_SKY_LEVEL
#define VIEWPOINT_SKY_LEVEL
uniform vec3 uSkyLevel;
#endif
uniform float uMoonLight;


vec3 landSky(vec3 n) {
  return skyIrradiance(n) / max(skyLevelGround(), 1.0e-9);
}



vec3 landBody() {
  float shown = smoothstep(SUN_LEAST, 2.0 * SUN_LEAST, uSunStrength);
  return uSkyLevel * skyBodyIrradiance() / max(skyLevelGround(), 1.0e-9) * shown;
}
#endif
