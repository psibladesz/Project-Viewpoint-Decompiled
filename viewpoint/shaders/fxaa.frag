#version 330





in vec2 vUv;
uniform sampler2D uColor;
uniform vec2 uTexel;
out vec4 fragColor;


const float EDGE_THRESHOLD_MIN = 0.0312, EDGE_THRESHOLD_MAX = 0.125, SUBPIXEL_QUALITY = 0.75;
const int ITERATIONS = 12;
float quality(int i) { return i < 5 ? 1.0 : i == 5 ? 1.5 : i < 10 ? 2.0 : i < 11 ? 4.0 : 8.0; }

float luma(vec3 c) { return sqrt(dot(c, vec3(0.299, 0.587, 0.114))); }
float lumaAt(vec2 uv) { return luma(textureLod(uColor, uv, 0.0).rgb); }
void main() {
  vec3 centre = textureLod(uColor, vUv, 0.0).rgb;
  float lumaCentre = luma(centre);
  float lumaDown = luma(textureLodOffset(uColor, vUv, 0.0, ivec2(0, -1)).rgb);
  float lumaUp = luma(textureLodOffset(uColor, vUv, 0.0, ivec2(0, 1)).rgb);
  float lumaLeft = luma(textureLodOffset(uColor, vUv, 0.0, ivec2(-1, 0)).rgb);
  float lumaRight = luma(textureLodOffset(uColor, vUv, 0.0, ivec2(1, 0)).rgb);
  float lumaMin = min(lumaCentre, min(min(lumaDown, lumaUp), min(lumaLeft, lumaRight)));
  float lumaMax = max(lumaCentre, max(max(lumaDown, lumaUp), max(lumaLeft, lumaRight)));
  float lumaRange = lumaMax - lumaMin;
  if (lumaRange < max(EDGE_THRESHOLD_MIN, lumaMax * EDGE_THRESHOLD_MAX)) { fragColor = vec4(centre, 1.0); return; }

  float lumaDownLeft = luma(textureLodOffset(uColor, vUv, 0.0, ivec2(-1, -1)).rgb);
  float lumaUpRight = luma(textureLodOffset(uColor, vUv, 0.0, ivec2(1, 1)).rgb);
  float lumaUpLeft = luma(textureLodOffset(uColor, vUv, 0.0, ivec2(-1, 1)).rgb);
  float lumaDownRight = luma(textureLodOffset(uColor, vUv, 0.0, ivec2(1, -1)).rgb);
  float lumaDownUp = lumaDown + lumaUp, lumaLeftRight = lumaLeft + lumaRight;
  float leftCorners = lumaDownLeft + lumaUpLeft, downCorners = lumaDownLeft + lumaDownRight;
  float rightCorners = lumaDownRight + lumaUpRight, upCorners = lumaUpRight + lumaUpLeft;
  float edgeHorizontal = abs(-2.0 * lumaLeft + leftCorners) + abs(-2.0 * lumaCentre + lumaDownUp) * 2.0
                       + abs(-2.0 * lumaRight + rightCorners);
  float edgeVertical = abs(-2.0 * lumaUp + upCorners) + abs(-2.0 * lumaCentre + lumaLeftRight) * 2.0
                     + abs(-2.0 * lumaDown + downCorners);
  bool horizontal = edgeHorizontal >= edgeVertical;


  float stepLength = horizontal ? uTexel.y : uTexel.x;
  float luma1 = horizontal ? lumaDown : lumaLeft, luma2 = horizontal ? lumaUp : lumaRight;
  float gradient1 = luma1 - lumaCentre, gradient2 = luma2 - lumaCentre;
  bool steepest1 = abs(gradient1) >= abs(gradient2);
  float gradientScaled = 0.25 * max(abs(gradient1), abs(gradient2));
  float localAverage = 0.5 * ((steepest1 ? luma1 : luma2) + lumaCentre);
  if (steepest1) stepLength = -stepLength;
  vec2 current = vUv + (horizontal ? vec2(0.0, stepLength * 0.5) : vec2(stepLength * 0.5, 0.0));


  vec2 offset = horizontal ? vec2(uTexel.x, 0.0) : vec2(0.0, uTexel.y);
  vec2 uv1 = current - offset * quality(0), uv2 = current + offset * quality(0);
  float end1 = lumaAt(uv1) - localAverage, end2 = lumaAt(uv2) - localAverage;
  bool reached1 = abs(end1) >= gradientScaled, reached2 = abs(end2) >= gradientScaled;
  if (!reached1) uv1 -= offset * quality(1);
  if (!reached2) uv2 += offset * quality(1);
  for (int i = 2; i < ITERATIONS && !(reached1 && reached2); i++) {
    if (!reached1) end1 = lumaAt(uv1) - localAverage;
    if (!reached2) end2 = lumaAt(uv2) - localAverage;
    reached1 = abs(end1) >= gradientScaled;
    reached2 = abs(end2) >= gradientScaled;
    if (!reached1) uv1 -= offset * quality(i);
    if (!reached2) uv2 += offset * quality(i);
  }

  float distance1 = horizontal ? vUv.x - uv1.x : vUv.y - uv1.y;
  float distance2 = horizontal ? uv2.x - vUv.x : uv2.y - vUv.y;
  bool nearer1 = distance1 < distance2;
  float nearest = min(distance1, distance2), thickness = distance1 + distance2;

  bool centreSmaller = lumaCentre < localAverage;
  bool rightWay = ((nearer1 ? end1 : end2) < 0.0) != centreSmaller;
  float edgeOffset = rightWay ? 0.5 - nearest / thickness : 0.0;

  float average = (2.0 * (lumaDownUp + lumaLeftRight) + leftCorners + rightCorners) / 12.0;
  float sub = clamp(abs(average - lumaCentre) / lumaRange, 0.0, 1.0);
  sub = (-2.0 * sub + 3.0) * sub * sub;
  float finalOffset = max(edgeOffset, sub * sub * SUBPIXEL_QUALITY);
  vec2 uv = vUv + (horizontal ? vec2(0.0, finalOffset * stepLength) : vec2(finalOffset * stepLength, 0.0));
  fragColor = vec4(textureLod(uColor, uv, 0.0).rgb, 1.0);
}
