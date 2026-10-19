




#ifndef DEFAULT_MIST
#define DEFAULT_MIST
#include "constants.glsl"
#include "lib/sky_light.glsl"
#include "lib/scotopic.glsl"
#include "lib/floor_plan.glsl"
#ifndef VIEWPOINT_SKY_LEVEL
#define VIEWPOINT_SKY_LEVEL
uniform vec3 uSkyLevel;
#endif
#ifndef VIEWPOINT_SCENE_ORIGIN
#define VIEWPOINT_SCENE_ORIGIN
uniform vec3 uSceneOrigin;
#endif
uniform float uMist;



const float MIST_EXTINCTION = 0.075, MIST_HEIGHT = 12.0, MIST_LOWEST = 3.0, MIST_ALBEDO = 0.95, MIST_G = 0.5;


const float MIST_ROUND = 0.7;

const float MIST_NIGHT_GREY = 0.85;

const float MIST_ENDLESS = 1.0e6;
const float MIST_PI = 3.14159265;




float mistOpen(vec3 eye, vec3 pos) {
  int level = int(floor((eye.y + uSceneOrigin.y) / SQRT6 + 1.0e-4));
  bool inside = planIndoors(planSquareAt(eye), level) && planIndoors(planSquareAt(pos), level);
  return inside ? 0.0 : 1.0;
}


float mistHeight(float sceneY) {
  return sceneY + uSceneOrigin.y;
}



float mistDepth(float h0, float mu, float d) {
  float sigma = uMist * MIST_EXTINCTION * exp(-max(h0, -MIST_LOWEST * MIST_HEIGHT) / MIST_HEIGHT);
  float x = d * mu / MIST_HEIGHT;
  return sigma * (abs(x) < 1.0e-3 ? d * (1.0 - 0.5 * x) : MIST_HEIGHT / mu * (1.0 - exp(-x)));
}







vec3 mistLight(vec3 view, vec3 body, vec3 sky) {
  vec3 land = uSkyLevel / max(skyLevelGround(), 1.0e-9);
  float mu = dot(view, normalize(body)), g2 = MIST_G * MIST_G;
  float phase = (1.0 - g2) / (4.0 * MIST_PI * pow(1.0 + g2 - 2.0 * MIST_G * mu, 1.5));
  vec3 allRound = mix(uSkyLevel, vec3(dot(uSkyLevel, vec3(0.2126, 0.7152, 0.0722))), MIST_NIGHT_GREY * scotopic());
  return MIST_ALBEDO * land * (MIST_PI * phase * skyBodyIrradiance()) + min(MIST_ROUND * allRound, sky);
}



vec3 mistedBeyond(vec3 beyond, vec3 view, float h0, vec3 body) {
  float through = exp(-mistDepth(h0, max(view.y, 1.0e-3), MIST_ENDLESS));
  return beyond * through + mistLight(view, body, beyond) * (1.0 - through);
}
#endif
