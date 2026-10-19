



uniform int uMeter;


const float METER_SKY = 0.5;
const float METER_EDGE = 0.4;
const float METER_FALLOFF = 2.0;
vec4 meterValue(vec3 color, float depth, vec2 uv) {
  float luminance = dot(max(color, vec3(0.0)), vec3(0.2126, 0.7152, 0.0722));
  vec2 off = uv * 2.0 - 1.0;
  float weight = mix(METER_EDGE, 1.0, exp(-METER_FALLOFF * dot(off, off))) * (depth < 1.0 ? 1.0 : METER_SKY);
  return vec4(log2(max(luminance, 1.0e-12)), weight, 0.0, 1.0);
}
