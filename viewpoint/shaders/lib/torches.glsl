



#ifndef VIEWPOINT_TORCHES
#define VIEWPOINT_TORCHES
#include "lib/cube_shadow.glsl"
uniform int uTorchCount;
uniform vec4 uTorchPos[8];
uniform vec4 uTorchDir[8];
uniform vec3 uTorchColor[8];
#if PIPELINE_FLASHLIGHT_SHADOW
uniform float uMuzzleShadowOn;
uniform vec3 uMuzzleMaps;
uniform sampler2DArrayShadow uMuzzleShadow;
#endif

float torchShadow(int i, vec3 pos, vec3 n) {
#if PIPELINE_FLASHLIGHT_SHADOW
  if (i > 0 || uMuzzleShadowOn < 0.5) return 1.0;
  vec3 v = pos - uTorchPos[0].xyz;
  float m = max(abs(v.x), max(abs(v.y), abs(v.z)));

  v += n * (0.02 + 3.0 * m / uMuzzleMaps.z);
  int face;
  vec2 uv;
  float depth;
  if (!cubeTexel(v, uMuzzleMaps.x, uMuzzleMaps.y, face, uv, depth)) return 1.0;
  float texel = 1.0 / uMuzzleMaps.z, lit = 0.0;
  for (int k = 0; k < 4; k++) {
    lit += texture(uMuzzleShadow, vec4(uv + (vec2(k & 1, k >> 1) - 0.5) * texel, float(face), depth));
  }
  return lit * 0.25;
#else
  return 1.0;
#endif
}
vec3 torches(vec3 pos, vec3 n) {
  vec3 sum = vec3(0.0);
  for (int i = 0; i < uTorchCount; i++) {
    vec3 d = pos - uTorchPos[i].xyz;
    float dist = length(d);
    float reach = uTorchPos[i].w;
    if (dist >= reach) continue;
    vec3 l = d / max(dist, 1.0e-4);
    float edge = uTorchDir[i].w;
    float beam = edge <= -1.0 ? 1.0 : smoothstep(edge, mix(edge, 1.0, 0.3), dot(l, uTorchDir[i].xyz));
    float facing = 0.35 + 0.65 * max(dot(n, -l), 0.0);
    sum += uTorchColor[i] * beam * facing * (1.0 - dist / reach) * torchShadow(i, pos, n);
  }
  return sum;
}
#endif
