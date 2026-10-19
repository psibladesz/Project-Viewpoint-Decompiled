




#ifndef VIEWPOINT_PRECIP_LOOK
#define VIEWPOINT_PRECIP_LOOK
#include "lib/common.glsl"
uniform sampler2D uScene;
uniform vec2 uViewport;




const float DROP_WIDTH = 0.0015, MIN_PIXELS = 0.7;
const float FLAKE = 0.026, FLAKE_PIXELS = 1.2;
const float SPLASH = 0.065, CROWN = 0.06, CROWN_HEIGHT = 0.08;

float precipWidth(float d) { return d * DROP_WIDTH; }

float precipBrightness(bool snow, float random) { return snow ? 1.0 : 0.85 + 0.3 * random; }


vec3 precipStreak(vec3 fall) { return fall; }



const float FORWARD = 1.6, BACK = 0.9;
float precipScatter(float c) { return FORWARD * pow(max(c, 0.0), 12.0) + BACK * smoothstep(-0.55, -0.85, c); }


const float OPACITY_LEAST = 0.25, OPACITY_MOST = 0.6;




const float BEND = 1.6, LENS = 1.2, WIDE = 0.35, RIM = 0.9, CORE = 0.35, GLINT = 1.0;


const float RING = 0.6, CROWN_COVER = 0.9, CROWN_FLIGHT = 0.4, DROPLET = 0.008;
const int DROPLETS = 6;

float precipHash(uint n) {
  n = n * 747796405u + 2891336453u;
  n = ((n >> ((n >> 28u) + 4u)) ^ n) * 277803737u;
  return float((n >> 22u) ^ n) / 4294967295.0;
}


vec4 streak(vec2 local, float fade, vec3 light, vec3 glint, float size, vec2 across, vec2 pixel) {
  float body = sqrt(max(1.0 - local.x * local.x, 0.0));
  float ends = smoothstep(-1.0, -0.5, local.y) * smoothstep(1.0, 0.75, local.y);
  float a = min(mix(OPACITY_LEAST, OPACITY_MOST, size) * body * ends * fade, 1.0);
  vec2 at = pixel - across * (local.x * BEND);
  vec3 behind = textureLod(uScene, at / uViewport, 0.0).rgb;
  float fresnel = 0.02 + 0.98 * pow(1.0 - body, 5.0);
  vec3 color = mix(behind * LENS + shoulder(light * WIDE), shoulder(light) * RIM, fresnel)
      + shoulder(light) * (CORE * pow(body, 8.0)) + shoulder(glint * GLINT) * body;
  return vec4(color * a, a);
}



vec4 crown(vec2 local, float fade, vec3 light, vec3 glint, float life, float seed) {
  float t = life / CROWN_FLIGHT;
  if (t >= 1.0) return vec4(0.0);
  vec2 here = vec2(local.x * CROWN, (local.y * 0.5 + 0.5) * CROWN_HEIGHT);
  float a = 0.0;
  for (int k = 0; k < DROPLETS; k++) {
    uint n = uint(seed) * 16u + uint(k);
    float outward = precipHash(n) * 2.0 - 1.0, up = 0.55 + 0.45 * precipHash(n + 8u);
    vec2 at = vec2(outward * 0.9 * t * CROWN, 4.0 * up * t * (1.0 - t) * 0.9 * CROWN_HEIGHT);
    a = max(a, smoothstep(DROPLET, DROPLET * 0.3, length(here - at)));
  }
  a *= CROWN_COVER * (1.0 - t) * fade;
  return vec4((shoulder(light) * 1.15 + shoulder(glint)) * a, a);
}





vec4 precipLook(float mode, vec2 local, float fade, vec3 light, vec3 glint, float size, vec2 across, float seed,
                vec2 pixel) {
  if (mode > 2.5) return crown(local, fade, light, glint, size, seed);
  if (mode < 0.5) return streak(local, fade, light, glint, size, across, pixel);
  float a;
  vec3 color = light;
  if (mode > 1.5) {
    float r = length(local), radius = 0.3 + 0.7 * size;
    a = smoothstep(0.18, 0.0, abs(r - radius)) * RING * (1.0 - size) * fade;
    color = color * 1.2 + glint;
  } else {
    a = smoothstep(1.0, 0.3, length(local)) * 0.85 * fade;
    color *= 1.15;
  }
  a = min(a, 1.0);
  return vec4(shoulder(color) * a, a);
}
#endif
