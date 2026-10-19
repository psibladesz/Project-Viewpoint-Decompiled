#version 330











in vec2 vUv;
uniform sampler2D uClosest;
uniform sampler2D uScene;
uniform sampler2D uAlbedo;
uniform sampler2D uNormal;
uniform sampler2D uEngineLight;
uniform mat4 uViewProjection;
uniform mat4 uPrevious;
uniform vec2 uSize;
uniform int uLevels;
uniform int uSteps;
uniform int uScale;
uniform float uSceneLevels;
uniform float uHistory;
uniform float uTolerance;
out vec4 fragColor;


const float FACING_TRUSTED = 0.4, FACING_NONE = 0.8;


const float THICKNESS_SHARE = 0.05;

const float SKY = 2.0;

const float LIFT_TEXELS = 1.0, LIFT_LEAST = 0.01;

float thickness(float d) { return uTolerance + d * THICKNESS_SHARE; }
#include "lib/camera.glsl"
#include "lib/reflective.glsl"

vec3 toScreen(vec3 p) {
  vec4 c = uViewProjection * vec4(p, 1.0);
  return c.xyz / c.w * 0.5 + 0.5;
}


bool previousUv(vec4 point, out vec2 uv) {
  vec4 c = uPrevious * point;
  uv = c.xy / c.w * 0.5 + 0.5;
  return c.w > 0.0 && all(greaterThanEqual(uv, vec2(0.0))) && all(lessThanEqual(uv, vec2(1.0)));
}


float margin(vec2 uv) {
  vec2 p = uv * uSize, m = vec2((uSize.x + uSize.y) * 0.05);
  vec2 g = mix(uSize - p, p, lessThan(p, uSize * 0.5));
  return smoothstep(0.0, m.x * m.y, g.x * g.y);
}

vec2 cellCount(int level) {
  return max(vec2(1.0), floor(uSize / exp2(float(level))));
}


float walk(vec3 s0, vec3 dir, bool toward, float tEnd, out bool found) {
  vec2 cellStep = vec2(dir.x < 0.0 ? -1.0 : 1.0, dir.y < 0.0 ? -1.0 : 1.0);
  vec2 ahead = clamp(cellStep, 0.0, 1.0);

  vec2 first = (floor(s0.xy * uSize) + ahead) / uSize + cellStep * 1.0e-6;
  vec2 firstT = (first - s0.xy) / dir.xy;
  float t = min(firstT.x, firstT.y);
  int level = 0;
  for (int i = 0; i < uSteps && level >= 0 && t < tEnd; i++) {
    vec3 p = s0 + dir * t;
    vec2 count = cellCount(level);
    vec2 cell = clamp(floor(p.xy * count), vec2(0.0), count - 1.0);
    float c = texelFetch(uClosest, ivec2(cell), level).r;
    float depthT = (c - s0.z) * dir.z;
    vec2 edgeT2 = ((cell + ahead) / count + cellStep * 1.0e-6 - s0.xy) / dir.xy;
    float edgeT = min(edgeT2.x, edgeT2.y);
    bool hit = toward ? t <= depthT : depthT <= edgeT;
    int next = hit ? -1 : 1;
    if (level == 0 && hit && linearDepth(p.z) - linearDepth(c) > thickness(linearDepth(c))) {
      hit = false;
      next = 0;
    }
    if (hit) {
      if (!toward) t = max(t, depthT);
    } else {
      t = edgeT;
    }
    level = min(level + next, uLevels - 1);
  }
  found = level < 0;
  return t;
}

