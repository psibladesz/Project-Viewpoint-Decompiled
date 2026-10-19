#version 330



flat in vec4 vRect;
uniform sampler2D uPage;
out vec4 fragColor;
const int GRID = 16;

void main() {
  vec3 sum = vec3(0.0);
  float count = 0.0;
  for (int j = 0; j < GRID; j++) {
    for (int i = 0; i < GRID; i++) {
      vec2 uv = mix(vRect.xy, vRect.zw, (vec2(float(i), float(j)) + 0.5) / float(GRID));
      vec4 texel = textureLod(uPage, uv, 0.0);
      if (texel.a > 0.5) {
        sum += texel.rgb;
        count += 1.0;
      }
    }
  }
  fragColor = count > 0.0 ? vec4(sum / count, 1.0) : vec4(0.0);
}
