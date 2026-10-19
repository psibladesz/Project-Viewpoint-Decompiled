#version 330

















in vec2 vUv;
uniform vec2 uFogRange;
uniform vec3 uFogColor;
uniform float uFloorY;
uniform float uDensity;
uniform float uClearDensity;
uniform float uDust;
uniform float uDustSun;
uniform float uAmbientHaze;
uniform float uTime;
#include "lib/weather.glsl"
uniform vec3 uNoiseOrigin;
uniform float uFrameNoise;
uniform sampler3D uNoise;
uniform sampler2D uExposure;
uniform int uLightCount;
uniform vec4 uLightPos[48];
uniform vec3 uLightColor[48];
uniform float uLightOutdoors[48];
uniform float uLampHalo;
uniform sampler2D uFarDepth;
uniform float uFarOn;
out vec4 fragColor;

#ifndef OPTION_VOLUMETRIC_STEPS
#define OPTION_VOLUMETRIC_STEPS 24
#endif
#ifndef OPTION_GODRAYS
#define OPTION_GODRAYS 1
#endif
#include "lib/camera.glsl"
#include "lib/sun.glsl"
#include "lib/spots_air.glsl"
#include "lib/grids.glsl"
#include "lib/common.glsl"
#include "lib/air_sun.glsl"
#include "lib/floor_plan.glsl"

float exposureAt(vec3 p) {
  vec2 euv = gridUv(p.xz);
  if (euv.x < 0.0 || euv.y < 0.0 || euv.x > 1.0 || euv.y > 1.0) return 1.0;
  return texture(uExposure, euv).r;
}

float density(vec3 p, out float indoor) {
  indoor = planInteriorAt(p) ? 1.0 : 0.0;
  float h = max(p.y - uFloorY, 0.0);
  float d = mix(uDensity, uClearDensity, indoor) * (0.35 + 0.65 * exp(-h / 10.0));
  float n = textureLod(uNoise, (p + uNoiseOrigin + vec3(uWind.x, 0.0, uWind.y) * uTime * 0.25) / 48.0, 0.0).r;
  d *= mix(0.5, 1.5, n);
  return d * (1.0 + indoor);
}
void main() {
  float depth = texture(uDepth, vUv).r;
  bool open = depth >= 1.0;
  bool sky = open && (uFarOn < 0.5 || texture(uFarDepth, vUv).r >= 1.0);
  vec3 end = open ? uEye + normalize(scenePos(vUv, 0.999) - uEye) * uFogRange.y : scenePos(vUv, depth);
  vec3 ray = end - uEye;
  float len = min(length(ray), uFogRange.y);
  vec3 dir = ray / max(length(ray), 0.0001);
  vec3 sunLight = airSunLight(dir);
  vec3 ambient = sky ? vec3(0.0) : uFogColor * uAmbientHaze;
  float jitter = fract(ign(gl_FragCoord.xy) + uFrameNoise * 0.618034);
  const int STEPS = OPTION_VOLUMETRIC_STEPS;
  vec3 inscatter = vec3(0.0);
  float transmittance = 1.0;
  for (int i = 0; i < STEPS; i++) {
    float a = float(i) / float(STEPS), b = float(i + 1) / float(STEPS);
    float t0 = len * a * a, t1 = len * b * b;
    vec3 p = uEye + dir * mix(t0, t1, jitter);
    float indoor;
    float s = density(p, indoor);
    float attenuation = exp(-s * (t1 - t0));
#if OPTION_GODRAYS && PIPELINE_SHADOWS
    float sun = uSunStrength > 0.0 ? sunAt(p) : 0.0;
#else
    float sun = 0.0;
#endif
    vec3 scattered = (sunLight * sun * mix(1.0, uDustSun, indoor) + spotsAir(p, dir)) * mix(1.0, uDust, indoor)
        + ambient * (1.0 - 0.92 * indoor);
    inscatter += scattered * transmittance * (1.0 - attenuation);
    transmittance *= attenuation;
  }


  bool outside = exposureAt(uEye) > 0.3;
  vec3 halo = vec3(0.0);
  for (int i = 0; i < min(uLightCount, 32); i++) {
    vec3 m = uEye - uLightPos[i].xyz;
    float bb = dot(dir, m);
    float miss = sqrt(max(dot(m, m) - bb * bb, 0.0));
    float h = sqrt(miss * miss + 0.08);
    float integral = (atan((len + bb) / h) - atan(bb / h)) / h;
    float reach = 1.0 - smoothstep(0.3 * uLightPos[i].w, uLightPos[i].w, miss);
    if (planIndoorsAt(uLightPos[i].xyz) && !planInteriorAt(uLightPos[i].xyz)) continue;
    float through = (uLightOutdoors[i] > 0.5) == outside ? 1.0 : 0.12;
    float air = uLightOutdoors[i] > 0.5 ? min(uDensity, 0.012) : uClearDensity;
    halo += uLightColor[i] * integral * reach * through * air;
  }
  inscatter += halo * uLampHalo;
  fragColor = vec4(inscatter, sky ? 1.0 : transmittance);
}
