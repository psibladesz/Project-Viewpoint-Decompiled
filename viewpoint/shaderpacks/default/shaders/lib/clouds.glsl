











#ifndef DEFAULT_CLOUDS
#define DEFAULT_CLOUDS

#include "lib/cloud_shape.glsl"
#include "lib/cloud_weather.glsl"
#ifndef VIEWPOINT_SCENE_ORIGIN
#define VIEWPOINT_SCENE_ORIGIN
uniform vec3 uSceneOrigin;
#endif
uniform sampler3D uCloudDetail;
uniform sampler2D uCloudCurl;
uniform sampler2D uCloudSheets;


uniform vec4 uCloudSheetMid;
uniform vec4 uCloudSheetHigh;
const float EARTH_RADIUS = 6371000.0;









const float LOW_BASE = 1200.0;
const vec2 KIND_SPAN[5] = vec2[5](vec2(350.0, 700.0), vec2(LOW_BASE, 1800.0), vec2(LOW_BASE, 3700.0),
                                  vec2(LOW_BASE, 5500.0), vec2(LOW_BASE, 11000.0));





const vec2 KIND_SHAPE[5] = vec2[5](vec2(0.15, 0.6), vec2(0.1, 0.5), vec2(0.15, 0.78), vec2(0.08, 0.78),
                                   vec2(0.04, 0.85));



const float BASE_WANDER = 150.0, BASE_WANDER_SHARE = 0.1;

const float DECK_BASE = 700.0;

const float LOW_FLOOR = 315.0;


const float SHAPE_TILE = 12000.0, DETAIL_TILE = 900.0, CURL_TILE = 6000.0;
const float SHAPE_TEXELS = 128.0, DETAIL_TEXELS = 32.0;


const float DETAIL_EROSION = 0.2, CURL_REACH = 150.0;



const float LEAN = 0.2, LEAN_FROM = 1200.0;

const float ANVIL = 0.5;
const float SHAPE_RISE = 0.3, DETAIL_RISE = 1.5;



const float MAP_FADE_FROM = 10000.0, MAP_FADE_TO = 14000.0;









const float ROUND_HEIGHT = 5000.0, ROUND_THICKNESS = 400.0, ROUND_DEPTH = 2.0, ROUND_IMAGE = 16384.0;
const float STREAK_HEIGHT = 9000.0, STREAK_THICKNESS = 600.0, STREAK_DEPTH = 0.1, STREAK_IMAGE = 32768.0;
const float ROUND_PATCH_TILE = 32768.0, ROUND_CELL_TILE = 4096.0;
const float STREAK_TILE = 32768.0, STREAK_FINE_TILE = 2048.0, STREAK_STRETCH = 8.0;


const float UNTILE = 4.0, REGION = 8.0, REGION_SPREAD = 0.6;








const float STRAND_CUT = 0.5, STRAND_SHIFT = 400.0;
const vec2 STRAND_CELLS = vec2(0.55, 0.85);
const float STRAND_ALONG = 51200.0, STRAND_ACROSS = 6400.0, STRAND_CHANGE = 20000.0;



void kindOf(float t, float deck, out vec2 span, out vec2 shape) {
  float s = clamp01(t) * 4.0;
  int i = min(int(s), 3);
  float f = s - float(i);
  span = mix(KIND_SPAN[i], KIND_SPAN[i + 1], f);
  shape = mix(KIND_SHAPE[i], KIND_SHAPE[i + 1], f);
  span.x = mix(span.x, min(span.x, DECK_BASE), deck);
}


float lowCeiling() {
  vec2 span, shape;
  kindOf(uCloudType + TYPE_SPREAD, 0.0, span, shape);
  return span.y;
}

float kindGradient(float h, vec2 shape) { return smoothstep(0.0, shape.x, h) * (1.0 - smoothstep(shape.y, 1.0, h)); }


float baseWander(vec2 span) { return min(BASE_WANDER, BASE_WANDER_SHARE * (span.y - span.x)); }
float lowestBase(vec2 span) { return span.x - baseWander(span); }


vec2 aloft(vec3 p) { return p.xz + uSceneOrigin.xz - uCloudCarried; }


vec2 leanAt(float altitude) {
  vec2 down = length(uCloudWind) > 1.0e-3 ? normalize(uCloudWind) : vec2(0.0);
  return down * (LEAN * max(altitude - LEAN_FROM, 0.0));
}


