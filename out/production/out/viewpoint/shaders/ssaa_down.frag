#version 330




in vec2 vUv;
uniform sampler2D uColor;
uniform vec2 uFootprint;
out vec4 fragColor;

void main() {
  ivec2 size = textureSize(uColor, 0);
  vec2 centre = vUv * vec2(size);
  ivec2 lo = ivec2(floor(centre - uFootprint)), hi = ivec2(ceil(centre + uFootprint)) - 1;
  vec3 sum = vec3(0.0);
  float weights = 0.0;
  for (int y = lo.y; y <= hi.y; y++) {
    for (int x = lo.x; x <= hi.x; x++) {
      vec2 tent = max(vec2(0.0), 1.0 - abs(vec2(x, y) + 0.5 - centre) / uFootprint);
      float w = tent.x * tent.y;
      sum += texelFetch(uColor, clamp(ivec2(x, y), ivec2(0), size - 1), 0).rgb * w;
      weights += w;
    }
  }
  fragColor = vec4(sum / max(weights, 1.0e-6), 1.0);
}
