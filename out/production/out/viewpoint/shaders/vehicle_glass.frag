#version 330




in vec3 vNormal;
in vec3 vPos;
uniform vec3 uEye;
flat in vec4 vLight;
flat in int vClear;
uniform float uGlassAlpha;
#include "lib/translucent.glsl"
#include "lib/vehicle.glsl"
#include "lib/fade.glsl"
#include "lib/camera_clear.glsl"
#include "lib/sun.glsl"
#include "lib/spots.glsl"
#include "lib/shade.glsl"
#include "lib/face_side.glsl"
void main() {
  vec3 face = facePlane(vPos);
  cameraClear(vPos, vClear);
  Vehicle v = vehicle();
  if (v.window < 0.5 || v.gone > 0.5) discard;
  vec3 n = normalize(vNormal);
  n = litSide(n, face, vPos, uEye);
  vec4 light = vec4(vLight.rgb, vLight.a >= 1.0 ? 1.0 : 0.0);
  float lit = sunlit(vPos, n, uEye, false);
#ifdef VIEWPOINT_LAMP_SHADOW
  light.rgb = lampShadowed(light.rgb, vPos, n, light.a);
#endif
  light.rgb = max(light.rgb, min(lamps(vPos, n), light.rgb * 1.35 + 0.02)) + torches(vPos, n);
  vec3 colour = shade(v.colour, light, n, lit) + v.colour * spots(vPos, n, false);
  translucent(mix(colour, v.lamp.rgb, v.lamp.a), uGlassAlpha, length(vPos - uEye));
}
