#version 330






in vec2 vUv;
uniform sampler2D uAlbedo;
uniform sampler2D uNormal;
uniform sampler2D uEngineLight;
uniform int uDebugView;
uniform vec3 uSkyLevel;
out vec4 fragColor;
#include "constants.glsl"
#include "lib/camera.glsl"
#include "lib/sun.glsl"
#include "lib/spots.glsl"
#include "lib/shade.glsl"
#include "lib/occlusion.glsl"
#include "lib/bounce.glsl"





vec3 lightShare(vec3 full, vec3 albedo, vec4 grids, float skyShare, vec3 n, float lit) {

  vec3 skyLight = max(uSkyLevel - (1.0 - skyShare) * (SKY_STEP * 15.0 / 255.0), vec3(0.0));
  vec4 sky = vec4(min(grids.rgb, skyLight), grids.a);
  vec3 skyOnly = shade(albedo, sky, n, 0.0), withSun = shade(albedo, sky, n, lit);
  if (uDebugView == 7) return skyOnly;
  if (uDebugView == 8) return max(withSun - skyOnly, vec3(0.0));
  return max(full - withSun, vec3(0.0));
}

void main() {
  float depth = texture(uDepth, vUv).r;
  if (depth >= 1.0) discard;
  vec4 albedo = texture(uAlbedo, vUv);
  vec4 normal = texture(uNormal, vUv);
  vec4 engine = texture(uEngineLight, vUv);
  spotOpen = engine.a;
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
  vec2 shadowed = lampShadowParts(light.rgb, pos, n, light.a);
  float keep = 1.0 - shadowed.x * (1.0 - shadowed.y);
  if (uDebugView == 5) {

    fragColor = vec4(keep, keep, shadowed.y, 1.0);
    return;
  }
  light.rgb *= keep;
  float seen = occlusionAt(ivec2(gl_FragCoord.xy), n).w;
  if (uDebugView == 4) { fragColor = vec4(vec3(seen), 1.0); return; }
  light.rgb *= multiBounce(seen, albedo.rgb);


  light.rgb = max(light.rgb, min(lamps(pos, n), light.rgb * 1.35 + 0.02)) + torches(pos, n);
  vec3 bounce = bounceAt(pos, n) + flashBounceAt(pos, n);
  if (uDebugView == 11) { fragColor = vec4(bounce * 4.0, 1.0); return; }
  vec3 full = shade(albedo.rgb, light, n, lit) + albedo.rgb * spots(pos, n, foliage)
      + albedo.rgb * bounce * multiBounce(seen, albedo.rgb);
  if (uDebugView >= 7 && uDebugView <= 9) {
    full = lightShare(full, albedo.rgb, vec4(engine.rgb, light.a), engine.a, n, lit);
  }
  fragColor = vec4(full, 1.0);
}
