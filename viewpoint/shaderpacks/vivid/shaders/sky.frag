#version 330





in vec3 vRay;
in vec2 vUv;
uniform sampler2D uClouds;
uniform float uCloudsOn;
uniform vec3 uHorizon;
uniform vec3 uZenith;
uniform vec3 uSunDir;
uniform vec3 uSunColor;
uniform float uSunStrength;
uniform float uNight;
uniform float uTime;
uniform vec3 uSkyMoon;
uniform vec3 uSunTrue;
uniform float uMoonDisc;
out vec4 fragColor;
#include "lib/noise.glsl"
#include "lib/sky.glsl"
#ifndef OPTION_NEBULA
#define OPTION_NEBULA 0
#endif
#ifndef OPTION_AURORA
#define OPTION_AURORA 0
#endif

const float DISC_EDGE = 0.99965;
const float DISC_FULL = 0.99988;
const float DISC_BRIGHTNESS = 12.0;
const vec3 MOON_COLOUR = vec3(0.62, 0.74, 1.0);


const float STAR_CELLS = 110.0;
const float STAR_FLATTEN = 0.4;
const float STAR_SHARE = 0.08;
const float STAR_SIZE = 0.12;
const float STAR_BRIGHTNESS = 1.2;
const vec3 STAR_COOL = vec3(0.75, 0.85, 1.0);
const vec3 STAR_WARM = vec3(1.0, 0.86, 0.7);


const vec3 NEBULA_AXIS = vec3(0.56, 0.42, -0.71);
const float NEBULA_NARROW = 9.0;
const float NEBULA_SCALE = 3.0;
const vec3 NEBULA_RED = vec3(0.32, 0.12, 0.45);
const vec3 NEBULA_BLUE = vec3(0.1, 0.25, 0.48);
const float NEBULA_BRIGHTNESS = 0.3;



const float AURORA_FOOT = 1.0;
const float AURORA_TOP = 1.8;
const float AURORA_NORTH = 3.0;
const float AURORA_WAVER = 0.8;
const float AURORA_THIN = 0.15;
const float AURORA_DRIFT = 0.03;
const int AURORA_STEPS = 12;
const vec3 AURORA_FOOT_COLOUR = vec3(0.1, 0.9, 0.45);
const vec3 AURORA_TOP_COLOUR = vec3(0.35, 0.2, 0.7);
const float AURORA_BRIGHTNESS = 2.0;
vec3 stars(vec3 r) {
  vec2 g = r.xz / (r.y + STAR_FLATTEN) * STAR_CELLS;
  vec2 cell = floor(g);
  if (hash(cell) < 1.0 - STAR_SHARE) return vec3(0.0);
  vec2 centre = 0.25 + 0.5 * vec2(hash(cell + 3.1), hash(cell + 7.7));
  float size = STAR_SIZE * (0.4 + 0.6 * hash(cell + 5.3));
  float phase = hash(cell + 1.7);
  float twinkle = 0.7 + 0.3 * sin(uTime * (1.0 + 3.0 * phase) + 40.0 * phase);
  vec3 tint = mix(STAR_COOL, STAR_WARM, hash(cell + 9.1));
  return tint * smoothstep(size, 0.0, length(fract(g) - centre)) * twinkle * STAR_BRIGHTNESS;
}
vec3 nebula(vec3 r) {
  float off = dot(r, normalize(NEBULA_AXIS));
  vec2 p = r.xz / (r.y + 0.6) * NEBULA_SCALE;
  float cloud = smoothstep(0.35, 0.8, fbm(p));
  vec3 colour = mix(NEBULA_RED, NEBULA_BLUE, fbm(p * 1.7 + 11.0));
  return colour * cloud * exp(-off * off * NEBULA_NARROW) * NEBULA_BRIGHTNESS;
}

vec3 aurora(vec3 r) {
  vec3 sum = vec3(0.0);
  for (int i = 0; i < AURORA_STEPS; i++) {
    float up = (float(i) + 0.5) / float(AURORA_STEPS);
    vec2 p = r.xz / r.y * mix(AURORA_FOOT, AURORA_TOP, up);
    float line = AURORA_NORTH + AURORA_WAVER * (noise(vec2(p.x * 0.35 + uTime * AURORA_DRIFT, 3.7)) * 2.0 - 1.0);
    float shimmer = 0.5 + noise(vec2(p.x * 1.3 - uTime * AURORA_DRIFT * 2.0, 1.1));
    sum += mix(AURORA_FOOT_COLOUR, AURORA_TOP_COLOUR, up) * exp(-abs(p.y - line) / AURORA_THIN) * shimmer * (1.0 - up);
  }
  return sum * (AURORA_BRIGHTNESS / float(AURORA_STEPS));
}


float moonLit(vec3 r) {
  vec3 across = (r - uSkyMoon * dot(r, uSkyMoon)) / sqrt(1.0 - DISC_EDGE * DISC_EDGE);
  vec3 normal = across - uSkyMoon * sqrt(max(1.0 - dot(across, across), 0.0));
  return smoothstep(-0.02, 0.0, dot(normal, uSunTrue));
}
vec3 nightSky(vec3 r) {
  vec3 sky = stars(r);
#if OPTION_NEBULA
  sky += nebula(r);
#endif
#if OPTION_AURORA
  sky += aurora(r);
#endif
  return sky;
}
void main() {
  vec3 r = normalize(vRay);
  vec3 sky = skyColor(r, uHorizon, uZenith, uSunDir, uSunColor, uSunStrength);
  if (uNight > 0.0 && r.y > 0.0) sky += nightSky(r) * uNight * smoothstep(0.0, 0.2, r.y);
  float disc = smoothstep(DISC_EDGE, DISC_FULL, dot(r, uSunDir)) * smoothstep(-0.004, 0.004, r.y);
  float moon = smoothstep(DISC_EDGE, DISC_FULL, dot(r, uSkyMoon)) * smoothstep(-0.004, 0.004, r.y);
  float lit = moonLit(r);
  moon *= lit;

  if (dot(uSunDir, uSkyMoon) > 0.9999) disc *= lit;
#if PIPELINE_CLOUDS

  vec4 cloud = uCloudsOn > 0.5 ? texture(uClouds, vUv) : vec4(0.0);
  sky = sky * (1.0 - cloud.a) + cloud.rgb;
  disc *= 1.0 - cloud.a;
  moon *= 1.0 - cloud.a;
#endif
  sky += (uSunColor * uSunStrength * disc + MOON_COLOUR * uMoonDisc * moon) * DISC_BRIGHTNESS;
  fragColor = vec4(sky * SKY_BRIGHTNESS, 1.0);
}
