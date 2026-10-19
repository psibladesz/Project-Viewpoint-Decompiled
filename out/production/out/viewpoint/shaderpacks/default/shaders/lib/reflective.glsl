


#ifndef VIEWPOINT_REFLECTIVE
#define VIEWPOINT_REFLECTIVE
#include "lib/materials.glsl"
#include "lib/specular_roughness.glsl"
#include "lib/rain_ground.glsl"
#include "lib/water_surface.glsl"


bool reflects(vec2 uv, vec3 pos, vec4 albedo, vec4 normal, vec4 engine, out vec3 n, out float rough) {
  n = normalize(normal.xyz);
  rough = 0.0;
  if (isWater(albedo.a)) {
    WaterSurface s = waterSurface(pos, length(pos - uEye));
    n = s.mirror;
    rough = s.rough;
    return true;
  }
  if (isPaint(albedo.a)) {
    rough = coatRoughness(coatWet(normal.a, engine.a));
    return true;
  }
  RainGround g = rainGround(uv, pos, n, albedo.a, normal.a, engine.a, length(pos - uEye));
  n = g.normal;
  rough = wetRoughness(surfaceRoughness(albedo.a), g);
  return g.puddle > 0.01;
}
#endif
