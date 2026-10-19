#version 330

in vec3 vColour;
out vec4 fragColor;
void main() {
  fragColor = vec4(vColour, 1.0);
}
