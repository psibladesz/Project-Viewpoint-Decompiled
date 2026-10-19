#version 330




uniform sampler2DArray uPages;
uniform int uLayer;
uniform int uLevel;
uniform vec2 uCorner;
out vec4 fragColor;

void main() {
  ivec2 square = ivec2(gl_FragCoord.xy) - ivec2(uCorner);
  fragColor = texelFetch(uPages, ivec3(square + 1, uLayer), uLevel);
}
