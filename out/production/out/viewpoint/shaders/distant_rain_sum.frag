#version 330


uniform sampler2D uStretches;
out vec4 fragColor;
void main() {
  ivec2 at = ivec2(gl_FragCoord.xy);
  float sum = 0.0;
  for (int row = 0; row <= at.y; row++) sum += texelFetch(uStretches, ivec2(at.x, row), 0).r;
  fragColor = vec4(sum, 0.0, 0.0, 1.0);
}
