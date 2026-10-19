





#include "lib/hsv.glsl"
#include "lib/lanczos.glsl"
in vec2 vUv;
in vec2 vUv2;
uniform sampler2D uTexture;
uniform int uModelFilter;
uniform sampler2D uRustMap;
uniform sampler2D uZones;
uniform sampler2D uLamps;
uniform sampler2D uDamage1Overlay;
uniform sampler2D uDamage1Shell;
uniform sampler2D uDamage2Overlay;
uniform sampler2D uDamage2Shell;


uniform mat4 uSwitches[12];
uniform float uRust;
uniform vec3 uPaint;
uniform int uKind;
flat in vec4 vTint;
struct Vehicle {
  vec3 colour;
  float window;
  float gone;
  float paint;
  vec4 lamp;
};

vec3 zone(int z) {
  const vec3 ZONES[27] = vec3[27](
    vec3(1.0, 0.0, 0.0), vec3(0.0, 1.0, 0.0), vec3(0.0, 1.0, 1.0), vec3(1.0, 1.0, 0.0),
    vec3(1.0, 0.0, 1.0), vec3(0.0, 0.0, 1.0), vec3(0.0, 0.5, 0.5), vec3(0.5, 0.5, 0.0),
    vec3(0.5, 0.0, 0.5), vec3(0.0, 0.0, 0.5), vec3(0.5, 0.0, 0.0), vec3(0.0, 0.5, 0.0),
    vec3(0.0, 0.75, 0.75), vec3(0.75, 0.75, 0.0), vec3(0.75, 0.0, 0.75), vec3(0.0, 0.0, 0.75),
    vec3(0.0, 0.0, 0.0), vec3(0.25, 0.0, 0.0), vec3(0.75, 0.0, 0.0), vec3(0.0, 0.75, 0.0),
    vec3(0.0, 0.25, 0.0), vec3(0.5, 0.25, 0.0), vec3(0.5, 0.75, 0.0), vec3(0.75, 0.75, 0.75),
    vec3(0.25, 0.25, 0.25), vec3(1.0, 0.0, 0.5), vec3(0.0, 1.0, 0.5));
  const vec3 GUARDS[4] = vec3[4](vec3(0.75, 0.0, 0.75), vec3(0.0, 0.0, 0.75), vec3(0.0, 0.75, 0.75), vec3(0.75, 0.75, 0.0));
  return uKind == 0 && z >= 12 && z < 16 ? GUARDS[z - 12] : ZONES[z];
}

float vehicleWindow() {
  vec3 m = texture(uZones, vUv).rgb;
  float w = 0.0;
  for (int z = 6; z < 12; z++) w += 1.0 - step(0.01, length(m - zone(z)));
  return clamp(w, 0.0, 1.0);
}

float switched(int k, mat4 zones1, mat4 zones2) {
  mat4 a = transpose(uSwitches[k]), b = transpose(uSwitches[k + 1]);
  float sum = 0.0;
  for (int c = 0; c < 4; c++) sum += dot(zones1[c], a[c]) + dot(zones2[c], b[c]);
  return sum;
}

vec3 painted(vec3 col, vec3 paint) {
  vec3 hsv = rgb2hsv(col);
  hsv.x = paint.x;
  hsv.y = clamp(hsv.y + paint.y - 0.5, 0.0, 0.9999);
  hsv.z = clamp(hsv.z + paint.z - 0.5, 0.0, 0.9999);
  return hsv2rgb(mod(hsv, 1.0));
}

vec3 blood(vec4 overlay, vec4 mask, float intensity, float alpha, vec3 col) {
  float i = clamp(intensity, 0.0, 1.0);
  if (i < 0.0001) return col;
  float a = (1.0 - pow(1.0 - overlay.a, 3.0)) * (1.0 - pow(1.0 - mask.a, 3.0));
  a = clamp((a - (1.0 - i)) / i, 0.0, 1.0);
  return mix(col, overlay.rgb * a, a * overlay.a * alpha);
}



