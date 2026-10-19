



uniform sampler2D vp_tintTex;
uniform vec4 vp_viewArea;
uniform float uIrisLampColours;
vec3 vp_lampTint(vec3 packColour) {
  vec4 t = texture(vp_tintTex, (gl_FragCoord.xy - vp_viewArea.xy) / vp_viewArea.zw);
  if (t.a <= 0.0 || uIrisLampColours <= 0.0) return packColour;
  const vec3 luma = vec3(0.2126, 0.7152, 0.0722);
  vec3 tinted = t.rgb * dot(packColour, luma) / max(dot(t.rgb, luma), 1.0e-3);
  return mix(packColour, tinted, uIrisLampColours * t.a);
}
