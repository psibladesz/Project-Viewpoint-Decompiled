#version 330




in vec2 vUv;
uniform int uLightCount;
uniform vec4 uLightPos[48];
uniform float uLightLevel[48];
out vec4 fragColor;
#include "lib/floor_plan.glsl"
#include "lib/lamp_field.glsl"

void main() {
  ivec2 p = ivec2(gl_FragCoord.xy), tile = p / FIELD_TEXELS;
  int i = tile.y * FIELD_COLUMNS + tile.x;
  if (i >= uLightCount) {
    fragColor = vec4(1.0);
    return;
  }
  vec2 from = lampFieldLight(i);
  vec2 to = floor(from) - FIELD_REACH + (vec2(p - tile * FIELD_TEXELS) + 0.5) / FIELD_PER_SQUARE;
  fragColor = vec4(planSight(from, to, int(uLightLevel[i]), FIELD_STEPS));
}
