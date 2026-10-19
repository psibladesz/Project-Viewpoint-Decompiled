


uniform vec3 uZenith;
uniform vec3 uSkySun;
uniform vec3 uSkySunColor;
uniform float uSkyGlow;
#include "lib/sky.glsl"
vec3 fogColor(vec3 horizon, vec3 view) {
  return skyColor(view, horizon, uZenith, uSkySun, uSkySunColor, uSkyGlow) * SKY_BRIGHTNESS;
}
