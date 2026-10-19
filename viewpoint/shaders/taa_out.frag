#version 330







in vec2 vUv;
uniform sampler2D uColor;
uniform float uSharpen;
out vec4 fragColor;
#include "lib/panini.glsl"
#include "lib/catmull_rom.glsl"

const float LIMIT = 0.25 - 1.0 / 16.0;
float max3(vec3 c) { return max(c.r, max(c.g, c.b)); }

vec3 rcas(vec3 e, vec3 b, vec3 d, vec3 f, vec3 h) {

  float bL = b.b * 0.5 + (b.r * 0.5 + b.g), dL = d.b * 0.5 + (d.r * 0.5 + d.g), eL = e.b * 0.5 + (e.r * 0.5 + e.g);
  float fL = f.b * 0.5 + (f.r * 0.5 + f.g), hL = h.b * 0.5 + (h.r * 0.5 + h.g);
  float range = max(max(max(bL, dL), max(eL, fL)), hL) - min(min(min(bL, dL), min(eL, fL)), hL);
  float noise = clamp(abs(0.25 * (bL + dL + fL + hL) - eL) / max(range, 1.0e-5), 0.0, 1.0);
  noise = 1.0 - 0.5 * noise;
  vec3 ringLo = min(min(b, d), min(f, h)), ringHi = max(max(b, d), max(f, h));
  vec3 hitMin = min(ringLo, e) / max(4.0 * ringHi, 1.0e-5);
  vec3 hitMax = (1.0 - max(ringHi, e)) / min(4.0 * ringLo - 4.0, -1.0e-5);
  vec3 lobes = max(-hitMin, hitMax);
  float lobe = max(-LIMIT, min(max3(lobes), 0.0)) * uSharpen * noise;
  return (lobe * (b + d + f + h) + e) / (4.0 * lobe + 1.0);
}
void main() {
  vec3 c;
  if (uPanini.x > 0.0) {
    vec2 at = paniniFrame(vUv), texel = 1.0 / vec2(textureSize(uColor, 0));
    c = rcas(catmullRom(uColor, at, texel), texture(uColor, at + vec2(0.0, texel.y)).rgb,
             texture(uColor, at - vec2(texel.x, 0.0)).rgb, texture(uColor, at + vec2(texel.x, 0.0)).rgb,
             texture(uColor, at - vec2(0.0, texel.y)).rgb);
  } else {

    ivec2 last = textureSize(uColor, 0) - 1, p = clamp(ivec2(vUv * vec2(last + 1)), ivec2(0), last);
    c = rcas(texelFetch(uColor, p, 0).rgb, texelFetch(uColor, clamp(p + ivec2(0, 1), ivec2(0), last), 0).rgb,
             texelFetch(uColor, clamp(p - ivec2(1, 0), ivec2(0), last), 0).rgb,
             texelFetch(uColor, clamp(p + ivec2(1, 0), ivec2(0), last), 0).rgb,
             texelFetch(uColor, clamp(p - ivec2(0, 1), ivec2(0), last), 0).rgb);
  }
  fragColor = vec4(clamp(c, 0.0, 1.0), 1.0);
}
