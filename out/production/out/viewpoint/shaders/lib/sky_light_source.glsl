

uniform vec3 uSunTrue;
uniform float uOvercast;
vec3 skyLightSource(vec3 d) {
  return airSky(d, uSunTrue);
}
