#version 330





in vec2 vUv;
uniform sampler2D uColor;
uniform sampler2D uVolume;
uniform sampler2D uBloom;
uniform vec2 uHalfSize;
uniform float uDesaturation;
uniform int uDebugView;
uniform float uNight;
uniform float uVolumeOn;
uniform float uBloomStrength;
uniform float uExposure;
uniform vec3 uFogColor;
uniform vec2 uFogRange;
uniform vec2 uHaze;
#if PIPELINE_SUN_SHAFTS
uniform sampler2D uSunShafts;
uniform float uShaftStrength;
#endif
out vec4 fragColor;
#include "lib/camera.glsl"
#include "lib/common.glsl"
#include "lib/distance.glsl"
#include "lib/eye_meter.glsl"


vec4 upsample(float depth) {
  float d0 = linearDepth(depth);
  vec2 texel = 1.0 / uHalfSize;
  vec2 base = (floor(vUv * uHalfSize - 0.5) + 0.5) * texel;
  vec4 volume = vec4(0.0);
  float weight = 0.0;
  for (int y = 0; y < 2; y++) {
    for (int x = 0; x < 2; x++) {
      vec2 uv = base + vec2(x, y) * texel;
      float d = linearDepth(texture(uDepth, uv).r);
      float w = 1.0 / (1.0 + abs(d - d0) * 8.0 / max(d0, 0.5));
      volume += texture(uVolume, uv) * w;
      weight += w;
    }
  }
  return volume / max(weight, 0.0001);
}
void main() {
  vec3 color = texture(uColor, vUv).rgb;
  if (uDebugView == 1 || uDebugView == 2 || uDebugView == 4 || uDebugView == 6 || uDebugView >= 11) {
    fragColor = vec4(color, 1.0);
    return;
  }
  float depth = texture(uDepth, vUv).r;
  vec4 volume = vec4(0.0, 0.0, 0.0, 1.0);
#if PIPELINE_VOLUMETRICS
  volume = upsample(depth);
#endif
  if (uDebugView == 3) { fragColor = vec4(volume.rgb * 4.0, 1.0); return; }

  if (any(isnan(color)) || any(isinf(color))) color = vec3(0.0);
  if (any(isnan(volume)) || any(isinf(volume))) volume = vec4(0.0, 0.0, 0.0, 1.0);
  volume = mix(vec4(0.0, 0.0, 0.0, 1.0), volume, uVolumeOn);
  vec3 pos = scenePos(vUv, depth);


  bool share = uDebugView >= 7 && uDebugView <= 9;
  if (uDebugView == 10) {
    float fog = depth < 1.0 ? distanceShare(pos, distance(pos, uEye), linearDepth(depth), uFogRange, uHaze) : 1.0;
    fragColor = vec4(vec3(fog), 1.0);
    return;
  }
  if (!share) color = color * volume.a + volume.rgb;
  if (depth < 1.0 && !share) {
    color = distanceFog(
        color, pos, normalize(pos - uEye), distance(pos, uEye), linearDepth(depth), uFogRange, uHaze, uFogColor);
  }
#if PIPELINE_SUN_SHAFTS



  float air = depth < 1.0 ? distanceShare(pos, distance(pos, uEye), linearDepth(depth), uFogRange, uHaze) : 1.0;
  if (!share) color += texture(uSunShafts, vUv).rgb * uShaftStrength * air;
#endif
  if (uBloomStrength > 0.0 && !share) color += texture(uBloom, vUv).rgb * uBloomStrength;


  if (uMeter != 0) { fragColor = meterValue(color, depth, vUv); return; }
  color = shoulder(max(color, vec3(0.0)) * uExposure);
  color += (ign(gl_FragCoord.xy + 17.0) - 0.5) / 255.0;
  fragColor = vec4(color, 1.0);
}
