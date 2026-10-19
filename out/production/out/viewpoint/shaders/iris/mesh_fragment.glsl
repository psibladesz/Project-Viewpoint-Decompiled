





#include "constants.glsl"
#define FADE_GIVEN
#define LIGHT_ORIGIN_GIVEN
vec2 vFade;
vec2 vLightOrigin;
#include "lib/fade.glsl"
#include "lib/sample_sprite.glsl"
#include "lib/sample_floor.glsl"
#include "lib/glass.glsl"
#include "lib/sample_light.glsl"
#include "lib/surface.glsl"
uniform int uLightCount;
uniform vec4 uLightPos[48];
uniform vec3 uLightColor[48];
#include "lib/lamp_shadow.glsl"
#include "lib/spots.glsl"
#include "iris/held_light.glsl"
uniform sampler2D vp_albedoTex;
uniform vec3 uIrisEye;
uniform int uIrisTranslucent;
in vec2 vUv;
in vec3 vLocal;
flat in vec4 vRect;
in vec3 vNormal;
in vec3 vPos;
flat in vec4 vMeshValues;
flat in vec4 vGridFlags;
float vFill;
int vFlags;
float vFloorLayer;


void unpackMesh() {
  vFill = vMeshValues.x;
  vFloorLayer = vMeshValues.y;
  vFade = vMeshValues.zw;
  vLightOrigin = vGridFlags.xy;
  vFlags = int(uint(vGridFlags.z) | uint(vGridFlags.w) << 16u);
}

bool foliage() { return dot(vNormal, vNormal) < 0.6; }

vec3 faceNormal() {
  vec3 n = foliage() ? vec3(0.0, 1.0, 0.0) : normalize(vNormal);
  return (vFlags & FLAG_SINGLE_SIDED) == 0 && dot(n, vPos - uIrisEye) > 0.0 ? -n : n;
}

vec3 gridLamps(vec2 p) {
  vec2 cell = floor(p), f = p - cell;
  vec2 t = vLightOrigin + vec2(cell.x * 2.0 + 0.5 + f.x, cell.y * 2.0 + 0.5 + f.y);
  return texture(uLight, t / uLightAtlasSize).rgb;
}



void lightAtPixel() {
  vec3 n = faceNormal();
  vec2 p = clamp(vLocal.xz + vec2(-n.x, -n.z) * 0.3, -0.99, 8.99) + 1.0;
  vec3 lamps = gridLamps(p);
  float sky = gridSky(vLightOrigin, p);
  lamps *= lampShadowKeep(lamps, vPos, n, step(1.0, sky));
  vec3 held = (spots(vPos, n, foliage()) + torchLight(vPos, n)) / max(uIrisFlashLevel, 1.0e-3);
  blockLight = lamps + held;
  levels = vec2(max(min(max(lamps.r, max(lamps.g, lamps.b)), 1.0), heldLevel(held)), sky) * 15.0;
}

void prologue() {
  unpackMesh();
  fadeDither();
  if ((vFlags & FLAG_BAKED_FLOOR) != 0 && fadeNoise() >= floorShare(vFloorLayer)) discard;
  vec3 n = foliage() ? vec3(0.0, 1.0, 0.0) : normalize(vNormal);
  if (!foliage() && (vFlags & FLAG_SINGLE_SIDED) != 0 && dot(n, vPos - uIrisEye) > 0.0) discard;
  bool water = (vFlags & FLAG_WATER) != 0;
  if (water && uIrisTranslucent == 0) discard;
  lightAtPixel();
}




const float MINECRAFT_WATER_ALPHA = 0.7;
const float WATER_ART_LOD = 3.0;


vec4 surface(vec2 uv) {
  if ((vFlags & FLAG_WATER) != 0) return vec4(textureLod(vp_albedoTex, uv, WATER_ART_LOD).rgb, MINECRAFT_WATER_ALPHA);
  if ((vFlags & FLAG_BAKED_FLOOR) != 0) {
    if (uIrisTranslucent != 0) discard;
    return sampleFloor(uv, vFloorLayer);
  }
  bool solid = glassSolid(vFlags, vPos, vNormal);
  vec4 c = sampleSprite(vp_albedoTex, uv, vRect, vFlags, solid, uGlassRange.w);
  c.rgb = vFill > 0.5 ? cladding(unpackFill(vFill), vLocal, faceNormal()) : unshaded(c.rgb, vFlags);
  if (uIrisTranslucent == 0) c.a = 1.0;
  return c;
}

vec4 vp_albedo(vec2 uv) { return surface(uv); }
vec4 vp_albedoBias(vec2 uv, float bias) { return surface(uv); }
vec4 vp_albedoLod(vec2 uv, float lod) { return surface(uv); }
vec4 vp_albedoGrad(vec2 uv, vec2 dx, vec2 dy) { return surface(uv); }
vec4 vp_albedoFetch(ivec2 p, int lod) { return texelFetch(vp_albedoTex, p, lod); }
