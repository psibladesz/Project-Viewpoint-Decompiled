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
uniform float uCloudCover;
uniform float uNight;
uniform float uTime;
uniform vec2 uCloudDrift;
uniform vec3 uSkyMoon;
uniform float uMoonDisc;
out vec4 fragColor;
const vec3 MOON_COLOUR = vec3(0.62, 0.74, 1.0);
#include "lib/noise.glsl"
void main() {
  vec3 r = normalize(vRay);
  float h = max(r.y, 0.0);
  vec3 sky = mix(uHorizon, uZenith, 1.0 - exp(-h * 3.0));
  float mu = max(dot(r, uSunDir), 0.0);
  sky += uSunColor * uSunStrength * (0.30 * pow(mu, 6.0) * (1.0 - h) + 0.45 * pow(mu, 80.0));
  float disc = smoothstep(0.99972, 0.99990, mu) * smoothstep(-0.004, 0.004, r.y);
  float moon = smoothstep(0.99972, 0.99990, dot(r, uSkyMoon)) * smoothstep(-0.004, 0.004, r.y);
  if (uNight > 0.0 && r.y > 0.0) {
    vec2 g = r.xz / (r.y + 0.35) * 90.0;
    vec2 cell = floor(g);
    float star = hash(cell);
    float twinkle = 0.7 + 0.3 * sin(uTime * (1.0 + star * 3.0) + star * 40.0);
    float d = length(fract(g) - 0.5 - (vec2(hash(cell + 3.1), hash(cell + 7.7)) - 0.5) * 0.6);
    sky += vec3(0.9, 0.95, 1.0) * step(0.93, star) * smoothstep(0.09, 0.0, d) * twinkle * uNight * smoothstep(0.0, 0.25, r.y) * 1.4;
  }

  vec4 cloud = uCloudsOn > 0.5 ? texture(uClouds, vUv) : vec4(0.0);
  sky = sky * (1.0 - cloud.a) + cloud.rgb;
  disc *= 1.0 - cloud.a;
  moon *= 1.0 - cloud.a;
  sky += uSunColor * uSunStrength * disc * 14.0 + MOON_COLOUR * uMoonDisc * moon * 14.0;
  fragColor = vec4(sky, 1.0);
}
