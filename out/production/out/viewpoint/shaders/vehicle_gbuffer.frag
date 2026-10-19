#version 330





in vec3 vNormal;
in vec3 vPos;
uniform vec3 uEye;
flat in vec4 vLight;
flat in int vClear;
layout(location = 0) out vec4 gAlbedo;
layout(location = 1) out vec4 gNormal;
layout(location = 2) out vec4 gLight;
layout(location = 3) out vec4 gVelocity;
#include "lib/motion.glsl"
#include "lib/vehicle.glsl"
#include "lib/fade.glsl"
#include "lib/camera_clear.glsl"
#include "lib/materials.glsl"
#include "lib/face_side.glsl"
void main() {
  vec3 face = facePlane(vPos);
  cameraClear(vPos, vClear);
  Vehicle v = vehicle();
  if (v.window > 0.5) discard;
  vec3 n = normalize(vNormal);
  n = litSide(n, face, vPos, uEye);
  bool lamp = v.lamp.a > 0.5;
  gAlbedo = vec4(lamp ? v.lamp.rgb : v.colour, lamp ? 0.25 : v.paint > 0.5 ? MATERIAL_PAINT : MATERIAL_MODEL);

  gNormal = vec4(n, vLight.a >= 1.0 ? 1.0 : 0.0);
  gLight = vec4(vLight.rgb, vLight.a);
  gVelocity = motion();
}
