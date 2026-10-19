#version 330





in vec2 vUv;
uniform sampler2D uTerm;
uniform sampler2D uEdges;
uniform mat3 uViewRot;
out vec4 fragColor;
#include "lib/gtao.glsl"

const float BLUR_BETA = 1.2;
const float DIAGONAL = 0.85 * 0.5;


vec4 edgesAt(ivec2 p) {
  float bits = floor(texelFetch(uEdges, p, 0).r * 255.5);
  return clamp(vec4(floor(bits / 64.0), mod(floor(bits / 16.0), 4.0), mod(floor(bits / 4.0), 4.0),
      mod(bits, 4.0)) / 3.0, 0.0, 1.0);
}

vec4 termAt(ivec2 p) {
  vec4 t = texelFetch(uTerm, p, 0);
  return vec4(t.xyz * 2.0 - 1.0, t.w);
}

void main() {
  ivec2 p = ivec2(gl_FragCoord.xy);
  vec4 l = edgesAt(p + ivec2(-1, 0)), r = edgesAt(p + ivec2(1, 0));
  vec4 t = edgesAt(p + ivec2(0, 1)), b = edgesAt(p + ivec2(0, -1));

  vec4 c = edgesAt(p) * vec4(l.y, r.x, t.w, b.z);
  const float LEAK_FROM = 2.5, LEAK = 0.5;
  c = clamp(c + clamp(4.0 - LEAK_FROM - dot(c, vec4(1.0)), 0.0, 1.0) / (4.0 - LEAK_FROM) * LEAK, 0.0, 1.0);
  float topLeft = DIAGONAL * (c.x * l.z + c.z * t.x), topRight = DIAGONAL * (c.z * t.y + c.y * r.z);
  float bottomLeft = DIAGONAL * (c.w * b.x + c.x * l.w), bottomRight = DIAGONAL * (c.y * r.w + c.w * b.y);
  vec4 sum = termAt(p) * BLUR_BETA;
  float weight = BLUR_BETA + dot(c, vec4(1.0)) + topLeft + topRight + bottomLeft + bottomRight;
  sum += termAt(p + ivec2(-1, 0)) * c.x + termAt(p + ivec2(1, 0)) * c.y;
  sum += termAt(p + ivec2(0, 1)) * c.z + termAt(p + ivec2(0, -1)) * c.w;
  sum += termAt(p + ivec2(-1, 1)) * topLeft + termAt(p + ivec2(1, 1)) * topRight;
  sum += termAt(p + ivec2(-1, -1)) * bottomLeft + termAt(p + ivec2(1, -1)) * bottomRight;
  sum /= weight;
  vec3 bent = uViewRot * normalize(sum.xyz + vec3(0.0, 0.0, 1.0e-6));
  fragColor = vec4(bent * 0.5 + 0.5, clamp(sum.w * GTAO_TERM_SCALE, 0.0, 1.0));
}
