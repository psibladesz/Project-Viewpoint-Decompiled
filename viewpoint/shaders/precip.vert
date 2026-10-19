#version 330


















#include "lib/spots.glsl"
#include "lib/weather.glsl"
#include "lib/precip_look.glsl"
#include "lib/far_relief.glsl"
uniform mat4 uViewProjection;
uniform vec3 uEye;
uniform vec3 uOrigin;
uniform float uFall, uExposure, uMode, uFar, uCount, uMean, uLampGain;
uniform float uPixel;
uniform vec3 uSky;
uniform float uViewExposure;
uniform vec2 uFogRange;
uniform vec2 uHaze;
#include "lib/distance.glsl"
uniform int uLightCount;
uniform vec4 uLightPos[48];
uniform vec3 uLightColor[48];
uniform mat4 uRainMatrix, uRainUnproject;
uniform sampler2D uRainMap;
out vec2 vLocal;
out float vFade;
out vec3 vColor;
out vec3 vGlint;
out float vSize;
flat out vec2 vAcross;
flat out float vSeed;



const vec3 BOX_NEAR = vec3(32.0, 24.0, 32.0), BELOW_NEAR = vec3(16.0, 9.6, 16.0);
const vec3 BOX_FAR = vec3(96.0, 32.0, 96.0), BELOW_FAR = vec3(48.0, 9.6, 48.0);
const float NEAR_IN = 2.5, NEAR_FADE = 9.6, NEAR_END = 16.0, FAR_FADE = 30.0, FAR_END = 48.0;


const float FAR_STRENGTH = 2.0;
const float RS = 24.0, TAU = 6.2831853;




const float SMALLEST = 0.5, LARGEST = 5.0, SLOPE = 1.4;



uint pcg(uint v) {
  uint state = v * 747796405u + 2891336453u;
  uint word = ((state >> ((state >> 28u) + 4u)) ^ state) * 277803737u;
  return (word >> 22u) ^ word;
}
vec3 hash3(uint n) {
  uint a = pcg(n), b = pcg(a), c = pcg(b);
  return vec3(uvec3(a, b, c) >> 8u) * (1.0 / 16777216.0);
}
void hide() {
  gl_Position = vec4(2.0, 2.0, 2.0, 1.0);
  vLocal = vec2(0.0);
  vFade = 0.0;
  vColor = vGlint = vec3(0.0);
  vSize = vSeed = 0.0;
  vAcross = vec2(0.0);
}

float fadeOut(float a, float b, float x) { return 1.0 - smoothstep(a, b, x); }

vec3 onMap(vec3 p) { return (uRainMatrix * vec4(p, 1.0)).xyz; }
bool inMap(vec3 m) { return all(greaterThanEqual(m.xy, vec2(0.0))) && all(lessThanEqual(m.xy, vec2(1.0))); }


float diameter(vec3 u) {
  return clamp(-log(max((1.0 - u.x) * (1.0 - u.y) * (1.0 - u.z), 1.0e-30)) / SLOPE, SMALLEST, LARGEST);
}

float terminal(float d) { return 9.65 - 10.3 * exp(-0.6 * d); }


vec3 light(vec3 p, out vec3 glint) {
  vec3 lit = vec3(0.0), toEye = normalize(uEye - p);
  glint = vec3(0.0);
  for (int i = 0; i < uLightCount; i++) {
    vec3 d = p - uLightPos[i].xyz;
    float dist = length(d), radius = uLightPos[i].w;
    if (dist >= radius) continue;
    float att = 1.0 - dist / radius;
    lit += uLightColor[i] * att * att;
    glint += uLightColor[i] * att * att * precipScatter(dot(d / max(dist, 1.0e-4), toEye));
  }
  for (int i = 0; i < uSpotCount; i++) {
    Spot s = spotAt(i);
    vec3 d = p - s.pos;
    float dist = length(d);
    if (dist < s.reach) {
      vec3 way = d / max(dist, 1.0e-4);
      float beam = spotBeam(s, way);
      if (beam > 0.0) {
        vec3 spot = s.colour * beam * spotFalloff(s, dist) * spotShadow(s, p, vec3(0.0), dist, false);
        lit += spot;
        glint += spot * precipScatter(dot(way, toEye));
      }
    }
  }
  glint *= uLampGain * uViewExposure;
  return (uSky + lit * uLampGain) * uViewExposure;
}

float seenThrough(vec3 pos, float depth) {
  return 1.0 - distanceShare(pos, distance(pos, uEye), depth, uFogRange, uHaze);
}


