




#include "constants.glsl"
#include "lib/fade.glsl"
#include "lib/far_crown.glsl"
uniform sampler2D uMask;
uniform vec3 uMaskGrid;
uniform float uIrisNearKept;
uniform sampler2D uShellMask;
uniform vec3 uShellGrid;
uniform sampler2D vp_albedoTex;
in vec2 vWorld;
in float vEyeDistance;
flat in int vFace;
in float vHeight;
in vec2 vCorner;
flat in float vCover;

void prologue() {
  fadeDither();
  if (vCorner.y < 1.5) {
    bool crown = inCrown(vCorner), trunk = inTrunk(vCorner);
    if (!crown && !trunk) discard;
    if (!trunk && !inBranches(vCorner, vWorld, vCover)) discard;
  }
  vec2 chunk = floor(vWorld / 8.0) - uMaskGrid.xy;
  if (vEyeDistance < uIrisNearKept && all(greaterThanEqual(chunk, vec2(0.0)))
      && all(lessThan(chunk, vec2(uMaskGrid.z))) && builtOver(texelFetch(uMask, ivec2(chunk), 0).r)) discard;

  vec2 block = floor(vWorld / 64.0) - uShellGrid.xy;
  if (all(greaterThanEqual(block, vec2(0.0))) && all(lessThan(block, vec2(uShellGrid.z)))) {
    float shell = shellMaskValue(texelFetch(uShellMask, ivec2(block), 0).rgb);
    if (vFace < 0 ? shell > 0.5 : shell > 1.5 || shell > 0.5 && (vFace != 0 || vHeight > 0.01)) discard;
  }
}

vec3 vp_carriedColour(vec3 c) { return c; }

vec4 vp_albedo(vec2 uv) { return vec4(1.0); }
vec4 vp_albedoBias(vec2 uv, float bias) { return vec4(1.0); }
vec4 vp_albedoLod(vec2 uv, float lod) { return vec4(1.0); }
vec4 vp_albedoGrad(vec2 uv, vec2 dx, vec2 dy) { return vec4(1.0); }
vec4 vp_albedoFetch(ivec2 p, int lod) { return vec4(1.0); }
vec3 vp_lampTint(vec3 packColour) { return packColour; }
vec2 lightmapDelta() { return vec2(0.0); }
