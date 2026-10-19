#version 330



in vec2 vUv;
flat in vec4 vRect;
flat in vec4 vTint;
uniform sampler2D uArt;
uniform sampler2D uSceneDepth;
uniform vec4 uOutput;
out vec4 fragColor;
void main() {
  if (vUv.x < min(vRect.x, vRect.z) || vUv.x > max(vRect.x, vRect.z) || vUv.y < min(vRect.y, vRect.w)
      || vUv.y > max(vRect.y, vRect.w)) discard;
  vec2 at = (gl_FragCoord.xy - uOutput.xy) / uOutput.zw;
  if (gl_FragCoord.z > texture(uSceneDepth, at).r) discard;
  vec4 c = texture(uArt, vUv);
  float a = c.a * vTint.a;
  if (a <= 0.0) discard;
  fragColor = vec4(mix(vec3(1.0), c.rgb * vTint.rgb, a), 1.0);
}
