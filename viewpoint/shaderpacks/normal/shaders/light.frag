#version 330




in vec2 vUv;
uniform sampler2D uAlbedo;
uniform sampler2D uNormal;
uniform sampler2D uEngineLight;
uniform int uDebugView;
uniform int uBlobCount;
uniform vec4 uBlobs[32];
out vec4 fragColor;
#include "lib/camera.glsl"
#include "lib/spots.glsl"
#include "lib/shade.glsl"
#ifndef OPTION_BLOB_SHADOWS
#define OPTION_BLOB_SHADOWS 1
#endif


float blobShadow(vec3 pos, vec3 n) {
#if OPTION_BLOB_SHADOWS
  if (n.y < 0.5) return 1.0;
  float dark = 0.0;
  for (int i = 0; i < uBlobCount; i++) {
    vec3 d = pos - uBlobs[i].xyz;
    if (abs(d.y) > 0.6) continue;
    float across = length(d.xz) / uBlobs[i].w;
    dark = max(dark, (1.0 - smoothstep(0.3, 1.0, across)) * (1.0 - abs(d.y) / 0.6));
  }
  return 1.0 - 0.45 * dark;
#else
  return 1.0;
#endif
}
void main() {
  float depth = texture(uDepth, vUv).r;
  if (depth >= 1.0) discard;
  vec4 albedo = texture(uAlbedo, vUv);
  vec4 normal = texture(uNormal, vUv);
  vec3 light = texture(uEngineLight, vUv).rgb;
  if (uDebugView == 1) { fragColor = vec4(albedo.rgb, 1.0); return; }
  if (uDebugView == 2) { fragColor = vec4(light, 1.0); return; }


  if (albedo.a > 0.125 && albedo.a < 0.375) {
    fragColor = vec4(albedo.rgb * spotLens(scenePos(vUv, depth), uEye), 1.0);
    return;
  }
  vec3 pos = scenePos(vUv, depth);
  vec3 n = normalize(normal.xyz);
  bool foliage = albedo.a > 0.75;
  light += torches(pos, n);

  vec3 color = shade(albedo.rgb, vec4(light, normal.a), foliage ? vec3(0.0, 1.0, 0.0) : n, 1.0);
  fragColor = vec4(color * blobShadow(pos, n) + albedo.rgb * spots(pos, n, foliage), 1.0);
}
