




#include "lib/cloud_blocks.glsl"
uniform sampler2D uCloudShadow;

uniform vec4 uCloudShadowPlace;
float cloudShadow(vec3 pos) {
#if !PIPELINE_CLOUDS
  return 1.0;
#else
  if (uSunDir.y <= 0.0) return 1.0;
#if OPTION_CLOUD_STYLE == OPTION_CLOUD_STYLE_LAYERED
  return blockShadow(pos);
#else
  vec2 plane = pos.xz + uSunDir.xz * ((uCloudShadowPlace.z - pos.y) / uSunDir.y);
  return texture(uCloudShadow, (plane - uCloudShadowPlace.xy) * uCloudShadowPlace.w).r;
#endif
#endif
}
