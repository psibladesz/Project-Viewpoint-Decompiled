


#ifndef VIEWPOINT_GRIDS
#define VIEWPOINT_GRIDS
uniform vec2 uExposureA;
uniform float uExposureSize;

vec2 gridUv(vec2 sceneXZ) { return (uExposureA - sceneXZ) / uExposureSize; }

vec2 gridCell(vec2 sceneXZ) { return floor(uExposureA - sceneXZ); }
#endif
