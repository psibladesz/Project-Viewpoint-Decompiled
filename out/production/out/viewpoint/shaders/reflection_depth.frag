#version 330









in vec2 vUv;
uniform sampler2D uSource;
uniform sampler2D uFarDepth;
uniform int uFirst;
uniform int uScale;
uniform vec2 uFarThen;
uniform mat4 uPrevious;
out vec4 fragColor;
#include "lib/camera.glsl"

const float SKY = 2.0;


float farDepth(ivec2 texel) {
  if (uFarThen.y <= 0.0) return SKY;
  vec2 uv = (vec2(texel) + 0.5) / vec2(textureSize(uSource, 0));
  vec3 way = normalize(scenePos(uv, 1.0) - uEye);
  vec4 c = uPrevious * vec4(way, 0.0);
  vec2 then = c.xy / c.w * 0.5 + 0.5;
  if (c.w <= 0.0 || any(lessThan(then, vec2(0.0))) || any(greaterThan(then, vec2(1.0)))) return SKY;
  vec2 size = vec2(textureSize(uFarDepth, 0));
  float d = texelFetch(uFarDepth, ivec2(min(then * size, size - 1.0)), 0).r;
  if (d >= 1.0) return SKY;

  float n = uFarThen.x, f = uFarThen.y, z = d * 2.0 - 1.0;
  float zThen = 2.0 * n * f / (f + n - z * (f - n));
  float distance = zThen * length(vec3((then * 2.0 - 1.0) * uCamera.zw, 1.0));
  float zNow = distance / length(vec3((uv * 2.0 - 1.0) * uCamera.zw, 1.0));
  n = uCamera.x;
  f = uCamera.y;
  return 0.5 + 0.5 * ((f + n) / (f - n) - 2.0 * f * n / ((f - n) * zNow));
}

float depthAt(ivec2 texel) {
  float d = texelFetch(uSource, texel, 0).r;
  return uFirst == 1 && d >= 1.0 ? farDepth(texel) : d;
}

void main() {
  int side = uFirst == 1 ? uScale : 2;
  ivec2 size = textureSize(uSource, 0), texel = ivec2(gl_FragCoord.xy), p = texel * side, last = size - 1;

  ivec2 lastTexel = max(size / side, ivec2(1)) - 1;
  ivec2 hi = min(p + side - 1, last);
  if (texel.x == lastTexel.x) hi.x = last.x;
  if (texel.y == lastTexel.y) hi.y = last.y;
  float d = 1.0e9;
  for (int y = p.y; y <= hi.y; y++) {
    for (int x = p.x; x <= hi.x; x++) d = min(d, depthAt(ivec2(x, y)));
  }
  fragColor = vec4(d);
}
