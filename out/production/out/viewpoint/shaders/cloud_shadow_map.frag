#version 330




in vec2 vUv;
uniform vec4 uCloudShadowPlace;
uniform vec3 uSunDir;
out vec4 fragColor;
#include "lib/cloud_shadow_ray.glsl"
void main() {
  vec2 place = uCloudShadowPlace.xy + vUv / uCloudShadowPlace.w;
  fragColor = vec4(cloudShadowRay(vec3(place.x, uCloudShadowPlace.z, place.y)), 0.0, 0.0, 1.0);
}
