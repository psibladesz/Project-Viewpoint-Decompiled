


const vec2 CROWN_CENTRE = vec2(0.0, 0.62), CROWN_RADII = vec2(0.82, 0.4);
const float TRUNK = 0.1;

const vec2 BRANCH_CELLS = vec2(6.0, 12.0);
bool inCrown(vec2 corner) {
  vec2 e = (corner - CROWN_CENTRE) / CROWN_RADII;
  return dot(e, e) <= 1.0;
}
bool inTrunk(vec2 corner) {
  return abs(corner.x) <= TRUNK && corner.y <= CROWN_CENTRE.y;
}


bool inBranches(vec2 corner, vec2 square, float cover) {
  if (cover >= 1.0) return true;
  uvec2 cell = uvec2(ivec2(floor(corner * BRANCH_CELLS)) + 64) + uvec2(square * 16.0);
  uint h = (cell.x * 0x27D4EB2Du) ^ (cell.y * 0x165667B1u);
  h ^= h >> 15u;
  h *= 0x85EBCA6Bu;
  h ^= h >> 13u;
  return float(h & 0xFFFFu) / 65535.0 < cover;
}