vec4 lowWeather(vec3 p, float altitude) {
  vec2 lean = leanAt(altitude);
  vec3 q = vec3(p.x - lean.x, p.y, p.z - lean.y);
  float beyond = smoothstep(MAP_FADE_FROM, MAP_FADE_TO, length(q.xz));
  vec4 w = beyond < 1.0 ? weatherAt(q) : vec4(0.0);
  if (beyond > 0.0) {
    vec2 sky = skyAlong(q);
    float rainHere = rainAlong(q);
    float share = sky.x * lowFraction(sky.x, upperAmount(sky, q), rainHere);
    w = mix(w, weatherOf(share, sky.x, sky.y, rainHere, 0.5), beyond);
  }
  return w;
}

float mipFor(float footprint, float texel) { return max(log2(footprint / texel), 0.0); }


float shapeBase(vec4 n) { return remap01(n.r, dot(n.gba, vec3(0.625, 0.25, 0.125)) - 1.0, 1.0); }







float lowDensity(vec3 p, float altitude, vec4 w, float footprint, float detail) {
  float coverage = w.r;
  if (coverage < 0.01) return 0.0;
  vec2 span, shape;
  kindOf(w.b, w.a, span, shape);
  if (altitude <= lowestBase(span) || altitude >= span.y) return 0.0;
  vec2 place = aloft(p) - leanAt(altitude);
  vec3 q = vec3(place.x, altitude - uCloudTime * SHAPE_RISE, place.y);
  vec4 n = textureLod(uCloudShape, q / SHAPE_TILE, mipFor(footprint, SHAPE_TILE / SHAPE_TEXELS));

  float bottom = span.x + baseWander(span) * (n.g * 2.0 - 1.0);
  float h = (altitude - bottom) / (span.y - bottom);
  if (h <= 0.0 || h >= 1.0) return 0.0;

  coverage = pow(coverage, mix(1.0, mix(1.0, ANVIL, smoothstep(0.75, 1.0, w.b)), remap01(h, 0.7, 0.8)));
  float base = shapeBase(n) * kindGradient(h, shape);


  float d = remap01(base, 1.0 - coverage, 1.0) * coverage;
  float lod = mipFor(detail, DETAIL_TILE / DETAIL_TEXELS);
  float weight = 1.0 - smoothstep(1.0, 3.0, lod);
  if (detail > 0.0 && d > 0.0 && weight > 0.0) {
    vec2 swirl = (textureLod(uCloudCurl, place / CURL_TILE, 0.0).rg * 2.0 - 1.0) * (CURL_REACH * (1.0 - h));
    vec3 dq = vec3(place.x + swirl.x, altitude - uCloudTime * DETAIL_RISE, place.y + swirl.y);
    float fine = dot(textureLod(uCloudDetail, dq / DETAIL_TILE, lod).rgb, vec3(0.625, 0.25, 0.125));
    float erosion = mix(fine, 1.0 - fine, clamp01(h * 10.0));
    d = remap01(d, erosion * DETAIL_EROSION * weight, 1.0);
  }
  return d;
}


float heightIn(float altitude, vec4 w) {
  vec2 span, shape;
  kindOf(w.b, w.a, span, shape);
  return clamp01((altitude - span.x) / (span.y - span.x));
}



vec2 sheetPlace(vec3 p, vec4 frame) { return vec2(dot(p.xz, frame.xy), dot(p.xz, vec2(-frame.y, frame.x))) + frame.zw; }





void footprintOn(vec4 frame, vec3 r, float across, float slant, out vec2 alongView, out vec2 acrossView) {
  vec2 h = length(r.xz) > 1.0e-4 ? normalize(r.xz) : vec2(1.0, 0.0);
  vec2 d = vec2(dot(h, frame.xy), dot(h, vec2(-frame.y, frame.x)));
  alongView = d * (across / slant);
  acrossView = vec2(-d.y, d.x) * across;
}


vec4 sheetImage(vec2 place, float tile, vec2 alongView, vec2 acrossView) {
  return textureGrad(uCloudSheets, place / tile, alongView / tile, acrossView / tile);
}





vec4 sheetRead(vec2 place, float tile, vec2 alongView, vec2 acrossView) {
  vec2 mirror = vec2(1.0, -1.0);
  vec4 a = sheetImage(place, tile, alongView, acrossView);
  vec4 b = sheetImage(place * mirror + vec2(0.4375, 0.3125) * tile, tile, alongView * mirror, acrossView * mirror);
  vec2 at = place / (tile * UNTILE);
  float pick = spread(textureLod(uCloudShape, vec3(at.x, 0.23, at.y), 0.0).r);

  return mix(a, b, smoothstep(0.4, 0.6, pick));
}



