#version 330


in vec2 vUv;
flat in vec4 vRect;
flat in vec4 vTint;
uniform sampler2D uArt;
layout(location = 0) out vec4 gAlbedo;
void main() {
  if (vUv.x < min(vRect.x, vRect.z) || vUv.x > max(vRect.x, vRect.z) || vUv.y < min(vRect.y, vRect.w)
      || vUv.y > max(vRect.y, vRect.w)) discard;
  vec4 c = texture(uArt, vUv);
  float a = c.a * vTint.a;
  if (a <= 0.0) discard;
  gAlbedo = vec4(c.rgb * vTint.rgb, a);
}
