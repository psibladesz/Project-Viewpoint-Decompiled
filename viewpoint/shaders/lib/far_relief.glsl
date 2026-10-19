






#ifndef VIEWPOINT_FAR_RELIEF
#define VIEWPOINT_FAR_RELIEF
#include "constants.glsl"
uniform sampler2D uRelief;
uniform sampler2D uReliefWater;
uniform sampler2D uReliefWalls;
uniform sampler2D uReliefFlow;
uniform vec4 uReliefGrid;
uniform vec2 uReliefCamera;
uniform float uReliefScale;
uniform float uReliefSplit;

ivec2 reliefOnGrid(ivec2 node) {
  return clamp(node, ivec2(0), ivec2(uReliefGrid.xy) - 1);
}
float reliefNode(ivec2 node) {
  return texelFetch(uRelief, reliefOnGrid(node), 0).r * uReliefScale - uReliefGrid.w;
}




vec4 reliefFlow(vec2 world) {
  vec2 g = world / RELIEF_FLOW_SPACING;
  ivec2 i0 = ivec2(floor(g)), last = textureSize(uReliefFlow, 0) - 1;
  vec2 f = g - vec2(i0);
  vec2 flow = vec2(0.0);
  float weights = 0.0, share = 0.0, kind = 0.0, nearest = 4.0;
  for (int k = 0; k < 4; k++) {
    ivec2 o = ivec2(k & 1, k >> 1);
    vec4 c = texelFetch(uReliefFlow, clamp(i0 + o, ivec2(0), last), 0);
    vec2 b = mix(1.0 - f, f, vec2(o));
    float w = b.x * b.y, cellKind = floor(c.z * 255.0 / 60.0 + 0.5), d = length(vec2(o) - f);
    share += c.w * w;
    if (cellKind < 0.5) continue;
    flow += (c.xy * 255.0 - 128.0) / 127.0 * RELIEF_FLOW_MOST * w;
    weights += w;
    if (d < nearest) { nearest = d; kind = cellKind; }
  }
  return vec4(weights > 0.0 ? flow / weights : vec2(0.0), kind, share);
}
float reliefWaterNode(ivec2 node) {
  ivec2 nodes = (ivec2(uReliefGrid.xy) - 1) / int(RELIEF_WATER_SPACING / RELIEF_SPACING + 0.5) + 1;
  return texelFetch(uReliefWater, clamp(node, ivec2(0), nodes - 1), 0).r * uReliefScale - uReliefGrid.w;
}


vec2 reliefBend(ivec2 cell, vec2 side, vec2 f) {
  if (any(lessThan(cell, ivec2(0))) || any(greaterThanEqual(cell, ivec2(uReliefGrid.xy) - 1))) return f;
  int code = int(texelFetch(uReliefWalls, cell, 0).r * 255.0 + 0.5);
  if (code == 0) return f;
  vec2 lines = vec2(cell) * RELIEF_SPACING + vec2(float(((code >> 2) & 3) + 1), float(((code >> 6) & 3) + 1));
  vec2 stepped = clamp((side - (lines - RELIEF_WALL_EPS)) / RELIEF_WALL_EPS, 0.0, 1.0);
  int x = code & 3, y = (code >> 4) & 3;
  vec2 bent = f;
  if (x != 0) bent.x = x == 1 ? stepped.x : mix(stepped.x, f.x, x == 2 ? f.y : 1.0 - f.y);
  if (y != 0) bent.y = y == 1 ? stepped.y : mix(stepped.y, f.y, y == 2 ? f.x : 1.0 - f.x);
  return bent;
}

float reliefSided(vec2 world, vec2 side) {
  if (uReliefGrid.z <= 0.0) return 0.0;
  vec2 g = world / RELIEF_SPACING;
  ivec2 i = ivec2(floor(g));
  vec2 f = reliefBend(i, side, g - vec2(i));
  float row = mix(reliefNode(i), reliefNode(i + ivec2(1, 0)), f.x);
  float next = mix(reliefNode(i + ivec2(0, 1)), reliefNode(i + ivec2(1, 1)), f.x);
  return mix(row, next, f.y) * uReliefGrid.z;
}
float relief(vec2 world) {
  return reliefSided(world, world);
}


