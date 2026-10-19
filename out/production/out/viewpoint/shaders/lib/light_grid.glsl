






#ifndef VIEWPOINT_LIGHT_GRID
#define VIEWPOINT_LIGHT_GRID
#include "constants.glsl"
uniform sampler2D uLight;
uniform vec2 uLightAtlasSize;
#ifndef VIEWPOINT_SKY_LEVEL
#define VIEWPOINT_SKY_LEVEL
uniform vec3 uSkyLevel;
#endif


vec4 gridLight(vec2 origin, vec2 p) {
  vec2 cell = floor(p), f = p - cell;
  vec2 t = origin + vec2(cell.x * 2.0 + 0.5 + f.x, cell.y * 2.0 + 0.5 + f.y);
  vec4 light = texture(uLight, t / uLightAtlasSize);
  float darker = texture(uLight, (t + vec2(0.0, LIGHT_GRID.y * 0.5)) / uLightAtlasSize).r;
  return vec4(max(light.rgb, uSkyLevel - darker), light.a);
}


float gridSky(vec2 origin, vec2 p) {
  vec2 cell = floor(p), f = p - cell;
  vec2 t = origin + vec2(cell.x * 2.0 + 0.5 + f.x, cell.y * 2.0 + 0.5 + f.y);
  float darker = texture(uLight, (t + vec2(0.0, LIGHT_GRID.y * 0.5)) / uLightAtlasSize).r * 255.0;
  return clamp(1.0 - darker / (SKY_STEP * 15.0), 0.0, 1.0);
}


bool gridOpenSky(vec2 origin, vec2 p) {
  vec2 cell = floor(p), f = p - cell;
  vec2 t = origin + vec2(cell.x * 2.0 + 0.5 + f.x, cell.y * 2.0 + 0.5 + f.y);
  return texture(uLight, (t + vec2(0.0, LIGHT_GRID.y * 0.5)) / uLightAtlasSize).r * 255.0 < SKY_STEP * 0.5;
}
#endif
