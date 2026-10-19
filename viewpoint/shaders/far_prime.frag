#version 330


uniform sampler2D uMainDepth;
void main() {
  if (texelFetch(uMainDepth, ivec2(gl_FragCoord.xy), 0).r >= 1.0) discard;
}
