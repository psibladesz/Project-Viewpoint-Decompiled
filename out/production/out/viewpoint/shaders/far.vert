#version 330




#include "constants.glsl"
#include "lib/far_relief.glsl"
layout(location = 0) in vec4 aVertex;
uniform mat4 uViewProjection;
uniform vec3 uOrigin;
uniform vec2 uCellWorld;
uniform vec2 uFade;
flat out vec2 vFade;
out vec3 vPos;
out vec2 vLocal;
out float vHeight;
flat out int vFace;
void main() {
  float lift = reliefCoarse(uCellWorld + aVertex.xy);
  vec3 pos = uOrigin + vec3(-aVertex.x, (aVertex.z + lift) * SQRT6, -aVertex.y);
  vPos = pos;
  vLocal = aVertex.xy;
  vHeight = aVertex.z;
  vFace = int(aVertex.w + 0.5);
  vFade = uFade;
  gl_Position = uViewProjection * vec4(pos, 1.0);
}
