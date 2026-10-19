


uniform sampler2D vp_albedoTex;
vec4 vp_albedo(vec2 uv) { return texture(vp_albedoTex, uv); }
vec4 vp_albedoBias(vec2 uv, float bias) { return texture(vp_albedoTex, uv, bias); }
vec4 vp_albedoLod(vec2 uv, float lod) { return textureLod(vp_albedoTex, uv, lod); }
vec4 vp_albedoGrad(vec2 uv, vec2 dx, vec2 dy) { return textureGrad(vp_albedoTex, uv, dx, dy); }
vec4 vp_albedoFetch(ivec2 p, int lod) { return texelFetch(vp_albedoTex, p, lod); }
vec3 vp_lampTint(vec3 packColour) { return packColour; }
vec2 lightmapDelta() { return vec2(0.0); }
void prologue() {}
