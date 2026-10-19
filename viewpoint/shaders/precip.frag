#version 330



#include "lib/precip_look.glsl"
in vec2 vLocal;
in float vFade;
in vec3 vColor;
in vec3 vGlint;
in float vSize;
flat in vec2 vAcross;
flat in float vSeed;
uniform float uMode;
out vec4 fragColor;
void main() { fragColor = precipLook(uMode, vLocal, vFade, vColor, vGlint, vSize, vAcross, vSeed, gl_FragCoord.xy); }
