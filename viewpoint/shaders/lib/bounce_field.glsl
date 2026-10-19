


#ifndef VIEWPOINT_BOUNCE_FIELD
#define VIEWPOINT_BOUNCE_FIELD
const vec3 BOUNCE_WAYS[6] = vec3[6](vec3(1.0, 0.0, 0.0), vec3(-1.0, 0.0, 0.0), vec3(0.0, 1.0, 0.0),
    vec3(0.0, -1.0, 0.0), vec3(0.0, 0.0, 1.0), vec3(0.0, 0.0, -1.0));

const int BOUNCE_STOREYS = 3;

const float BOUNCE_RECEIVER = 0.8;

const int FLASH_ROWS = 4, FLASH_COLUMNS = 4, FLASH_LIGHTS = FLASH_ROWS * FLASH_COLUMNS;
#endif
