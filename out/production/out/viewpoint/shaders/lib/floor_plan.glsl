



#ifndef VIEWPOINT_FLOOR_PLAN
#define VIEWPOINT_FLOOR_PLAN
#include "constants.glsl"
#include "lib/far_relief.glsl"
uniform usampler2DArray uFloorPlan;
uniform vec3 uPlanCorner;
#ifndef VIEWPOINT_SCENE_ORIGIN
#define VIEWPOINT_SCENE_ORIGIN
uniform vec3 uSceneOrigin;
#endif



const int PLAN_SIZE = 128, PLAN_LEVELS = 5, PLAN_LEVEL_LIFT = 40;
const uint PLAN_KNOWN = 1073741824u;



const uint PLAN_EXISTS = 1u, PLAN_OUTDOORS = 64u, PLAN_EXTERIOR = 128u, PLAN_INTERIOR = 256u;

const uint PLAN_CLOSED_DOOR = 3u, PLAN_BLOCKED = 4u;
const int PLAN_NORTH = 0, PLAN_EAST = 2, PLAN_SOUTH = 4, PLAN_WEST = 6;

const float PLAN_DOOR_THROUGH = 0.15, PLAN_CURTAIN_THROUGH = 0.33;


ivec2 planSquareAt(vec3 scenePos) {
  return ivec2(floor(-(uSceneOrigin.x + scenePos.x)), floor(-(uSceneOrigin.z + scenePos.z)));
}




float planHeightAt(vec3 scenePos) {
  return scenePos.y - relief(reliefWorld(scenePos)) * SQRT6 + uSceneOrigin.y;
}


int planLevelAt(vec3 scenePos) {
  return int(floor(planHeightAt(scenePos) / SQRT6 + 1.0e-4));
}



uvec3 planTexel(ivec2 square, int level) {
  ivec3 at = ivec3(square, level) - ivec3(uPlanCorner);
  bool held = all(greaterThanEqual(at, ivec3(0))) && all(lessThan(at, ivec3(PLAN_SIZE, PLAN_SIZE, PLAN_LEVELS)));
  if (!held) return uvec3(0u);
  return texelFetch(uFloorPlan, ivec3(square & (PLAN_SIZE - 1), (level + PLAN_LEVEL_LIFT) % PLAN_LEVELS), 0).rgb;
}


uvec2 planSquare(ivec2 square, int level) {
  return planTexel(square, level).rg;
}



uvec2 planWalls(ivec2 square, int level) {
  uint walls = planTexel(square, level).b;
  return uvec2(walls & 65535u, walls >> 16u);
}



bool planSquareIndoors(uvec2 s) {
  return (s.y & PLAN_EXISTS) != 0u && (s.y & PLAN_EXTERIOR) == 0u;
}


bool planIndoors(ivec2 square, int level) {
  uvec2 s = planSquare(square, level);
  return (s.x & PLAN_KNOWN) != 0u && planSquareIndoors(s);
}





bool planIndoorsAt(vec3 scenePos) {
  ivec2 square = planSquareAt(scenePos);
  int level = planLevelAt(scenePos);
  uvec2 above = planSquare(square, level + 1);
  return planIndoors(square, level) && (above.x & PLAN_KNOWN) != 0u && (above.y & PLAN_EXISTS) != 0u;
}




bool planInteriorAt(vec3 scenePos) {
  return planIndoorsAt(scenePos)
      && (planSquare(planSquareAt(scenePos), planLevelAt(scenePos)).y & PLAN_INTERIOR) != 0u;
}

float planThrough(uint test) {
  return test == PLAN_BLOCKED ? 0.0 : test == PLAN_CLOSED_DOOR ? PLAN_DOOR_THROUGH : 1.0;
}


uint planCurtain(int dir) {
  return dir == PLAN_NORTH ? 4u : dir == PLAN_EAST ? 8u : dir == PLAN_SOUTH ? 16u : 32u;
}





float planStepOf(uvec2 pa, uvec2 pb, int dir) {
  int back = (dir + 4) % 8;
  if ((pa.x & PLAN_KNOWN) == 0u || (pb.x & PLAN_KNOWN) == 0u) return 1.0;
  bool aExists = (pa.y & PLAN_EXISTS) != 0u, bExists = (pb.y & PLAN_EXISTS) != 0u;
  float ta = planThrough((pa.x >> uint(dir * 3)) & 7u), tb = planThrough((pb.x >> uint(back * 3)) & 7u);
  float t = aExists && bExists ? max(ta, tb) : aExists ? ta : bExists ? tb : 1.0;
  return (pa.y & planCurtain(dir)) != 0u || (pb.y & planCurtain(back)) != 0u ? t * PLAN_CURTAIN_THROUGH : t;
}


float planStep(ivec2 a, int dir, int level) {
  ivec2 b = a + (dir == PLAN_EAST ? ivec2(1, 0) : dir == PLAN_WEST ? ivec2(-1, 0) : dir == PLAN_SOUTH ? ivec2(0, 1)
      : ivec2(0, -1));
  return planStepOf(planSquare(a, level), planSquare(b, level), dir);
}



float planSight(vec2 from, vec2 to, int level, int steps) {
  ivec2 cell = ivec2(floor(from)), end = ivec2(floor(to));
  vec2 d = to - from;
  ivec2 walk = ivec2(sign(d));
  vec2 delta = 1.0 / max(abs(d), vec2(1.0e-6));
  vec2 next = vec2(walk.x > 0 ? float(cell.x + 1) - from.x : from.x - float(cell.x),
      walk.y > 0 ? float(cell.y + 1) - from.y : from.y - float(cell.y)) * delta;
  float through = 1.0;
  for (int k = 0; k < steps && any(notEqual(cell, end)) && through > 0.0; k++) {
    if (next.x < next.y) {
      through *= planStep(cell, walk.x > 0 ? PLAN_EAST : PLAN_WEST, level);
      cell.x += walk.x;
      next.x += delta.x;
    } else {
      through *= planStep(cell, walk.y > 0 ? PLAN_SOUTH : PLAN_NORTH, level);
      cell.y += walk.y;
      next.y += delta.y;
    }
  }
  return through;
}
#endif
