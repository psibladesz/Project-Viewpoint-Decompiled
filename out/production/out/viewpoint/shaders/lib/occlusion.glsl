


#ifndef VIEWPOINT_OCCLUSION
#define VIEWPOINT_OCCLUSION
uniform sampler2D uOcclusion;



vec4 occlusionAt(ivec2 pixel, vec3 n) {
  vec4 o = texelFetch(uOcclusion, pixel, 0);
  vec3 bent = o.xyz * 2.0 - 1.0;
  float squared = dot(bent, bent);
  return vec4(squared > 0.25 ? bent * inversesqrt(squared) : n, o.w);
}



vec3 multiBounce(float visibility, vec3 albedo) {
  vec3 a = 2.0404 * albedo - 0.3324, b = -4.7951 * albedo + 0.6417, c = 2.7552 * albedo + 0.6903;
  return max(vec3(visibility), ((visibility * a + b) * visibility + c) * visibility);
}
#endif
