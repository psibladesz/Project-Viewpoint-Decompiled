



uniform float uIrisLampColours;
uniform float uIrisFlashLevel;
uniform int uTorchCount;
uniform vec4 uTorchPos[8];
uniform vec4 uTorchDir[8];
uniform vec3 uTorchColor[8];
in vec2 vLevels;
vec3 blockLight;
vec2 levels;

vec3 torchLight(vec3 pos, vec3 n) {
  vec3 sum = vec3(0.0);
  for (int i = 0; i < uTorchCount; i++) {
    vec3 d = pos - uTorchPos[i].xyz;
    float dist = length(d), reach = uTorchPos[i].w;
    if (dist >= reach) continue;
    vec3 l = d / max(dist, 1.0e-4);
    float edge = uTorchDir[i].w;
    float beam = edge <= -1.0 ? 1.0 : smoothstep(edge, mix(edge, 1.0, 0.3), dot(l, uTorchDir[i].xyz));
    sum += uTorchColor[i] * beam * (0.35 + 0.65 * max(dot(n, -l), 0.0)) * (1.0 - dist / reach);
  }
  return sum;
}




float heldLevel(vec3 held) { return 1.0 - exp(-max(held.r, max(held.g, held.b))); }


vec2 lightmapDelta() { return (levels - vLevels) / 15.0; }


vec3 vp_lampTint(vec3 packColour) {
  float strength = max(blockLight.r, max(blockLight.g, blockLight.b));
  if (strength <= 1.0e-4 || uIrisLampColours <= 0.0) return packColour;
  vec3 hue = blockLight / strength;
  const vec3 luma = vec3(0.2126, 0.7152, 0.0722);
  vec3 tinted = hue * dot(packColour, luma) / max(dot(hue, luma), 1.0e-3);
  return mix(packColour, tinted, uIrisLampColours * smoothstep(0.0, 0.05, strength));
}


vec4 tintOutput() {
  float strength = max(blockLight.r, max(blockLight.g, blockLight.b));
  return strength <= 1.0e-4 ? vec4(0.0) : vec4(blockLight / strength, smoothstep(0.0, 0.05, strength));
}
