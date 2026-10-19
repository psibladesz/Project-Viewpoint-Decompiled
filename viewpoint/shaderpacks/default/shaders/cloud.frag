#version 330






















in vec2 vUv;
uniform vec3 uForward;
uniform vec3 uRight;
uniform vec3 uUp;
uniform vec3 uHorizon;
uniform vec3 uZenith;
uniform vec3 uSunDir;
uniform vec3 uSunColor;
uniform float uSunStrength;
uniform float uMoonLight;
uniform vec3 uSkyMoon;
uniform float uNight;
uniform float uPixelAngle;
uniform int uFrame;
uniform vec3 uEye;
uniform vec2 uCloudPick;
uniform vec2 uCloudSize;
out vec4 fragColor;
#include "lib/common.glsl"
#include "lib/sky.glsl"
#include "lib/clouds.glsl"
#include "lib/cloud_blocks_draw.glsl"


const float REACH = 80000.0, SHEET_REACH = 150000.0, MAX_SPAN = 40000.0;

const float LOW_HAZE = 22000.0, HIGH_HAZE = 60000.0;


const float STEP = 15.0, STEP_GROWTH = 0.015, DEFAULT_SAMPLES = 30.0;
const int MAX_STEPS = 384;


const float DETAIL_PIXELS = 3.0;


const float EXTINCTION = 0.06, ALBEDO = 0.98;






const int LIGHT_SAMPLES = 3, LIGHT_SPAN = 2;
const float LIGHT_STEP = 30.0, FINE_UNTIL = 0.3;
const vec3 CONE_KERNEL[6] = vec3[6](vec3(0.38, -0.52, 0.21), vec3(-0.61, 0.17, -0.44), vec3(0.14, 0.62, -0.57),
                                    vec3(0.55, 0.31, 0.49), vec3(-0.29, -0.47, 0.66), vec3(-0.48, 0.58, 0.27));



const float LIGHT_ABSORPTION = 3.0, RAIN_ABSORPTION = 2.0;



const float ECCENTRICITY = 0.2, SILVER_INTENSITY = 0.6, SILVER_SPREAD = 0.1;




const int OCTAVES = 3;
const float OCTAVE_LIGHT = 0.5, OCTAVE_THROUGH = 0.5, OCTAVE_LEAN = 0.5;


const float DENSITY_MOST = 0.3;



const float SUN_LIGHT = 1.8;
const float SKY_BASE = 0.3, SKY_TOP = 0.6, SKY_TINT = 0.7;


const int SHEET_LIGHT_SAMPLES = 4;
const float SHEET_LIGHT_COARSE = 4.0;

const vec3 FLASH_LIGHT = vec3(5.0, 5.4, 6.0);
const float FLASH_REACH = 1400.0;



float lightDensity(vec3 p, float a, vec4 w, float footprint, bool fine) {
  float coarse = 2.0 * max(footprint, LIGHT_STEP * 0.25), sum = 0.0, stride = LIGHT_STEP * float(LIGHT_SPAN);
  vec3 q = p;
  float qa = a;
  for (int i = 0; i < LIGHT_SAMPLES; i++) {
    vec3 move = uSunDir * stride + CONE_KERNEL[i * LIGHT_SPAN] * (stride * float(i));
    q += move;
    qa += move.y;
    sum += float(LIGHT_SPAN) * lowDensity(q, qa, w, coarse, fine ? coarse : 0.0);
  }
  float far = 3.0 * stride * float(LIGHT_SAMPLES);
  vec3 f = p + uSunDir * far;
  float fa = a + uSunDir.y * far;
  return sum + lowDensity(f, fa, lowWeather(f, fa), max(coarse, far * 0.25), 0.0);
}













