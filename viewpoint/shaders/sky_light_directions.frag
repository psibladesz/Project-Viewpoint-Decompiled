#version 330



uniform float uAirExposure;
out vec4 fragColor;
#include "lib/atmosphere.glsl"
#include "lib/sky_light_source.glsl"
#include "lib/sky_light_directions.glsl"
void main() {
  float solid;
  vec3 d = skyLightDirection(ivec2(gl_FragCoord.xy), solid);
  fragColor = vec4(skyLightSource(d) * solid, 1.0);
}
