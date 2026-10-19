#version 330






in vec2 vUv;
uniform sampler2D uCoords;
out vec4 fragColor;
#include "lib/camera.glsl"
#include "lib/air_point.glsl"

void main() {
  vec4 here = texelFetch(uCoords, ivec2(gl_FragCoord.xy), 0);
  if (here.z < 0.0) {
    fragColor = vec4(0.0, 0.0, 0.0, 1.0);
    return;
  }
  vec3 dir = normalize(scenePos(here.xy, 1.0) - uEye);
  fragColor = vec4(sunAirMarch(dir, here.z, 0.5), 1.0);
}
