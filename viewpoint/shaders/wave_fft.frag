#version 330



uniform sampler2D uInputA;
uniform sampler2D uInputB;
uniform float uSpan;
uniform int uAlong;
layout(location = 0) out vec4 outA;
layout(location = 1) out vec4 outB;
const float WAVE_TAU = 6.28318531;
vec2 mulComplex(vec2 a, vec2 b) { return vec2(a.x * b.x - a.y * b.y, a.x * b.y + a.y * b.x); }
void main() {
  ivec2 p = ivec2(gl_FragCoord.xy);
  int size = textureSize(uInputA, 0).x;
  float index = float(uAlong == 1 ? p.x : p.y);
  int even = int(floor(index / uSpan) * (uSpan * 0.5) + mod(index, uSpan * 0.5));
  ivec2 e = uAlong == 1 ? ivec2(even, p.y) : ivec2(p.x, even);
  ivec2 o = e + (uAlong == 1 ? ivec2(size / 2, 0) : ivec2(0, size / 2));
  float angle = WAVE_TAU * index / uSpan;
  vec2 twiddle = vec2(cos(angle), sin(angle));
  vec4 a0 = texelFetch(uInputA, e, 0), a1 = texelFetch(uInputA, o, 0);
  vec4 b0 = texelFetch(uInputB, e, 0), b1 = texelFetch(uInputB, o, 0);
  outA = vec4(a0.xy + mulComplex(twiddle, a1.xy), a0.zw + mulComplex(twiddle, a1.zw));
  outB = vec4(b0.xy + mulComplex(twiddle, b1.xy), b0.zw + mulComplex(twiddle, b1.zw));
}