vec3 scattered(float d, float ds, float h, float rain, float mu, vec3 sun, vec3 sky) {
  float absorbed = d * (1.0 + RAIN_ABSORPTION * rain), powder = 1.0 - exp(-2.0 * d);
  float thick = clamp01(ds / DENSITY_MOST);
  float verticalChance = pow(mix(0.1, 1.0, remap01(h, 0.07, 0.14)), 0.8);
  float lit = 0.0, share = 1.0, through = 1.0, lean = 1.0, shares = 0.0;
  for (int i = 0; i < OCTAVES; i++) {
    float phase = max(hg(mu, ECCENTRICITY * lean), SILVER_INTENSITY * hg(mu, (0.99 - SILVER_SPREAD) * lean));
    lit += share * exp(-absorbed * through) * phase * 12.566371;
    shares += share;
    share *= OCTAVE_LIGHT;
    through *= OCTAVE_THROUGH;
    lean *= OCTAVE_LEAN;
  }
  float energy = lit / shares * mix(1.0, 2.0 * powder, clamp01(0.5 - 0.5 * mu)) * verticalChance;

  return sun * energy + sky * (mix(SKY_BASE, SKY_TOP, h) * sqrt(1.0 - thick));
}



vec3 sunAt(vec3 p, float a) {
  if (dot(uSunDir, uSunTrue) < 0.9999) {
    vec2 moonPlace = airPlace(p, uEye, a, uSkyMoon);
    return AIR_SUN * airSunThrough(moonPlace.x, moonPlace.y);
  }
  vec2 place = airPlace(p, uEye, a, uSunTrue);
  return airSunlight(place.x, place.y);
}


vec3 lightAt(vec3 p, float a, vec4 w, float mu, float footprint, bool fine, vec3 sun, vec3 sky) {
  float d = lightDensity(p, a, w, footprint, fine) * LIGHT_ABSORPTION;
  float ds = lowDensity(p, a, w, 2.0 * footprint, 0.0);
  vec3 light = scattered(d, ds, heightIn(a, w), w.g, mu, sun * sunAt(p, a), sky);
  vec3 fromFlash = p - uLightning.xyz;
  return light + FLASH_LIGHT * uLightning.w * exp(-dot(fromFlash, fromFlash) / (FLASH_REACH * FLASH_REACH));
}



vec2 lowStretch(vec3 r, float alt, float ground) {
  float ceiling = lowCeiling();
  vec2 top = crossings(alt, r.y, ceiling);
  float t0 = alt < LOW_FLOOR ? crossings(alt, r.y, LOW_FLOOR).y : (alt < ceiling ? 0.0 : top.x);
  float t1 = top.y;
  if (ground > 0.0) t1 = min(t1, ground);
  return vec2(t0, min(t1, t0 + MAX_SPAN));
}


float stepAt(float t) { return (STEP + t * STEP_GROWTH) * DEFAULT_SAMPLES / float(PIPELINE_CLOUD_STEPS); }





vec4 lowClouds(vec3 r, float alt, float ground, float jitter, vec3 sun, vec3 sky, vec3 haze, out float entry) {
  entry = 1.0e30;
  vec2 stretch = lowStretch(r, alt, ground);
  float t0 = stretch.x, t1 = stretch.y;
  if (t0 < 0.0 || t1 <= t0 || t0 > REACH) return vec4(0.0);
  float mu = dot(r, uSunDir);
  vec3 color = vec3(0.0);
  float transmittance = 1.0, t = t0;
  for (int i = 0; i < MAX_STEPS && t < t1; i++) {
    float dt = stepAt(t);
    float s = t + jitter * dt, footprint = s * uPixelAngle, detail = footprint * DETAIL_PIXELS;
    vec3 p = uEye + r * s;
    float a = heightAlong(alt, r.y, s);
    vec4 w = lowWeather(p, a);
    float d = lowDensity(p, a, w, footprint, detail);
    if (d > 0.0) {
      entry = min(entry, s);
      vec3 light = lightAt(p, a, w, mu, detail, transmittance > 1.0 - FINE_UNTIL, sun, sky) * ALBEDO;
      float through = exp(-d * EXTINCTION * dt);
      color += mix(haze, light, exp(-s / LOW_HAZE)) * (1.0 - through) * transmittance;
      transmittance *= through;
      if (transmittance < 0.01) break;
    }
    t += dt;
  }
  return vec4(color, 1.0 - transmittance);
}









