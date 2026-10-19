#version 330







in vec2 vUv;
uniform vec3 uSunTrue;
uniform float uEyeHeight;
uniform float uAirExposure;
out vec4 fragColor;
#include "lib/atmosphere.glsl"
const int STEPS = 32;

void main() {
  float phi = AIR_PI * vUv.x * vUv.x, v = 2.0 * vUv.y - 1.0, up = sign(v) * 0.5 * AIR_PI * v * v;
  vec3 dir = vec3(cos(up) * cos(phi), sin(up), cos(up) * sin(phi));
  float s = clamp(uSunTrue.y, -1.0, 1.0);
  vec3 sun = vec3(sqrt(1.0 - s * s), s, 0.0);
  float r = AIR_BOTTOM + AIR_GROUND + max(uEyeHeight, 1.0) * 0.001, mu = dir.y;
  float toGround = r * r * (mu * mu - 1.0) + AIR_BOTTOM * AIR_BOTTOM;
  bool ground = mu < 0.0 && toGround >= 0.0;
  float end = ground ? -r * mu - sqrt(toGround) : airToTop(r, mu);
  float nu = dot(dir, sun), rayleigh = airRayleighPhase(nu), mie = airMiePhase(nu);
  vec3 light = vec3(0.0), through = vec3(1.0);
  float last = 0.0;
  for (int i = 0; i < STEPS; i++) {
    float f = float(i + 1) / float(STEPS), t = end * f * f, dt = t - last;
    vec3 x = vec3(0.0, r, 0.0) + dir * (0.5 * (t + last));
    last = t;
    float rx = length(x), h = rx - AIR_BOTTOM, mus = dot(x, sun) / rx, mieHere = airMie(h);
    vec3 rayleighHere = airRayleigh(h), extinction = airExtinction(h);
    vec3 source = (rayleighHere * rayleigh + mieHere * mie) * airSunThrough(rx, mus)
        + (rayleighHere + mieHere) * airScatteredMore(rx, mus);
    vec3 kept = exp(-extinction * dt);
    light += through * source * (1.0 - kept) / max(extinction, vec3(1.0e-9));
    through *= kept;
  }
  if (ground) {
    vec3 x = vec3(0.0, r, 0.0) + dir * end;
    float mus = dot(x, sun) / length(x);
    light += through * airSunThrough(AIR_BOTTOM, mus) * max(mus, 0.0) * AIR_GROUND_ALBEDO / AIR_PI;
  }
  fragColor = vec4(light * AIR_SUN * uAirExposure * uAirLight, 1.0);
}
