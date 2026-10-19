#version 330





in vec2 vUv;
in vec3 vLocal;
flat in vec4 vRect;
in vec3 vNormal;
in vec3 vPos;
flat in float vFill;
flat in int vFlags;
flat in float vFloorLayer;
flat in float vFoliage;
uniform sampler2D uTexture;
uniform vec3 uEye;
layout(location = 0) out vec4 gAlbedo;
layout(location = 1) out vec4 gNormal;
layout(location = 2) out vec4 gLight;
#include "lib/fade.glsl"
#include "lib/camera_clear.glsl"
uniform int uModels;
uniform int uPlants;
#include "lib/sample_sprite.glsl"
#include "lib/sample_floor.glsl"
#include "lib/sample_light.glsl"
#include "lib/surface.glsl"
#include "lib/lod.glsl"
#include "lib/glass.glsl"
#include "lib/materials.glsl"
#include "lib/wall_surface.glsl"
void main() {
  vec3 face = facePlane(vPos);
#if PIPELINE_WALL_SURFACES
  vec2 uvDx = dFdx(vUv), uvDy = dFdy(vUv);
  vec3 posDx = dFdx(vPos), posDy = dFdy(vPos);
#endif
  fadeDither();
  if (uModels != 0 || uPlants != 0 && uClearSelf.w > 0.5) cameraClear(vPos, (vFlags & FLAG_DOOR) != 0 ? 2 : 0);

  if ((vFlags & FLAG_BAKED_FLOOR) != 0 && fadeNoise() >= floorShare(vFloorLayer)) discard;
  bool solid = glassSolid(vFlags, vPos, vNormal);
  vec4 c = (vFlags & FLAG_BAKED_FLOOR) != 0
      ? sampleFloor(vUv, vFloorLayer)
      : sampleSprite(uTexture, vUv, vRect, vFlags, solid, uGlassRange.w, vFoliage > 0.5, vLocal);
  Surface s = resolve(c, vLocal, vNormal, vFill, vFlags, vPos, uEye, face);
  bool lowFloor = (vFlags & FLAG_BAKED_FLOOR) != 0 && floorLow(vFloorLayer);
  bool water = (vFlags & FLAG_WATER) != 0;
  float material = s.foliage ? 1.0 : water ? MATERIAL_WATER : vFill > 0.5 ? 0.5 : 0.0;
#if PIPELINE_FLOOR_SURFACES
  if ((vFlags & FLAG_BAKED_FLOOR) != 0 && !water && s.normal.y > 0.9) {
    vec4 surface = floorSurface(vUv, vFloorLayer);
    s.normal = surface.xyz;
    material = roughnessMaterial(surface.w);
  }
#endif
#if PIPELINE_WALL_SURFACES
  int family = (vFlags >> WALL_FAMILY_SHIFT) & WALL_FAMILY_MASK;
  bool wall = (vFlags & (FLAG_SINGLE_SIDED | FLAG_STANDING | FLAG_ROOF)) == FLAG_SINGLE_SIDED;
  if (wall && family != 0 && uPlants == 0 && uModels == 0 && vFill < 0.5) {
    vec4 surface = wallSurface(uTexture, vUv, vRect, family, s.normal, uvDx, uvDy, posDx, posDy);
    if (surface.w >= 0.0) {
      s.normal = surface.xyz;
      material = roughnessMaterial(surface.w);
    }
  }
#endif
  gAlbedo = vec4(lodView(s.albedo, lowFloor ? LOD_FLOOR_LOW : LOD_NEAR), material);
  gNormal = vec4(water ? vec3(0.0, 1.0, 0.0) : s.normal, s.light.a);
  float sky = sampleSky(vLocal, s.foliage ? vec3(0.0) : s.normal);
  gLight = vec4(uWireframe == 2 ? vec3(1.0) : s.light.rgb, sky);
}
