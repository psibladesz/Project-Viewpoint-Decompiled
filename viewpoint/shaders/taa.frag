#version 330


















in vec2 vUv;
uniform sampler2D uCurrent;
uniform sampler2D uHistory;
uniform mat4 uUnproject;
uniform mat4 uPrevious;
uniform vec2 uTexel;
uniform vec2 uHistoryTexel;
uniform vec2 uJitter;
uniform float uHistoryValid;
uniform float uFeedback;
uniform sampler2D uFarDepth;
uniform mat4 uFarUnproject;
uniform vec2 uFarRange;
uniform sampler2D uVelocity;
uniform sampler2D uHistoryCount;
uniform int uCounts;
layout(location = 0) out vec4 fragColor;
layout(location = 1) out float fragCount;
#include "lib/camera.glsl"


const ivec2 OFFSETS[9] = ivec2[9](ivec2(0, 0), ivec2(0, -1), ivec2(-1, 0), ivec2(1, 0), ivec2(0, 1),
                                  ivec2(-1, -1), ivec2(1, -1), ivec2(-1, 1), ivec2(1, 1));




const float GAMMA_STILL = 1.5, GAMMA_MOVING = 1.25, PIXEL_MOTION = 1.0;





const float STILL_SHARE = 0.03, SPEED_PIXELS = 8.0, STILL_FRAMES = 32.0;

vec3 toYCoCg(vec3 c) { return vec3(dot(c, vec3(0.25, 0.5, 0.25)), dot(c, vec3(0.5, 0.0, -0.5)), dot(c, vec3(-0.25, 0.5, -0.25))); }
vec3 toRgb(vec3 c) { return vec3(c.x + c.y - c.z, c.x + c.z, c.x - c.y - c.z); }
float max3(vec3 c) { return max(c.r, max(c.g, c.b)); }

vec3 weigh(vec3 c) { return c / (1.0 + max3(c)); }
vec3 unweigh(vec3 c) { return c / max(1.0 - max3(c), 1.0 / 65504.0); }


float lanczos2(float x) {
  float x2 = x * x, a = x2 - 4.0, b = x2 * a - a;
  a *= a;
  return b * a / 64.0;
}



float lanczos2(float x, float scale) { return lanczos2(scale > 1.0 ? clamp(x * scale, -2.0, 2.0) : x); }


vec3 catmullRom(sampler2D tex, vec2 uv, vec2 texel) {
  vec2 size = 1.0 / texel, p = uv * size, p1 = floor(p - 0.5) + 0.5, f = p - p1, f2 = f * f, f3 = f2 * f;
  vec2 w0 = f2 - 0.5 * (f3 + f), w1 = 1.5 * f3 - 2.5 * f2 + 1.0, w3 = 0.5 * (f3 - f2), w2 = 1.0 - w0 - w1 - w3;
  vec2 w12 = w1 + w2;
  vec2 t0 = (p1 - 1.0) * texel, t3 = (p1 + 2.0) * texel, t12 = (p1 + w2 / w12) * texel;
  float k0 = w12.x * w0.y, k1 = w0.x * w12.y, k2 = w12.x * w12.y, k3 = w3.x * w12.y, k4 = w12.x * w3.y;
  vec3 s0 = texture(tex, vec2(t12.x, t0.y)).rgb, s1 = texture(tex, vec2(t0.x, t12.y)).rgb;
  vec3 s2 = texture(tex, t12).rgb, s3 = texture(tex, vec2(t3.x, t12.y)).rgb, s4 = texture(tex, vec2(t12.x, t3.y)).rgb;
  vec3 r = (k0 * s0 + k1 * s1 + k2 * s2 + k3 * s3 + k4 * s4) / (k0 + k1 + k2 + k3 + k4);
  return clamp(r, min(min(min(s0, s1), min(s2, s3)), s4), max(max(max(s0, s1), max(s2, s3)), s4));
}


vec3 clipToBox(vec3 lo, vec3 hi, vec3 current, vec3 history) {
  if (all(greaterThanEqual(history, lo)) && all(lessThanEqual(history, hi))) return history;
  vec3 r = current - history, ir = 1.0 / (1.0e-4 + r);
  vec3 entry = min((hi - history) * ir, (lo - history) * ir);
  return history + r * clamp(max3(entry), 0.0, 1.0);
}

float farLinear(float z) { z = z * 2.0 - 1.0; return 2.0 * uFarRange.x * uFarRange.y / (uFarRange.y + uFarRange.x - z * (uFarRange.y - uFarRange.x)); }

