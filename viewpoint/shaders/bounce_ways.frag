#version 330









in vec2 vUv;
uniform sampler2D uCascade0;
uniform sampler2D uCascade0Walls;
out vec4 fragColor;
#include "lib/bounce_cascades.glsl"



const float WALL_UP = 2.5;

void main() {
  int tile = int(gl_FragCoord.x) / CASCADE_SQUARES;
  ivec2 cell = ivec2(int(gl_FragCoord.x) - tile * CASCADE_SQUARES, int(gl_FragCoord.y) % CASCADE_SQUARES);
  vec3 q[4], walls = vec3(0.0);
  for (int d = 0; d < 4; d++) {
    q[d] = texelFetch(uCascade0, cascadeTexel(0, cell, d), 0).rgb;
    walls += texelFetch(uCascade0Walls, cascadeTexel(0, cell, d), 0).rgb;
  }


  vec3 e = tile == 0 ? q[1] + q[2]
      : tile == 1 ? q[0] + q[3]
      : tile == 2 ? walls * WALL_UP
      : tile == 3 ? q[0] + q[1] + q[2] + q[3]
      : tile == 4 ? q[2] + q[3]
      : tile == 5 ? q[0] + q[1]
      : vec3(0.0);
  fragColor = vec4(e, 1.0);
}
