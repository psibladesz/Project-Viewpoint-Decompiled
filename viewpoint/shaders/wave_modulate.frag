#version 330







uniform sampler2D uSpectrum;
uniform sampler2D uSpectrumBefore;
uniform float uSpectrumMix;
uniform float uTileMetres;
uniform float uDepth;
uniform float uTime;
uniform float uPeriod;
layout(location = 0) out vec4 outA;
layout(location = 1) out vec4 outB;
const float WAVE_G = 9.81, WAVE_TAU = 6.28318531;
vec2 mulComplex(vec2 a, vec2 b) { return vec2(a.x * b.x - a.y * b.y, a.x * b.y + a.y * b.x); }

vec2 pack(vec2 a, vec2 b) { return vec2(a.x - b.y, a.y + b.x); }
void main() {
  ivec2 id = ivec2(gl_FragCoord.xy);
  int size = textureSize(uSpectrum, 0).x;
  vec2 k = vec2(id - size / 2) * (WAVE_TAU / uTileMetres);
  float kl = max(length(k), 1.0e-6);
  vec4 h0 = mix(texelFetch(uSpectrumBefore, id, 0), texelFetch(uSpectrum, id, 0), uSpectrumMix);
  float w0 = WAVE_TAU / uPeriod, w = floor(sqrt(WAVE_G * kl * tanh(kl * uDepth)) / w0) * w0;
  vec2 turn = vec2(cos(w * uTime), sin(w * uTime));
  vec2 h = mulComplex(h0.xy, turn) + mulComplex(h0.zw, vec2(turn.x, -turn.y));
  vec2 ih = vec2(-h.y, h.x);
  outA = vec4(pack(h, ih * k.x), pack(ih * k.y, h * (k.x * k.x / kl)));
  outB = vec4(pack(h * (k.y * k.y / kl), h * (k.x * k.y / kl)), 0.0, 0.0);
}
