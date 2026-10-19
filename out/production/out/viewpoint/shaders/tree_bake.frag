#version 330


uniform sampler2D uSource;
in vec2 vUv;
out vec4 fragColor;
void main() {
  vec2 dx = dFdx(vUv), dy = dFdy(vUv);
  vec4 sum = vec4(0.0);
  for (int j = 0; j < 4; j++) {
    for (int i = 0; i < 4; i++) {
      vec4 c = textureLod(uSource, vUv + dx * ((float(i) + 0.5) / 4.0 - 0.5) + dy * ((float(j) + 0.5) / 4.0 - 0.5), 0.0);
      sum += vec4(c.rgb * c.a, c.a);
    }
  }
  fragColor = sum / 16.0;
}
