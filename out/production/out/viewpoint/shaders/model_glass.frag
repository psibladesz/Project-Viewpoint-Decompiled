#version 330
#extension GL_ARB_gpu_shader5 : enable



in vec2 vUv;
in vec3 vNormal;
in vec3 vPos;
#include "constants.glsl"
uniform sampler2D uTextures[MODEL_TEXTURES];
flat in int vTexture;
uniform vec3 uEye;
flat in vec4 vTint;
flat in int vClear;
flat in vec4 vLight;
flat in int vGlass;
uniform vec2 uAlphaRange;
#include "lib/translucent.glsl"
#include "lib/hsv.glsl"
#include "lib/fade.glsl"
#include "lib/camera_clear.glsl"
#include "lib/sun.glsl"
#include "lib/spots.glsl"
#include "lib/shade.glsl"
#include "lib/face_side.glsl"
void main() {
  vec3 face = facePlane(vPos);
  fadeDither();
  cameraClear(vPos, vClear);
  vec4 c = texture(uTextures[vTexture], vUv);
  if (vGlass == 0 || c.a < uAlphaRange.x || c.a >= uAlphaRange.y) discard;
  vec3 albedo = (vTint.a != 0.0 ? hueShift(c.rgb, vTint.a) : c.rgb) * vTint.rgb;
  vec3 n = normalize(vNormal);
  n = litSide(n, face, vPos, uEye);
  vec4 light = vec4(vLight.rgb, vLight.a >= 1.0 ? 1.0 : 0.0);
  float lit = sunlit(vPos, n, uEye, false);
#ifdef VIEWPOINT_LAMP_SHADOW
  light.rgb = lampShadowed(light.rgb, vPos, n, light.a);
#endif
  light.rgb = max(light.rgb, min(lamps(vPos, n), light.rgb * 1.35 + 0.02)) + torches(vPos, n);
  vec3 colour = shade(albedo, light, n, lit) + albedo * spots(vPos, n, false);
  translucent(colour, c.a, length(vPos - uEye));
}
