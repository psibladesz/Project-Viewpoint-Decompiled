#version 330




uniform sampler2D uFloorColours;
uniform int uRow;
out vec4 fragColor;


const int REACH = 6;
const float SPREAD = 2.5, WHOLE = 0.05;

bool waterColour(vec3 c) {
  return c.b >= c.g && c.b > c.r + 12.0 / 255.0;
}

void main() {
  int size = textureSize(uFloorColours, 0).x;
  ivec2 at = ivec2(gl_FragCoord.xy);
  vec3 sum = vec3(0.0);
  float weights = 0.0;
  for (int y = -REACH; y <= REACH; y++) {
    for (int x = -REACH; x <= REACH; x++) {
      ivec2 square = (at + ivec2(x, y) + size) % size;
      vec4 c = texelFetch(uFloorColours, square + ivec2(0, uRow * size), 0);
      if (c.a < 0.5) continue;
      vec3 colour = c.rgb / c.a;
      if (waterColour(colour)) continue;
      float w = exp(-float(x * x + y * y) / (SPREAD * SPREAD));
      sum += colour * w;
      weights += w;
    }
  }
  float land = 1.0 - exp(-weights / WHOLE);
  fragColor = weights > 0.0 ? vec4(sum / weights * land, land) : vec4(0.0);
}
