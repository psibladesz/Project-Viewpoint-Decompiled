#version 330



uniform sampler2D uSkyDirections;
out vec4 fragColor;
#include "lib/atmosphere.glsl"
#include "lib/sky_light_directions.glsl"
void main() {
  ivec2 at = ivec2(gl_FragCoord.xy);
  vec3 sum = vec3(0.0);
  for (int j = 0; j < AROUND; j++) {
    float solid;
    vec3 d = skyLightDirection(ivec2(j, at.y), solid);
    vec3 light = texelFetch(uSkyDirections, ivec2(j, at.y), 0).rgb;
    sum += light * (at.x < 9 ? harmonic(at.x, d) : d.y);
  }
  fragColor = vec4(sum, 1.0);
}
