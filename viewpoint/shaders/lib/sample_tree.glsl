



uniform sampler2D uTreeAtlas;
vec4 sampleTree(vec2 uv, vec4 rect) {
  vec4 c = texture(uTreeAtlas, uv);
  if (uWireframe != 0) return vec4(uWireframe == 2 || c.a < 0.3 ? vec3(0.8) : c.rgb / max(c.a, 0.004), 1.0);
  if (uv.x < rect.x || uv.y < rect.y || uv.x > rect.z || uv.y > rect.w || c.a < 0.4) discard;
  return vec4(c.rgb / c.a, 1.0);
}