void main() {
  ivec2 full = ivec2(gl_FragCoord.xy) * uScale + (uScale - 1) / 2;
  vec2 fullSize = vec2(textureSize(uDepth, 0));
  vec2 uv = (vec2(full) + 0.5) / fullSize;
  float depth = texelFetch(uDepth, full, 0).r;
  vec3 n;
  float rough;
  if (depth >= 1.0) { fragColor = vec4(0.0, 0.0, 0.0, -1.0); return; }
  vec3 pos = scenePos(uv, depth);
  vec4 normal = texelFetch(uNormal, full, 0);
  if (!reflects(uv, pos, texelFetch(uAlbedo, full, 0), normal, texelFetch(uEngineLight, full, 0), n, rough)) {
    fragColor = vec4(0.0, 0.0, 0.0, -1.0);
    return;
  }
  if (uHistory < 0.5) { fragColor = vec4(0.0); return; }
  vec3 face = normalize(normal.xyz);
  vec3 r = reflect(normalize(pos - uEye), n);
  if (dot(r, face) < 0.0) r = reflect(r, face);


  pos += face * max(LIFT_LEAST, length(pos - uEye) * 2.0 * uCamera.w / uSize.y * LIFT_TEXELS);

  vec4 c0 = uViewProjection * vec4(pos, 1.0), c1 = uViewProjection * vec4(pos + r, 1.0);
  float reach = length(pos - uEye) + 1.0, closing = c0.w - c1.w;
  if (closing > 0.0) reach = min(reach, (c0.w - 2.0 * uCamera.x) / closing);
  vec3 s0 = toScreen(pos), s1 = toScreen(pos + r * max(reach, 1.0e-3));
  vec3 dir = s1 - s0;
  dir /= max(abs(dir.z), 1.0e-7);
  bool toward = dir.z < 0.0;
  vec2 exitT = max((vec2(0.0) - s0.xy) / dir.xy, (vec2(1.0) - s0.xy) / dir.xy);

  float infinity = 0.5 + 0.5 * (uCamera.y + uCamera.x) / (uCamera.y - uCamera.x);
  float tEnd = min(min(exitT.x, exitT.y), toward ? s0.z : infinity - s0.z);
  bool found;
  float t = walk(s0, dir, toward, tEnd, found);
  vec3 p = s0 + dir * t;
  float sure = found && t < tEnd ? 1.0 : 0.0;
  ivec2 at = ivec2(clamp(p.xy * uSize, vec2(0.0), uSize - 1.0));
  float met = texelFetch(uClosest, at, 0).r;
  if (met >= SKY) sure = 0.0;
  if (length(dir.xy * t) * uSize.x < 2.0 && dot(r, texture(uNormal, p.xy).xyz) >= 0.0) sure = 0.0;
  float behind = abs(linearDepth(p.z) - linearDepth(met));
  float close = 1.0 - smoothstep(0.0, thickness(linearDepth(met)), behind);
  sure *= close * close;
  vec2 then;
  if (sure > 0.0 && previousUv(vec4(scenePos(p.xy, p.z), 1.0), then)) {
    sure *= margin(then);
  } else {
    sure = 0.0;
  }


  sure *= 1.0 - smoothstep(FACING_TRUSTED, FACING_NONE, dot(r, normalize(uEye - pos)));



  float cone = atan(rough * rough), rayLength = max(length(dir.xy * t), 1.0e-4);
  float opposite = 2.0 * tan(cone) * rayLength;
  float radius = opposite * (sqrt(opposite * opposite + 4.0 * rayLength * rayLength) - opposite) / (4.0 * rayLength);
  float level = clamp(log2(radius * max(uSize.x, uSize.y) + 1.0), 0.0, uSceneLevels - 1.0);
  vec3 light = sure > 0.0 ? textureLod(uScene, then, level).rgb * sure : vec3(0.0);

  vec4 q = uViewProjection * vec4(r, 0.0);
  vec2 far = q.xy / q.w * 0.5 + 0.5;
  vec2 farThen;
  if (sure < 1.0 && q.w > 0.0 && all(greaterThanEqual(far, vec2(0.0))) && all(lessThanEqual(far, vec2(1.0)))
      && texelFetch(uClosest, ivec2(min(far * uSize, uSize - 1.0)), 0).r >= SKY && previousUv(vec4(r, 0.0), farThen)) {
    float share = (1.0 - sure) * margin(farThen);

    float farLevel = clamp(log2(rough * rough / (2.0 * uCamera.w) * uSize.y + 1.0), 0.0, uSceneLevels - 1.0);
    light += textureLod(uScene, farThen, farLevel).rgb * share;
    sure += share;
  }

  if (any(isnan(light)) || any(isinf(light))) {
    light = vec3(0.0);
    sure = 0.0;
  }
  fragColor = vec4(light, sure);
}