void main() {
  ivec2 size = textureSize(uCurrent, 0), historySize = textureSize(uHistory, 0);
  float scale = float(historySize.x) / float(size.x);
  vec2 pixel = vUv * vec2(size);

  vec2 nearestSample = floor(pixel - uJitter) + 0.5;
  vec2 dc = nearestSample + uJitter - pixel;
  ivec2 at = ivec2(nearestSample);
  vec3 s[9];
  for (int i = 0; i < 9; i++) s[i] = texelFetch(uCurrent, clamp(at + OFFSETS[i], ivec2(0), size - 1), 0).rgb;
  vec3 box5Lo = min(s[0], min(min(s[1], s[2]), min(s[3], s[4]))), box5Hi = max(s[0], max(max(s[1], s[2]), max(s[3], s[4])));
  vec3 box9Lo = min(box5Lo, min(min(s[5], s[6]), min(s[7], s[8]))), box9Hi = max(box5Hi, max(max(s[5], s[6]), max(s[7], s[8])));
  float wx0 = lanczos2(dc.x - 1.0, scale), wx1 = lanczos2(dc.x, scale), wx2 = lanczos2(dc.x + 1.0, scale);
  float wy0 = lanczos2(dc.y - 1.0, scale), wy1 = lanczos2(dc.y, scale), wy2 = lanczos2(dc.y + 1.0, scale);
  vec3 filtered = (s[5] * wx0 + s[1] * wx1 + s[6] * wx2) * wy0 + (s[2] * wx0 + s[0] * wx1 + s[3] * wx2) * wy1
                + (s[7] * wx0 + s[4] * wx1 + s[8] * wx2) * wy2;
  float sum = (wx0 + wx1 + wx2) * (wy0 + wy1 + wy2);

  float confidence = scale > 1.0 ? clamp(sum, 0.0, 1.0) : 1.0;
  filtered = abs(sum) > 1.0e-3 ? filtered / sum : s[0];

  filtered = clamp(filtered, box9Lo, box9Hi);

  float ownDepth = texture(uDepth, vUv).r;
  bool farOn = uFarRange.x > 0.0;
  float ownFar = farOn ? texture(uFarDepth, vUv).r : 1.0;
  bool far = ownDepth >= 1.0 && ownFar < 1.0;
  float distanceNow = far ? farLinear(ownFar) : linearDepth(ownDepth);
  fragCount = confidence;
  if (uHistoryValid < 0.5) { fragColor = vec4(filtered, distanceNow); return; }
  float nearest = 1.0, nearestFar = 1.0;
  vec2 nearestUv = vUv;
  for (int i = 0; i < 9; i++) {
    vec2 uv = vUv + vec2(OFFSETS[i]) * uTexel;
    float d = texture(uDepth, uv).r;
    if (d < nearest) { nearest = d; nearestUv = uv; }
    if (farOn) nearestFar = min(nearestFar, texture(uFarDepth, uv).r);
  }



  vec4 scene = nearest < 1.0 || nearestFar >= 1.0 ? uUnproject * vec4(vUv * 2.0 - 1.0, nearest * 2.0 - 1.0, 1.0)
                                                  : uFarUnproject * vec4(vUv * 2.0 - 1.0, nearestFar * 2.0 - 1.0, 1.0);
  vec4 previous = uPrevious * vec4(scene.xyz / scene.w, 1.0);
  vec2 previousUv = previous.xy / previous.w * 0.5 + 0.5;
  float previousDistance = previous.w;
  vec4 motion = texture(uVelocity, nearestUv);
  if (motion.w > 0.5) { previousUv = vUv - motion.xy; previousDistance = motion.z; }
  if (any(lessThan(previousUv, vec2(0.0))) || any(greaterThan(previousUv, vec2(1.0)))) { fragColor = vec4(filtered, distanceNow); return; }




  ivec2 h = clamp(ivec2(previousUv * vec2(historySize)), ivec2(1), historySize - 2);
  float keptLo = 1.0e9, keptHi = 0.0;
  for (int i = 0; i < 9; i++) {
    float kept = texelFetch(uHistory, h + OFFSETS[i], 0).a;
    keptLo = min(keptLo, kept);
    keptHi = max(keptHi, kept);
  }
  float outside = max(max(keptLo - previousDistance, previousDistance - keptHi), 0.0) / max(previousDistance, 0.1);
  float trust = ownDepth >= 1.0 && !far ? 1.0 : 1.0 - smoothstep(0.03, 0.12, outside);

  vec3 c = toYCoCg(filtered), history = toYCoCg(catmullRom(uHistory, previousUv, uHistoryTexel));
  for (int i = 0; i < 9; i++) s[i] = toYCoCg(s[i]);
  box5Lo = min(s[0], min(min(s[1], s[2]), min(s[3], s[4]))); box5Hi = max(s[0], max(max(s[1], s[2]), max(s[3], s[4])));
  box9Lo = min(box5Lo, min(min(s[5], s[6]), min(s[7], s[8]))); box9Hi = max(box5Hi, max(max(s[5], s[6]), max(s[7], s[8])));
  vec3 m1 = s[0] + s[1] + s[2] + s[3] + s[4], m2 = s[0] * s[0] + s[1] * s[1] + s[2] * s[2] + s[3] * s[3] + s[4] * s[4];
  vec3 mean = m1 / 5.0, deviation = sqrt(abs(m2 / 5.0 - mean * mean));
  float moving = clamp(length((vUv - previousUv) * vec2(historySize)) / PIXEL_MOTION, 0.0, 1.0), gamma = mix(GAMMA_STILL, GAMMA_MOVING, moving);
  vec3 lo = max(mix(box5Lo, box9Lo, 0.5), mean - gamma * deviation);
  vec3 hi = min(mix(box5Hi, box9Hi, 0.5), mean + gamma * deviation);
  history = clipToBox(lo, hi, c, history);

  float lumaDiff = 1.0 - abs(c.x - history.x) / (0.001 + max(c.x, history.x));
  float speed = clamp(length((vUv - previousUv) * vec2(historySize)) / SPEED_PIXELS, 0.0, 1.0);
  float damped = mix(lumaDiff * lumaDiff, 1.0, moving);
  float share = mix(1.0, uFeedback * damped * confidence, trust);
  if (uCounts != 0) {

    float held = min(texelFetch(uHistoryCount, h, 0).r, STILL_FRAMES) * trust;
    share = max(mix(STILL_SHARE, uFeedback, speed) * damped * confidence, confidence / max(held + confidence, 1.0e-4));
    fragCount = held + confidence;
  }
  fragColor = vec4(unweigh(mix(weigh(toRgb(history)), weigh(toRgb(c)), share)), distanceNow);
}
