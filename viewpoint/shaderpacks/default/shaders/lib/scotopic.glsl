



#ifndef DEFAULT_SCOTOPIC
#define DEFAULT_SCOTOPIC
uniform float uAdapted;


const float SCOTOPIC_LOG = -2.0, PHOTOPIC_LOG = 0.5;

float scotopic() {
  if (uAdapted <= 0.0) return 0.0;
  return 1.0 - smoothstep(SCOTOPIC_LOG, PHOTOPIC_LOG, log(uAdapted) / log(10.0));
}
#endif
