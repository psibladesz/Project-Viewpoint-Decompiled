#version 330



in vec2 vCorner;
flat in vec3 vGlow;
out vec4 fragColor;
#include "lib/camera.glsl"
void main() {
  float r2 = dot(vCorner, vCorner);
  if (r2 >= 1.0) discard;
  float near = texelFetch(uDepth, ivec2(gl_FragCoord.xy), 0).r;
  if (near < 1.0 && 1.0 / gl_FragCoord.w > linearDepth(near) + 0.5) discard;
  fragColor = vec4(vGlow * exp(-4.0 * r2) * (1.0 - r2), 1.0);
}
