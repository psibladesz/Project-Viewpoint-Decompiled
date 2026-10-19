






#ifndef DEFAULT_CHARACTER_OCCLUSION
#define DEFAULT_CHARACTER_OCCLUSION
uniform int uBlobCount;
uniform vec4 uBlobs[32];


const vec3 CHARACTER_AXES = vec3(0.3, 0.8, 0.3);
const float CHARACTER_MIDDLE = 0.8;


const float CHARACTER_MOST = 0.7;

const float CHARACTER_REACH = 10.0;


float characterVisibility(vec3 pos, vec3 n) {
  float seen = 1.0;
  for (int i = 0; i < uBlobCount; i++) {
    vec3 d = (uBlobs[i].xyz + vec3(0.0, CHARACTER_MIDDLE, 0.0) - pos) / CHARACTER_AXES;
    float dist2 = dot(d, d);
    if (dist2 > CHARACTER_REACH * CHARACTER_REACH) continue;


    float dist = max(sqrt(dist2), 1.0), sine = 1.0 / dist;
    float cosine = dot(normalize(n * CHARACTER_AXES), d / max(sqrt(dist2), 1.0e-4));

    float facing = cosine >= sine ? cosine : cosine <= -sine ? 0.0 : (cosine + sine) * (cosine + sine) / (4.0 * sine);
    seen *= 1.0 - CHARACTER_MOST * min(facing * sine * sine, 1.0);
  }
  return seen;
}
#endif
