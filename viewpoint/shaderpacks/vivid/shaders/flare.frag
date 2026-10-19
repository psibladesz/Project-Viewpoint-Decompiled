#version 330







in vec2 vUv;
uniform sampler2D uColor;
uniform sampler2D uDepth;
uniform sampler2D uFarDepth;
uniform mat4 uPlainViewProjection;
uniform vec3 uSkySun;
uniform vec3 uSunColor;
uniform float uSkyGlow;
uniform vec2 uFarRange;
out vec4 fragColor;
#include "lib/flare_burst.glsl"
const int GHOSTS = 5;

const vec3 GHOST[GHOSTS] = vec3[GHOSTS](vec3(0.45, 0.025, 0.20), vec3(0.8, 0.05, 0.12), vec3(1.25, 0.09, 0.10),
                                         vec3(1.6, 0.035, 0.16), vec3(2.1, 0.16, 0.06));
const vec3 GHOST_TINT[GHOSTS] = vec3[GHOSTS](vec3(1.0, 0.8, 0.5), vec3(0.6, 1.0, 0.7), vec3(0.5, 0.7, 1.0),
                                              vec3(1.0, 0.6, 0.8), vec3(0.7, 0.8, 1.0));

const float HALO_RADIUS = 0.42, HALO_WIDTH = 0.035, HALO_STRENGTH = 0.08;

const float FULL_SUN = 3.0;

float sunShows(vec2 sun) {
  float sky = 0.0;
  for (int y = -2; y <= 2; y++) {
    for (int x = -2; x <= 2; x++) {
      vec2 uv = sun + vec2(x, y) * 0.004;
      bool open = texture(uDepth, uv).r >= 1.0 && (uFarRange.x <= 0.0 || texture(uFarDepth, uv).r >= 1.0);
      sky += open && uv == clamp(uv, 0.0, 1.0) ? 1.0 : 0.0;
    }
  }
  return sky / 25.0;
}
void main() {
  fragColor = vec4(0.0, 0.0, 0.0, 1.0);
  vec4 clip = uPlainViewProjection * vec4(uSkySun, 0.0);
  if (clip.w <= 0.0 || uSkyGlow <= 0.0) return;
  vec2 sun = clip.xy / clip.w * 0.5 + 0.5;
  float shows = sunShows(sun);
  if (shows <= 0.0) return;
  float lit = min(dot(texture(uColor, clamp(sun, 0.0, 1.0)).rgb, vec3(0.2126, 0.7152, 0.0722)) / FULL_SUN, 1.0);
  vec2 size = vec2(textureSize(uColor, 0)), aspect = vec2(size.x / size.y, 1.0);
  vec2 p = vUv * aspect, s = sun * aspect, centre = 0.5 * aspect;
  vec3 flare = vec3(0.0);
  for (int i = 0; i < GHOSTS; i++) {
    vec2 at = mix(s, centre, GHOST[i].x);
    float d = length(p - at) / GHOST[i].y;
    flare += GHOST_TINT[i] * GHOST[i].z * smoothstep(1.0, 0.6, d);
  }
  vec2 fromCentre = p - centre, sunSide = normalize(s - centre + 1.0e-5);
  float ring = exp(-pow((length(fromCentre) - HALO_RADIUS) / HALO_WIDTH, 2.0));
  float side = max(dot(normalize(fromCentre + 1.0e-5), sunSide), 0.0);
  flare += vec3(0.8, 0.9, 1.0) * ring * side * side * HALO_STRENGTH;
  flare += flareBurst(p - s);
  fragColor = vec4(flare * uSunColor * uSkyGlow * shows * lit, 1.0);
}
