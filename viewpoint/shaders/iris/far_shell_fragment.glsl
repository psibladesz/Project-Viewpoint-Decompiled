




#include "constants.glsl"
#include "lib/fade.glsl"
#include "lib/sample_sprite.glsl"
#include "lib/sample_floor.glsl"
#include "lib/sample_tree.glsl"
#include "lib/shell_root.glsl"

vec4 sampleLight(vec3 local, vec3 n) { return vec4(1.0); }
#include "lib/surface.glsl"
uniform sampler2D uTexture;
uniform sampler2D uMask;
uniform vec3 uMaskGrid;
uniform float uIrisNearKept;
uniform sampler2D vp_albedoTex;
uniform vec3 uIrisEye;
in vec2 vUv;
in vec3 vLocal;
flat in float vFill;
flat in int vFlags;
flat in vec4 vRect;
in vec3 vNormal;
in vec3 vPos;
flat in vec2 vCellWorld;
flat in float vFloorLayer;
vec4 shellTexel;

void prologue() {
  vec3 face = facePlane(vPos);
  fadeDither();
  vec2 chunk = floor((vCellWorld + maskedSquare(vLocal, vFlags)) / 8.0) - uMaskGrid.xy;
  if (length(vPos - uIrisEye) < uIrisNearKept && all(greaterThanEqual(chunk, vec2(0.0)))
      && all(lessThan(chunk, vec2(uMaskGrid.z))) && builtOver(texelFetch(uMask, ivec2(chunk), 0).r)) discard;
  shellTexel = (vFlags & FLAG_BAKED_FLOOR) != 0 ? sampleFloor(vUv, vFloorLayer)
      : (vFlags & FLAG_BILLBOARD) != 0 ? sampleTree(vUv, vRect) : sampleSprite(uTexture, vUv, vRect, vFlags);
  shellTexel.rgb = resolve(shellTexel, vLocal, vNormal, vFill, vFlags, vPos, uIrisEye, face).albedo;
}

vec3 vp_carriedColour(vec3 c) { return c * shellTexel.rgb; }
vec4 vp_albedo(vec2 uv) { return shellTexel; }
vec4 vp_albedoBias(vec2 uv, float bias) { return shellTexel; }
vec4 vp_albedoLod(vec2 uv, float lod) { return shellTexel; }
vec4 vp_albedoGrad(vec2 uv, vec2 dx, vec2 dy) { return shellTexel; }
vec4 vp_albedoFetch(ivec2 p, int lod) { return shellTexel; }
vec3 vp_lampTint(vec3 packColour) { return packColour; }
vec2 lightmapDelta() { return vec2(0.0); }
