#version 330











in vec2 vUv;
uniform sampler2D uViewDepth;
uniform sampler2D uNormal;
uniform mat3 uViewRot;
uniform vec2 uTanHalf;
uniform vec2 uPixel;
uniform float uRadius;
uniform int uNoiseIndex;
layout(location = 0) out vec4 fragTerm;
layout(location = 1) out float fragEdges;
#include "lib/gtao.glsl"


#ifndef OPTION_OCCLUSION_QUALITY
#define OPTION_OCCLUSION_QUALITY 2
#define OPTION_OCCLUSION_QUALITY_LOW 0
#define OPTION_OCCLUSION_QUALITY_MEDIUM 1
#define OPTION_OCCLUSION_QUALITY_HIGH 2
#endif
#ifndef OPTION_OCCLUSION_RADIUS
#define OPTION_OCCLUSION_RADIUS 1.0
#endif
#ifndef OPTION_OCCLUSION_STRENGTH
#define OPTION_OCCLUSION_STRENGTH 1.0
#endif

#if OPTION_OCCLUSION_QUALITY == OPTION_OCCLUSION_QUALITY_LOW
const int SLICES = 1, STEPS = 2;
#elif OPTION_OCCLUSION_QUALITY == OPTION_OCCLUSION_QUALITY_MEDIUM
const int SLICES = 2, STEPS = 2;
#elif OPTION_OCCLUSION_QUALITY == OPTION_OCCLUSION_QUALITY_HIGH
const int SLICES = 3, STEPS = 3;
#else
const int SLICES = 9, STEPS = 3;
#endif

const float TOO_CLOSE = 1.3;


uint hilbert(uvec2 p) {
  uint index = 0u;
  for (uint level = 32u; level > 0u; level /= 2u) {
    uint rx = (p.x & level) > 0u ? 1u : 0u, ry = (p.y & level) > 0u ? 1u : 0u;
    index += level * level * ((3u * rx) ^ ry);
    if (ry == 0u) {
      if (rx == 1u) p = uvec2(63u) - p;
      p = p.yx;
    }
  }
  return index;
}

vec2 noise(ivec2 pixel) {
  uint index = hilbert(uvec2(pixel) % 64u) + 288u * uint(uNoiseIndex % 64);
  return fract(0.5 + float(index) * vec2(0.75487766624669276005, 0.5698402909980532659114));
}


vec4 edges(float c, float l, float r, float t, float b) {
  vec4 e = vec4(l, r, t, b) - c;
  float slopeLR = (e.y - e.x) * 0.5, slopeTB = (e.w - e.z) * 0.5;
  e = min(abs(e), abs(e + vec4(slopeLR, -slopeLR, slopeTB, -slopeTB)));
  return clamp(1.25 - e / (c * 0.011), 0.0, 1.0);
}


float packEdges(vec4 e) {
  return dot(floor(clamp(e, 0.0, 1.0) * 2.9 + 0.5), vec4(64.0, 16.0, 4.0, 1.0) / 255.0);
}


vec3 rotateFromTo(vec3 from, vec3 to, vec3 v) {
  float e = dot(from, to);
  if (abs(e) > 1.0 - 0.0003) return v;
  vec3 c = cross(from, to);
  float h = 1.0 / (1.0 + e);
  vec3 row0 = vec3(e + h * c.x * c.x, h * c.x * c.y - c.z, h * c.x * c.z + c.y);
  vec3 row1 = vec3(h * c.x * c.y + c.z, e + h * c.y * c.y, h * c.y * c.z - c.x);
  vec3 row2 = vec3(h * c.x * c.z - c.y, h * c.y * c.z + c.x, e + h * c.z * c.z);
  return vec3(dot(row0, v), dot(row1, v), dot(row2, v));
}





vec3 viewNormal(ivec2 p) {
  return normalize(transpose(uViewRot) * texelFetch(uNormal, p, 0).xyz);
}



vec3 depthNormal(vec2 uv, vec3 center, vec4 e, float l, float r, float t, float b) {
  vec3 left = normalize(gtaoViewPos(uv - vec2(uPixel.x, 0.0), l, uTanHalf) - center);
  vec3 right = normalize(gtaoViewPos(uv + vec2(uPixel.x, 0.0), r, uTanHalf) - center);
  vec3 top = normalize(gtaoViewPos(uv + vec2(0.0, uPixel.y), t, uTanHalf) - center);
  vec3 bottom = normalize(gtaoViewPos(uv - vec2(0.0, uPixel.y), b, uTanHalf) - center);
  vec4 kept = clamp(vec4(e.y * e.z, e.z * e.x, e.x * e.w, e.w * e.y) + 0.01, 0.0, 1.0);
  vec3 n = kept.x * cross(right, top) + kept.y * cross(top, left) + kept.z * cross(left, bottom)
      + kept.w * cross(bottom, right);
  return n * inversesqrt(max(dot(n, n), 1.0e-12));
}


float sampleDepth(vec2 uv, float level, out float blocks) {
  float raw = textureLod(uViewDepth, uv, level).r;
  blocks = raw < 0.0 ? GTAO_PLANT_BLOCKS : 1.0;
  return abs(raw);
}

