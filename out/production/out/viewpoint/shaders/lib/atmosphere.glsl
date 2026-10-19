






#ifndef VIEWPOINT_ATMOSPHERE
#define VIEWPOINT_ATMOSPHERE
#include "constants.glsl"
uniform sampler2D uTransmittance;
uniform sampler2D uMultipleScattering;
uniform sampler2D uSkyView;
uniform vec3 uAirLight;
const float AIR_PI = 3.14159265;

const float AIR_SUN_HALF = 0.0047;

vec3 airRayleigh(float h) {
  return AIR_RAYLEIGH * exp(-h / AIR_RAYLEIGH_HEIGHT);
}

float airMie(float h) {
  return AIR_MIE_SCATTERING * exp(-h / AIR_MIE_HEIGHT);
}

vec3 airExtinction(float h) {
  float ozone = max(0.0, 1.0 - abs(h - AIR_OZONE_CENTRE) / AIR_OZONE_HALF);
  return airRayleigh(h) + AIR_MIE_EXTINCTION * exp(-h / AIR_MIE_HEIGHT) + AIR_OZONE * ozone;
}

float airRayleighPhase(float nu) {
  return 3.0 / (16.0 * AIR_PI) * (1.0 + nu * nu);
}

float airHenyeyGreenstein(float nu, float g) {
  return (1.0 - g * g) / (4.0 * AIR_PI * pow(1.0 + g * g - 2.0 * g * nu, 1.5));
}


float airMiePhase(float nu) {
  return AIR_MIE_PEAK * airHenyeyGreenstein(nu, AIR_MIE_PEAK_G)
      + (1.0 - AIR_MIE_PEAK) * airHenyeyGreenstein(nu, AIR_MIE_BROAD_G);
}


vec2 airTexel(vec2 x, sampler2D table) {
  vec2 size = vec2(textureSize(table, 0));
  return (0.5 + clamp(x, 0.0, 1.0) * (size - 1.0)) / size;
}


float airToTop(float r, float mu) {
  return max(0.0, -r * mu + sqrt(max(r * r * (mu * mu - 1.0) + AIR_TOP * AIR_TOP, 0.0)));
}


float airHorizon(float r) {
  float q = AIR_BOTTOM / r;
  return -sqrt(max(1.0 - q * q, 0.0));
}


vec3 airTransmittance(float r, float mu) {
  float h = sqrt(AIR_TOP * AIR_TOP - AIR_BOTTOM * AIR_BOTTOM), rho = sqrt(max(r * r - AIR_BOTTOM * AIR_BOTTOM, 0.0));
  float d = airToTop(r, mu), dMin = AIR_TOP - r, dMax = rho + h;
  return texture(uTransmittance, airTexel(vec2((d - dMin) / (dMax - dMin), rho / h), uTransmittance)).rgb;
}



vec3 airSunThrough(float r, float mus) {
  float horizon = airHorizon(r);
  return airTransmittance(r, max(mus, horizon)) * smoothstep(horizon - AIR_SUN_HALF, horizon + AIR_SUN_HALF, mus);
}


vec3 airScatteredMore(float r, float mus) {
  vec2 x = vec2(0.5 + 0.5 * mus, (r - AIR_BOTTOM) / (AIR_TOP - AIR_BOTTOM));
  return texture(uMultipleScattering, airTexel(x, uMultipleScattering)).rgb;
}




vec2 airPlace(vec3 p, vec3 eye, float height, vec3 sunDir) {
  vec3 up = normalize(vec3((p.x - eye.x) * 0.001, AIR_BOTTOM + AIR_GROUND, (p.z - eye.z) * 0.001));
  return vec2(AIR_BOTTOM + AIR_GROUND + height * 0.001, dot(up, sunDir));
}


vec3 airSunlight(float r, float mus) {
  return AIR_SUN * airSunThrough(r, mus) * uAirLight;
}



vec2 airSkyViewPlace(vec3 dir, vec3 sunDir) {
  float across = length(dir.xz), sunAcross = length(sunDir.xz);
  float c = across > 1.0e-5 && sunAcross > 1.0e-5 ? dot(dir.xz, sunDir.xz) / (across * sunAcross) : 1.0;
  float phi = acos(clamp(c, -1.0, 1.0)), up = asin(clamp(dir.y, -1.0, 1.0));
  return vec2(sqrt(phi / AIR_PI), 0.5 + 0.5 * sign(up) * sqrt(abs(up) / (0.5 * AIR_PI)));
}


vec3 airSky(vec3 dir, vec3 sunDir) {
  return texture(uSkyView, airSkyViewPlace(dir, sunDir)).rgb;
}
#endif
