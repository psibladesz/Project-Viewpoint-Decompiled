
uniform sampler2D uDepth;
uniform mat4 uInvViewProjection;
uniform vec4 uCamera;
uniform vec3 uEye;
float linearDepth(float z) { z = z * 2.0 - 1.0; return 2.0 * uCamera.x * uCamera.y / (uCamera.y + uCamera.x - z * (uCamera.y - uCamera.x)); }
vec3 scenePos(vec2 uv, float depth) {
  vec4 p = uInvViewProjection * vec4(uv * 2.0 - 1.0, depth * 2.0 - 1.0, 1.0);
  return p.xyz / p.w;
}
vec3 viewPos(vec2 uv) {
  float d = linearDepth(texture(uDepth, uv).r);
  return vec3((uv * 2.0 - 1.0) * uCamera.zw * d, -d);
}


vec3 derivativeNormal(vec3 dx, vec3 dy, vec3 fallback) {
  vec3 c = cross(dx, dy);
  float len = length(c);
  return len > 1.0e-12 ? c / len : fallback;
}
