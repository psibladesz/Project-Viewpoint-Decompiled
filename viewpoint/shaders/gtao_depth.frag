#version 330






in vec2 vUv;


uniform sampler2D uSource;
uniform sampler2D uAlbedo;
uniform int uLevel;
uniform float uRadius;
out float fragDepth;
#include "lib/camera.glsl"
#include "lib/gtao.glsl"
#include "lib/materials.glsl"

void main() {
  ivec2 p = ivec2(gl_FragCoord.xy);
  if (uLevel == 0) {
    float z = min(linearDepth(texelFetch(uSource, p, 0).r), GTAO_DEPTH_MOST);
    fragDepth = z < GTAO_DEPTH_MOST && isFoliage(texelFetch(uAlbedo, p, 0).a) ? -z : z;
    return;
  }
  vec4 raw = vec4(texelFetch(uSource, p * 2, 0).r, texelFetch(uSource, p * 2 + ivec2(1, 0), 0).r,
                  texelFetch(uSource, p * 2 + ivec2(0, 1), 0).r, texelFetch(uSource, p * 2 + ivec2(1, 1), 0).r);
  vec4 d = abs(raw);
  float far = max(max(d.x, d.y), max(d.z, d.w));

  float radius = 0.75 * uRadius, range = GTAO_FALLOFF_RANGE * radius, from0 = radius * (1.0 - GTAO_FALLOFF_RANGE);
  float mul = -1.0 / range, add = from0 / range + 1.0;
  vec4 w = clamp((far - d) * mul + add, 0.0, 1.0);
  float total = max(w.x + w.y + w.z + w.w, 1.0e-6), depth = dot(w, d) / total;
  fragDepth = dot(w, vec4(lessThan(raw, vec4(0.0)))) > 0.5 * total ? -depth : depth;
}
