





#ifndef DEFAULT_SPECULAR
#define DEFAULT_SPECULAR

const float BODY_SINE = 0.00465;
#include "lib/specular_roughness.glsl"


const float SPECULAR_MOST = 64.0;
const float PI_SPECULAR = 3.14159265;




float sphereNoH2(vec3 n, vec3 v, vec3 l, float s) {
  vec3 r = reflect(-v, n);
  float c = sqrt(1.0 - s * s), rl = dot(r, l);
  if (rl >= c) return 1.0;
  vec3 t = normalize(r - rl * l), b = cross(l, t);
  float nt = dot(n, t), vt = dot(v, t);


  float p = c * dot(n, l) + s * nt + dot(n, v), p1 = s * dot(n, b), p2 = -s * nt;
  float q = 1.0 + c * dot(v, l) + s * vt, q1 = s * dot(v, b), q2 = -s * vt;
  q = max(q, 1.0e-4);
  float d1 = 2.0 * p * p1 / q - p * p * q1 / (q * q);
  float d2 = 2.0 * (p1 * p1 + p * p2) / q - 4.0 * p * p1 * q1 / (q * q) - p * p * q2 / (q * q)
      + 2.0 * p * p * q1 * q1 / (q * q * q);

  float phi = d2 < 0.0 ? 2.0 * atan(-0.5 * d1 / d2) : 0.0;
  vec3 bent = c * l + s * (cos(phi) * t + sin(phi) * b);
  vec3 h = normalize(bent + v);
  float noh = max(dot(n, h), 0.0);
  return noh * noh;
}

float ggx(float noh2, float a) {
  float a2 = a * a, d = noh2 * (a2 - 1.0) + 1.0;
  return a2 / (PI_SPECULAR * d * d);
}

float smithHeightCorrelated(float nov, float nol, float a) {
  float a2 = a * a;
  float gv = nol * sqrt(nov * nov * (1.0 - a2) + a2), gl = nov * sqrt(nol * nol * (1.0 - a2) + a2);
  return 0.5 / max(gv + gl, 1.0e-6);
}

float schlick(float f0, float cosine) {
  float m = 1.0 - cosine, m2 = m * m;
  return f0 + (1.0 - f0) * m2 * m2 * m;
}





vec3 bodySpecular(vec3 n, vec3 v, float rough, float f0, float lit) {
  vec3 l = uSunDir;
  float nol = dot(n, l), nov = dot(n, v);
  if (lit <= 0.0 || nol <= 0.0 || nov <= 0.0) return vec3(0.0);
  float a = rough * rough, wide = min(a + 0.5 * BODY_SINE, 1.0);
  float d = ggx(sphereNoH2(n, v, l, BODY_SINE), a) * (a / wide) * (a / wide);
  float f = schlick(f0, max(dot(v, normalize(l + v)), 0.0));
  return landBody() * (PI_SPECULAR * d * smithHeightCorrelated(nov, nol, a) * f * nol * lit);
}




vec3 skySpecular(vec3 n, vec3 v, float rough, float f0, vec3 sky, float seen) {
  float nov = max(dot(n, v), 0.0);
  vec3 r = reflect(-v, n);
  float m = 1.0 - nov, m2 = m * m;
  float f = f0 + (max(1.0 - rough, f0) - f0) * m2 * m2 * m;
  return sky * landSky(r) * (f * seen);
}

vec3 specularClamped(vec3 s) {
  return min(s, vec3(SPECULAR_MOST));
}
#endif