vec4 zonesAt(vec3 m, int first) {
  vec4 on = vec4(0.0);
  for (int k = 0; k < 4; k++) if (first + k < 27) on[k] = 1.0 - step(0.01, length(m - zone(first + k)));
  return on;
}
Vehicle vehicle() {
  vec2 size = vec2(textureSize(uTexture, 0));
  vec2 dx = dFdx(vUv), dy = dFdy(vUv);
  vec4 tex = uModelFilter == FILTER_LANCZOS && magnified(size, dx, dy)
      ? lanczos(uTexture, vUv, ivec2(0), ivec2(size) - 1, true)
      : uModelFilter == FILTER_SHARP ? textureGrad(uTexture, sharpUv(vUv, size, dx, dy), dx, dy) : texture(uTexture, vUv);
  vec2 uv2 = uKind == 0 ? vUv : vUv2;
  vec4 mask = texture(uZones, vUv);
  vec4 rust = texture(uRustMap, uv2);
  vec4 lights = texture(uLamps, vUv);
  vec4 overlay1 = texture(uDamage1Overlay, uv2), shell1 = texture(uDamage1Shell, uv2);
  vec4 overlay2 = texture(uDamage2Overlay, uv2), shell2 = texture(uDamage2Shell, uv2);
  mat4 zones1 = mat4(zonesAt(mask.rgb, 0), zonesAt(mask.rgb, 4), zonesAt(mask.rgb, 8), zonesAt(mask.rgb, 12));
  mat4 zones2 = mat4(zonesAt(mask.rgb, 16), zonesAt(mask.rgb, 20), zonesAt(mask.rgb, 24), vec4(0.0));
  float gone = step(0.5, switched(0, zones1, zones2));
  float lit = step(0.5, switched(2, zones1, zones2));
  float damage1 = step(0.5, switched(4, zones1, zones2));
  float damage2 = step(0.5, switched(6, zones1, zones2));
  float window = clamp(zones1[1][2] + zones1[1][3] + zones1[2][0] + zones1[2][1] + zones1[2][2] + zones1[2][3], 0.0, 1.0);
  vec3 col = tex.rgb;
  if (uKind != 2) col = mix(col, painted(col, uPaint), 1.0 - tex.a);
  col = mix(col, rust.rgb, rust.a * uRust);
  if (uKind == 0) {
    col = mix(col, painted(shell1.rgb, uPaint), shell1.a * damage1);
    col = mix(col, overlay1.rgb, overlay1.a * damage1);
    col = mix(col, painted(shell2.rgb, uPaint), shell2.a * damage2);
    col = mix(col, overlay2.rgb, overlay2.a * damage2);
  } else {
    vec3 paint = uKind == 1 ? uPaint : rgb2hsv(tex.rgb);
    float front = clamp(zones1[0][0] + zones2[0][1] + zones2[0][2], 0.0, 1.0);
    float tail = clamp(zones1[0][1] + zones2[0][3] + zones2[1][0] + zones2[1][1] + zones2[1][2], 0.0, 1.0);
    float bare = clamp(window + front + tail, 0.0, 1.0);
    float intensity = switched(8, zones1, zones2);
    float bloody = step(0.5, switched(10, zones1, zones2));
    col = blood(overlay2, overlay1, intensity, bloody * window, col);
    col = mix(col, painted(shell1.rgb, paint), shell1.a * 0.75 * damage1 * (1.0 - bare));
    col = mix(col, shell1.rgb, shell1.a * damage1 * bare);
    col = mix(col, painted(shell2.rgb, paint), shell2.a * 0.75 * damage2 * (1.0 - bare));
    col = mix(col, shell2.rgb, shell2.a * damage2 * bare);
    col = blood(overlay2, overlay1, intensity, bloody * (1.0 - window), col);
  }
  float paint = (1.0 - tex.a) * (1.0 - rust.a * uRust) * (1.0 - window) * (1.0 - gone);
  paint *= (1.0 - shell1.a * damage1) * (1.0 - shell2.a * damage2);
  col = mix(col, vec3(0.2), gone);
  Vehicle v;
  v.colour = col * desaturate(vTint.rgb, 0.3);
  v.window = window;
  v.gone = gone;
  v.paint = paint;
  v.lamp = vec4(lights.rgb, lights.a * lit);
  return v;
}
