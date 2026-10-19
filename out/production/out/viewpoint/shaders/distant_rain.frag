#version 330






in vec2 vUv;
uniform sampler2D uDistantRain;
uniform sampler2D uFarDepth;
uniform vec2 uFarRange;
uniform vec3 uHorizon, uSunDir, uForward;
uniform float uSunStrength;
out vec4 fragColor;
#include "lib/camera.glsl"
#include "lib/fog.glsl"
#include "lib/distant_rain.glsl"
const float TAU = 6.2831853;


const float RAIN_TOP = 600.0;



const float EXTINCTION = 0.002, LEAST_LEVEL = 0.1;

const float SHADE = 0.8, GREY = 0.5, SUN_LIGHT = 0.4, SUN_FOCUS = 8.0;
float farLinear(float z) {
  z = z * 2.0 - 1.0;
  return 2.0 * uFarRange.x * uFarRange.y / (uFarRange.y + uFarRange.x - z * (uFarRange.y - uFarRange.x));
}
void main() {
  vec3 r = normalize(scenePos(vUv, 1.0) - uEye);
  float depth = texture(uDepth, vUv).r, along = 1.0e9;
  if (depth < 1.0) {
    along = distance(scenePos(vUv, depth), uEye);
  } else if (uFarRange.x > 0.0) {
    float far = texture(uFarDepth, vUv).r;
    if (far < 1.0) along = farLinear(far) / max(dot(r, uForward), 1.0e-3);
  }
  float level = max(length(r.xz), 1.0e-4), ground = along * level;
  float top = r.y > 1.0e-4 ? RAIN_TOP / r.y * level : 1.0e9, turn = fract(atan(r.z, r.x) / TAU);
  float rain = distantRainAt(uDistantRain, turn, min(ground, top));
  float a = 1.0 - exp(-EXTINCTION * rain / max(level, LEAST_LEVEL));
  if (a < 1.0 / 512.0) discard;
  vec3 fog = fogColor(uHorizon, r), grey = vec3(dot(fog, vec3(0.2126, 0.7152, 0.0722)));
  float sun = SUN_LIGHT * min(uSunStrength, 1.0) * pow(max(dot(r, uSunDir), 0.0), SUN_FOCUS);
  vec3 light = mix(fog, grey, GREY) * (SHADE + sun);
  fragColor = vec4(light * a, a);
}
