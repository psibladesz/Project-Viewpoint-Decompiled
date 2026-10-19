




#include "constants.glsl"
#include "lib/mesh_vertex.glsl"
uniform vec3 uEye;
uniform vec3 uIrisEye;
uniform int uIrisShadow;
uniform int uBlockIds[16];
uniform sampler2D uLight;
uniform vec2 uLightAtlasSize;
vec4 vp_Vertex;
vec3 vp_Normal;
vec4 vp_Color;
vec4 vp_MultiTexCoord0;
vec4 vp_MultiTexCoord1;
const vec4 vp_MultiTexCoordZero = vec4(0.0, 0.0, 0.0, 1.0);
vec4 vp_entity;
vec4 vp_midTexCoord;
vec4 vp_tangent;
vec4 vp_midBlock;
vec3 vp_velocity;
out vec2 vUv;
out vec3 vLocal;
flat out vec4 vRect;
out vec3 vNormal;
out vec3 vPos;

flat out vec4 vMeshValues;
flat out vec4 vGridFlags;
out vec2 vLevels;
bool hiddenVertex;



vec2 gridLevels(vec2 origin, vec2 p) {
  vec2 cell = floor(p), f = p - cell;
  vec2 t = origin + vec2(cell.x * 2.0 + 0.5 + f.x, cell.y * 2.0 + 0.5 + f.y);
  vec3 lamps = texture(uLight, t / uLightAtlasSize).rgb;
  float darker = texture(uLight, (t + vec2(0.0, LIGHT_GRID.y * 0.5)) / uLightAtlasSize).r * 255.0;
  float sky = clamp(1.0 - darker / (SKY_STEP * 15.0), 0.0, 1.0);
  return vec2(max(lamps.r, max(lamps.g, lamps.b)), sky) * 15.0;
}



int materialClass(int flags, bool plant) {
  if ((flags & FLAG_WATER) != 0) return 1;
  if ((flags & FLAG_GLASS) != 0) return 2;
  if ((flags & FLAG_GRASS) != 0) return 3;
  if (plant) return 4;
  if ((flags & FLAG_BAKED_FLOOR) != 0) return 5;
  return 0;
}

vec3 toMinecraft(vec3 sceneDir) { return vec3(-sceneDir.x, sceneDir.y, -sceneDir.z); }

void fetchMesh() {
  MeshVertex v = meshVertex(uEye);
  bool plant = uPlants != 0;
  int flags = v.flags;
  vec3 normal = meshNormal(v);
  vec2 uv = v.uv;
  vec4 rect = v.rect;
  hiddenVertex = v.hidden;
  vUv = uv; vRect = rect;
  vMeshValues = vec4(v.fill, v.fade.w, v.fade.xy);
  vLocal = v.local;
  vNormal = normal;
  vec2 origin = lightOrigin(v, uIrisEye.y);
  vGridFlags = vec4(origin, float(uint(flags) & 0xFFFFu), float(uint(flags) >> 16u));
  vPos = uIrisShadow == 0 ? pulled(v, uEye) : v.pos;
  vec2 p = clamp(vLocal.xz + vec2(-normal.x, -normal.z) * 0.3, -0.99, 8.99) + 1.0;
  vLevels = gridLevels(origin, p);

  vec3 n = normalize(dot(normal, normal) < 0.01 ? vec3(0.0, 1.0, 0.0) : normal);
  if ((flags & FLAG_SINGLE_SIDED) == 0 && !plant && dot(n, uIrisEye - vPos) < 0.0) n = -n;
  vec3 tangent = abs(n.y) > 0.9 ? vec3(-1.0, 0.0, 0.0) : normalize(cross(vec3(0.0, 1.0, 0.0), n));
  vp_Vertex = vec4(toMinecraft(vPos - uIrisEye), 1.0);
  vp_Normal = toMinecraft(n);
  vp_Color = vec4(1.0);
  vp_MultiTexCoord0 = vec4(uv, 0.0, 1.0);
  vp_MultiTexCoord1 = vec4(vLevels * 16.0, 0.0, 1.0);
  int material = materialClass(flags, plant);
  vp_entity = vec4(float(uBlockIds[material]), material == 1 ? 1.0 : -1.0, 0.0, 0.0);
  vp_midTexCoord = vec4((rect.xy + rect.zw) * 0.5, 0.0, 1.0);
  vp_tangent = vec4(toMinecraft(tangent), 1.0);

  vec3 square = vec3(floor(vLocal.x) + 0.5, floor(vLocal.y / SQRT6) * SQRT6 + SQRT6 * 0.5, floor(vLocal.z) + 0.5);
  vp_midBlock = vec4((square - vLocal) * 64.0, 0.0);
  vp_velocity = vec3(0.0);
}
