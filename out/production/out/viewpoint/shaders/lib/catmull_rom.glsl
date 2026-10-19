

#ifndef VIEWPOINT_CATMULL_ROM
#define VIEWPOINT_CATMULL_ROM
vec3 catmullRom(sampler2D tex, vec2 uv, vec2 texel) {
  vec2 size = 1.0 / texel;
  vec2 p = uv * size;
  vec2 p1 = floor(p - 0.5) + 0.5;
  vec2 f = p - p1;
  vec2 w0 = f * (-0.5 + f * (1.0 - 0.5 * f));
  vec2 w1 = 1.0 + f * f * (-2.5 + 1.5 * f);
  vec2 w2 = f * (0.5 + f * (2.0 - 1.5 * f));
  vec2 w3 = f * f * (-0.5 + 0.5 * f);
  vec2 w12 = w1 + w2;
  vec2 t0 = (p1 - 1.0) * texel, t3 = (p1 + 2.0) * texel, t12 = (p1 + w2 / w12) * texel;
  vec3 r = texture(tex, vec2(t0.x, t0.y)).rgb * w0.x * w0.y + texture(tex, vec2(t12.x, t0.y)).rgb * w12.x * w0.y
         + texture(tex, vec2(t3.x, t0.y)).rgb * w3.x * w0.y + texture(tex, vec2(t0.x, t12.y)).rgb * w0.x * w12.y
         + texture(tex, vec2(t12.x, t12.y)).rgb * w12.x * w12.y + texture(tex, vec2(t3.x, t12.y)).rgb * w3.x * w12.y
         + texture(tex, vec2(t0.x, t3.y)).rgb * w0.x * w3.y + texture(tex, vec2(t12.x, t3.y)).rgb * w12.x * w3.y
         + texture(tex, vec2(t3.x, t3.y)).rgb * w3.x * w3.y;
  return max(r, vec3(0.0));
}
#endif
