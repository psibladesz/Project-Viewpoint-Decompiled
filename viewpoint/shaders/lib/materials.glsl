


#ifndef VIEWPOINT_MATERIALS
#define VIEWPOINT_MATERIALS
const float MATERIAL_PAINT = 160.0 / 255.0, MATERIAL_WATER = 0.75;

bool isPlain(float material) { return material < 0.125; }
bool isPaint(float material) { return abs(material - MATERIAL_PAINT) < 0.02; }
bool isWater(float material) { return abs(material - MATERIAL_WATER) < 0.02; }


const float MATERIAL_ROUGH_STEPS = 29.0;
float roughnessMaterial(float roughness) {
  return (1.0 + round(clamp(roughness, 0.0, 1.0) * MATERIAL_ROUGH_STEPS)) / 255.0;
}
float materialRoughness(float material, float plain) {
  float code = material * 255.0;
  return code > 0.5 && code < MATERIAL_ROUGH_STEPS + 1.5 ? (round(code) - 1.0) / MATERIAL_ROUGH_STEPS : plain;
}
bool isFoliage(float material) { return material > 0.875; }


const float MATERIAL_MODEL = 31.0 / 255.0;
bool isModelFace(float material) { return abs(material * 255.0 - 31.0) < 0.5; }
#endif
