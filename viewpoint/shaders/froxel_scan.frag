#version 330





in vec2 vUv;
uniform sampler2D uSource;
uniform int uOffset;
out vec4 fragColor;
#include "lib/froxels.glsl"

void main() {
  ivec2 texel = ivec2(gl_FragCoord.xy);
  ivec3 f = froxelAt(texel);
  if (f.z >= int(uFroxels.z)) {
    fragColor = vec4(0.0, 0.0, 0.0, 1.0);
    return;
  }
  vec4 here = texelFetch(uSource, texel, 0);
  if (uOffset == 0) {
    float through = exp(-here.a * (froxelDistance(float(f.z) + 1.0) - froxelDistance(float(f.z))));
    fragColor = vec4(here.rgb * (1.0 - through), through);
    return;
  }
  if (f.z < uOffset) {
    fragColor = here;
    return;
  }
  vec4 nearer = texelFetch(uSource, froxelTexel(ivec3(f.xy, f.z - uOffset)), 0);
  fragColor = vec4(nearer.rgb + nearer.a * here.rgb, nearer.a * here.a);
}
