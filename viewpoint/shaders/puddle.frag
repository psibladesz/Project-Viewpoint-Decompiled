#version 330


in vec2 vUv;
in vec3 vLocal;
flat in vec4 vRect;
in vec3 vNormal;
in vec3 vPos;
in float vDistance;
flat in int vFlags;
flat in float vFloorLayer;
uniform sampler2D uTexture;
uniform vec3 uEye, uSunDir, uSunColor, uHorizon, uZenith;
uniform float uSunStrength, uPuddles, uWet;
uniform vec2 uFogRange, uCam;
out vec4 fragColor;
#include "lib/fade.glsl"
#include "lib/sample_sprite.glsl"
#include "lib/sample_floor.glsl"
#include "lib/sample_light.glsl"
#include "lib/wet.glsl"
#include "lib/weather.glsl"




const int RIPPLE_LAYERS = 2;
const float RIPPLE_CELLS = 4.0, RIPPLE_FINER = 1.37;
const vec2 RIPPLE_SHIFT = vec2(0.43, 0.71);


const float RIPPLE_FASTEST = 900.0, RIPPLE_SLOWEST = 700.0, RIPPLE_REACH = 0.3, RIPPLE_LINE = 0.045;
float hash2(vec2 p) { return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453); }
float vnoise(vec2 p) {
  vec2 i = floor(p), f = fract(p); f = f * f * (3.0 - 2.0 * f);
  return mix(mix(hash2(i), hash2(i + vec2(1.0, 0.0)), f.x), mix(hash2(i + vec2(0.0, 1.0)), hash2(i + vec2(1.0, 1.0)), f.x), f.y);
}

uint pcg(uint v) {
  uint state = v * 747796405u + 2891336453u;
  uint word = ((state >> ((state >> 28u) + 4u)) ^ state) * 277803737u;
  return (word >> 22u) ^ word;
}
float unit(uint v) { return float(v >> 8u) * (1.0 / 16777216.0); }

float ripples(vec2 w, float rain) {
  float sum = 0.0;
  for (int layer = 0; layer < RIPPLE_LAYERS; layer++) {
    vec2 g = w * RIPPLE_CELLS * pow(RIPPLE_FINER, float(layer)) + RIPPLE_SHIFT * float(layer);
    vec2 cell = floor(g), c = g - cell;
    uint key = pcg(((uint(int(cell.x)) & 0xFFFFu) | (uint(int(cell.y)) << 16u)) ^ (uint(layer) * 0x9E3779B9u));
    float cycles = floor(mix(RIPPLE_SLOWEST, RIPPLE_FASTEST, unit(key)));
    float t = uRainClock * cycles / RAIN_PERIOD + unit(pcg(key));
    float cycle = mod(floor(t), cycles), life = fract(t);
    uint drop = pcg(key ^ (uint(cycle) * 2654435769u));
    uint a = pcg(drop), b = pcg(a), d = pcg(b);
    if (unit(drop) >= rain) continue;
    vec2 centre = RIPPLE_REACH + (1.0 - 2.0 * RIPPLE_REACH) * vec2(unit(a), unit(b));
    float radius = life * RIPPLE_REACH * mix(0.6, 1.0, unit(d));
    sum += smoothstep(RIPPLE_LINE, 0.0, abs(length(c - centre) - radius)) * (1.0 - life);
  }
  return min(sum, 1.0);
}
void main() {
  fadeDither();
  vec3 n = normalize(vNormal);
  if (n.y < 0.9 || sampleLight(vLocal, vec3(0.0)).a < 0.5) discard;
  if ((vFlags & FLAG_BAKED_FLOOR) != 0) sampleFloor(vUv, vFloorLayer); else sampleSprite(uTexture, vUv, vRect);
  if (!openSky(vLocal)) discard;
  vec2 w = uCam - vec2(vPos.x, vPos.z);
  float field = vnoise(w * 0.55) * 0.65 + vnoise(w * 1.7 + 3.1) * 0.35;
  float edge = 1.0 - 0.8 * uPuddles;
  float puddle = smoothstep(edge, edge + 0.1, field);
  vec3 view = normalize(uEye - vPos);
  vec3 r = reflect(-view, n);
  float fresnel = pow(1.0 - max(dot(view, n), 0.0), 3.0);
  vec3 sky = mix(uHorizon, uZenith, clamp(r.y, 0.0, 1.0));
  float glint = pow(max(dot(r, uSunDir), 0.0), 160.0) * uSunStrength;
  float ripple = uRain > 0.0 && puddle > 0.0 ? ripples(w, clamp(uRain * sheetsAt(vPos), 0.0, 1.0)) : 0.0;
  vec3 water = sky * mix(0.22, 0.75, fresnel) + uSunColor * glint * 3.0 + ripple * 0.2;
  float alpha = mix(WET_DARK * uWet, 0.7, puddle);
  float d = clamp((vDistance - uFogRange.x) / (uFogRange.y - uFogRange.x), 0.0, 1.0);
  fragColor = vec4(mix(vec3(0.0), water, puddle), alpha * (1.0 - d));
}
