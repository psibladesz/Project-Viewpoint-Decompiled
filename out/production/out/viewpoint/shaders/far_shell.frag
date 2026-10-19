#version 330
#extension GL_ARB_gpu_shader5 : enable






in vec2 vUv;
in vec3 vLocal;
flat in vec4 vRect;
in vec3 vNormal;
in vec3 vPos;
flat in float vFill;
flat in int vFlags;
flat in vec2 vCellWorld;
flat in float vFloorLayer;
flat in int vAtlas;
#include "constants.glsl"
uniform sampler2D uAtlases[ATLAS_SLOTS];
uniform sampler2D uMask;
uniform sampler2D uMainDepth;
uniform vec3 uMaskGrid;
uniform sampler2D uShellMask;
uniform vec3 uShellGrid;
uniform vec3 uEye;
uniform vec2 uFogRange;
uniform vec2 uHaze;
uniform float uWet;
uniform int uDebugView;
out vec4 fragColor;
#include "lib/sample_sprite.glsl"
#include "lib/sample_floor.glsl"
#include "lib/sample_tree.glsl"
#include "lib/far_light.glsl"

vec4 sampleLight(vec3 local, vec3 n) { return farLight(vCellWorld + local.xz, local.y, n); }
#include "lib/surface.glsl"
#include "lib/sun.glsl"
#include "lib/shade.glsl"
#include "lib/lod.glsl"
#include "lib/fade.glsl"
#include "lib/far_relief.glsl"
#include "lib/shell_root.glsl"
#include "lib/distance.glsl"
#include "lib/wet.glsl"
#include "lib/shell_water.glsl"
void main() {
  vec3 face = facePlane(vPos);
  float d = length(vPos - uEye);
  if (d < SHELL_BAND || texelFetch(uMainDepth, ivec2(gl_FragCoord.xy), 0).r < 1.0) discard;
  fadeDither();
  vec2 chunk = floor((vCellWorld + maskedSquare(vLocal, vFlags)) / 8.0) - uMaskGrid.xy;
  if (all(greaterThanEqual(chunk, vec2(0.0))) && all(lessThan(chunk, vec2(uMaskGrid.z)))
      && builtOver(texelFetch(uMask, ivec2(chunk), 0).r)) discard;
  vec4 c = (vFlags & FLAG_BAKED_FLOOR) != 0 ? sampleFloor(vUv, vFloorLayer)
      : (vFlags & FLAG_BILLBOARD) != 0 ? sampleTree(vUv, vRect) : sampleSprite(uAtlases[vAtlas], vUv, vRect, vFlags);

  vec3 ground = (vFlags & FLAG_BAKED_FLOOR) != 0 ? floorSquare(vUv, vFloorLayer, vCellWorld + vLocal.xz) : c.rgb;
  vec3 normal = reliefNormalAt(vNormal, vCellWorld + vLocal.xz);
  Surface s = resolve(c, vLocal, normal, vFill, vFlags, vPos, uEye, face);
  vec3 n = s.foliage ? vec3(0.0, 1.0, 0.0) : s.normal;
  float lit = sunlit(vPos, n, uEye, s.foliage);
  if (uDebugView == 6) { fragColor = vec4(sunMapsView(vPos, n), 1.0); return; }
  vec3 color = wetGround(shade(s.albedo, s.light, n, lit), s.normal, s.foliage ? 0.0 : s.light.a, uWet);
  if (!s.foliage && vNormal.y > 0.9 && vLocal.y < 0.1 && shellWater(ground, vCellWorld + vLocal.xz)) {
    vec3 white = shade(vec3(1.0), s.light, vec3(0.0, 1.0, 0.0), lit);
    color = shellWaterShade(color, white, uFogColor, vPos, uEye);
  }
  vec2 block = floor((vCellWorld + vLocal.xz) / 64.0) - uShellGrid.xy;
  float tier = texelFetch(uShellMask, ivec2(clamp(block, vec2(0.0), vec2(uShellGrid.z - 1.0))), 0).r * 255.0;
  color = lodView(color, abs(tier - 2.0) < 0.5 ? LOD_SHELL_FULL : LOD_SHELL_LITE);
  vec3 fogged = distanceFog(color, vPos, (vPos - uEye) / d, d, d, uFogRange, uHaze, uFogColor);
  fragColor = vec4(uLodView == 2 ? color : fogged, 1.0);
}
