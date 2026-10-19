









#ifndef DEFAULT_CLOUD_WEATHER
#define DEFAULT_CLOUD_WEATHER
uniform sampler3D uCloudShape;


const vec4 COVERAGE_CURVE = vec4(0.2247, 0.6041, -0.9085, 0.6041);
const float DECK_COVERAGE = 0.25;

const float CORE_LEAST = 0.35, CORE_DEPTH = 0.4;

const float HIGH_AHEAD = 15000.0;



const float TYPE_SPREAD = 0.25, HEAPED_FROM = 0.25;




const vec2 LAYER_MEAN = vec2(0.31, 0.38);

float clamp01(float v) { return clamp(v, 0.0, 1.0); }
float remap01(float v, float lo, float hi) { return clamp((v - lo) / max(hi - lo, 1.0e-4), 0.0, 1.0); }


float spread(float perlinWorley) { return remap01(perlinWorley, 0.3, 0.7); }


float coverAhead(vec3 p) {
  vec2 low = length(uCloudWind) > 1.0e-3 ? normalize(uCloudWind) : vec2(1.0, 0.0);

  return skyAlong(vec3(p.x - low.x * HIGH_AHEAD, p.y, p.z - low.y * HIGH_AHEAD)).x;
}








vec2 layerWeights(vec2 sky, vec3 p) {
  float fair = spread(textureLod(uCloudShape, vec3(0.37, uCloudTime / 90000.0, 0.61), 0.0).r);
  float front = smoothstep(0.6, 0.95, coverAhead(p));
  float layered = clamp(1.0 - 2.0 * sky.y, -1.0, 1.0);

  return LAYER_MEAN * vec2(1.0 - 0.5 * layered, (1.0 + 0.5 * layered) * (mix(0.5, 1.0, fair) + 1.5 * front));
}





vec2 layerAmounts(float cover, vec2 weights) {
  float least = 0.0, most = 1.0 / max(weights.x, weights.y);
  for (int i = 0; i < 12; i++) {
    float scale = 0.5 * (least + most);
    vec2 a = min(weights * scale, vec2(1.0));
    if (1.0 - (1.0 - a.x) * (1.0 - a.y) < cover) least = scale; else most = scale;
  }

  return min(weights * (0.5 * (least + most)), vec2(1.0));
}


float upperAmount(vec2 sky, vec3 p) { return layerAmounts(sky.x, layerWeights(sky, p)).y; }






float upperType(vec3 p) {
  float billowy = smoothstep(0.35, 0.75, spread(textureLod(uCloudShape, vec3(0.71, uCloudTime / 70000.0, 0.13), 0.0).r));

  return mix(billowy, 0.5, smoothstep(0.6, 0.95, coverAhead(p)));
}






float lowFraction(float cover, float upper, float rainHere) {
  float low = clamp(1.0 - (1.0 - cover) / max(1.0 - upper, 1.0e-3), 0.0, cover);

  return mix(cover > 1.0e-3 ? low / cover : 1.0, 1.0, clamp01(rainHere / 0.1));
}



float typeAt(float type, float v) {
  float spread = TYPE_SPREAD * smoothstep(0.0, HEAPED_FROM, type);

  return clamp(type + spread * (2.0 * v - 1.0), min(type, HEAPED_FROM), 1.0);
}


float coverageFor(float share) {
  float c = COVERAGE_CURVE.x + share * (COVERAGE_CURVE.y + share * (COVERAGE_CURVE.z + share * COVERAGE_CURVE.w));

  return c * smoothstep(0.0, 0.04, share);
}






vec4 weatherOf(float share, float cover, float type, float rainHere, float u) {
  float wet = smoothstep(0.45, 0.8, share);
  float rain = rainHere * wet * mix(CORE_LEAST, 1.0, smoothstep(1.0 - cover, 1.0 - cover + CORE_DEPTH, u));
  float deck = clamp(rainHere / 0.1, 0.0, 1.0) * wet;

  float kind = min(type, mix(0.5, 1.0, share));

  return vec4(coverageFor(share) + DECK_COVERAGE * deck, rain, kind, deck);
}
#endif
