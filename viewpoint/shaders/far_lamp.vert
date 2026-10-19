#version 330





#include "constants.glsl"
#include "lib/fog_amount.glsl"
#include "lib/far_relief.glsl"
layout(location = 0) in vec3 aLamp;
layout(location = 1) in vec4 aColour;
uniform mat4 uViewProjection;
uniform vec3 uCameraSquare;
uniform vec3 uEye;
uniform vec3 uRight, uUp;
uniform float uPixel;
uniform vec2 uCard;
uniform float uGlow;
uniform vec2 uNearFade;
uniform vec2 uFogRange;
uniform vec2 uHaze;
uniform vec3 uDaylight;
out vec2 vCorner;
flat out vec3 vGlow;
void main() {
  float level = aLamp.z + reliefCoarse(aLamp.xy);
  vec3 centre = vec3(uCameraSquare.x - aLamp.x, (level - uCameraSquare.z) * SQRT6 + LAMP_HEIGHT, uCameraSquare.y - aLamp.y);
  float d = length(centre - uEye);
  float width = max(uCard.x, uCard.y * uPixel * d);
  float fog = max(fogAt(d, uFogRange), hazeAt(d, uHaze));
  float shown = smoothstep(uNearFade.x, uNearFade.x + uNearFade.y, length(centre.xz)) * (1.0 - fog);
  vGlow = max(aColour.rgb - uDaylight, vec3(0.0)) * uGlow * (uCard.x / width) * shown;
  vCorner = vec2(gl_VertexID & 1, gl_VertexID >> 1) * 2.0 - 1.0;
  vec3 pos = centre + (uRight * vCorner.x + uUp * vCorner.y) * (width * 0.5);

  gl_Position = shown > 0.0 && dot(vGlow, vec3(1.0)) > 0.0 ? uViewProjection * vec4(pos, 1.0) : vec4(0.0, 0.0, 2.0, 1.0);
}
