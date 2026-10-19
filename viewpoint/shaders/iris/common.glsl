

uniform mat4 vp_ModelViewMatrix;
uniform mat4 vp_ProjectionMatrix;
uniform mat4 vp_ModelViewProjectionMatrix;
uniform mat3 vp_NormalMatrix;
uniform mat4 vp_TextureMatrix[3];
uniform mat4 vp_ModelViewMatrixInverse;
uniform mat4 vp_ProjectionMatrixInverse;
struct vp_FogParameters {
  vec4 color;
  float density;
  float start;
  float end;
  float scale;
};
uniform vp_FogParameters vp_Fog;
