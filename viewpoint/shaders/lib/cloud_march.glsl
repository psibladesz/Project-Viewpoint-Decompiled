




uniform vec3 uHorizon;
uniform vec3 uZenith;
uniform vec3 uSunDir;
uniform vec3 uSunColor;
uniform float uSunStrength;
uniform float uNight;
uniform float uPixelAngle;
uniform float uCloudPhase;
#include "lib/common.glsl"
#include "lib/cloud_shape.glsl"

const float CLOUD_REACH = 30000.0, CLOUD_HAZE = 9000.0;

const float LIGHT_STEP = 60.0, EXTINCTION = 0.012, LIGHT_EXTINCTION = 0.01;

const float BASE_SHADE = 0.5;


const vec3 FLASH_LIGHT = vec3(5.0, 5.4, 6.0);
const float FLASH_REACH = 1400.0;
vec4 marchClouds(vec3 r) {
  if (r.y < 0.015) return vec4(0.0);
  vec2 layer = cloudLayer();
  float t0 = layer.x / r.y, t1 = min(layer.y / r.y, CLOUD_REACH);
  if (t1 <= t0) return vec4(0.0);

  int steps = int(mix(float(PIPELINE_CLOUD_STEPS), float(PIPELINE_CLOUD_STEPS) * 0.5, smoothstep(0.05, 0.4, r.y)));
  float dt = (t1 - t0) / float(steps);
  float jitter = fract(ign(gl_FragCoord.xy) + uCloudPhase * 0.618034);
  float mu = dot(r, uSunDir);
  float phase = mix(hg(mu, 0.35), hg(mu, -0.15), 0.35) * 4.0 * 3.14159265;
  vec3 sunLight = uSunColor * max(uSunStrength, 0.0) * 2.2 * mix(1.0, 0.25, uNight);
  vec3 ambient = mix(uHorizon, uZenith, 0.5) * 1.1;
  vec3 color = vec3(0.0);
  float transmittance = 1.0;
  for (int i = 0; i < steps; i++) {
    float t = t0 + (float(i) + jitter) * dt;
    vec3 p = r * t;

    float footprint = max(t * uPixelAngle, dt * 0.25);
    vec4 w = cloudWeather(p);
    float d = cloudDensity(p, w, footprint, true);
    if (d < 0.002) continue;

    float depthToSun = 0.0;
    for (int j = 0; j < 3; j++) depthToSun += cloudDensity(p + uSunDir * (float(j) + 0.5) * LIGHT_STEP, w, max(footprint, LIGHT_STEP), false) * LIGHT_STEP;
    float beer = exp(-depthToSun * LIGHT_EXTINCTION * (1.0 + w.g));
    float powder = 1.0 - exp(-depthToSun * 0.02);

    vec2 slab = cloudSlab(w);
    float shade = mix(BASE_SHADE, 1.0, sqrt(clamp((p.y - slab.x) / slab.y, 0.0, 1.0)));
    vec3 light = sunLight * beer * (0.55 + 0.45 * powder) * phase + ambient * shade * (1.0 - 0.4 * w.g);
    vec3 fromFlash = p - uLightning.xyz;
    light += FLASH_LIGHT * uLightning.w * exp(-dot(fromFlash, fromFlash) / (FLASH_REACH * FLASH_REACH));
    float a = 1.0 - exp(-d * EXTINCTION * dt);
    color += light * a * transmittance;
    transmittance *= 1.0 - a;
    if (transmittance < 0.03) break;
  }

  float fade = smoothstep(0.015, 0.12, r.y) * exp(-CLOUD_BASE / r.y / CLOUD_HAZE);

  return vec4(color, 1.0 - transmittance) * fade;
}
