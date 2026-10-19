#version 330






#include "lib/spots.glsl"
uniform mat4 uViewProjection;
uniform vec3 uEye;
uniform vec2 uPixel;
uniform float uFocal;



uniform vec2 uCoronaScale;
uniform vec2 uCorona;
uniform float uCoronaDepth;
uniform sampler2D uDepth;
out vec2 vCorner;
out vec3 vColour;
out float vStreak;
out float vLong;



const float CORONA_CLEAR = 0.45, CORONA_LEAST = 3.0, CORONA_MOST = 0.12, CORONA_FADE = 30.0, CORONA_STREAK = 5.0;
void hide() { gl_Position = vec4(2.0, 2.0, 2.0, 1.0); }

float unhidden(vec3 at, float radius) {
  if (uCoronaDepth < 0.5) return 1.0;
  vec4 c = uViewProjection * vec4(at, 1.0);
  if (c.w <= 0.0) return 0.0;
  vec3 ndc = c.xyz / c.w;
  vec2 uv = ndc.xy * 0.5 + 0.5, step = uPixel * 0.5 * clamp(radius * 0.3, 1.0, 4.0);
  float depth = ndc.z * 0.5 + 0.5, shown = 0.0;
  for (int k = 0; k < 5; k++) {
    vec2 o = k == 0 ? vec2(0.0) : vec2(k == 1 ? 1.0 : k == 2 ? -1.0 : 0.0, k == 3 ? 1.0 : k == 4 ? -1.0 : 0.0);
    shown += depth <= textureLod(uDepth, uv + o * step, 0.0).r ? 1.0 : 0.0;
  }
  return shown / 5.0;
}
void main() {
  int i = gl_VertexID / 6, k = gl_VertexID - i * 6;
  Spot s = spotAt(i);
  vec3 toEye = uEye - s.pos;
  float away = max(length(toEye), 1.0e-3);
  vec3 dir = toEye / away;
  bool head = s.kind == SPOT_LOW_BEAM;
  float shows = s.kind == SPOT_TORCH ? 0.0 : spotSeen(dot(dir, s.facing)) * (head ? 1.0 : 0.5);
  vec4 clip = uViewProjection * vec4(s.pos, 1.0);
  if (shows <= 0.0 || clip.w <= 0.05) { hide(); return; }
  float wide = uCorona.y * 2.0 * s.lens * (0.6 + 0.4 * shows) * uFocal / clip.w;
  float radius = clamp(wide, CORONA_LEAST, CORONA_MOST / uPixel.y * 2.0);
  float shown = unhidden(s.pos + dir * CORONA_CLEAR, radius);
  if (shown <= 0.0) { hide(); return; }
  float ahead = max(dot(dir, s.axis), 0.0);
  vStreak = head ? pow(ahead, 16.0) : 0.0;
  vLong = vStreak > 0.01 ? CORONA_STREAK : 1.0;
  float scale = head ? uCoronaScale.x : uCoronaScale.y;
  vColour = s.colour * scale * uCorona.x * shows * shown / (1.0 + away / CORONA_FADE);
  int corner = k < 3 ? k : k - 2;
  vCorner = vec2((corner & 1) == 1 ? vLong : -vLong, corner >= 2 ? 1.0 : -1.0);
  gl_Position = vec4(clip.xy / clip.w + vCorner * radius * uPixel, 0.0, 1.0);
}
