#version 330




in vec2 vUv;
in vec3 vAnchor;
flat in vec4 vRect;
flat in float vFoliage;
flat in int vFlags;
uniform sampler2D uTexture;


const float GLASS_CASTS = 0.85;
#include "lib/fade.glsl"
#include "lib/sample_sprite.glsl"
#include "lib/crown_shadow.glsl"
void main() {
  if ((vFlags & FLAG_WATER) != 0) discard;
  fadeDither();
  if ((vFlags & FLAG_BAKED_FLOOR) != 0) return;
  if (vFoliage > 0.5 && (vFlags & (FLAG_GRASS | FLAG_FACING)) == 0) crownShadow(uTexture, vUv, vRect);
  else if (sampleSprite(uTexture, vUv, vRect, vFlags, false, 1.0, vFoliage > 0.5, vAnchor).a < GLASS_CASTS
      && (vFlags & FLAG_GLASS) != 0) discard;
}
