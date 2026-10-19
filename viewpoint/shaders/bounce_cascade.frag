#version 330









in vec2 vUv;
uniform sampler2D uEmit;
uniform sampler2D uEmitWalls;
uniform sampler2D uUpper;
uniform sampler2D uUpperWalls;
uniform int uCascade;
uniform vec2 uCorner;
uniform int uPlanLevel;
layout(location = 0) out vec4 fragColor;
layout(location = 1) out vec4 fragWalls;
#include "lib/floor_plan.glsl"
#include "lib/bounce_cascades.glsl"

#ifndef OPTION_BOUNCE_REACH
#define OPTION_BOUNCE_REACH 6
#endif
const float FADE = 3.0 / float(OPTION_BOUNCE_REACH);


const float FADED = 6.9 / FADE;

const int MARCH_STEPS = 140, MERGE_STEPS = 24;



vec4 march(vec2 origin, vec2 dir, float from, float to, out vec3 walls) {
  vec2 p = origin + dir * from;
  ivec2 cell = ivec2(floor(p)), walk = ivec2(sign(dir));
  vec2 delta = 1.0 / max(abs(dir), vec2(1.0e-6));
  vec2 next = vec2(walk.x > 0 ? float(cell.x + 1) - p.x : p.x - float(cell.x),
      walk.y > 0 ? float(cell.y + 1) - p.y : p.y - float(cell.y)) * delta;
  float t = 0.0, span = to - from, through = 1.0;
  vec3 light = vec3(0.0);
  walls = vec3(0.0);
  span = min(span, FADED - from);
  for (int k = 0; k < MARCH_STEPS && t < span && through > 0.001; k++) {
    bool acrossX = next.x < next.y;
    float leave = min(min(next.x, next.y), span), l = leave - t;
    if (all(greaterThanEqual(cell, ivec2(0))) && all(lessThan(cell, ivec2(CASCADE_SQUARES)))) {
      vec3 leaving = texelFetch(uEmit, cell, 0).rgb;
      float kept = exp(-FADE * l), gathered = through * 0.25 * (1.0 - kept);
      light += gathered * leaving;
      walls += gathered * texelFetch(uEmitWalls, cell, 0).rgb;
      through *= kept;
    }
    t = leave;
    if (t >= span) break;
    ivec2 square = ivec2(uCorner) + cell;
    if (acrossX) {
      through *= planStep(square, walk.x > 0 ? PLAN_EAST : PLAN_WEST, uPlanLevel);
      cell.x += walk.x;
      next.x += delta.x;
    } else {
      through *= planStep(square, walk.y > 0 ? PLAN_SOUTH : PLAN_NORTH, uPlanLevel);
      cell.y += walk.y;
      next.y += delta.y;
    }
  }
  return vec4(light, through);
}



vec3 merged(vec2 origin, vec2 dir, int d, float end, out vec3 walls) {
  int upper = uCascade + 1, probes = CASCADE_SQUARES >> upper;
  float spacing = float(cascadeSpacing(upper));
  vec2 u = origin / spacing - 0.5, f = fract(u);
  ivec2 q0 = ivec2(floor(u));
  vec2 ending = vec2(uCorner) + origin + dir * end;
  vec3 sum = vec3(0.0), wallSum = vec3(0.0);
  float weights = 0.0;
  walls = vec3(0.0);
  for (int k = 0; k < 4; k++) {
    ivec2 o = ivec2(k & 1, k >> 1), q = clamp(q0 + o, ivec2(0), ivec2(probes - 1));
    float w = (o.x == 1 ? f.x : 1.0 - f.x) * (o.y == 1 ? f.y : 1.0 - f.y);
    if (w <= 0.0) continue;
    vec2 start = vec2(uCorner) + cascadeProbe(upper, q) + dir * end;
    w *= planSight(ending, start, uPlanLevel, MERGE_STEPS);
    if (w <= 0.0) continue;
    vec3 light = vec3(0.0), wallLight = vec3(0.0);
    for (int c = 0; c < 4; c++) {
      ivec2 at = cascadeTexel(upper, q, 4 * d + c);
      light += texelFetch(uUpper, at, 0).rgb;
      wallLight += texelFetch(uUpperWalls, at, 0).rgb;
    }
    sum += w * light * 0.25;
    wallSum += w * wallLight * 0.25;
    weights += w;
  }
  if (weights <= 1.0e-4) return vec3(0.0);
  walls = wallSum / weights;
  return sum / weights;
}

void main() {
  int b = cascadeBlock(uCascade);
  ivec2 texel = ivec2(gl_FragCoord.xy), probe = texel / b, inBlock = texel - probe * b;
  int d = inBlock.y * b + inBlock.x;
  vec2 dir = cascadeDirection(uCascade, d), origin = cascadeProbe(uCascade, probe);
  float from = cascadeStart(uCascade), to = cascadeStart(uCascade + 1);
  if (from >= FADED) {
    fragColor = fragWalls = vec4(0.0, 0.0, 0.0, 1.0);
    return;
  }
  vec3 walls, farWalls;
  vec4 near = march(origin, dir, from, to, walls);
  vec3 light = near.rgb;
  if (uCascade < CASCADES - 1 && to < FADED && near.a > 0.001) {
    light += near.a * merged(origin, dir, d, to, farWalls);
    walls += near.a * farWalls;
  }
  fragColor = vec4(light, 1.0);
  fragWalls = vec4(walls, 1.0);
}
