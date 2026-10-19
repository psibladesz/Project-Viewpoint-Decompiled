#version 330








in vec2 vUv;
uniform sampler3D uCloudNoise;
uniform vec2 uMapCentre;
uniform float uCloudTime;
out vec4 fragColor;
#include "lib/weather.glsl"


const float TILES[5] = float[5](32768.0, 16384.0, 8192.0, 4096.0, 2048.0);
const float CHANGE[5] = float[5](76800.0, 38400.0, 19200.0, 9600.0, 4800.0);

const float WEIGHTS[5] = float[5](0.10, 0.15, 0.25, 0.28, 0.22);

const float WARP_TILE = 65536.0, WARP_REACH = 3000.0, WARP_CHANGE = 115200.0;

const float SPREAD = 0.16, EDGE = 0.12;

const float RAIN_CLOUD = 0.1;

const float CORE_LEAST = 0.35, CORE_DEPTH = 0.4;


float noiseAt(vec2 n, float tile, float depth) { return texture(uCloudNoise, vec3(n.x / tile, depth, n.y / tile)).r; }

float field(vec2 n) {
  float wt = uCloudTime / WARP_CHANGE;
  n += WARP_REACH * vec2(noiseAt(n, WARP_TILE, wt) - 0.5, noiseAt(n + vec2(21700.0, 9300.0), WARP_TILE, wt + 0.5) - 0.5);
  float sum = 0.0, norm = 0.0;
  for (int i = 0; i < 5; i++) {
    sum += WEIGHTS[i] * (noiseAt(n, TILES[i], uCloudTime / CHANGE[i] + float(i) * 0.27) - 0.5);
    norm += WEIGHTS[i] * WEIGHTS[i];
  }
  return 1.0 / (1.0 + exp(-sum / sqrt(norm) / SPREAD));
}
void main() {
  vec2 place = (vUv - 0.5) * uWeatherSpan;

  vec2 sky = skyAlong(vec3(place.x - uWeatherOffset.x, 0.0, place.y - uWeatherOffset.y));
  float coverHere = sky.x, type = sky.y;

  float middle = (1.0 - coverHere) * (1.0 + EDGE) - 0.5 * EDGE;
  float u = field(uMapCentre + place);
  float rainHere = rainAlong(vec3(place.x - uWeatherOffset.x, 0.0, place.y - uWeatherOffset.y));
  float cover = max(smoothstep(middle - 0.5 * EDGE, middle + 0.5 * EDGE, u), clamp(rainHere / RAIN_CLOUD, 0.0, 1.0));
  float wet = smoothstep(0.45, 0.8, cover);

  float cores = max(middle, 0.5);
  float rain = rainHere * wet * mix(CORE_LEAST, 1.0, smoothstep(cores, cores + CORE_DEPTH, u));

  float kind = min(type, mix(0.5, 1.0, cover));
  fragColor = vec4(cover, rain, kind, clamp(rainHere / 0.1, 0.0, 1.0) * wet);
}