vec4 sheet(vec3 r, float alt, float ground, bool round, vec3 sun, vec3 sky, vec3 haze, out float at) {
  at = ahead(alt, r.y, round ? ROUND_HEIGHT : STREAK_HEIGHT);
  if (at <= 0.0 || at > SHEET_REACH || (ground > 0.0 && ground < at)) return vec4(0.0);
  vec3 p = uEye + r * at;
  vec2 weather = sheetWeather(p, round);
  if (weather.x < 0.005) return vec4(0.0);
  vec4 frame = round ? uCloudSheetMid : uCloudSheetHigh;
  float slant = max(abs(r.y + at / EARTH_RADIUS), 0.04);
  vec2 alongView, acrossView;
  footprintOn(frame, r, at * uPixelAngle, slant, alongView, acrossView);
  float density = sheetDensity(sheetPlace(p, frame), round, weather, alongView, acrossView);
  if (density <= 0.0) return vec4(0.0);
  float depth = round ? ROUND_DEPTH : STREAK_DEPTH, thickness = round ? ROUND_THICKNESS : STREAK_THICKNESS;
  float path = 0.5 * thickness / max(uSunDir.y, 0.05), sum = 0.0;
  for (int i = 0; i < SHEET_LIGHT_SAMPLES; i++) {
    vec3 q = p + uSunDir * (path * (float(i) + 0.5) / float(SHEET_LIGHT_SAMPLES));
    sum += sheetDensity(sheetPlace(q, frame), round, weather, alongView * SHEET_LIGHT_COARSE,
                        acrossView * SHEET_LIGHT_COARSE);
  }
  float d = LIGHT_ABSORPTION * depth / (EXTINCTION * thickness) * sum / float(SHEET_LIGHT_SAMPLES) * path / LIGHT_STEP;
  float mu = dot(r, uSunDir), stopped = 1.0 - exp(-depth * density / slant);

  vec3 lit = sun * sunAt(p, round ? ROUND_HEIGHT : STREAK_HEIGHT);
  vec3 light = scattered(d, 0.5 * density * DENSITY_MOST, 0.5, 0.0, mu, lit, sky);
  return vec4(mix(haze, light * ALBEDO, exp(-at / HIGH_HAZE)) * stopped, stopped);
}


vec4 clouds(vec3 r, vec2 pixel) {
  float alt = max(uSceneOrigin.y + uEye.y, 1.0);
  float ground = ahead(alt, r.y, 0.0);
  float jitter = fract(ign(pixel) + float(uFrame / 16) * 0.618034);

  bool moonLit = dot(uSunDir, uSunTrue) < 0.9999;
  vec3 sun = vec3(SUN_LIGHT * (moonLit ? uMoonLight : 1.0 - 0.85 * uOvercast));

  vec3 sky = 0.5 * skyPlain(vec3(0.0, 1.0, 0.0));
  for (int i = 0; i < 4; i++) {
    float a = 1.5707963 * float(i);
    sky += 0.125 * skyPlain(vec3(0.98 * cos(a), 0.2, 0.98 * sin(a)));
  }
  sky = mix(vec3(dot(sky, vec3(0.2126, 0.7152, 0.0722))), sky, SKY_TINT);
  vec3 haze = skyColor(r, uHorizon, uZenith, uSunDir, uSunColor, uSunStrength);
  float at[3];
  vec4 layer[3];
  layer[0] = lowClouds(r, alt, ground, jitter, sun, sky, haze, at[0]);
  layer[1] = sheet(r, alt, ground, true, sun, sky, haze, at[1]);
  layer[2] = sheet(r, alt, ground, false, sun, sky, haze, at[2]);
  for (int i = 0; i < 2; i++) {
    for (int j = 0; j < 2 - i; j++) {
      if (at[j] > at[j + 1]) {
        float t = at[j];
        at[j] = at[j + 1];
        at[j + 1] = t;
        vec4 l = layer[j];
        layer[j] = layer[j + 1];
        layer[j + 1] = l;
      }
    }
  }
  vec4 sum = vec4(0.0);
  for (int i = 0; i < 3; i++) sum += layer[i] * (1.0 - sum.a);
  return sum;
}

void main() {

  vec2 pixel = floor(gl_FragCoord.xy) * 4.0 + uCloudPick;
  vec2 ndc = (pixel + 0.5) / uCloudSize * 2.0 - 1.0;
  vec3 r = normalize(uForward + uRight * ndc.x + uUp * ndc.y);
#if OPTION_CLOUD_STYLE == OPTION_CLOUD_STYLE_LAYERED
  fragColor = blockClouds(r);
#else
  fragColor = clouds(r, pixel);
#endif
}
