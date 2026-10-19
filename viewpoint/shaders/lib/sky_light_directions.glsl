


const int RINGS = 16;
const int AROUND = 32;
vec3 skyLightDirection(ivec2 at, out float solid) {
  float ringStep = 0.5 * AIR_PI / float(RINGS), aroundStep = 2.0 * AIR_PI / float(AROUND);
  float up = (float(at.y) + 0.5) * ringStep, around = (float(at.x) + 0.5) * aroundStep;
  solid = cos(up) * ringStep * aroundStep;
  return vec3(cos(up) * cos(around), sin(up), cos(up) * sin(around));
}
float harmonic(int i, vec3 d) {
  if (i == 0) return 0.282095;
  if (i == 1) return 0.488603 * d.y;
  if (i == 2) return 0.488603 * d.z;
  if (i == 3) return 0.488603 * d.x;
  if (i == 4) return 1.092548 * d.x * d.z;
  if (i == 5) return 1.092548 * d.y * d.z;
  if (i == 6) return 0.315392 * (3.0 * d.y * d.y - 1.0);
  if (i == 7) return 1.092548 * d.x * d.y;
  return 0.546274 * (d.x * d.x - d.z * d.z);
}
