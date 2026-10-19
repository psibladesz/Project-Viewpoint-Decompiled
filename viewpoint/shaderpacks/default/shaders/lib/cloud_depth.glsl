


#ifndef VIEWPOINT_SCENE_ORIGIN
#define VIEWPOINT_SCENE_ORIGIN
uniform vec3 uSceneOrigin;
#endif
const float DEPTH_HEIGHT = 1700.0, NEAR_DEPTH = 300.0, FAR_DEPTH = 50000.0;
float cloudDepth(vec3 r) {
  float t = (DEPTH_HEIGHT - uSceneOrigin.y - uEye.y) / (abs(r.y) > 1.0e-4 ? r.y : 1.0e-4);

  return t > 0.0 ? clamp(t, NEAR_DEPTH, FAR_DEPTH) : FAR_DEPTH;
}
