#version 330










uniform float uAirExposure;
uniform vec3 uSkyMoon;
uniform float uMoonLight;
uniform sampler2D uSkyRings;
out vec4 fragColor;
#include "lib/atmosphere.glsl"
#include "lib/sky_light_source.glsl"
#include "lib/sky_light_directions.glsl"
const vec3 LUMA = vec3(0.2126, 0.7152, 0.0722);


const float OVERCAST_DIMS = 0.85;



vec3 bodyIrradiance(float sine) {
  return AIR_SUN * airSunThrough(AIR_BOTTOM + AIR_GROUND, sine) * uAirExposure;
}

void main() {
  int which = int(gl_FragCoord.x);
  vec3 coefficient = vec3(0.0), skyUp = vec3(0.0);
  for (int k = 0; k < RINGS; k++) {
    coefficient += texelFetch(uSkyRings, ivec2(which, k), 0).rgb;
    skyUp += texelFetch(uSkyRings, ivec2(9, k), 0).rgb;
  }

  vec3 sun = bodyIrradiance(uSunTrue.y) * (1.0 - OVERCAST_DIMS * uOvercast) * uAirLight;
  vec3 moon = bodyIrradiance(uSkyMoon.y) * uMoonLight;
  vec3 level = skyUp + sun * max(uSunTrue.y, 0.0) + moon * max(uSkyMoon.y, 0.0);
  if (which == 9) {
    fragColor = vec4(sun + moon, dot(level, LUMA));
    return;
  }


  vec3 ground = AIR_GROUND_ALBEDO / AIR_PI * level;
  if (which == 0) coefficient += ground * 0.282095 * 2.0 * AIR_PI;
  if (which == 1) coefficient -= ground * 0.488603 * AIR_PI;
  fragColor = vec4(coefficient, 1.0);
}
