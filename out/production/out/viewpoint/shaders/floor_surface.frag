#version 330








in vec2 vUv;
uniform sampler2D uColour;
uniform sampler2D uMaterial;
uniform sampler2D uStones;
uniform vec2 uOrigin;
uniform float uTexelsPerTile;
out vec4 fragSurface;
#include "lib/floor_materials.glsl"


const float LOCAL_LEVEL = 3.0;

const float RELIEF = 0.05, FINE = 0.006, STONES = 0.012;
const int FINE_FEATURES = 24, STONE_FEATURES = 12;

const float HEIGHT_RANGE = 0.04;

const vec4 FLAT = vec4(0.5, 0.5, 0.75, 0.5);


uint mixBits(uint x) {
  x ^= x >> 16u;
  x *= 0x7feb352du;
  x ^= x >> 15u;
  x *= 0x846ca68bu;
  return x ^ (x >> 16u);
}

float hash(ivec2 cell, uint seed) {
  return float(mixBits(uint(cell.x) ^ mixBits(uint(cell.y) ^ seed))) / 4294967295.0;
}




float valueNoise(ivec2 base, vec2 local, uint seed) {
  vec2 whole = floor(local), f = local - whole, u = f * f * (3.0 - 2.0 * f);
  ivec2 i = base + ivec2(whole);
  float a = hash(i, seed), b = hash(i + ivec2(1, 0), seed), c = hash(i + ivec2(0, 1), seed);
  float d = hash(i + ivec2(1), seed);
  return mix(mix(a, b, u.x), mix(c, d, u.x), u.y) * 2.0 - 1.0;
}


float grain(ivec2 base, vec2 local) {
  return 0.67 * valueNoise(base, local, 1u) + 0.33 * valueNoise(base * 2, local * 2.0, 2u);
}


float stones(ivec2 base, vec2 local) {
  vec2 whole = floor(local), f = local - whole;
  ivec2 i = base + ivec2(whole);
  float nearest = 1.0;
  for (int y = -1; y <= 1; y++) {
    for (int x = -1; x <= 1; x++) {
      ivec2 cell = ivec2(x, y);
      vec2 at = vec2(cell) + 0.15 + 0.7 * vec2(hash(i + cell, 3u), hash(i + cell, 4u));
      nearest = min(nearest, length(at - f));
    }
  }
  return max(0.0, 1.0 - 2.0 * nearest) * 2.0 - 0.5;
}

float heightAt(ivec2 p, ivec2 size) {
  p = clamp(p, ivec2(0), size - 1);
  vec4 c = texelFetch(uColour, p, 0);
  if (c.a <= 0.0) return 0.0;
  vec3 material = texelFetch(uMaterial, p, 0).rgb / c.a;
  vec2 stoneAndPaving = texelFetch(uStones, p, 0).rg / c.a;
  float stoneShare = stoneAndPaving.x;
  vec4 around = textureLod(uColour, (vec2(p) + 0.5) / vec2(size), LOCAL_LEVEL);
  vec3 colour = c.rgb / c.a, aroundColour = around.a > 0.0 ? around.rgb / around.a : colour;
  float brick = stoneAndPaving.y * floorWarmth(aroundColour);
  float lum = jointHeight(colour, brick), mean = jointHeight(aroundColour, brick);
  ivec2 origin = ivec2(uOrigin);
  vec2 local = (vec2(p) + 0.5) / uTexelsPerTile;
  return (lum - mean) * material.g * RELIEF
      + material.b * FINE * grain(origin * FINE_FEATURES, local * float(FINE_FEATURES))
      + stoneShare * STONES * stones(origin * STONE_FEATURES, local * float(STONE_FEATURES));
}

void main() {
  ivec2 p = ivec2(gl_FragCoord.xy), size = textureSize(uColour, 0);
  vec4 c = texelFetch(uColour, p, 0);
  if (c.a <= 0.0) {
    fragSurface = FLAT;
    return;
  }
  float east = heightAt(p + ivec2(1, 0), size) - heightAt(p - ivec2(1, 0), size);
  float south = heightAt(p + ivec2(0, 1), size) - heightAt(p - ivec2(0, 1), size);
  vec2 slope = vec2(east, south) * uTexelsPerTile * 0.5;
  vec3 n = normalize(vec3(-slope.x, 1.0, -slope.y));
  float roughness = texelFetch(uMaterial, p, 0).r / c.a;
  float height = clamp(heightAt(p, size) / HEIGHT_RANGE * 0.5 + 0.5, 0.0, 1.0);
  fragSurface = vec4(n.x * 0.5 + 0.5, n.z * 0.5 + 0.5, roughness, height);
}
