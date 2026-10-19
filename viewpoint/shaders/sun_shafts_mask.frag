#version 330





in vec2 vUv;
uniform sampler2D uColor;
uniform sampler2D uFarDepth;
uniform float uFarOn;
uniform vec3 uBody;
uniform float uLobe;
uniform float uCap;
out vec4 fragColor;
#include "lib/camera.glsl"

const int SCALE = 4;

void main() {
  ivec2 first = ivec2(gl_FragCoord.xy) * SCALE;
  vec2 size = vec2(textureSize(uColor, 0));
  vec3 sum = vec3(0.0);
  for (int y = 0; y < SCALE; y++) {
    for (int x = 0; x < SCALE; x++) {
      ivec2 pixel = first + ivec2(x, y);
      vec2 uv = (vec2(pixel) + 0.5) / size;
      bool sky = texelFetch(uDepth, pixel, 0).r >= 1.0 && (uFarOn < 0.5 || texture(uFarDepth, uv).r >= 1.0);
      if (!sky) continue;
      vec3 c = texelFetch(uColor, pixel, 0).rgb;
      float l = dot(c, vec3(0.2126, 0.7152, 0.0722));
      sum += c * min(1.0, uCap / max(l, 1.0e-6)) * pow(max(dot(normalize(scenePos(uv, 1.0) - uEye), uBody), 0.0), uLobe);
    }
  }
  fragColor = vec4(sum / float(SCALE * SCALE), 1.0);
}
