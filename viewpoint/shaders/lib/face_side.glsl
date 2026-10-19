




#ifndef VIEWPOINT_FACE_SIDE
#define VIEWPOINT_FACE_SIDE


vec3 facePlane(vec3 pos) {
  return cross(dFdx(pos), dFdy(pos));
}

bool seenFromBehind(vec3 n, vec3 face, vec3 pos, vec3 eye) {
  return dot(face, n) * dot(face, pos - eye) > 0.0;
}



vec3 litSide(vec3 n, vec3 face, vec3 pos, vec3 eye) {
  return seenFromBehind(n, face, pos, eye) ? -n : n;
}
#endif
