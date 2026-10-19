#version 330


uniform int uGlass;
#include "lib/vehicle.glsl"
void main() {
  if (uGlass == 0 && vehicleWindow() > 0.5) discard;
}
