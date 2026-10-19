#version 330
#extension GL_ARB_gpu_shader5 : enable






in vec2 vUv;
in vec3 vNormal;
in vec3 vPos;
#include "constants.glsl"
#include "lib/materials.glsl"
uniform sampler2D uTextures[MODEL_TEXTURES];
flat in int vTexture;
uniform vec3 uEye;
flat in vec4 vTint;
flat in int vClear;
flat in vec4 vLight;
flat in int vGlass;
uniform float uGlassOpaque;
uniform int uWireframe;
uniform int uModelFilter;
layout(location = 0) out vec4 gAlbedo;
layout(location = 1) out vec4 gNormal;
layout(location = 2) out vec4 gLight;
layout(location = 3) out vec4 gVelocity;
#include "lib/motion.glsl"
#include "lib/hsv.glsl"
#include "lib/lanczos.glsl"
#include "lib/fade.glsl"
#include "lib/camera_clear.glsl"
#include "lib/face_side.glsl"
void main() {
  vec3 face = facePlane(vPos);
  fadeDither();
  cameraClear(vPos, vClear);
  vec2 size = vec2(textureSize(uTextures[vTexture], 0));
  vec2 dx = dFdx(vUv), dy = dFdy(vUv);
  vec4 c = uModelFilter == FILTER_LANCZOS && magnified(size, dx, dy)
      ? lanczos(uTextures[vTexture], vUv, ivec2(0), ivec2(size) - 1, true)
      : uModelFilter == FILTER_SHARP ? textureGrad(uTextures[vTexture], sharpUv(vUv, size, dx, dy), dx, dy)
      : texture(uTextures[vTexture], vUv);
  float noise = fadeNoise();
  bool cut = vGlass == 1 ? c.a < uGlassOpaque : c.a < 0.01 || c.a < 0.99 && c.a <= noise;
  if (uWireframe == 0 && cut) discard;
  vec3 colour = vTint.a != 0.0 ? hueShift(c.rgb, vTint.a) : c.rgb;
  vec3 n = normalize(vNormal);
  n = litSide(n, face, vPos, uEye);
  gAlbedo = vec4(colour * vTint.rgb, MATERIAL_MODEL);



  gNormal = vec4(n, vLight.a >= 1.0 ? 1.0 : 0.0);
  gLight = vec4(vLight.rgb, vLight.a);
  gVelocity = motion();
  if (uWireframe == 2) {
    gAlbedo.rgb = vec3(0.45, 0.65, 1.0);
    gLight = vec4(1.0);
  }
}
