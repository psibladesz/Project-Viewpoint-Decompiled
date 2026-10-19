#version 330








in vec2 vUv;
uniform sampler2D uFroxelVolume;
uniform sampler2D uFarDepth;
uniform float uFarOn;
uniform vec3 uFogColor;
uniform float uAmbientHaze;
uniform sampler2D uSunAirCoords;
uniform sampler2D uSunAir;
out vec4 fragColor;


const float FACE_SIDE = 0.1;
#include "lib/camera.glsl"
#include "lib/air_point.glsl"
#include "lib/froxels.glsl"
#include "lib/epipolar.glsl"

void main() {
  float depth = texture(uDepth, vUv).r;
  bool open = depth >= 1.0;
  bool sky = open && (uFarOn < 0.5 || texture(uFarDepth, vUv).r >= 1.0);
  vec3 pos = open ? uEye : scenePos(vUv, depth);
  float along = open ? uFroxelRange : min(distance(pos, uEye), uFroxelRange);
  vec2 xy = vUv * uFroxels.xy;
  float z = min(froxelSlices(along), uFroxels.z - 1.0e-3);
  int slice = int(floor(z));
  vec4 before = slice == 0 ? vec4(0.0, 0.0, 0.0, 1.0) : froxelSlice(uFroxelVolume, xy, slice - 1);
  vec4 run = mix(before, froxelSlice(uFroxelVolume, xy, slice), z - float(slice));
  vec4 sun = epiUnwarp(uSunAirCoords, uSunAir, vUv, along);
  vec3 dir = normalize(scenePos(vUv, 1.0) - uEye);
  vec3 sunLight = sun.a > 1.0e-3 ? sun.rgb / sun.a : sunAirMarch(dir, along, 0.5);
  float indoor = !open && planInteriorAt(pos + normalize(uEye - pos) * FACE_SIDE) ? 1.0 : 0.0;
  vec3 ambient = sky ? vec3(0.0) : uFogColor * uAmbientHaze * (1.0 - 0.92 * indoor);
  fragColor = vec4(run.rgb + sunLight + ambient * (1.0 - run.a), sky ? 1.0 : run.a);
}
