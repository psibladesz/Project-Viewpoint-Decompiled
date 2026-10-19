#version 330





in vec3 vPos;
in vec2 vCorner;
flat in vec2 vLocal;
flat in vec3 vColour;
flat in float vKeep;
flat in float vCover;
uniform sampler2D uMask;
uniform sampler2D uMainDepth;
uniform sampler2D uShellMask;
uniform vec3 uMaskGrid;
uniform vec3 uShellGrid;
uniform float uNearReach;
uniform vec2 uCellWorld;
uniform vec3 uEye;
uniform vec3 uDaylight;
uniform vec2 uFogRange;
uniform vec2 uHaze;
uniform vec2 uTreeVariation;
uniform int uDebugView;
out vec4 fragColor;
#include "lib/sun.glsl"
#include "lib/shade.glsl"
#include "lib/lod.glsl"
#include "lib/fade.glsl"
#include "lib/far_crown.glsl"
#include "lib/distance.glsl"

const float TRUNK_SHADE = 0.45;
void main() {
  bool crown = inCrown(vCorner), trunk = inTrunk(vCorner);
  if (!crown && !trunk) discard;
  if (!trunk && !inBranches(vCorner, vLocal, vCover)) discard;
  if (texelFetch(uMainDepth, ivec2(gl_FragCoord.xy), 0).r < 1.0) discard;
  fadeDither();
  if (fadeNoise() >= vKeep) discard;
  vec2 world = uCellWorld + vLocal;
  vec2 chunk = floor(world / 8.0) - uMaskGrid.xy;
  if (all(greaterThanEqual(chunk, vec2(0.0))) && all(lessThan(chunk, vec2(uMaskGrid.z)))
      && builtOver(texelFetch(uMask, ivec2(chunk), 0).r)) discard;
  if (dot(vPos.xz, vPos.xz) < uNearReach * uNearReach) discard;
  vec2 block = floor(world / 64.0) - uShellGrid.xy;
  if (all(greaterThanEqual(block, vec2(0.0))) && all(lessThan(block, vec2(uShellGrid.z)))
      && shellMaskValue(texelFetch(uShellMask, ivec2(block), 0).rgb) > 0.5) discard;
  vec3 n = vec3(0.0, 1.0, 0.0);
  float lit = sunlit(vPos, n, uEye, true);
  if (uDebugView == 6) { fragColor = vec4(sunMapsView(vPos, n), 1.0); return; }

  float up = clamp((vCorner.y - (CROWN_CENTRE.y - CROWN_RADII.y)) / (2.0 * CROWN_RADII.y), 0.0, 1.0);
  vec3 albedo = crown ? vColour * (1.0 - uTreeVariation.y * (1.0 - up)) : vColour * TRUNK_SHADE;
  vec3 color = shade(albedo, vec4(uDaylight, 1.0), n, lit);
  float d = length(vPos - uEye);
  color = lodView(color, LOD_BOXES);
  vec3 fogged = distanceFog(color, vPos, (vPos - uEye) / d, d, d, uFogRange, uHaze, uFogColor);
  fragColor = vec4(uLodView == 2 ? color : fogged, 1.0);
}
