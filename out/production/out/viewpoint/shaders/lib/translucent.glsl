







#ifndef VIEWPOINT_TRANSLUCENT
#define VIEWPOINT_TRANSLUCENT
layout(location = 0) out vec4 translucentSum;
layout(location = 1) out float translucentReveal;

const float WEIGHT_NEAREST = 0.01;

void translucent(vec3 colour, float alpha, float fromEye) {
  float weight = inversesqrt(max(fromEye, WEIGHT_NEAREST));
  translucentSum = vec4(colour * alpha, alpha) * weight;
  translucentReveal = alpha;
}
#endif
