



uniform vec3 uFogColor;
uniform int uLightCount;
uniform vec4 uLightPos[48];
uniform vec3 uLightColor[48];
#include "lib/lamp_shadow.glsl"



vec3 lamps(vec3 pos, vec3 n) {
  vec3 sum = vec3(0.0);
  for (int i = 0; i < uLightCount; i++) {
    vec3 d = uLightPos[i].xyz - pos;
    float dist = length(d);
    float radius = uLightPos[i].w;
    if (dist >= radius) continue;
    float att = 1.0 - dist / radius;
    att *= att;
    float facing = 0.35 + 0.65 * max(dot(n, d / max(dist, 0.001)), 0.0);
    sum += uLightColor[i] * att * facing * lampVisibility(i, pos, n);
  }
  return sum;
}
#include "lib/torches.glsl"
vec3 shade(vec3 albedo, vec4 light, vec3 n, float lit) {



  float s = uSunStrength;
  float outdoors = mix(1.0 - 0.35 * s, 1.0 + 0.06 * s, lit);
  float indoors = 1.0 + 0.5 * s * lit;
  float k = mix(indoors, outdoors, light.a);
  vec3 warm = mix(vec3(1.0), uSunColor, lit * s * 0.45);
  vec3 cool = mix(vec3(1.0), vec3(0.90, 0.95, 1.05), (1.0 - lit) * s * light.a);
  float up = 0.5 + 0.5 * n.y;
  vec3 skyTint = mix(vec3(1.0), uFogColor / max(dot(uFogColor, vec3(0.3333)), 0.001), 0.2);
  vec3 hemi = mix(vec3(mix(0.86, 1.0, up)), mix(vec3(0.86, 0.85, 0.84), skyTint * 1.02, up), light.a);
  return albedo * light.rgb * k * warm * cool * hemi;
}