float reliefCoarse(vec2 world) {
  if (uReliefGrid.z <= 0.0) return 0.0;
  int every = max(1, int(uReliefSplit / RELIEF_SPACING + 0.5));
  vec2 g = world / (RELIEF_SPACING * float(every));
  ivec2 i = ivec2(floor(g));
  vec2 f = g - vec2(i);
  float row = mix(reliefNode(i * every), reliefNode((i + ivec2(1, 0)) * every), f.x);
  float next = mix(reliefNode((i + ivec2(0, 1)) * every), reliefNode((i + ivec2(1, 1)) * every), f.x);
  return mix(row, next, f.y) * uReliefGrid.z;
}


float reliefWater(vec2 world) {
  if (uReliefGrid.z <= 0.0) return 0.0;
  vec2 g = world / RELIEF_WATER_SPACING;
  ivec2 i = ivec2(floor(g));
  vec2 f = g - vec2(i);
  float row = mix(reliefWaterNode(i), reliefWaterNode(i + ivec2(1, 0)), f.x);
  float next = mix(reliefWaterNode(i + ivec2(0, 1)), reliefWaterNode(i + ivec2(1, 1)), f.x);
  return mix(row, next, f.y) * uReliefGrid.z;
}

vec2 reliefWorld(vec3 scenePos) {
  return uReliefCamera - scenePos.xz;
}

vec3 reliefLifted(vec3 scenePos) {
  return scenePos + vec3(0.0, relief(reliefWorld(scenePos)) * SQRT6, 0.0);
}

vec3 reliefWaterLifted(vec3 scenePos) {
  return scenePos + vec3(0.0, reliefWater(reliefWorld(scenePos)) * SQRT6, 0.0);
}



vec2 reliefSlope(vec2 world) {
  const float HALF = RELIEF_SPACING * 0.5;
  return vec2(reliefSided(world + vec2(HALF, 0.0), world) - reliefSided(world - vec2(HALF, 0.0), world),
              reliefSided(world + vec2(0.0, HALF), world) - reliefSided(world - vec2(0.0, HALF), world))
         * (SQRT6 / RELIEF_SPACING);
}



vec3 reliefNormalAt(vec3 n, vec2 world) {
  if (n.y == 0.0 || uReliefGrid.z <= 0.0) return n;
  vec2 slope = reliefSlope(world);
  return normalize(n + vec3(slope.x, 0.0, slope.y) * n.y) * length(n);
}
vec3 reliefNormal(vec3 n, vec3 scenePos) {
  return reliefNormalAt(n, reliefWorld(scenePos));
}



const float RELIEF_ROOT = 0.25;


float reliefBend(float height, float bend) {
  return 1.0 - smoothstep(0.0, bend, height);
}



vec3 reliefUpright(vec3 scenePos, vec3 feet, float bend) {
  float under = relief(reliefWorld(feet));
  float own = reliefBend(scenePos.y - feet.y, bend);
  return scenePos + vec3(0.0, mix(under, relief(reliefWorld(scenePos)), own) * SQRT6, 0.0);
}





vec3 reliefOnPlane(vec3 scenePos, vec3 feet, float bend) {
  vec2 at = reliefWorld(feet), world = reliefWorld(scenePos);
  float plane = relief(at) + dot(reliefSlope(at) / SQRT6, world - at);
  float own = reliefBend(scenePos.y - feet.y, bend);
  return scenePos + vec3(0.0, mix(plane, relief(world), own) * SQRT6, 0.0);
}




vec3 reliefStood(vec3 scenePos, vec3 feet, bool upright, bool placed) {
  if (placed) return scenePos;
  if (!upright) return reliefLifted(scenePos);
  return reliefUpright(scenePos, feet, RELIEF_KNEE);
}
#endif
