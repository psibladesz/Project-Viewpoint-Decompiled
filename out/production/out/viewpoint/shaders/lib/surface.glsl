




#include "constants.glsl"
#include "lib/face_side.glsl"
float hash21(vec2 p) { p = fract(p * vec2(123.34, 456.21)); p += dot(p, p + 45.32); return fract(p.x * p.y); }
vec3 unpackFill(float f) {
  f -= 1.0;
  float r = floor(f / 65536.0), g = floor((f - r * 65536.0) / 256.0);
  return vec3(r, g, f - r * 65536.0 - g * 256.0) / 255.0;
}
vec3 cladding(vec3 base, vec3 local, vec3 n) {
  float along = abs(n.z) > 0.5 ? local.x : local.z;
  float storey = local.y / SQRT6;
  float band = smoothstep(0.0, 0.05, fract(storey)) * smoothstep(1.0, 0.94, fract(storey));
  float seam = smoothstep(0.0, 0.03, abs(fract(along * 2.0) - 0.5) * 0.5);
  float grain = 0.96 + 0.08 * hash21(floor(vec2(along, local.y) * 24.0));
  float shade = mix(0.82, 1.0, band) * mix(0.88, 1.0, seam) * grain * (0.94 + 0.06 * fract(storey));
  return base * shade;
}

vec3 unshaded(vec3 art, int flags) {
  return (flags & FLAG_SHADED_ART) == FLAG_SHADED_ART ? min(art / ROOF_EAST_ART, vec3(1.0)) : art;
}
struct Surface { vec3 albedo; vec3 normal; vec4 light; bool foliage; };

Surface resolve(vec4 c, vec3 local, vec3 rawNormal, float fill, int flags, vec3 pos, vec3 eye, vec3 face) {
  Surface s;
  s.foliage = dot(rawNormal, rawNormal) < 0.6;
  vec3 n = normalize(rawNormal);
  if (!s.foliage) {
    if ((flags & FLAG_SINGLE_SIDED) != 0 && seenFromBehind(n, face, pos, eye)) discard;
    n = litSide(n, face, pos, eye);
  }
  s.normal = n;
  s.albedo = fill > 0.5 ? cladding(unpackFill(fill), local, n) : unshaded(c.rgb, flags);
  s.light = sampleLight(local, s.foliage ? vec3(0.0) : n);
  return s;
}
