








#ifndef VIEWPOINT_CLOUD_SHAPE
#define VIEWPOINT_CLOUD_SHAPE
#include "lib/weather.glsl"
uniform sampler3D uCloudNoise;
uniform float uCloudCover;
uniform float uCloudTime;
uniform vec2 uCloudOffset;


const float CLOUD_BASE = 600.0;
const float LAYER_THICK = 350.0, HEAPED_THICK = 650.0, TOWER_THICK = 2800.0;

const float CLOUD_TOP = CLOUD_BASE + TOWER_THICK;

const float CLOUD_PERIOD = 1280.0;
const float CLOUD_TEXEL = CLOUD_PERIOD / 64.0, CLOUD_DETAIL_TEXEL = CLOUD_TEXEL / 5.0;


const float CLOUD_CHURN = 0.25;

const float CLOUD_LEAN = 0.03;

const float CLOUD_EROSION = 0.35, RAGGED_EROSION = 0.6;

const float DECK_DENSITY = 2.0;


float cloudThickness(float type) {
  float towering = max(type * 2.0 - 1.0, 0.0);
  return type < 0.5 ? mix(LAYER_THICK, HEAPED_THICK, type * 2.0) : mix(HEAPED_THICK, TOWER_THICK, towering * towering);
}


vec2 cloudLayer() { return vec2(CLOUD_BASE, CLOUD_BASE + cloudThickness(uCloudType)); }

vec3 cloudSpace(vec3 p) {
  return vec3(p.x + uCloudOffset.x, p.y + uCloudTime * CLOUD_CHURN, p.z + uCloudOffset.y) / CLOUD_PERIOD;
}

vec4 cloudWeather(vec3 p) {
  vec2 lean = uCloudWind * (CLOUD_LEAN * max(p.y - CLOUD_BASE, 0.0));
  return weatherAt(vec3(p.x - lean.x, p.y, p.z - lean.y));
}

vec2 cloudSlab(vec4 w) { return vec2(CLOUD_BASE, cloudThickness(w.b) * mix(0.35, 1.0, w.r)); }


float cloudProfile(float h, float t) {
  float bottom = smoothstep(0.0, mix(0.2, 0.05, smoothstep(0.0, 0.5, t)), h);
  return bottom * (1.0 - smoothstep(mix(0.45, 0.75, t), 1.0, h));
}

float cloudRemap(float n, float c) { return clamp((n - 1.0 + c) / max(c, 0.001), 0.0, 1.0) * c; }





float cloudDensity(vec3 p, vec4 w, float footprint, bool detail) {
  vec2 slab = cloudSlab(w);
  float lod = max(log2(footprint / CLOUD_TEXEL), 0.0);
  vec3 q = cloudSpace(p);
  float h = (p.y - slab.x) / slab.y;
  float d = 0.0;
  if (h > 0.0 && h < 1.0 && w.r > 0.004) {
    d = cloudRemap(textureLod(uCloudNoise, q, lod).r, w.r * cloudProfile(h, w.b));
  }
  float weight = 1.0 - smoothstep(0.5, 2.5, log2(footprint / CLOUD_DETAIL_TEXEL));
  if (detail && d > 0.0 && weight > 0.0) {
    float fine = textureLod(uCloudNoise, q * 5.0, max(log2(footprint / CLOUD_DETAIL_TEXEL), 0.0)).r;


    float ragged = mix(CLOUD_EROSION, RAGGED_EROSION, w.g * (1.0 - clamp(h * 3.0, 0.0, 1.0)));
    float erode = mix(fine, 1.0 - fine, clamp(h * 6.0, 0.0, 1.0)) * ragged * weight;
    d = clamp((d - erode) / (1.0 - erode), 0.0, 1.0);
  }
  return d * mix(1.0, DECK_DENSITY, w.a);
}

float cloudShape(vec3 p, float footprint, bool detail) { return cloudDensity(p, cloudWeather(p), footprint, detail); }
#endif
