




#include "constants.glsl"
#include "lib/spots.glsl"
#include "iris/held_light.glsl"
#include "lib/vehicle.glsl"
uniform sampler2D vp_albedoTex;
uniform int uIrisGlass;
uniform float uIrisGlassAlpha;
in vec3 vPos;
in vec3 vNormal;
flat in vec4 vLight;
vec4 car;

void prologue() {
  Vehicle v = vehicle();
  bool glass = uIrisGlass == 1;
  if ((v.window > 0.5) != glass || glass && v.gone > 0.5) discard;
  car = vec4(mix(v.colour, v.lamp.rgb, v.lamp.a), glass ? uIrisGlassAlpha : 1.0);
  vec3 n = normalize(vNormal);
  vec3 held = (spots(vPos, n, false) + torchLight(vPos, n)) / max(uIrisFlashLevel, 1.0e-3);
  blockLight = vLight.rgb + held;
  float lamps = max(vLight.r, max(vLight.g, vLight.b));
  levels = vec2(max(min(lamps, 1.0), heldLevel(held)) * 15.0, vLevels.y);
}

vec4 vp_albedo(vec2 uv) { return car; }
vec4 vp_albedoBias(vec2 uv, float bias) { return car; }
vec4 vp_albedoLod(vec2 uv, float lod) { return car; }
vec4 vp_albedoGrad(vec2 uv, vec2 dx, vec2 dy) { return car; }
vec4 vp_albedoFetch(ivec2 p, int lod) { return car; }
