#version 330



in vec2 vUv;
in float vFromEye;
flat in vec4 vTint;
flat in float vGlow;
uniform sampler2D uArt;
uniform sampler2D uSceneDepth;
uniform vec4 uOutput;
uniform vec3 uSkyLevel;
out vec4 fragColor;
const float SMOKE_LEAST = 0.08;
void main() {
  vec2 at = (gl_FragCoord.xy - uOutput.xy) / uOutput.zw;
  if (gl_FragCoord.z > texture(uSceneDepth, at).r) discard;
  vec4 c = texture(uArt, vUv) * vTint;
  if (c.a < 0.004) discard;
  fragColor = vec4(vGlow > 0.5 ? c.rgb : c.rgb * max(uSkyLevel, vec3(SMOKE_LEAST)), c.a);
}
