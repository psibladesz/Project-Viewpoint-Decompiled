




void crownShadow(sampler2D tex, vec2 uv, vec4 rect) {
  vec2 f = (uv - rect.xy) / max(rect.zw - rect.xy, vec2(1.0e-6));
  vec2 e = (f - vec2(0.5, 0.42)) / vec2(0.46, 0.40);
  bool trunk = abs(f.x - 0.5) < 0.05 && f.y > 0.5;
  if (!trunk && (dot(e, e) > 1.0 || textureLod(tex, uv, 3.0).a < 0.08)) discard;
}
