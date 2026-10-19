











#ifndef DEFAULT_WATER_SURFACE
#define DEFAULT_WATER_SURFACE
#include "constants.glsl"
#include "lib/far_relief.glsl"
#include "lib/weather.glsl"
#include "lib/rain_ground.glsl"
#include "lib/specular_roughness.glsl"
uniform sampler2DArray uWaves;
uniform vec2 uWaveTiles;
uniform float uWaveTime;
uniform vec4 uWaveDrift;

const float FLOW_CYCLE = 2.0, FLOW_SHIFT = 0.5;


const float WIND_FULL = 6.0, CALM_LEAST = 0.12, RUNNING_LEAST = 0.45;

const float RIPPLE_SHARE = 0.6;

const float STILL_LAYER = 2.0;


const float MIRROR_SHARP = 0.4, MIRROR_SPREAD = 4.0, MIRROR_KEPT = 0.15, ROUGH_WATER_SPREAD = 0.35;

struct WaterSurface {
  vec3 normal;
  float height;
  float foam;
  vec2 flow;
  vec3 mirror;
  float rough;
  float kind;
  float strength;
};



vec4 waveTile(vec2 p, vec2 q, vec2 drift, vec2 ripple, float layer) {
  vec4 a = texture(uWaves, vec3((p - drift) / uWaveTiles.x, layer));
  vec4 b = texture(uWaves, vec3((q - ripple) / uWaveTiles.y, layer + 1.0));
  return vec4(a.xy + b.xy * RIPPLE_SHARE, a.z + b.z * RIPPLE_SHARE, max(a.w, b.w));
}


WaterSurface waterSurface(vec3 pos, float far) {
  vec2 p = (pos + uSceneOrigin).xz;
  vec4 body = uReliefGrid.x > 0.0 ? reliefFlow(uReliefCamera - pos.xz) : vec4(0.0, 0.0, 2.0, 1.0);
  vec2 flow = -body.xy * SQUARES_A_METRE;
  float wind = length(uWind) / SQUARES_A_METRE, speed = length(flow) / SQUARES_A_METRE;
  float strength = max(clamp(wind / WIND_FULL, CALM_LEAST, 1.0), body.z > 2.5 ? RUNNING_LEAST + speed * 0.4 : 0.0);
#if PIPELINE_WATER_FFT
  vec4 drift = vec4(0.0);
#else
  vec4 drift = uWaveDrift;
#endif
  vec4 w;
  if (speed > 1.0e-3) {
    float shift = texture(uRainTextures, vec3(p / PUDDLE_SQUARES, 1.0)).r * FLOW_SHIFT;
    float phase = fract(uWaveTime / FLOW_CYCLE + shift), other = fract(phase + 0.5);
    vec2 a = flow * (phase - 0.5) * FLOW_CYCLE, b = flow * (other - 0.5) * FLOW_CYCLE;


    vec4 one = waveTile(p, p, a, a, STILL_LAYER);
    vec4 two = waveTile(p, p + vec2(0.37, 0.71) * uWaveTiles.y, b, b, STILL_LAYER);
    w = mix(one, two, abs(phase - 0.5) * 2.0);
  } else {
    w = waveTile(p, p, drift.xy, drift.zw, 0.0);
  }
  vec2 tilt = w.xy * strength + rings(p, far);
  WaterSurface s;
  s.normal = normalize(vec3(-tilt.x, 1.0, -tilt.y));
  s.height = w.z * strength;
  s.foam = w.w * strength;
  s.flow = flow;
  s.kind = body.z;
  s.strength = strength;


  float pixel = 2.0 * uCamera.w / float(textureSize(uDepth, 0).y);
  float rise = max(abs(normalize(uEye - pos).y), 0.05);
  float spread = smoothstep(MIRROR_SHARP, MIRROR_SPREAD, far * pixel / rise);
  s.mirror = normalize(mix(s.normal, vec3(0.0, 1.0, 0.0), spread * (1.0 - MIRROR_KEPT)));
  s.rough = mix(ROUGH_WATER, ROUGH_WATER_SPREAD, spread);
  return s;
}
#endif
