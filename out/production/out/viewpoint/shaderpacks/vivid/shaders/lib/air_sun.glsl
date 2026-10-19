


uniform float uVolumeSun;
uniform vec3 uGlowDir;


const float SHAFT_LOW_GAIN = 2.5;
const float SHAFT_LOW_SATURATION = 0.6;
const float SHAFT_LOW_FULL = 0.1;
const float SHAFT_LOW_GONE = 0.5;
const vec3 SHAFT_LUMA = vec3(0.2126, 0.7152, 0.0722);
vec3 airSunLight(vec3 dir) {
  float mu = dot(dir, uGlowDir);
  float low = 1.0 - smoothstep(SHAFT_LOW_FULL, SHAFT_LOW_GONE, uGlowDir.y);
  vec3 colour = max(mix(vec3(dot(uSunColor, SHAFT_LUMA)), uSunColor, 1.0 + SHAFT_LOW_SATURATION * low), 0.0);
  float gain = mix(1.0, SHAFT_LOW_GAIN, low);
  return colour * uSunStrength * uVolumeSun * gain * mix(hg(mu, 0.2), hg(mu, 0.65), 0.4);
}
