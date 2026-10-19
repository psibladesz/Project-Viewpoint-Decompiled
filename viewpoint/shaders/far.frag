#version 330









in vec3 vPos;
in vec2 vLocal;
in float vHeight;
flat in int vFace;
uniform sampler2D uTop;
uniform sampler2D uSide;
uniform sampler2D uMask;
uniform sampler2D uMainDepth;
uniform sampler2D uShellMask;
uniform vec3 uMaskGrid;
uniform vec3 uShellGrid;
uniform float uInterior;
uniform float uNearReach;
uniform vec2 uCellWorld;
uniform vec3 uEye;
uniform vec2 uFogRange;
uniform float uWindowGlow;
uniform vec2 uHaze;
uniform float uWet;
uniform int uDebugView;
out vec4 fragColor;
#include "lib/sun.glsl"
#include "lib/shade.glsl"
#include "lib/far_light.glsl"
#include "lib/lod.glsl"
#include "lib/fade.glsl"
#include "lib/far_relief.glsl"
#include "lib/distance.glsl"
#include "lib/wet.glsl"
#include "lib/shell_water.glsl"
const vec3 NORMALS[5] = vec3[5](vec3(0.0, 1.0, 0.0), vec3(1.0, 0.0, 0.0), vec3(-1.0, 0.0, 0.0), vec3(0.0, 0.0, 1.0), vec3(0.0, 0.0, -1.0));

const vec2 INSIDE[5] = vec2[5](vec2(0.0), vec2(0.5, 0.0), vec2(-0.5, 0.0), vec2(0.0, 0.5), vec2(0.0, -0.5));
void main() {
  if (texelFetch(uMainDepth, ivec2(gl_FragCoord.xy), 0).r < 1.0) discard;
  fadeDither();
  vec2 world = uCellWorld + vLocal;
  vec2 chunk = floor(world / 8.0) - uMaskGrid.xy;
  if (all(greaterThanEqual(chunk, vec2(0.0))) && all(lessThan(chunk, vec2(uMaskGrid.z)))
      && builtOver(texelFetch(uMask, ivec2(chunk), 0).r)) discard;

  if (uInterior < 0.5 && dot(vPos.xz, vPos.xz) < uNearReach * uNearReach) discard;
  vec2 block = floor(world / 64.0) - uShellGrid.xy;
  if (uInterior < 0.5 && all(greaterThanEqual(block, vec2(0.0))) && all(lessThan(block, vec2(uShellGrid.z)))) {
    float shell = shellMaskValue(texelFetch(uShellMask, ivec2(block), 0).rgb);
    if (shell > 1.5 || shell > 0.5 && (vFace != 0 || vHeight > 0.01)) discard;
  }
  vec3 n = reliefNormalAt(NORMALS[vFace], world);
  vec3 albedo = uInterior > 0.5 ? vec3(0.07, 0.065, 0.06)
      : vFace == 0 ? texture(uTop, vLocal / 256.0).rgb : texture(uSide, (vLocal + INSIDE[vFace]) / 256.0).rgb;
  float lit = sunlit(vPos, n, uEye, false);
  if (uDebugView == 6) { fragColor = vec4(sunMapsView(vPos, n), 1.0); return; }
  vec2 column = world + INSIDE[vFace];
  vec3 color = shade(albedo, uInterior > 0.5 ? vec4(uDaylight, 1.0) : farLight(world, vHeight * SQRT6, n), n, lit);
  color = wetGround(color, n, uInterior > 0.5 ? 0.0 : 1.0, uWet);
  if (uInterior < 0.5 && vFace == 0 && vHeight < 0.01 && farWater(albedo, world)) {
    vec3 white = shade(vec3(1.0), farLight(world, 0.0, NORMALS[0]), NORMALS[0], lit);
    color = shellWaterShade(color, white, uFogColor, vPos, uEye);
  }
  if (vFace != 0 && litRoom(column, floor(vHeight))) {

    float along = fract(vFace < 3 ? world.y : world.x), up = fract(vHeight);
    float window = uInterior > 0.5 ? 1.0 : step(0.2, along) * step(along, 0.8) * step(0.25, up) * step(up, 0.75);
    color += roomGlow(uWindowGlow) * window;
  }
  float d = length(vPos - uEye);
  color = lodView(color, uInterior > 0.5 ? LOD_INSIDES : LOD_BOXES);
  vec3 fogged = distanceFog(color, vPos, (vPos - uEye) / d, d, d, uFogRange, uHaze, uFogColor);
  fragColor = vec4(uLodView == 2 ? color : fogged, 1.0);
}
