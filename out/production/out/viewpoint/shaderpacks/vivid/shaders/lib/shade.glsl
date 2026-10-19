





uniform vec3 uFogColor;
uniform int uLightCount;
uniform vec4 uLightPos[48];
uniform vec3 uLightColor[48];
#include "lib/lamp_shadow.glsl"



const float SUN_FULL = 0.35;

const float AMBIENT_SHADE = 0.55;


const float SUN_GAIN = 1.35;
const float INDOOR_SUN_GAIN = 1.3;

const float SUN_WRAP = 0.4;

const float SKY_TINT = 0.4;
const float SUN_TINT = 0.6;

const float BOUNCE = 0.2;

const float MIN_LIGHT = 0.02;

const vec3 FACE_SHADE = vec3(0.8, 0.9, 0.62);

const float LAMP_PEAK = 1.5;




vec3 lamps(vec3 pos, vec3 n) {
  vec3 sum = vec3(0.0);
  for (int i = 0; i < uLightCount; i++) {
    vec3 d = uLightPos[i].xyz - pos;
    float dist = length(d);
    float radius = uLightPos[i].w;
    if (dist >= radius) continue;
    float near = 1.0 - dist / radius;
    float att = near * near * (1.0 + LAMP_PEAK * pow(near, 6.0));
    float facing = 0.35 + 0.65 * max(dot(n, d / max(dist, 0.001)), 0.0);
    sum += uLightColor[i] * att * facing * lampVisibility(i, pos, n);
  }
  return sum;
}
#include "lib/torches.glsl"
float faceShade(vec3 n) {
  vec3 a = abs(n);
  float side = (a.x * FACE_SHADE.x + a.z * FACE_SHADE.y) / max(a.x + a.z, 1.0e-4);
  return n.y >= 0.0 ? mix(side, 1.0, n.y) : mix(side, FACE_SHADE.z, -n.y);
}

vec3 hueOf(vec3 c) {
  return c / max(dot(c, vec3(0.3333)), 1.0e-3);
}
vec3 shade(vec3 albedo, vec4 light, vec3 n, float lit) {
  float s = smoothstep(0.0, SUN_FULL, uSunStrength);
  float outdoors = light.a;
  vec3 sunHue = mix(vec3(1.0), hueOf(uSunColor), SUN_TINT);
  vec3 skyHue = mix(vec3(1.0), hueOf(uFogColor), SKY_TINT * s * outdoors);
  vec3 ambient = light.rgb * mix(1.0, AMBIENT_SHADE, s * outdoors) * faceShade(n) * skyHue;
  float facing = mix(SUN_WRAP, 1.0, max(dot(n, uSunDir), 0.0));
  vec3 sun = light.rgb * sunHue * (s * lit * facing * mix(INDOOR_SUN_GAIN, SUN_GAIN, outdoors));
  vec3 bounce = light.rgb * sunHue * (BOUNCE * s * outdoors * (1.0 - abs(n.y)));
  vec3 total = sqrt(ambient * ambient + sun * sun + bounce * bounce);
  return albedo * max(total, vec3(MIN_LIGHT));
}