void splash(uint key, float fi, vec3 h, vec2 c) {
  vec3 eyeWorld = uEye + uOrigin;
  float rate = mix(2.0, 3.0, h.z);
  float life = fract(h.y + uRainClock * rate), cycle = floor(h.y + uRainClock * rate);
  vec2 jitter = hash3((key * 2u + 1u) ^ (uint(cycle) * 2654435769u)).xz * 6.0 - 3.0;
  vec2 at = uEye.xz + mod(h.xz * RS + jitter - eyeWorld.xz + RS * 0.5, RS) - RS * 0.5;
  if (fi >= uMean * sheetsAt(vec3(at.x, 0.0, at.y))) { hide(); return; }
  vec3 m = onMap(vec3(at.x, -uOrigin.y, at.y));
  if (!inMap(m)) { hide(); return; }
  float depth = texture(uRainMap, m.xy).r;
  if (depth >= 0.99999) { hide(); return; }
  vec4 s = uRainUnproject * vec4(m.xy, depth, 1.0);
  vec3 pos = s.xyz / s.w + vec3(0.0, 0.01, 0.0), p;
  if (uMode > 2.5) {
    vec3 toEye = normalize(uEye - pos), side = normalize(cross(vec3(0.0, 1.0, 0.0), toEye) + vec3(1.0e-4, 0.0, 0.0));
    p = pos + side * (c.x * CROWN) + vec3(0.0, (c.y * 0.5 + 0.5) * CROWN_HEIGHT, 0.0);
  } else {
    float size = SPLASH * (0.25 + life);
    p = pos + vec3(c.x * size, 0.0, c.y * size);
  }
  gl_Position = uViewProjection * vec4(p, 1.0);
  vLocal = c;
  vFade = fadeOut(RS * 0.3, RS * 0.5, length(pos - uEye)) * seenThrough(pos, gl_Position.w);
  vColor = light(pos, vGlint);
  vSize = life;
  vAcross = vec2(0.0);
  vSeed = cycle * 7.0 + h.x * 997.0;
}
void main() {
  int id = gl_VertexID / 6, corner = gl_VertexID - id * 6;
  float fi = float(id);
  if (fi >= uCount) { hide(); return; }
  bool far = uFar > 0.5;
  uint key = uint(id) + (far ? 0x10000000u : 0u);
  vec3 h = hash3(key * 2u), h2 = hash3(key * 2u + 1u);
  vec2 c = vec2((corner == 1 || corner == 2 || corner == 4) ? 1.0 : -1.0, (corner == 2 || corner == 4 || corner == 5) ? 1.0 : -1.0);
  if (uMode > 1.5) { splash(key, fi, h, c); return; }
  bool snow = uMode > 0.5;
  vec3 box = far ? BOX_FAR : BOX_NEAR, below = far ? BELOW_FAR : BELOW_NEAR;
  float d = diameter(vec3(h2.y, hash3(pcg(key) ^ 0x9E3779B9u).xy));

  float speed = snow ? uFall * mix(0.6, 1.4, h.z) : terminal(d);
  speed = max(floor(speed * RAIN_PERIOD / box.y + 0.5), 1.0) * box.y / RAIN_PERIOD;
  float turn = uRainClock * TAU / RAIN_PERIOD;
  vec2 flutter = snow ? vec2(sin(turn * 162.0 + fi), cos(turn * 124.0 + fi)) * 0.4 : vec2(0.0);

  vec3 air = h * box + vec3(flutter.x, -speed * uRainClock, flutter.y);
  vec3 eyeAir = vec3(uEye.x + uWindOffset.x, uEye.y + uOrigin.y, uEye.z + uWindOffset.y);
  vec3 rel = mod(air - eyeAir + below, box) - below;
  float dist = length(rel);
  float handOver = smoothstep(NEAR_FADE, NEAR_END, dist);
  float fade = far ? handOver * fadeOut(FAR_FADE, FAR_END, dist) : 1.0 - handOver;
  fade *= smoothstep(-below.y, 3.0 - below.y, rel.y) * fadeOut(box.y - below.y - 3.0, box.y - below.y, rel.y);
  if (fade <= 0.0 || fi >= uMean * sheetsAt(uEye + rel)) { hide(); return; }



  float gust = gustAt(uEye + rel);
  float age = (box.y - below.y - rel.y) / speed;
  rel.xz += uWind * gust * GUST_SECONDS * (1.0 - exp(-age / GUST_SECONDS));
  dist = length(rel);
  fade *= far ? 1.0 : smoothstep(1.0, NEAR_IN, dist);
  if (fade <= 0.0) { hide(); return; }
  vec3 pos = reliefLifted(uEye + rel);
  vec3 m = onMap(pos);
  if (inMap(m) && m.z > texture(uRainMap, m.xy).r + 1.0e-4) { hide(); return; }
  vec3 toEye = -rel / max(dist, 0.001);
  vec3 velocity = precipStreak(vec3(uWind.x * (1.0 + gust), -speed, uWind.y * (1.0 + gust)));
  vec3 p, side;
  if (snow) {
    vec3 dir = normalize(velocity);
    side = cross(dir, toEye);
    side = length(side) > 1.0e-3 ? normalize(side) : vec3(1.0, 0.0, 0.0);
    float size = max(FLAKE * (0.5 + h2.x), dist * uPixel * FLAKE_PIXELS);
    p = pos + dir * (c.y * size) + side * (c.x * size);
  } else {

    vec3 path = velocity * uExposure;
    vec3 dir = normalize(path);
    side = cross(dir, toEye);
    side = length(side) > 1.0e-3 ? normalize(side) : vec3(1.0, 0.0, 0.0);
    float width = max(precipWidth(d), dist * uPixel * MIN_PIXELS);
    side *= width;
    p = pos - path * (0.5 - 0.5 * c.y) + side * c.x;
  }
  gl_Position = uViewProjection * vec4(p, 1.0);
  vec4 across = uViewProjection * vec4(pos + side, 1.0), middle = uViewProjection * vec4(pos, 1.0);
  vAcross = (across.xy / across.w - middle.xy / middle.w) * 0.5 * uViewport;
  vLocal = c;
  vFade = fade * (far ? FAR_STRENGTH : 1.0) * seenThrough(pos, middle.w);
  vSize = snow ? 0.0 : d / LARGEST;
  vSeed = 0.0;
  vColor = light(pos, vGlint) * precipBrightness(snow, h2.z);
}
