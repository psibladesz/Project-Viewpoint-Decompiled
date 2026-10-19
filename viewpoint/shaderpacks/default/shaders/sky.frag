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
uniform float uTime;
uniform vec3 uSkyMoon;
uniform float uMoonFace;
uniform mat3 uStarFrame;
uniform sampler2D uStars;
uniform sampler2D uMoonMap;
uniform vec3 uEye;
out vec4 fragColor;
#include "lib/noise.glsl"
#include "lib/sky.glsl"
#include "lib/mist.glsl"
#ifndef OPTION_NEBULA
#define OPTION_NEBULA 0
#endif


const float DISC_EDGE = 0.99965;
const float DISC_FULL = 0.99988;


const float SUN_RADIUS = 0.004654, DISC_BRIGHTNESS = 12.0, LIMB_DARKENING = 0.6;



const vec3 MOON_TINT = vec3(1.0, 0.97, 0.92);
const float MOON_MAP_MEAN = 0.258, MOON_BRIGHTNESS = 6.0, EARTHSHINE = 0.02;

const int STAR_CELLS = 32, STAR_SLOTS = 16;




const float STAR_ZERO = 12.0, EXTINCTION = 0.25, STAR_HIDE = 1.0, STAR_PIXELS = 1.0;
const float TWINKLE = 0.6, TWINKLE_RATE = 9.0;

const vec3 STAR_BLUE = vec3(0.78, 0.86, 1.0), STAR_SUN = vec3(1.0, 0.97, 0.93), STAR_RED = vec3(1.0, 0.8, 0.62);


const vec3 GALACTIC_POLE = vec3(-0.8676661, -0.1980764, 0.4559838);
const vec3 GALACTIC_CENTRE = vec3(-0.0548755, -0.8734371, -0.4838350);
const float BAND_WIDTH = 0.12, BULGE_WIDTH = 0.22, MILKY_BRIGHTNESS = 0.015, MILKY_HIDE = 0.6;


const vec3 NEBULA_AXIS = vec3(0.56, 0.42, -0.71);
const float NEBULA_NARROW = 9.0;
const float NEBULA_SCALE = 3.0;
const vec3 NEBULA_RED = vec3(0.32, 0.12, 0.45);
const vec3 NEBULA_BLUE = vec3(0.1, 0.25, 0.48);
const float NEBULA_BRIGHTNESS = 0.3;

float luminance(vec3 c) { return dot(c, vec3(0.2126, 0.7152, 0.0722)); }


ivec2 starCell(vec3 e) {
  vec3 a = abs(e);
  int face;
  vec2 uv;
  if (a.x >= a.y && a.x >= a.z) {
    face = e.x > 0.0 ? 0 : 1;
    uv = e.yz / a.x;
  } else if (a.y >= a.z) {
    face = e.y > 0.0 ? 2 : 3;
    uv = e.xz / a.y;
  } else {
    face = e.z > 0.0 ? 4 : 5;
    uv = e.xy / a.z;
  }
  ivec2 c = clamp(ivec2(floor((uv * 0.5 + 0.5) * float(STAR_CELLS))), ivec2(0), ivec2(STAR_CELLS - 1));
  return ivec2(c.x * STAR_SLOTS, face * STAR_CELLS + c.y);
}

vec3 starTint(float bv) {
  return bv < 0.6 ? mix(STAR_BLUE, STAR_SUN, clamp((bv + 0.3) / 0.9, 0.0, 1.0))
                  : mix(STAR_SUN, STAR_RED, clamp((bv - 0.6) / 0.9, 0.0, 1.0));
}



vec3 stars(vec3 e, vec3 r, float pixel, float sky) {
  ivec2 cell = starCell(e);
  float airmass = 1.0 / max(r.y, 0.05), low = smoothstep(0.5, 0.0, r.y);
  vec3 sum = vec3(0.0);
  for (int k = 0; k < STAR_SLOTS; k++) {
    vec4 s = texelFetch(uStars, ivec2(cell.x + k, cell.y), 0);
    if (s.w <= 0.0) break;
    float d = length(e - s.xyz) / pixel;
    if (d > 3.0) continue;
    float magnitude = floor(s.w) / 100.0 - 2.0, bv = fract(s.w) / 0.98 * 2.5 - 0.4;
    float twinkle = 1.0 + TWINKLE * low * (noise(vec2(dot(s.xyz, vec3(91.7, 37.3, 13.9)), uTime * TWINKLE_RATE)) - 0.5);
    float peak = STAR_ZERO * pow(10.0, -0.4 * (magnitude + EXTINCTION * airmass)) * twinkle;
    sum += starTint(bv) * max(peak - STAR_HIDE * sky, 0.0) * exp(-0.5 * d * d / (STAR_PIXELS * STAR_PIXELS));
  }
  return sum;
}



