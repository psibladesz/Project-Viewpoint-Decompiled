#version 330









in vec2 vUv;
uniform sampler2D uAlbedo;
uniform sampler2D uNormal;
uniform sampler2D uEngineLight;
uniform int uDebugView;
out vec4 fragColor;

const float FOAM_ALBEDO = 0.8;
#include "constants.glsl"
#include "lib/camera.glsl"
#include "lib/sun.glsl"
#include "lib/spots.glsl"
#include "lib/shade.glsl"
#include "lib/occlusion.glsl"
#include "lib/bounce.glsl"
#include "lib/specular.glsl"
#include "lib/character_occlusion.glsl"
#include "lib/rain_ground.glsl"
#include "lib/grids.glsl"
#include "lib/water_surface.glsl"
#include "lib/water_body.glsl"
#include "lib/reflective.glsl"
#include "lib/reflected.glsl"






vec3 lightShare(vec3 full, vec3 albedo, vec4 grids, float skyShare, vec3 n, float lit, vec4 occlusion, vec3 bounce) {
  vec4 sky = vec4(skyPartOf(grids.rgb, skyShare), grids.a);
  vec3 skyOnly = shadeShared(albedo, sky, skyShare, n, 0.0, occlusion, bounce);
  vec3 withSun = shadeShared(albedo, sky, skyShare, n, lit, occlusion, bounce);
  if (uDebugView == 7) return skyOnly;
  if (uDebugView == 8) return max(withSun - skyOnly, vec3(0.0));
  return max(full - withSun, vec3(0.0));
}






vec3 spotHighlight(Spot s, vec3 e, vec3 pos, vec3 n, vec3 v, float rough, float f0) {
  vec3 toLens = s.pos - pos;
  float dist = length(toLens);
  vec3 l = toLens / max(dist, 1.0e-4);
  float nol = dot(n, l), nov = dot(n, v);
  if (nol <= 0.0 || nov <= 0.0) return vec3(0.0);
  float sine = min(s.lens / max(dist, 1.0e-4), 1.0), a = rough * rough, wide = min(a + 0.5 * sine, 1.0);
  float d = ggx(sphereNoH2(n, v, l, sine), a) * (a / wide) * (a / wide);
  float f = schlick(f0, max(dot(v, normalize(l + v)), 0.0));
  return e * (PI_SPECULAR * d * smithHeightCorrelated(nov, nol, a) * f);
}
vec3 spotSpecular(Spot s, vec3 pos, vec3 n, vec3 v, float rough, float f0) {
  vec3 e = spotLight(s, pos, n, false);
  if (e.r + e.g + e.b <= 0.0) return vec3(0.0);
  return spotHighlight(s, e, pos, n, v, rough, f0);
}
vec3 spotsSpecular(vec3 pos, vec3 n, vec3 v, float rough, float f0) {
  vec3 sum = vec3(0.0);
  for (int i = 0; i < uSpotCount; i++) sum += spotSpecular(spotAt(i), pos, n, v, rough, f0);
  return sum;
}


vec3 spotsOn(vec3 pos, vec3 n, bool foliage, vec3 glintN, vec3 v, float rough, float f0, inout vec3 glint) {
  vec3 sum = vec3(0.0);
  for (int i = 0; i < uSpotCount; i++) {
    Spot s = spotAt(i);
    vec3 e = spotLight(s, pos, n, foliage);
    if (e.r + e.g + e.b <= 0.0) continue;
    sum += e;
    glint += spotHighlight(s, e, pos, glintN, v, rough, f0);
  }
  return sum;
}

vec3 water(vec3 pos, float depth, vec4 light, float skyShare, float lit, vec4 occlusion) {
  WaterSurface s = waterSurface(pos, length(pos - uEye));
  vec3 v = normalize(uEye - pos), up = vec3(0.0, 1.0, 0.0);

  vec3 white = shadeShared(vec3(1.0), light, skyShare, up, lit, occlusion, vec3(0.0)) + spots(pos, up, false);
  float m = 1.0 - max(dot(s.mirror, v), 0.0), m2 = m * m;
  float fresnel = F0_WATER + (max(1.0 - s.rough, F0_WATER) - F0_WATER) * m2 * m2 * m;
  vec3 mirror = reflectedLight(pos, reflect(-v, s.mirror), vUv, depth, skyShare * occlusion.w);
  if (uDebugView == 12) return mirror;
  if (uDebugView == 13) return vec3(reflectionAt(vUv, depth).a);
  vec3 under = underwater(pos, v, s, white, lit * max(uSunDir.y, 0.0));
  vec3 glint = specularClamped(
      bodySpecular(s.normal, v, ROUGH_WATER, F0_WATER, lit) + spotsSpecular(pos, s.normal, v, ROUGH_WATER, F0_WATER));
  return mix(mix(under, mirror, fresnel) + glint, white * FOAM_ALBEDO, waterFoam(pos, s));
}

