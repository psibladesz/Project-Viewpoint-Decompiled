








#include "lib/land_light.glsl"
#include "lib/occlusion.glsl"
#include "lib/scotopic.glsl"
uniform vec3 uFogColor;
uniform int uLightCount;
uniform vec4 uLightPos[48];
uniform vec3 uLightColor[48];
#include "lib/lamp_shadow.glsl"

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


vec3 skyPartOf(vec3 light, float skyShare) {
  return min(light, max(uSkyLevel - (1.0 - skyShare) * (SKY_STEP * 15.0 / 255.0), vec3(0.0)));
}



const float NIGHT_SKY_GREY = 0.85;




vec3 shadeShared(vec3 albedo, vec4 light, float skyShare, vec3 n, float lit, vec4 occlusion, vec3 bounce) {
  vec3 skyPart = skyPartOf(light.rgb, skyShare), lampPart = light.rgb - skyPart;
  vec3 seen = multiBounce(occlusion.w, albedo);
  vec3 sky = skyPart * mix(vec3(1.0), landSky(occlusion.xyz), skyShare);
  sky = mix(sky, vec3(dot(sky, vec3(0.2126, 0.7152, 0.0722))), NIGHT_SKY_GREY * scotopic());
  vec3 body = landBody() * (max(dot(n, uSunDir), 0.0) * lit);
  return albedo * ((sky + lampPart + bounce) * seen + body);
}

vec3 shade(vec3 albedo, vec4 light, vec3 n, float lit) {
  return shadeShared(albedo, light, light.a, n, lit, vec4(n, 1.0), vec3(0.0));
}
