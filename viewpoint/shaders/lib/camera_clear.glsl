





uniform vec4 uClearEye;
uniform vec4 uClearChest;
uniform vec4 uClearShape;
uniform vec4 uClearSelf;

float clearShare(vec3 p, int kind) {
  if (uClearEye.w < 0.5 || kind == 2) return 0.0;
  float d = distance(p, uClearEye.xyz);
  if (kind == 1) return 1.0 - smoothstep(uClearSelf.x, uClearSelf.y, d);
  float sphere = 1.0 - smoothstep(uClearShape.y, uClearShape.z, d);
  vec3 axis = uClearChest.xyz - uClearEye.xyz;
  float len = length(axis);
  if (len < 1.0e-3) return sphere;
  vec3 dir = axis / len;
  float along = dot(p - uClearEye.xyz, dir);
  float radius = mix(uClearShape.x, uClearChest.w, clamp(along / len, 0.0, 1.0));
  float off = length(p - uClearEye.xyz - dir * along);
  float inside = (1.0 - smoothstep(radius - uClearShape.w, radius, off))
      * (1.0 - smoothstep(len - uClearSelf.z, len, along)) * step(0.0, along);
  return max(sphere, inside);
}

void cameraClear(vec3 p, int kind) {
  float share = clearShare(p, kind);
  if (share > 0.0 && fadeNoise() < share) discard;
}
