#version 330



uniform sampler2D uTranslucentSum;
uniform sampler2D uTranslucentReveal;
out vec4 fragColor;
void main() {
  ivec2 p = ivec2(gl_FragCoord.xy);
  float reveal = texelFetch(uTranslucentReveal, p, 0).r;
  if (reveal >= 1.0) discard;
  vec4 sum = texelFetch(uTranslucentSum, p, 0);
  fragColor = vec4(sum.rgb / max(sum.a, 1.0e-5), reveal);
}
