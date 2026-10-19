#version 330





in vec2 vUv;
uniform vec3 uEye;
uniform vec2 uMapCentre;
out vec4 fragColor;
#include "lib/weather.glsl"
#include "lib/distant_rain.glsl"
const float TAU = 6.2831853;

const float DROPS_END = 48.0;

const int STEPS = 8;

const float HAZE = 9000.0;


const float STREAK_TILE = 32768.0, STREAK_FINE_TILE = 8192.0, STREAK_SPREAD = 0.14, STREAKS = 0.7;
void main() {
  float angle = vUv.x * TAU, row = floor(vUv.y * float(DISTANT_ROWS));
  vec2 dir = vec2(cos(angle), sin(angle));
  float here = weatherAt(uEye).g;
  float from = max(row - 1.0, 0.0) / float(DISTANT_ROWS - 1), to = row / float(DISTANT_ROWS - 1);
  float du = (to - from) / float(STEPS), sum = 0.0;
  for (int i = 0; i < STEPS; i++) {
    float u = from + (float(i) + 0.5) * du, ground = distantDistance(u);
    vec3 p = vec3(uEye.x + dir.x * ground, 0.0, uEye.z + dir.y * ground);
    float rain = max(weatherAt(p).g - here, 0.0);
    if (rain <= 0.0) continue;
    vec2 q = uMapCentre + p.xz + uWeatherOffset;
    float streak = 0.65 * textureLod(uWeatherNoise, q / STREAK_TILE, 0.0).a;
    streak += 0.35 * textureLod(uWeatherNoise, q / STREAK_FINE_TILE, 0.0).a;
    float streaks = clamp(1.0 + STREAKS * (streak - 0.5) / STREAK_SPREAD, 0.0, 2.0);
    float along = 2.0 * (DISTANT_FAR - DISTANT_NEAR) * u * du;
    sum += rain * streaks * smoothstep(DISTANT_NEAR, DROPS_END, ground) * exp(-ground / HAZE) * along;
  }
  fragColor = vec4(sum, 0.0, 0.0, 1.0);
}
