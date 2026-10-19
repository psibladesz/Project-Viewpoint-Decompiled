



#include "constants.glsl"
layout(location = 0) in vec3 aPosition;
layout(location = 1) in vec4 aColor;
layout(location = 2) in vec2 aTexCoord;
layout(location = 3) in vec3 aNormal;
uniform vec2 uIrisLevels;
uniform vec3 uIrisEye;
uniform mat4 uRainMatrix;
uniform sampler2D uRainMap;
vec4 vp_Vertex;
vec3 vp_Normal;
vec4 vp_Color;
vec4 vp_MultiTexCoord0;
vec4 vp_MultiTexCoord1;
const vec4 vp_MultiTexCoordZero = vec4(0.0, 0.0, 0.0, 1.0);
vec4 vp_entity;
vec4 vp_midTexCoord;
vec4 vp_tangent;
vec4 vp_midBlock;
vec3 vp_velocity;
bool hiddenVertex;

void fetchWeather() {
  vec3 column = vec3(-aNormal.x, aNormal.y, -aNormal.z) + uIrisEye;
  vec3 m = (uRainMatrix * vec4(column, 1.0)).xyz;
  bool inMap = all(greaterThanEqual(m.xy, vec2(0.0))) && all(lessThanEqual(m.xy, vec2(1.0)));
  hiddenVertex = inMap && m.z > texture(uRainMap, m.xy).r + 1.0e-4;
  vp_Vertex = vec4(aPosition, 1.0);
  vp_Normal = vec3(0.0, 1.0, 0.0);
  vp_Color = aColor;
  vp_MultiTexCoord0 = vec4(aTexCoord, 0.0, 1.0);
  vp_MultiTexCoord1 = vec4(uIrisLevels * 16.0, 0.0, 1.0);
  vp_entity = vec4(-1.0, -1.0, 0.0, 0.0);
  vp_midTexCoord = vec4(0.5, 0.5, 0.0, 1.0);
  vp_tangent = vec4(1.0, 0.0, 0.0, 1.0);
  vp_midBlock = vec4(0.0);
  vp_velocity = vec3(0.0);
}
