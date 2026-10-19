#version 330


in vec2 vUv;
uniform sampler2D uColor;
uniform vec2 uTexel;
uniform int uMode;
out vec4 fragColor;
void main() {
  if (uMode == 2) {
    vec3 s = vec3(0.0);
    s += texture(uColor, vUv + vec2(-1.0, -1.0) * uTexel).rgb + texture(uColor, vUv + vec2(1.0, -1.0) * uTexel).rgb
       + texture(uColor, vUv + vec2(-1.0, 1.0) * uTexel).rgb + texture(uColor, vUv + vec2(1.0, 1.0) * uTexel).rgb;
    s += 2.0 * (texture(uColor, vUv + vec2(-1.0, 0.0) * uTexel).rgb + texture(uColor, vUv + vec2(1.0, 0.0) * uTexel).rgb
       + texture(uColor, vUv + vec2(0.0, -1.0) * uTexel).rgb + texture(uColor, vUv + vec2(0.0, 1.0) * uTexel).rgb);
    s += 4.0 * texture(uColor, vUv).rgb;
    fragColor = vec4(s / 16.0, 1.0);
    return;
  }
  vec3 s = 0.25 * (texture(uColor, vUv + vec2(-0.5, -0.5) * uTexel).rgb + texture(uColor, vUv + vec2(0.5, -0.5) * uTexel).rgb
         + texture(uColor, vUv + vec2(-0.5, 0.5) * uTexel).rgb + texture(uColor, vUv + vec2(0.5, 0.5) * uTexel).rgb);

  if (uMode == 0 && (any(isnan(s)) || any(isinf(s)))) s = vec3(0.0);
  fragColor = vec4(s, 1.0);
}
