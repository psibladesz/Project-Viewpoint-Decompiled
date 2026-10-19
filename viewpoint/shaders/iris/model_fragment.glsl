



#include "constants.glsl"
#include "lib/hsv.glsl"
#include "lib/spots.glsl"
#include "lib/fade.glsl"
#include "lib/camera_clear.glsl"
#include "iris/held_light.glsl"
uniform sampler2D uTextures[MODEL_TEXTURES];
uniform sampler2D vp_albedoTex;
in vec2 vUv;
in vec3 vPos;
in vec3 vNormal;
flat in vec4 vTint;
flat in vec4 vLight;
flat in int vTexture;
flat in int vClear;
flat in int vGlass;
uniform int uIrisGlass;
uniform vec2 uIrisGlassRange;


vec4 modelTexel(vec2 uv) {
  switch (vTexture) {
    case 0: return texture(uTextures[0], uv);
    case 1: return texture(uTextures[1], uv);
    case 2: return texture(uTextures[2], uv);
    case 3: return texture(uTextures[3], uv);
    case 4: return texture(uTextures[4], uv);
    case 5: return texture(uTextures[5], uv);
    case 6: return texture(uTextures[6], uv);
    case 7: return texture(uTextures[7], uv);
    case 8: return texture(uTextures[8], uv);
    case 9: return texture(uTextures[9], uv);
    case 10: return texture(uTextures[10], uv);
    case 11: return texture(uTextures[11], uv);
    case 12: return texture(uTextures[12], uv);
    case 13: return texture(uTextures[13], uv);
    case 14: return texture(uTextures[14], uv);
    default: return texture(uTextures[15], uv);
  }
}






void cutout() {
  fadeDither();
  cameraClear(vPos, vClear);
  float a = modelTexel(vUv).a;
  if (uIrisGlass == 1) {
    if (vGlass == 0 || a < uIrisGlassRange.x || a >= uIrisGlassRange.y) discard;
    return;
  }
  if (vGlass == 1 ? a < uIrisGlassRange.y : a < 0.01 || a < 0.99 && a <= fadeNoise()) discard;
}

void prologue() {
  cutout();
  vec3 n = normalize(vNormal);
  vec3 held = (spots(vPos, n, false) + torchLight(vPos, n)) / max(uIrisFlashLevel, 1.0e-3);
  blockLight = vLight.rgb + held;
  float lamps = max(vLight.r, max(vLight.g, vLight.b));
  levels = vec2(max(min(lamps, 1.0), heldLevel(held)) * 15.0, vLevels.y);
}

vec4 surface(vec2 uv) {
  vec4 c = modelTexel(uv);
  return vec4(vTint.a != 0.0 ? hueShift(c.rgb, vTint.a) : c.rgb, c.a);
}

vec4 vp_albedo(vec2 uv) { return surface(uv); }
vec4 vp_albedoBias(vec2 uv, float bias) { return surface(uv); }
vec4 vp_albedoLod(vec2 uv, float lod) { return surface(uv); }
vec4 vp_albedoGrad(vec2 uv, vec2 dx, vec2 dy) { return surface(uv); }
vec4 vp_albedoFetch(ivec2 p, int lod) { return surface(vUv); }