float sheetRegion(vec2 place, float tile) {
  vec2 at = place / (tile * REGION);

  return mix(1.0 - REGION_SPREAD, 1.0 + REGION_SPREAD, spread(textureLod(uCloudShape, vec3(at.x, 0.61, at.y), 0.0).r));
}





vec2 sheetWeather(vec3 p, bool round) {
  float type = upperType(p), billowy = remap01(type, 0.5, 1.0);

  return vec2(upperAmount(skyAlong(p), p) * (round ? billowy : 1.0 - billowy), type);
}








float upperDensity(float u, float share, float cut) {
  return pow(remap01(u, 1.0 - share * (1.0 - cut), 1.0), 1.9 - 1.8 * share);
}




vec2 strands(vec2 place, float acrossWind) {
  float lod = mipFor(acrossWind, STRAND_ACROSS / SHAPE_TEXELS);
  vec3 at = vec3(place.x / STRAND_ALONG, uCloudTime / STRAND_CHANGE, place.y / STRAND_ACROSS);
  float cut = remap01(textureLod(uCloudShape, at + vec3(0.0, 0.83, 0.0), lod).b, STRAND_CELLS.x, STRAND_CELLS.y);
  float slide = remap01(textureLod(uCloudShape, at + vec3(0.0, 0.29, 0.0), lod).g, STRAND_CELLS.x, STRAND_CELLS.y);

  return (2.0 * vec2(cut, slide) - 1.0) * vec2(STRAND_CUT, STRAND_SHIFT);
}







float sheetDensity(vec2 place, bool round, vec2 weather, vec2 alongView, vec2 acrossView) {
  float share = weather.x * sheetRegion(place, round ? ROUND_IMAGE : STREAK_IMAGE);
  if (share < 0.005) return 0.0;
  vec2 strand = round ? vec2(0.0) : strands(place, max(abs(alongView.y), abs(acrossView.y)));
  float u;
#if OPTION_HIGH_CLOUDS == OPTION_HIGH_CLOUDS_TEXTURED
  if (round) {
    u = sheetRead(place, ROUND_IMAGE, alongView, acrossView).g;
  } else {
    vec4 image = sheetRead(place + vec2(strand.y, 0.0), STREAK_IMAGE, alongView, acrossView);
    u = mix(image.r, image.b, remap01(weather.y, 0.0, 0.5));
  }
#else
  float footprint = max(length(alongView), length(acrossView));
  float drift = uCloudTime * SHAPE_RISE * (round ? 1.0 : 0.5);
  if (round) {
    float patches = spread(textureLod(uCloudShape, vec3(place.x, drift, place.y) / ROUND_PATCH_TILE, 0.0).r);
    float lod = mipFor(footprint, ROUND_CELL_TILE / SHAPE_TEXELS);
    vec4 cells = textureLod(uCloudShape, vec3(place.x, drift * 4.0, place.y) / ROUND_CELL_TILE, lod);
    u = patches * remap01(dot(cells.gb, vec2(0.7, 0.3)), 0.5, 0.9);
  } else {
    vec2 along = vec2(place.x / STREAK_STRETCH, place.y);
    float sheet = spread(textureLod(uCloudShape, vec3(along.x, drift, along.y) / STREAK_TILE, 0.0).r);
    float lod = mipFor(footprint, STREAK_FINE_TILE / SHAPE_TEXELS);
    vec3 at = vec3(along.x, drift + 0.5 * STREAK_TILE, along.y) / STREAK_FINE_TILE;
    float streaks = remap01(spread(textureLod(uCloudShape, at, lod).r), 0.3, 0.8);
    u = sheet * mix(streaks, 1.0, remap01(weather.y, 0.0, 0.5));
  }
#endif

  return upperDensity(u, share, strand.x);
}




vec2 crossings(float alt, float dy, float target) {
  float a = (1.0 - dy * dy) / (2.0 * EARTH_RADIUS), c = alt - target;
  float disc = dy * dy - 4.0 * a * c;
  if (disc < 0.0) return vec2(-1.0);
  float q = -0.5 * (dy + (dy < 0.0 ? -sqrt(disc) : sqrt(disc)));
  float t1 = abs(q) > 1.0e-12 ? c / q : 0.0, t2 = a > 1.0e-15 ? q / a : 1.0e30;

  return vec2(min(t1, t2), max(t1, t2));
}


float ahead(float alt, float dy, float target) {
  vec2 t = crossings(alt, dy, target);
  return t.x > 0.0 ? t.x : t.y;
}


float heightAlong(float alt, float dy, float t) { return alt + dy * t + (1.0 - dy * dy) * t * t / (2.0 * EARTH_RADIUS); }
#endif
