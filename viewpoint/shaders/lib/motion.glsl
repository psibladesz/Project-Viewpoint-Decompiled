



in vec4 vClip;
in vec4 vPrevClip;
flat in int vMotion;
vec4 motion() {
  if (vMotion == 0) return vec4(0.0);
  return vec4((vClip.xy / vClip.w - vPrevClip.xy / vPrevClip.w) * 0.5, vPrevClip.w, 1.0);
}