vec3 milkyWay(vec3 e, float sky) {
  float sinB = dot(e, GALACTIC_POLE);
  vec3 inPlane = normalize(e - GALACTIC_POLE * sinB);
  float towards = dot(inPlane, GALACTIC_CENTRE), bulge = smoothstep(0.3, 1.0, towards);
  float width = mix(BAND_WIDTH, BULGE_WIDTH, bulge), b = asin(clamp(sinB, -1.0, 1.0));
  float l = atan(dot(inPlane, cross(GALACTIC_POLE, GALACTIC_CENTRE)), towards);


  vec2 p = vec2(l * 4.0, b * 12.0);
  float clouds = fbm(p);
  float dust = smoothstep(0.5, 0.72, fbm(p * 2.0 + vec2(7.1, 3.3))) * exp(-b * b / (0.3 * width * width));
  float glow = exp(-b * b / (width * width)) * (0.35 + 0.65 * clouds) * (1.0 + 2.0 * bulge) * (1.0 - 0.75 * dust);
  vec3 tint = mix(vec3(0.8, 0.86, 1.0), vec3(1.0, 0.9, 0.76), bulge);
  return tint * max(glow * MILKY_BRIGHTNESS - MILKY_HIDE * sky, 0.0);
}

vec3 nebula(vec3 r) {
  float off = dot(r, normalize(NEBULA_AXIS));
  vec2 p = r.xz / (r.y + 0.6) * NEBULA_SCALE;
  float cloud = smoothstep(0.35, 0.8, fbm(p));
  vec3 colour = mix(NEBULA_RED, NEBULA_BLUE, fbm(p * 1.7 + 11.0));
  return colour * cloud * exp(-off * off * NEBULA_NARROW) * NEBULA_BRIGHTNESS;
}


vec3 nightSky(vec3 r, float sky, float pixel) {
  vec3 e = transpose(uStarFrame) * r;
  vec3 night = stars(e, r, pixel, sky) + milkyWay(e, sky);
#if OPTION_NEBULA
  night += nebula(r);
#endif
  return night;
}







vec3 moonFace(vec3 r, out float cover) {
  float c = dot(r, uSkyMoon);
  cover = smoothstep(DISC_EDGE, DISC_FULL, c) * smoothstep(-0.004, 0.004, r.y);
  if (cover <= 0.0) return vec3(0.0);
  vec3 north = normalize(uStarFrame[2] - uSkyMoon * dot(uStarFrame[2], uSkyMoon));
  vec3 right = cross(uSkyMoon, north);
  vec3 across = (r - uSkyMoon * c) / sqrt(1.0 - DISC_EDGE * DISC_EDGE);
  vec2 p = vec2(dot(across, right), dot(across, north));
  float z = sqrt(max(1.0 - dot(p, p), 0.0));
  vec3 normal = right * p.x + north * p.y - uSkyMoon * z;
  vec2 uv = vec2(0.5 + atan(p.x, z) / 6.2831853, 0.5 - asin(clamp(p.y, -1.0, 1.0)) / 3.1415927);
  vec3 albedo = pow(texture(uMoonMap, uv).rgb, vec3(2.2)) / MOON_MAP_MEAN;
  float incidence = max(dot(normal, uSunTrue), 0.0);
  float lit = 2.0 * incidence / max(incidence + z, 1.0e-3);
  float earthshine = EARTHSHINE * 0.5 * (1.0 + dot(uSkyMoon, uSunTrue));
  return MOON_TINT * albedo * (lit + earthshine) * uMoonFace * MOON_BRIGHTNESS;
}

void main() {
  vec3 r = normalize(vRay);
  vec3 sky = skyColor(r, uHorizon, uZenith, uSunDir, uSunColor, uSunStrength);
  float moonCover;
  vec3 moon = moonFace(r, moonCover);

  float pixel = max(0.5 * length(fwidth(r)), 1.0e-5);



  float night = smoothstep(-0.02, -0.17, uSunTrue.y);
  if (r.y > 0.0 && night > 0.0) {
    sky += nightSky(r, luminance(sky), pixel) * (night * smoothstep(0.0, 0.05, r.y) * (1.0 - moonCover));
  }


  float off = length(r - uSunTrue), limb = sqrt(max(1.0 - off * off / (SUN_RADIUS * SUN_RADIUS), 0.0));
  float disc = clamp((SUN_RADIUS - off) / pixel + 0.5, 0.0, 1.0) * smoothstep(-0.004, 0.004, r.y);
  vec3 sun = airSunlight(AIR_BOTTOM + AIR_GROUND, uSunTrue.y) * (1.0 - 0.85 * uOvercast) * DISC_BRIGHTNESS
      * (1.0 - LIMB_DARKENING * (1.0 - limb));
#if PIPELINE_CLOUDS

  vec4 cloud = uCloudsOn > 0.5 ? texture(uClouds, vUv) : vec4(0.0);
  sky = sky * (1.0 - cloud.a) + cloud.rgb;
  disc *= 1.0 - cloud.a;
  moon *= 1.0 - cloud.a;
#endif
  sky += moon + sun * disc;
  fragColor = vec4(mistedBeyond(sky * SKY_BRIGHTNESS, r, mistHeight(uEye.y), uSunDir), 1.0);
}
