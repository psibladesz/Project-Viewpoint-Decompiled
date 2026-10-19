#version 330



layout(location = 0) in vec4 aRect;
layout(location = 1) in vec4 aPlacement;
layout(location = 2) in vec4 aTint;
layout(location = 3) in vec2 aTile;
layout(location = 4) in vec2 aFlags;
flat out vec4 vRect;
flat out vec4 vPlacement;
flat out vec4 vTint;
flat out vec2 vTile;
flat out int vOpaque;
flat out int vFlip;
flat out int vFamily;
uniform float uTiles;
uniform float uTexelsPerTile;

void main() {
    vRect = aRect;
    vPlacement = aPlacement;
    vTint = aTint;
    vTile = aTile;
    vOpaque = int(aFlags.x);
    vFlip = int(mod(aFlags.y, 2.0));
    vFamily = int(aFlags.y) / 2;
    vec2 lo = vec2((aPlacement.x + aPlacement.y) * 0.5, (aPlacement.y - aPlacement.x - aPlacement.z) * 0.5);
    vec2 hi = vec2((aPlacement.x + aPlacement.z + aPlacement.y + aPlacement.w) * 0.5,
                   (aPlacement.y + aPlacement.w - aPlacement.x) * 0.5);


    if (vOpaque == 1) {
        lo = max(lo, vec2(-1.0 / 64.0));
        hi = min(hi, vec2(1.0 + 1.0 / 64.0));
    } else {

        lo -= vec2(1.0 / uTexelsPerTile);
        hi += vec2(1.0 / uTexelsPerTile);
    }

    hi = max(hi, lo);
    const vec2 corners[6] = vec2[6](vec2(0, 0), vec2(1, 0), vec2(1, 1), vec2(0, 0), vec2(1, 1), vec2(0, 1));
    vec2 p = (mix(lo, hi, corners[gl_VertexID]) + aTile + vec2(1.0)) / uTiles;
    gl_Position = vec4(p * 2.0 - 1.0, 0.0, 1.0);
}
