








#ifndef DEFAULT_RAIN_GROUND
#define DEFAULT_RAIN_GROUND
#include "lib/materials.glsl"
#include "lib/specular_roughness.glsl"
#include "lib/weather.glsl"
uniform sampler2DArray uRainTextures;
uniform float uPuddles;
#ifndef VIEWPOINT_RAIN_STRENGTH
#define VIEWPOINT_RAIN_STRENGTH
uniform float uRainStrength;
#endif
#ifndef VIEWPOINT_SCENE_ORIGIN
#define VIEWPOINT_SCENE_ORIGIN
uniform vec3 uSceneOrigin;
#endif



const float PUDDLE_SQUARES = 12.0, REGION_SQUARES = 97.0, RIPPLE_SQUARES = 1.5;
const vec2 REGION_TURN = vec2(0.8, 0.6);


const float WARP = 0.6, FILL_LEAST = 0.35, FILL_MOST = 1.65;

const float LEVEL = 0.9986, RUNS_OFF = 0.9945;


const float PUDDLE_FILL = 0.4, PUDDLE_MARGIN = 0.03;


const float POROSITY = 0.45, POROUS_WET = 0.2;


const float ROUGH_PUDDLE = 0.02, RIPPLE_TILT = 0.35, RIPPLE_REACH = 24.0, RIPPLE_PI = 3.14159265;


const vec4 RIPPLE_CYCLES = vec4(960.0, 816.0, 893.0, 1085.0);
const vec2 RIPPLE_SHIFT[4] = vec2[4](vec2(0.25, 0.0), vec2(-0.55, 0.3), vec2(0.6, 0.85), vec2(0.5, -0.75));

struct RainGround {
  float wet;
  float puddle;
  vec3 normal;
};


vec2 ripple(vec2 uv, float moment, float weight) {
  vec4 r = texture(uRainTextures, vec3(uv, 0.0));
  float drop = fract(r.a + moment);
  float wave = clamp(0.2 + weight * 0.8 - drop, 0.0, 1.0) * r.r * sin(clamp((drop - 1.0 + r.r) * 9.0, 0.0, 3.0) * RIPPLE_PI);
  return (r.gb * 2.0 - 1.0) * wave * RIPPLE_TILT;
}


vec2 rings(vec2 w, float far) {
  vec4 weights = clamp((uRainStrength - vec4(0.0, 0.25, 0.5, 0.75)) * 4.0, 0.0, 1.0) * smoothstep(RIPPLE_REACH, 0.5 *
      RIPPLE_REACH, far);
  if (weights.x <= 0.0) return vec2(0.0);
  vec4 moments = fract(uRainClock * RIPPLE_CYCLES / RAIN_PERIOD);
  vec2 uv = w / RIPPLE_SQUARES, tilt = vec2(0.0);
  for (int k = 0; k < 4; k++) tilt += weights[k] * ripple(uv + RIPPLE_SHIFT[k], moments[k], weights[k]);
  return tilt;
}



float faceUp(vec2 uv, vec3 pos) {
  vec2 texel = 1.0 / vec2(textureSize(uDepth, 0));
  vec2 ux = uv + vec2(texel.x, 0.0), uy = uv + vec2(0.0, texel.y);
  vec3 c = cross(scenePos(ux, texture(uDepth, ux).r) - pos, scenePos(uy, texture(uDepth, uy).r) - pos);
  float length2 = dot(c, c);
  return length2 > 1.0e-12 ? abs(c.y) * inversesqrt(length2) : 0.0;
}



RainGround rainGround(vec2 uv, vec3 pos, vec3 n, float material, float outdoors, float skyShare, float far) {
  RainGround g = RainGround(0.0, 0.0, n);
  if (n.y < 0.9 || outdoors < 0.5 || skyShare < 0.9) return g;
  g.wet = uWet;
  if (!isPlain(material) || isModelFace(material)) return g;
  vec2 w = (pos + uSceneOrigin).xz;
  vec2 turned = mat2(REGION_TURN.x, REGION_TURN.y, -REGION_TURN.y, REGION_TURN.x) * w / REGION_SQUARES;
  vec4 region = texture(uRainTextures, vec3(turned, 1.0));
  float height = texture(uRainTextures, vec3(w / PUDDLE_SQUARES + (region.ba * 2.0 - 1.0) * WARP, 1.0)).r;
  float fill = uPuddles * PUDDLE_FILL * mix(FILL_LEAST, FILL_MOST, region.g);
  float level = uPuddles > 0.0 ? smoothstep(RUNS_OFF, LEVEL, faceUp(uv, pos)) : 0.0;
  g.puddle = clamp((fill - height) / PUDDLE_MARGIN, 0.0, 1.0) * level;
  g.wet = max(g.wet, g.puddle);
  vec2 tilt = g.puddle > 0.0 ? rings(w, far) : vec2(0.0);
  g.normal = normalize(mix(n, normalize(vec3(tilt.x, 1.0, tilt.y)), g.puddle));
  return g;
}


vec3 wetAlbedo(vec3 albedo, float wet) {
  return albedo * mix(1.0, mix(1.0, POROUS_WET, POROSITY), wet);
}


float wetRoughness(float rough, RainGround g) {
  float smoother = rough * mix(1.0, mix(1.0, POROUS_WET, POROSITY), 0.5 * g.wet);
  return mix(smoother, ROUGH_PUDDLE, g.puddle);
}
#endif
