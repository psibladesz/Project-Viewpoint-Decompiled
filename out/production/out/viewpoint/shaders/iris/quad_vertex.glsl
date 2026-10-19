


in vec2 aQuad;
vec4 vp_Vertex;
vec3 vp_Normal;
vec4 vp_Color;
vec4 vp_MultiTexCoord0;
vec4 vp_MultiTexCoord1;
const vec4 vp_MultiTexCoordZero = vec4(0.0, 0.0, 0.0, 1.0);
void fetchQuad() {
  vp_Vertex = vec4(aQuad, 0.0, 1.0);
  vp_Normal = vec3(0.0, 0.0, 1.0);
  vp_Color = vec4(1.0);
  vp_MultiTexCoord0 = vec4(aQuad, 0.0, 1.0);
  vp_MultiTexCoord1 = vec4(240.0, 240.0, 0.0, 1.0);
}
