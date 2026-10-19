#version 330




in vec2 vUv;
in vec3 vLocal;
flat in vec4 vRect;
in vec3 vNormal;
in vec3 vPos;
in float vDistance;
flat in float vFill;
flat in int vFlags;
uniform sampler2D uTexture;
uniform vec3 uEye;
uniform vec2 uFogRange;
uniform int uDebugView;
#include "lib/translucent.glsl"
#include "lib/fade.glsl"
#include "lib/sample_sprite.glsl"
#include "lib/sample_light.glsl"
#include "lib/surface.glsl"
#include "lib/sun.glsl"
#include "lib/spots.glsl"
#include "lib/shade.glsl"
#include "lib/glass.glsl"
#include "lib/fog_amount.glsl"
void main() {
  vec3 face = facePlane(vPos);
  fadeDither();
  if ((vFlags & FLAG_BAKED_FLOOR) != 0) discard;

  vec4 c = sampleSprite(uTexture, vUv, vRect, vFlags, glassSolid(vFlags, vPos, vNormal), uGlassRange.w);
  Surface s = resolve(c, vLocal, vNormal, vFill, vFlags, vPos, uEye, face);
  float fromEye = length(vPos - uEye);
  if (uDebugView == 1 || uDebugView == 2) { translucent(uDebugView == 1 ? s.albedo : s.light.rgb, c.a, fromEye); return; }
  float lit = sunlit(vPos, s.normal, uEye, s.foliage);

#ifdef VIEWPOINT_LAMP_SHADOW
  s.light.rgb = lampShadowed(s.light.rgb, vPos, s.normal, s.light.a);
#endif
  s.light.rgb = max(s.light.rgb, min(lamps(vPos, s.normal), s.light.rgb * 1.35 + 0.02)) + torches(vPos, s.normal);
  vec3 color = shade(s.albedo, s.light, s.normal, lit) + s.albedo * spots(vPos, s.normal, s.foliage);


  float alpha = (vFlags & FLAG_GLASS) != 0 ? mix(c.a, 1.0, glassFar(vPos, vNormal)) : c.a;
  translucent(mix(color, uFogColor, fogAt(vDistance, uFogRange)), alpha, fromEye);
}
