#version 330










uniform sampler2DArray uFlashDepth;
uniform float uFlashBounceOn;
uniform mat4 uFlashUnproject;
uniform mat4 uViewProjection;
uniform sampler2D uAlbedo;
out vec4 fragColor;
#include "lib/camera.glsl"
#include "lib/spots.glsl"
#include "lib/bounce_field.glsl"

const int TAPS = 8;

const vec3 UNSEEN = vec3(0.35);

Spot flash;

vec3 unprojected(vec2 t, float depth) {
  vec4 s = uFlashUnproject * vec4(t * 2.0 - 1.0, depth * 2.0 - 1.0, 1.0);
  return s.xyz / s.w;
}

vec3 flashPoint(vec2 t) {
  return unprojected(t, texture(uFlashDepth, vec3(t, 0.0)).r);
}


vec3 colourAt(vec3 q) {
  vec4 c = uViewProjection * vec4(q, 1.0);
  if (c.w > 0.05) {
    vec2 uv = c.xy / c.w * 0.5 + 0.5;
    if (all(greaterThan(uv, vec2(0.0))) && all(lessThan(uv, vec2(1.0)))
        && abs(linearDepth(texture(uDepth, uv).r) - c.w) < 0.05 + 0.02 * c.w) return texture(uAlbedo, uv).rgb;
  }
  return UNSEEN;
}



vec3 faceAt(vec2 t, vec3 q, vec2 texel) {
  vec3 right = flashPoint(t + vec2(texel.x, 0.0)) - q, left = q - flashPoint(t - vec2(texel.x, 0.0));
  vec3 up = flashPoint(t + vec2(0.0, texel.y)) - q, down = q - flashPoint(t - vec2(0.0, texel.y));
  vec3 n = cross(dot(right, right) < dot(left, left) ? right : left, dot(up, up) < dot(down, down) ? up : down);
  float len = length(n);
  n = len > 1.0e-8 ? n / len : normalize(flash.pos - q);
  return dot(n, flash.pos - q) < 0.0 ? -n : n;
}



void cell(int i, out vec4 light, out vec4 place, out vec3 face) {
  light = vec4(0.0);
  place = vec4(0.0);
  face = vec3(0.0);
  vec2 texel = 1.0 / vec2(textureSize(uFlashDepth, 0).xy);
  vec2 corner = vec2(i % FLASH_COLUMNS, i / FLASH_COLUMNS) / vec2(FLASH_COLUMNS, FLASH_ROWS);
  vec2 span = 1.0 / vec2(FLASH_COLUMNS * TAPS, FLASH_ROWS * TAPS);


  vec3 middle = unprojected(vec2(0.5), 0.0), edge = unprojected(vec2(1.0, 0.5), 0.0);
  float tanHalf = distance(edge, middle) / distance(middle, flash.pos);
  float tapArea = 4.0 * tanHalf * tanHalf * span.x * span.y;
  for (int y = 0; y < TAPS; y++) {
    for (int x = 0; x < TAPS; x++) {
      vec2 t = corner + (vec2(x, y) + 0.5) * span;
      if (texture(uFlashDepth, vec3(t, 0.0)).r >= 1.0) continue;
      vec3 q = flashPoint(t);
      vec3 way = normalize(q - flash.pos);
      float lens = distance(q, flash.pos), offAxis = dot(way, flash.axis);
      float flux = torchBeam(flash, offAxis) * spotFalloff(flash, lens) * lens * lens * tapArea
          * offAxis * offAxis * offAxis;
      if (flux <= 0.0) continue;
      vec3 reflected = flash.colour * flux * colourAt(q), n = faceAt(t, q, texel);
      float weight = dot(reflected, vec3(0.2126, 0.7152, 0.0722));

      float area = tapArea * offAxis * offAxis * offAxis * lens * lens / max(dot(n, -way), 0.2);
      light += vec4(reflected, area);
      place += vec4(q * weight, weight);
      face += n * weight;
    }
  }
}

void main() {
  int i = int(gl_FragCoord.x), row = int(gl_FragCoord.y);
  fragColor = vec4(0.0);
  if (uFlashBounceOn < 0.5) return;
  flash = spotAt(0);
  vec4 light, place;
  vec3 face;
  cell(i, light, place, face);
  bool here = place.w > 0.0;
  if (row == 0) {
    fragColor = here ? vec4(place.xyz / place.w, 1.0) : vec4(0.0);
  } else if (row == 1) {
    bool found = here;
    if (i == 0) {
      for (int j = 1; j < FLASH_LIGHTS && !found; j++) {
        vec4 l, p;
        vec3 f;
        cell(j, l, p, f);
        found = p.w > 0.0;
      }
    }
    fragColor = vec4(here ? normalize(face) : vec3(0.0), found ? 1.0 : 0.0);
  } else {
    fragColor = light;
  }
}