void main() {
  float depth = texture(uDepth, vUv).r;
  if (depth >= 1.0) discard;
  vec4 albedo = texture(uAlbedo, vUv);
  vec4 normal = texture(uNormal, vUv);
  vec4 engine = texture(uEngineLight, vUv);
  vec4 light = vec4(engine.rgb, normal.a);
  if (uDebugView == 1) { fragColor = vec4(albedo.rgb, 1.0); return; }
  if (uDebugView == 2) { fragColor = vec4(light.rgb, 1.0); return; }


  if (albedo.a > 0.125 && albedo.a < 0.375) {
    fragColor = vec4(albedo.rgb * spotLens(scenePos(vUv, depth), uEye), 1.0);
    return;
  }
  vec3 pos = scenePos(vUv, depth);
  vec3 n = normalize(normal.xyz);
  bool foliage = albedo.a > 0.875;
  float lit = sunlit(pos, n, uEye, foliage);
  if (uDebugView == 6) { fragColor = vec4(sunMapsView(pos, n), 1.0); return; }

  vec3 skyPart = skyPartOf(light.rgb, engine.a);
  vec2 shadowed = lampShadowParts(light.rgb - skyPart, pos, n, light.a);
  float keep = 1.0 - shadowed.x * (1.0 - shadowed.y);
  if (uDebugView == 5) {

    fragColor = vec4(keep, keep, shadowed.y, 1.0);
    return;
  }
  light.rgb = skyPart + (light.rgb - skyPart) * keep;
  vec4 occlusion = occlusionAt(ivec2(gl_FragCoord.xy), n);
  occlusion.w *= characterVisibility(pos, n);
  if (uDebugView == 4) { fragColor = vec4(vec3(occlusion.w), 1.0); return; }


  light.rgb = max(light.rgb, min(lamps(pos, n), light.rgb * 1.35 + 0.02)) + torches(pos, n);
  RainGround rain = rainGround(vUv, pos, n, albedo.a, normal.a, engine.a, length(pos - uEye));
  albedo.rgb = wetAlbedo(albedo.rgb, rain.wet);
  if (isWater(albedo.a)) {
    fragColor = vec4(water(pos, depth, light, engine.a, lit, occlusion), 1.0);
    return;
  }
  vec3 bounce = bounceAt(pos, n) + flashBounceAt(pos, n);
  if (uDebugView == 11) { fragColor = vec4(bounce * 4.0, 1.0); return; }
  vec3 v = normalize(uEye - pos);
  float rough = wetRoughness(surfaceRoughness(albedo.a), rain), f0 = mix(surfaceF0(albedo.a), F0_WATER, rain.puddle);
  vec3 spotGlint = vec3(0.0);
  spotOpen = engine.a;
  vec3 full = shadeShared(albedo.rgb, light, engine.a, n, lit, occlusion, bounce)
      + albedo.rgb * spotsOn(pos, n, foliage, rain.normal, v, max(rough, ROUGH_LEAST), f0, spotGlint);
  vec3 skyGlint = skySpecular(rain.normal, v, rough, f0, skyPartOf(light.rgb, engine.a) * engine.a, occlusion.w);
  vec3 bodyGlint = bodySpecular(rain.normal, v, max(rough, ROUGH_LEAST), f0, lit);
  vec3 mirrorN;
  float mirrorRough;
  if (reflects(vUv, pos, albedo, normal, engine, mirrorN, mirrorRough)) {


    bool coat = isPaint(albedo.a);
    float share = coat ? mix(COAT_DRY, 1.0, coatWet(normal.a, engine.a)) : isWater(albedo.a) ? 1.0 : rain.puddle;
    float mf0 = coat ? F0_COAT : F0_WATER;
    vec3 mirror = mirrorSpecular(pos, mirrorN, v, mirrorRough, mf0, vUv, depth, engine.a * occlusion.w);
    if (uDebugView == 12) { fragColor = vec4(reflectedLight(pos, reflect(-v, mirrorN), vUv, depth, 1.0), 1.0); return; }
    if (uDebugView == 13) { fragColor = vec4(vec3(reflectionAt(vUv, depth).a), 1.0); return; }
    vec3 mirrorBody = bodySpecular(mirrorN, v, max(mirrorRough, ROUGH_LEAST), mf0, lit);
    skyGlint = coat ? skyGlint + mirror * share : mix(skyGlint, mirror, share);
    bodyGlint = coat ? bodyGlint + mirrorBody * share : mix(bodyGlint, mirrorBody, share);
  }


  if (foliage) skyGlint = bodyGlint = spotGlint = vec3(0.0);
  if (uDebugView >= 7 && uDebugView <= 9) {
    full = lightShare(full, albedo.rgb, vec4(engine.rgb, light.a), engine.a, n, lit, occlusion, bounce)
        + (uDebugView == 7 ? skyGlint : uDebugView == 8 ? bodyGlint : spotGlint);
  } else {
    full += specularClamped(skyGlint + bodyGlint + spotGlint);
  }
  fragColor = vec4(full, 1.0);
}
