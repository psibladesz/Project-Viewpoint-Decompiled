









#ifndef DEFAULT_WATER_BODY
#define DEFAULT_WATER_BODY
#include "lib/water_kinds.glsl"
uniform sampler2D uWaterGrid;
uniform sampler2D uBedColours;
uniform float uBedColoursOn;

const int BED_STEPS = 20, BED_HALVINGS = 4;
const float BED_REACH = 24.0;

const float WATER_ETA = 0.75, CAUSTICS = 0.35;


const float CAUSTIC_MOST = 3.0, CAUSTIC_LEAST = 0.5;


const float CAUSTIC_FOCUS = 2.5;

const float SHORE_FOAM = 0.6, CALM_FOAM = 0.15;



float shoreDistance(vec2 xz) {
  vec2 uv = gridUv(xz);
  if (any(lessThan(uv, vec2(0.0))) || any(greaterThan(uv, vec2(1.0)))) return 16.0;
  return max(texture(uWaterGrid, uv).r * 255.0 / 16.0 - 0.5, 0.0);
}


float bedDepth(vec2 xz, WaterBody b) {
  float t = clamp(shoreDistance(xz) / b.bank, 0.0, 1.0);
  return b.depth * SQUARES_A_METRE * t * t * (3.0 - 2.0 * t);
}


float bedWay(vec3 pos, vec3 r, WaterBody b) {
  float deepest = b.depth * SQUARES_A_METRE, reach = min(BED_REACH, deepest / max(-r.y, 0.05));
  float before = 0.0, after = -1.0;
  for (int i = 1; i <= BED_STEPS; i++) {
    float t = reach * float(i) / float(BED_STEPS);
    vec3 p = pos + r * t;
    if (-r.y * t >= bedDepth(p.xz, b)) { after = t; break; }
    before = t;
  }


  if (after < 0.0) return reach < BED_REACH ? reach : BED_REACH;
  for (int i = 0; i < BED_HALVINGS; i++) {
    float t = 0.5 * (before + after);
    if (-r.y * t >= bedDepth((pos + r * t).xz, b)) after = t; else before = t;
  }
  return after;
}







float caustics(vec2 p, float depth, float strength, vec3 sun, float spread) {
  vec3 r = refract(-sun, vec3(0.0, 1.0, 0.0), WATER_ETA);
  vec2 from = p - r.xz * depth / max(-r.y, 0.2);
  float texel = uWaveTiles.y / 128.0;
  vec2 dx = texture(uWaves, vec3((from + vec2(texel, 0.0)) / uWaveTiles.y, 1.0)).xy
      - texture(uWaves, vec3((from - vec2(texel, 0.0)) / uWaveTiles.y, 1.0)).xy;
  vec2 dz = texture(uWaves, vec3((from + vec2(0.0, texel)) / uWaveTiles.y, 1.0)).xy
      - texture(uWaves, vec3((from - vec2(0.0, texel)) / uWaveTiles.y, 1.0)).xy;

  float lever = (1.0 - WATER_ETA) * depth * strength * RIPPLE_SHARE * CAUSTIC_FOCUS / (2.0 * texel);
  float j = (1.0 - lever * dx.x) * (1.0 - lever * dz.y) - lever * lever * dx.y * dz.x;
  float gathered = clamp(1.0 / max(abs(j), 1.0e-3), CAUSTIC_LEAST, CAUSTIC_MOST);
  return mix(1.0, gathered, CAUSTICS * spread);
}



vec3 bedColour(vec3 p, WaterBody b) {
  if (uBedColoursOn < 0.5) return b.bed;
  vec2 world = -(uSceneOrigin.xz + p.xz);
  vec4 c = texture(uBedColours, world / vec2(textureSize(uBedColours, 0)));
  return c.a > 0.0 ? mix(b.bed, wetAlbedo(c.rgb / c.a, 1.0), c.a) : b.bed;
}



vec3 underwater(vec3 pos, vec3 v, WaterSurface s, vec3 lit, float sun) {
  WaterBody b = waterBody(s.kind);
  vec3 r = refract(-v, s.normal, WATER_ETA);
  if (dot(r, r) < 1.0e-6) r = vec3(0.0, -1.0, 0.0);
  float way = bedWay(pos, r, b), metres = way / SQUARES_A_METRE;
  vec3 through = exp(-b.absorb * metres);
  vec3 bedPos = pos + r * way;
  float down = bedDepth(bedPos.xz, b) / SQUARES_A_METRE;
  vec3 reaching = exp(-b.absorb * down * 1.3);
  float focus = caustics((bedPos + uSceneOrigin).xz, down * SQUARES_A_METRE, s.strength, uSunDir, sun * reaching.g);
  vec3 bed = way < BED_REACH ? bedColour(bedPos, b) * lit * reaching * focus * through : vec3(0.0);
  return bed + b.scatter * lit * (1.0 - through);
}


float waterFoam(vec3 pos, WaterSurface s) {
  float shore = smoothstep(SHORE_FOAM, 0.0, shoreDistance(pos.xz));
  float stir = max(CALM_FOAM, clamp(length(uWind) * 0.25 + length(s.flow) * 0.5, 0.0, 1.0));
  return clamp(s.foam + shore * stir, 0.0, 1.0);
}
#endif
