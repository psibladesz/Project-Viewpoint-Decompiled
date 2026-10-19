#version 330













in vec2 vUv;
uniform sampler2D uFloorColours;
uniform sampler2D uExposure;
uniform sampler2D uRising;
uniform int uRisingHeld;
uniform sampler2D uLampFields;
uniform sampler2D uTilePalette;
uniform vec2 uMapCorner;
uniform vec2 uCorner;
uniform float uFloorY;
uniform int uPlanLevel;
uniform int uMapStorey;
uniform int uCameraStorey;
uniform int uLightCount;
uniform vec4 uLightPos[48];
uniform vec3 uLightColor[48];
uniform float uLightLevel[48];
layout(location = 0) out vec4 fragColor;
layout(location = 1) out vec4 fragWalls;
layout(location = 2) out vec4 fragRising;
#include "constants.glsl"
#include "lib/camera.glsl"
#include "lib/sun.glsl"
#include "lib/grids.glsl"
#include "lib/far_relief.glsl"
#include "lib/floor_plan.glsl"
#include "lib/lamp_field.glsl"
#include "lib/bounce_source.glsl"

const float PORCH_SKY = 0.5;


const vec3 WALL_UNKNOWN = vec3(0.45, 0.43, 0.40);
const int PALETTE_SIDE = 256;

const float WALL_SHARE = 0.5;

const float WALL_HEIGHT = 1.2;


vec3 lampsOn(vec2 world, vec3 floorAt) {
  vec3 sum = vec3(0.0);
  for (int i = 0; i < uLightCount; i++) {
    if (int(uLightLevel[i]) != uPlanLevel) continue;
    float dist = length(uLightPos[i].xz - floorAt.xz), radius = uLightPos[i].w;
    if (dist >= radius) continue;
    vec2 texel = lampFieldTexel(i, world);
    float through = texture(uLampFields, texel / vec2(textureSize(uLampFields, 0))).r;
    sum += uLightColor[i] * ((1.0 - dist / radius) * through);
  }
  return sum;
}


vec3 wallColour(uint id) {
  vec4 c = texelFetch(uTilePalette, ivec2(int(id) % PALETTE_SIDE, int(id) / PALETTE_SIDE), 0);
  return id != 0u && c.a > 0.5 ? c.rgb : WALL_UNKNOWN;
}



vec3 wallOn(ivec2 square, int dir) {
  return dir == PLAN_NORTH ? wallColour(planWalls(square, uPlanLevel).x)
      : dir == PLAN_WEST ? wallColour(planWalls(square, uPlanLevel).y)
      : dir == PLAN_SOUTH ? wallColour(planWalls(square + ivec2(0, 1), uPlanLevel).x)
      : wallColour(planWalls(square + ivec2(1, 0), uPlanLevel).y);
}


vec3 walls(ivec2 square, vec2 cell, float sky) {
  vec3 sum = vec3(0.0);
  for (int dir = PLAN_NORTH; dir <= PLAN_WEST; dir += 2) {
    if (planStep(square, dir, uPlanLevel) > 0.5) continue;
    vec2 toward = dir == PLAN_EAST ? vec2(1.0, 0.0) : dir == PLAN_WEST ? vec2(-1.0, 0.0) : dir == PLAN_SOUTH
        ? vec2(0.0, 1.0) : vec2(0.0, -1.0);
    vec2 sceneXZ = uExposureA - (cell + 0.5 + toward * 0.45);
    vec3 n = vec3(toward.x, 0.0, toward.y);
    vec3 at = reliefLifted(vec3(sceneXZ.x, uFloorY + WALL_HEIGHT, sceneXZ.y)) + n * 0.05;
    float lit = uSunStrength > 0.0 && dot(n, uSunDir) > 0.0 ? sunAt(at) : 0.0;
    sum += wallOn(square, dir) * bounceIrradiance(sky, lit, n);
  }
  return sum * WALL_SHARE;
}

void main() {
  ivec2 cell = ivec2(gl_FragCoord.xy);
  ivec2 mapSize = textureSize(uFloorColours, 0);
  ivec2 square = ivec2(uCorner) + cell;
  ivec2 inMap = (ivec2(uMapCorner) + cell) % mapSize.x + ivec2(0, uMapStorey * mapSize.x);
  vec4 floorColour = texelFetch(uFloorColours, inMap, 0);
  float exposure = texelFetch(uExposure, cell, 0).r * 255.0;
  uvec2 planned = planSquare(square, uPlanLevel);

  bool air = uPlanLevel > 0 && (planned.y & PLAN_EXISTS) == 0u;
  bool open = (planned.x & PLAN_KNOWN) != 0u ? air || (planned.y & PLAN_OUTDOORS) != 0u : uPlanLevel >= 0;
  float sky = uCameraStorey == 1 ? (exposure > 254.5 ? 1.0 : exposure > 0.5 ? PORCH_SKY : 0.0) : open ? 1.0 : 0.0;
  vec2 sceneXZ = uExposureA - (vec2(cell) + 0.5);
  vec3 floorAt = reliefLifted(vec3(sceneXZ.x, uFloorY, sceneXZ.y)) + vec3(0.0, 0.05, 0.0);
  float lit = uSunStrength > 0.0 && uSunDir.y > 0.0 ? sunAt(floorAt) : 0.0;
  vec3 floorLight = bounceIrradiance(sky, lit, vec3(0.0, 1.0, 0.0)) + lampsOn(vec2(square) + 0.5, floorAt);
  vec3 wallLight = walls(square, vec2(cell), sky);
  vec3 under = air && uRisingHeld == 1 ? texelFetch(uRising, cell, 0).rgb : vec3(0.0);
  fragColor = vec4(floorColour.rgb * floorLight + wallLight + under, 1.0);
  fragWalls = vec4(wallLight, 1.0);
  fragRising = vec4(air ? under : floorColour.rgb * floorLight, 1.0);
}
