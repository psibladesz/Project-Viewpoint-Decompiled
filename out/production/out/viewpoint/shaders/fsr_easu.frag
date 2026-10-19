#version 330






in vec2 vUv;
uniform sampler2D uColor;
out vec4 fragColor;

vec3 tap(ivec2 p) {
  return texelFetch(uColor, clamp(p, ivec2(0), textureSize(uColor, 0) - 1), 0).rgb;
}


float luma(vec3 c) {
  return c.b * 0.5 + (c.r * 0.5 + c.g);
}



void tapF(inout vec3 sum, inout float weights, vec2 off, vec2 dir, vec2 len, float lob, float clp, vec3 c) {
  vec2 v = vec2(off.x * dir.x + off.y * dir.y, -off.x * dir.y + off.y * dir.x) * len;
  float d2 = min(dot(v, v), clp);
  float wB = 0.4 * d2 - 1.0, wA = lob * d2 - 1.0;
  wB = 25.0 / 16.0 * wB * wB - (25.0 / 16.0 - 1.0);
  float w = wB * wA * wA;
  sum += c * w;
  weights += w;
}



void setF(inout vec2 dir, inout float len, float w, float lA, float lB, float lC, float lD, float lE) {
  float lenX = abs(lD - lB) / max(max(abs(lD - lC), abs(lC - lB)), 1.0e-30);
  lenX = clamp(lenX, 0.0, 1.0);
  dir.x += (lD - lB) * w;
  len += lenX * lenX * w;
  float lenY = abs(lE - lA) / max(max(abs(lE - lC), abs(lC - lA)), 1.0e-30);
  lenY = clamp(lenY, 0.0, 1.0);
  dir.y += (lE - lA) * w;
  len += lenY * lenY * w;
}

void main() {
  vec2 pp = vUv * vec2(textureSize(uColor, 0)) - 0.5, fp = floor(pp);
  pp -= fp;
  ivec2 f = ivec2(fp);




  vec3 b = tap(f + ivec2(0, -1)), c = tap(f + ivec2(1, -1));
  vec3 e = tap(f + ivec2(-1, 0)), ff = tap(f), g = tap(f + ivec2(1, 0)), h = tap(f + ivec2(2, 0));
  vec3 i = tap(f + ivec2(-1, 1)), j = tap(f + ivec2(0, 1)), k = tap(f + ivec2(1, 1)), l = tap(f + ivec2(2, 1));
  vec3 n = tap(f + ivec2(0, 2)), o = tap(f + ivec2(1, 2));
  float bL = luma(b), cL = luma(c), eL = luma(e), fL = luma(ff), gL = luma(g), hL = luma(h);
  float iL = luma(i), jL = luma(j), kL = luma(k), lL = luma(l), nL = luma(n), oL = luma(o);
  vec2 dir = vec2(0.0);
  float len = 0.0;
  setF(dir, len, (1.0 - pp.x) * (1.0 - pp.y), bL, eL, fL, gL, jL);
  setF(dir, len, pp.x * (1.0 - pp.y), cL, fL, gL, hL, kL);
  setF(dir, len, (1.0 - pp.x) * pp.y, fL, iL, jL, kL, nL);
  setF(dir, len, pp.x * pp.y, gL, jL, kL, lL, oL);

  float dirR = dot(dir, dir);
  bool none = dirR < 1.0 / 32768.0;
  dir = none ? vec2(1.0, 0.0) : dir * inversesqrt(dirR);
  len = 0.25 * len * len;


  float stretch = dot(dir, dir) / max(abs(dir.x), abs(dir.y));
  vec2 len2 = vec2(1.0 + (stretch - 1.0) * len, 1.0 - 0.5 * len);
  float lob = 0.5 + (0.25 - 0.04 - 0.5) * len, clp = 1.0 / lob;
  vec3 least = min(min(ff, g), min(j, k)), most = max(max(ff, g), max(j, k));
  vec3 sum = vec3(0.0);
  float weights = 0.0;
  tapF(sum, weights, vec2(0.0, -1.0) - pp, dir, len2, lob, clp, b);
  tapF(sum, weights, vec2(1.0, -1.0) - pp, dir, len2, lob, clp, c);
  tapF(sum, weights, vec2(-1.0, 1.0) - pp, dir, len2, lob, clp, i);
  tapF(sum, weights, vec2(0.0, 1.0) - pp, dir, len2, lob, clp, j);
  tapF(sum, weights, vec2(0.0, 0.0) - pp, dir, len2, lob, clp, ff);
  tapF(sum, weights, vec2(-1.0, 0.0) - pp, dir, len2, lob, clp, e);
  tapF(sum, weights, vec2(1.0, 1.0) - pp, dir, len2, lob, clp, k);
  tapF(sum, weights, vec2(2.0, 1.0) - pp, dir, len2, lob, clp, l);
  tapF(sum, weights, vec2(2.0, 0.0) - pp, dir, len2, lob, clp, h);
  tapF(sum, weights, vec2(1.0, 0.0) - pp, dir, len2, lob, clp, g);
  tapF(sum, weights, vec2(1.0, 2.0) - pp, dir, len2, lob, clp, o);
  tapF(sum, weights, vec2(0.0, 2.0) - pp, dir, len2, lob, clp, n);
  fragColor = vec4(min(most, max(least, sum / weights)), 1.0);
}