void main() {
  ivec2 p = ivec2(gl_FragCoord.xy);
  vec2 uv = (vec2(p) + 0.5) * uPixel;
  float z = abs(texelFetch(uViewDepth, p, 0).r);
  float l = abs(texelFetch(uViewDepth, p + ivec2(-1, 0), 0).r), r = abs(texelFetch(uViewDepth, p + ivec2(1, 0), 0).r);
  float t = abs(texelFetch(uViewDepth, p + ivec2(0, 1), 0).r), b = abs(texelFetch(uViewDepth, p + ivec2(0, -1), 0).r);
  vec4 e = edges(z, l, r, t, b);
  fragEdges = packEdges(e);
  if (z >= GTAO_DEPTH_MOST) {
    fragTerm = vec4(0.5, 0.5, 1.0, 1.0 / GTAO_TERM_SCALE);
    return;
  }
  vec3 shading = viewNormal(p);
  vec3 center = gtaoViewPos(uv, z * 0.99999, uTanHalf);
  vec3 n = depthNormal(uv, gtaoViewPos(uv, z, uTanHalf), e, l, r, t, b);
  vec3 viewVec = normalize(-center);
  float radius = uRadius * float(OPTION_OCCLUSION_RADIUS);
  float range = GTAO_FALLOFF_RANGE * radius, from = radius * (1.0 - GTAO_FALLOFF_RANGE);
  float falloffMul = -1.0 / range, falloffAdd = from / range + 1.0;
  float screenRadius = radius / (z * 2.0 * uTanHalf.x * uPixel.x);
  float visibility = clamp((10.0 - screenRadius) / 100.0, 0.0, 1.0) * 0.5;
  vec3 bent = vec3(0.0);
  vec2 rnd = noise(p);
  float minS = TOO_CLOSE / screenRadius;
  for (int slice = 0; slice < SLICES; slice++) {
    float phi = (float(slice) + rnd.x) / float(SLICES) * GTAO_PI, cosPhi = cos(phi), sinPhi = sin(phi);
    vec2 omega = vec2(cosPhi, sinPhi) * screenRadius;
    vec3 dir = vec3(cosPhi, sinPhi, 0.0);
    vec3 ortho = dir - dot(dir, viewVec) * viewVec;
    vec3 axis = normalize(cross(ortho, viewVec));
    vec3 projected = n - axis * dot(n, axis);
    float projectedLength = length(projected);
    float cosNorm = clamp(dot(projected, viewVec) / projectedLength, 0.0, 1.0);
    float angle = sign(dot(ortho, projected)) * gtaoAcos(cosNorm);
    float low0 = cos(angle + GTAO_HALF_PI), low1 = cos(angle - GTAO_HALF_PI);
    float horizon0 = low0, horizon1 = low1;
    for (int k = 0; k < STEPS; k++) {
      float stepNoise = fract(rnd.y + float(slice + k * STEPS) * 0.6180339887);
      float s = pow((float(k) + stepNoise) / float(STEPS), GTAO_DISTRIBUTION_POWER) + minS;
      vec2 offset = s * omega;
      float level = clamp(log2(length(offset)) - GTAO_MIP_OFFSET, 0.0, float(GTAO_LEVELS - 1));
      offset = floor(offset + 0.5) * uPixel;
      vec2 uv0 = uv + offset, uv1 = uv - offset;
      float blocks0, blocks1;
      vec3 delta0 = gtaoViewPos(uv0, sampleDepth(uv0, level, blocks0), uTanHalf) - center;
      vec3 delta1 = gtaoViewPos(uv1, sampleDepth(uv1, level, blocks1), uTanHalf) - center;
      float dist0 = length(delta0), dist1 = length(delta1);
      float weight0 = clamp(dist0 * falloffMul + falloffAdd, 0.0, 1.0) * blocks0;
      float weight1 = clamp(dist1 * falloffMul + falloffAdd, 0.0, 1.0) * blocks1;
      horizon0 = max(horizon0, mix(low0, dot(delta0 / dist0, viewVec), weight0));
      horizon1 = max(horizon1, mix(low1, dot(delta1 / dist1, viewVec), weight1));
    }
    projectedLength = mix(projectedLength, 1.0, 0.05);
    float h0 = -gtaoAcos(horizon1), h1 = gtaoAcos(horizon0);
    float arc0 = (cosNorm + 2.0 * h0 * sin(angle) - cos(2.0 * h0 - angle)) / 4.0;
    float arc1 = (cosNorm + 2.0 * h1 * sin(angle) - cos(2.0 * h1 - angle)) / 4.0;
    visibility += projectedLength * (arc0 + arc1);

    float t0 = (6.0 * sin(h0 - angle) - sin(3.0 * h0 - angle) + 6.0 * sin(h1 - angle) - sin(3.0 * h1 - angle)
        + 16.0 * sin(angle) - 3.0 * (sin(h0 + angle) + sin(h1 + angle))) / 12.0;
    float t1 = (-cos(3.0 * h0 - angle) - cos(3.0 * h1 - angle) + 8.0 * cos(angle)
        - 3.0 * (cos(h0 + angle) + cos(h1 + angle))) / 12.0;
    bent += rotateFromTo(vec3(0.0, 0.0, 1.0), viewVec, vec3(dir.xy * t0, t1)) * projectedLength;
  }
  visibility = max(0.03, pow(visibility / float(SLICES), GTAO_FINAL_POWER * float(OPTION_OCCLUSION_STRENGTH)));
  vec3 carried = rotateFromTo(n, normalize(bent), shading);
  fragTerm = vec4(carried * 0.5 + 0.5, clamp(visibility / GTAO_TERM_SCALE, 0.0, 1.0));
}
