#version 330




uniform sampler2D uInputA;
uniform sampler2D uInputB;
uniform float uSquaresAMetre;
uniform float uWhitecap;
out vec4 fragColor;
void main() {
  ivec2 p = ivec2(gl_FragCoord.xy);
  float sign = ((p.x + p.y) & 1) == 0 ? 1.0 : -1.0;
  vec4 a = texelFetch(uInputA, p, 0) * sign, b = texelFetch(uInputB, p, 0) * sign;
  float height = a.x, slopeX = a.y, slopeZ = a.z, stretchX = a.w, stretchZ = b.x, shear = b.y;
  vec2 slope = vec2(slopeX, slopeZ) / (1.0 + abs(vec2(stretchX, stretchZ)));

  float jacobian = (1.0 - stretchX) * (1.0 - stretchZ) - shear * shear;
  fragColor = vec4(slope, height * uSquaresAMetre, clamp((uWhitecap - jacobian) * 2.0, 0.0, 1.0));
}
