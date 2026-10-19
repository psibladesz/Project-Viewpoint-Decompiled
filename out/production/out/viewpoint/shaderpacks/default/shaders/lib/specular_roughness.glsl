


#ifndef DEFAULT_SPECULAR_ROUGHNESS
#define DEFAULT_SPECULAR_ROUGHNESS
#include "lib/materials.glsl"
uniform float uWet;

const float F0_PLAIN = 0.04, F0_WATER = 0.02;


const float ROUGH_PLAIN = 0.75, ROUGH_PAINTED = 0.6, ROUGH_FOLIAGE = 0.6, ROUGH_WATER = 0.08;
const float ROUGH_LEAST = 0.08;


const float F0_COAT = 0.04, ROUGH_COAT = 0.1, ROUGH_COAT_WET = 0.03, COAT_DRY = 0.5;

float surfaceRoughness(float material) {
  float rough = isWater(material) ? ROUGH_WATER : material > 0.875 ? ROUGH_FOLIAGE : material > 0.375 ? ROUGH_PAINTED
      : materialRoughness(material, ROUGH_PLAIN);
  return max(rough, ROUGH_LEAST);
}

float surfaceF0(float material) {
  return isWater(material) ? F0_WATER : F0_PLAIN;
}


float coatWet(float outdoors, float skyShare) {
  return uWet * step(0.5, outdoors) * step(0.5, skyShare);
}

float coatRoughness(float wet) {
  return mix(ROUGH_COAT, ROUGH_COAT_WET, wet);
}
#endif
