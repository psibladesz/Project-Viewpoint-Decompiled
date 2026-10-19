




uniform float uVolumeSun;
uniform vec3 uGlowDir;
vec3 airSunLight(vec3 dir) {
  float mu = dot(dir, uGlowDir);
  return uSunColor * uSunStrength * uVolumeSun * mix(hg(mu, 0.2), hg(mu, 0.65), 0.4);
}
