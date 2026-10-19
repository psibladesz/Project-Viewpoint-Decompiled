













#ifndef DEFAULT_PURKINJE
#define DEFAULT_PURKINJE
#ifndef OPTION_NIGHT_COLOUR
#define OPTION_NIGHT_COLOUR 1.0
#endif
#include "lib/scotopic.glsl"
#ifndef VIEWPOINT_SKY_LEVEL
#define VIEWPOINT_SKY_LEVEL
uniform vec3 uSkyLevel;
#endif

const mat3 RGB_TO_LMS = mat3(0.3139, 0.1553, 0.0177, 0.6396, 0.7579, 0.1094, 0.0465, 0.0867, 0.8727);
const mat3 LMS_TO_RGB = mat3(5.4725, -1.1247, 0.0299, -4.6422, 2.2926, -0.1933, 0.1696, -0.1679, 1.1634);

const mat3 RGB_TO_XYZ = mat3(0.4124, 0.2126, 0.0193, 0.3576, 0.7152, 0.1192, 0.1805, 0.0722, 0.9505);
const vec3 LUMINANCE_709 = vec3(0.2126, 0.7152, 0.0722);



const float PURKINJE_MID = 0.18, LAMP_LIT = 4.0, LAMP_LUMINANCE = 30.0, TROLANDS = 50.0;



const float L_MAX = 0.637, M_MAX = 0.392, S_MAX = 1.606;
const float K1 = 1.111, K2 = 0.939, K3 = 0.4, K4 = 0.15, ALPHA = 0.619;
const float RHO_LM = 0.25, RHO_S = 0.4;

const float SCALE_RG = 0.085, SCALE_BY = 6.0;




float pixelLuminance(float againstWhite) {
  float sky = uAdapted * againstWhite / PURKINJE_MID;
  float lamp = smoothstep(1.0, LAMP_LIT, againstWhite);
  return max(sky, exp(mix(log(max(sky, 1.0e-9)), log(LAMP_LUMINANCE), lamp)));
}

vec3 purkinje(vec3 exposed) {
  float rodShare = scotopic() * float(OPTION_NIGHT_COLOUR);
  float luminance = dot(exposed, LUMINANCE_709);
  if (rodShare <= 0.0 || luminance <= 0.0) return exposed;
  float white = max(dot(uSkyLevel, LUMINANCE_709) * uExposure * float(OPTION_EXPOSURE), 1.0e-6);
  float scale = pixelLuminance(luminance / white) * TROLANDS / luminance;
  vec3 q = RGB_TO_LMS * exposed * scale;
  vec3 xyz = RGB_TO_XYZ * exposed * scale;
  float rod = max(xyz.y * (1.33 * (1.0 + (xyz.y + xyz.z) / max(xyz.x, 1.0e-9)) - 1.68), 0.0);
  float rhoLm = RHO_LM * rodShare, rhoS = RHO_S * rodShare;
  float gL = inversesqrt(1.0 + 0.33 * (q.x + rhoLm * rod));
  float gM = inversesqrt(1.0 + 0.33 * (q.y + rhoLm * rod));
  float gS = inversesqrt(1.0 + 0.33 * (q.z + rhoS * rod));
  float redGreen = SCALE_RG * (K1 * rhoLm * gM / M_MAX - K2 * rhoLm * gL / L_MAX) * rod;
  float blueYellow =
      SCALE_BY * (K3 * rhoS * gS / S_MAX - K4 * (ALPHA * rhoLm * gL / L_MAX + (1.0 - ALPHA) * rhoLm * gM / M_MAX)) * rod;

  vec3 shift = vec3(-0.5 * redGreen, 0.5 * redGreen, blueYellow);
  vec3 shifted = max(LMS_TO_RGB * (q + shift) / scale, vec3(0.0));
  float after = dot(shifted, LUMINANCE_709);
  return after > 0.0 ? shifted * (luminance / after) : exposed;
}
#endif
