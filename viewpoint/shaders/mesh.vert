#version 330


#include "constants.glsl"
#include "lib/mesh_vertex.glsl"
uniform mat4 uViewProjection;
uniform vec3 uEye;


uniform int uNoFoliage;
out vec2 vUv;
out vec3 vLocal;
flat out float vFill;
flat out int vFlags;
flat out vec4 vRect;
out vec3 vNormal;
out vec3 vPos;
out float vDistance;
flat out vec2 vFade;
flat out vec2 vLightOrigin;
flat out float vFloorLayer;
flat out float vFoliage;
void main() {
  MeshVertex v = meshVertex(uEye);
  vFoliage = dot(v.normal, v.normal) < 0.6 ? 1.0 : 0.0;
  if (v.hidden || uNoFoliage != 0 && vFoliage > 0.5) {
    gl_Position = vec4(2.0, 2.0, 2.0, 1.0);
    return;
  }
  vUv = v.uv; vRect = v.rect; vNormal = meshNormal(v); vFill = v.fill;
  vFlags = v.flags;
  vFade = v.fade.xy;
  vFloorLayer = v.fade.w;
  vLocal = v.local;
  vLightOrigin = lightOrigin(v, uEye.y);
  vPos = pulled(v, uEye);
  gl_Position = uViewProjection * vec4(vPos, 1.0);
  vDistance = gl_Position.w;
}
