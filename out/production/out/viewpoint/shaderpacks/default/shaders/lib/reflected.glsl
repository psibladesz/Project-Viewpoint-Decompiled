





#ifndef DEFAULT_REFLECTED
#define DEFAULT_REFLECTED
#if PIPELINE_REFLECTIONS
uniform sampler2D uReflection;
uniform sampler2D uReflectionDepth;
#endif
#if PIPELINE_REFLECTION_CLOUDS && PIPELINE_CLOUDS && OPTION_CLOUD_STYLE != OPTION_CLOUD_STYLE_LAYERED
#define REFLECTED_CLOUDS 1
#include "lib/clouds.glsl"


const int REFLECTED_SAMPLES = 4;
const float REFLECTED_FOOTPRINT = 60.0, REFLECTED_EXTINCTION = 0.02, CLOUD_SKY = 1.6, CLOUD_BODY = 0.5;
#endif

const float REFLECTION_DEPTH_FALL = 40.0;



vec4 reflectionAt(vec2 uv, float depth) {
#if PIPELINE_REFLECTIONS
  vec2 size = vec2(textureSize(uReflection, 0));
  vec2 p = uv * size - 0.5;
  ivec2 i0 = ivec2(floor(p));
  vec2 f = p - floor(p);
  float near = linearDepth(depth);
  vec4 sum = vec4(0.0);
  float weights = 0.0;
  for (int k = 0; k < 4; k++) {
    ivec2 o = ivec2(k & 1, k >> 1);
    ivec2 at = clamp(i0 + o, ivec2(0), ivec2(size) - 1);
    vec4 s = texelFetch(uReflection, at, 0);
    if (s.a < 0.0) continue;
    vec2 b = mix(1.0 - f, f, vec2(o));
    float d = linearDepth(texelFetch(uReflectionDepth, at, 0).r);
    float w = b.x * b.y * exp(-REFLECTION_DEPTH_FALL * abs(d - near) / max(near, 1.0e-3)) + 1.0e-5;
    sum += s * w;
    weights += w;
  }
  return weights > 0.0 ? sum / weights : vec4(0.0);
#else
  return vec4(0.0);
#endif
}

#if REFLECTED_CLOUDS


vec4 reflectedClouds(vec3 pos, vec3 r) {
  if (r.y < 0.02) return vec4(0.0);
  float alt = pos.y + uSceneOrigin.y;
  vec3 middle = pos + r * ((KIND_SPAN[2].x + KIND_SPAN[2].y) * 0.5 - alt) / r.y;
  vec4 w = weatherAt(middle);
  vec2 span, shape;
  kindOf(w.b, w.a, span, shape);
  float lowest = lowestBase(span), stride = (span.y - lowest) / float(REFLECTED_SAMPLES), depth = 0.0;
  for (int i = 0; i < REFLECTED_SAMPLES; i++) {
    float height = lowest + (float(i) + 0.5) * stride;
    vec3 at = pos + r * ((height - alt) / r.y);
    depth += lowDensity(at, height, lowWeather(at, height), REFLECTED_FOOTPRINT, 0.0) * stride;
  }
  float cover = 1.0 - exp(-depth / r.y * REFLECTED_EXTINCTION);
  vec3 overhead = skyPlain(vec3(0.0, 1.0, 0.0));
  vec3 light = vec3(dot(overhead, vec3(0.2126, 0.7152, 0.0722))) * CLOUD_SKY + uSunColor * uSunStrength * CLOUD_BODY;
  return vec4(light * cover, cover);
}
#endif


vec3 reflectedSky(vec3 pos, vec3 r, float seen) {
  vec3 up = r.y < 0.0 ? normalize(vec3(r.x, 0.0, r.z) + vec3(0.0, 1.0e-3, 0.0)) : r;
  vec3 sky = skyColor(up, vec3(0.0), vec3(0.0), uSunDir, uSunColor, uSunStrength);
#if REFLECTED_CLOUDS
  vec4 cloud = reflectedClouds(pos, up);
  sky = sky * (1.0 - cloud.a) + cloud.rgb;
#endif
  return sky * SKY_BRIGHTNESS * seen;
}



vec3 reflectedLight(vec3 pos, vec3 r, vec2 uv, float depth, float seen) {
  vec4 traced = reflectionAt(uv, depth);
  return traced.a >= 0.999 ? traced.rgb : traced.rgb + (1.0 - traced.a) * reflectedSky(pos, r, seen);
}



vec3 mirrorSpecular(vec3 pos, vec3 n, vec3 v, float rough, float f0, vec2 uv, float depth, float seen) {
  float m = 1.0 - max(dot(n, v), 0.0), m2 = m * m;
  float f = f0 + (max(1.0 - rough, f0) - f0) * m2 * m2 * m;
  return reflectedLight(pos, reflect(-v, n), uv, depth, seen) * f;
}
#endif
