#version 330






















uniform sampler2D uSource;
uniform float uTexelsPerTile;
uniform int uBakeFilter;
const int BAKE_NEAREST = 0, BAKE_LINEAR = 1, BAKE_LANCZOS = 2;

const int MOST_SAMPLES = 32;
flat in vec4 vRect;
flat in vec4 vPlacement;
flat in vec4 vTint;
flat in vec2 vTile;
flat in int vOpaque;
flat in int vFlip;
flat in int vFamily;
layout(location = 0) out vec4 fragColor;
layout(location = 1) out vec4 fragMaterial;
layout(location = 2) out vec4 fragStones;
#include "lib/floor_materials.glsl"



const vec2 DIAMOND_CENTRE = vec2(-1.0 / 64.0, 1.0);


const float TIP_HALF_WIDTH = 1.0 / 64.0;

bool onTile(vec2 iso) {
    vec2 offset = abs(iso - DIAMOND_CENTRE);

    if (offset.x <= TIP_HALF_WIDTH) {
        return offset.y <= 1.0;
    }

    return offset.x + offset.y <= 1.0 && offset.x <= 1.0 - TIP_HALF_WIDTH;
}

vec2 texelIso(ivec2 at, vec2 start, vec2 end) {
    vec2 q = (vec2(at) + 0.5 - start) / (end - start);

    if (vFlip == 1) {
        q.x = 1.0 - q.x;
    }

    return vPlacement.xy + q * vPlacement.zw;
}

#include "lib/lanczos.glsl"

float kernel(float x, int bakeFilter) {
    return bakeFilter == BAKE_LANCZOS ? lanczos2(x) : max(0.0, 1.0 - abs(x));
}

vec4 sampleArt(vec2 q, bool inside, float shift, int bakeFilter) {
    vec2 size = vec2(textureSize(uSource, 0));
    vec2 start = vRect.xy * size;
    vec2 end = vRect.zw * size;

    ivec2 low = ivec2(floor(min(start, end) + 0.5));
    ivec2 high = ivec2(floor(max(start, end) - 0.5));
    vec2 corner = mix(start, end, q) + vec2(shift, 0.0) - 0.5;

    if (bakeFilter == BAKE_NEAREST) {
        ivec2 at = ivec2(floor(corner + 0.5));

        if (onTile(texelIso(at, start, end)) == inside) {
            return texelFetch(uSource, clamp(at, low, high), 0);
        }
    }


    int reach = bakeFilter == BAKE_LANCZOS ? 2 : 1;
    ivec2 base = ivec2(floor(corner));
    vec2 f = corner - vec2(base);
    vec3 colour = vec3(0.0), plainColour = vec3(0.0);
    float coverage = 0.0, total = 0.0, plainCoverage = 0.0, plainTotal = 0.0;
    vec4 lo = vec4(1.0), hi = vec4(0.0);

    for (int j = 1 - reach; j <= reach; j++) {
        for (int i = 1 - reach; i <= reach; i++) {
            ivec2 at = base + ivec2(i, j);
            vec4 tap = texelFetch(uSource, clamp(at, low, high), 0);
            float w = kernel(float(i) - f.x, bakeFilter) * kernel(float(j) - f.y, bakeFilter);
            plainColour += tap.rgb * tap.a * w;
            plainCoverage += tap.a * w;
            plainTotal += w;

            if (i >= 0 && i <= 1 && j >= 0 && j <= 1) {
                lo = min(lo, tap);
                hi = max(hi, tap);
            }

            if (onTile(texelIso(at, start, end)) == inside) {
                colour += tap.rgb * tap.a * w;
                coverage += tap.a * w;
                total += w;
            }
        }
    }


    if (total <= 1.0e-4) {
        colour = plainColour;
        coverage = plainCoverage;
        total = plainTotal;
    }

    if (coverage <= 1.0e-4 || total <= 1.0e-4) {
        return vec4(0.0);
    }


    return clamp(vec4(colour / coverage, coverage / total), vec4(lo.rgb, 0.0), vec4(hi.rgb, 1.0));
}

vec4 artwork(vec2 ground, int bakeFilter) {
    vec2 iso = vec2(ground.x - ground.y, ground.x + ground.y);
    vec2 q = (iso - vPlacement.xy) / vPlacement.zw;

    if (any(lessThan(q, vec2(0.0))) || any(greaterThan(q, vec2(1.0)))) {
        return vec4(0.0);
    }

    if (vFlip == 1) {
        q.x = 1.0 - q.x;
    }



    bool inside = onTile(iso);

    if (bakeFilter == BAKE_NEAREST) {
        return sampleArt(q, inside, 0.0, bakeFilter);
    }

    vec4 left = sampleArt(q, inside, -0.5, bakeFilter);
    vec4 right = sampleArt(q, inside, 0.5, bakeFilter);
    float coverage = left.a + right.a;

    if (coverage <= 0.0) {
        return vec4(0.0);
    }

    return vec4((left.rgb * left.a + right.rgb * right.a) / coverage, 0.5 * coverage);
}








const float RIM = 0.02;
vec4 opaqueArtwork(vec2 ground, vec4 source, int bakeFilter) {
    if (source.a >= 0.99) {
        return vec4(source.rgb, 1.0);
    }

    float stepSize = 1.5 / uTexelsPerTile;
    vec4 filled = source;

    for (int i = 1; i <= 4; i++) {
        vec4 candidate = artwork(mix(ground, vec2(0.5), min(0.49, stepSize * float(i))), bakeFilter);

        if (candidate.a > filled.a) {
            filled = candidate;
        }

        if (filled.a >= 0.99) {
            break;
        }
    }


    if (filled.a <= 0.0) {
        filled = artwork(vec2(0.5), bakeFilter);
    }

    return vec4(filled.rgb, 1.0);
}




vec4 layerAt(vec2 ground, int bakeFilter, out vec4 kind, out float paving) {
    kind = vec4(0.0);
    paving = 0.0;

    if (vOpaque == 0) {
        vec4 source = artwork(ground, bakeFilter) * vTint;
        kind = floorKind(vFamily, source.rgb) * source.a;
        paving = vFamily == FAMILY_PAVING ? source.a : 0.0;

        return vec4(source.rgb * source.a, source.a);
    }

    bool inSquare = all(greaterThanEqual(ground, vec2(0.0))) && all(lessThanEqual(ground, vec2(1.0)));


    if (!inSquare && !onTile(vec2(ground.x - ground.y, ground.x + ground.y))) {
        return vec4(0.0);
    }

    vec2 art = vec2(0.5) + (ground - vec2(0.5)) * (1.0 - 2.0 * RIM);
    vec4 source = opaqueArtwork(art, artwork(art, bakeFilter), bakeFilter) * vTint;
    kind = floorKind(vFamily, source.rgb) * source.a;
    paving = vFamily == FAMILY_PAVING ? source.a : 0.0;

    return vec4(source.rgb * source.a, source.a);
}

void main() {


    vec2 pixels = abs(vRect.zw - vRect.xy) * vec2(textureSize(uSource, 0)) / vPlacement.zw / uTexelsPerTile;
    int samples = clamp(int(ceil(max(pixels.x, pixels.y) - 1.0e-3)), 1, MOST_SAMPLES);
    int bakeFilter = samples > 1 ? BAKE_NEAREST : uBakeFilter;
    vec4 sum = vec4(0.0), kinds = vec4(0.0);
    float pavings = 0.0;

    for (int j = 0; j < samples; j++) {
        for (int i = 0; i < samples; i++) {
            vec2 at = floor(gl_FragCoord.xy) + (vec2(i, j) + 0.5) / float(samples);
            vec4 kind;
            float paving;
            sum += layerAt(at / uTexelsPerTile - vec2(1.0) - vTile, bakeFilter, kind, paving);
            kinds += kind;
            pavings += paving;
        }
    }

    sum /= float(samples * samples);
    kinds /= float(samples * samples);
    pavings /= float(samples * samples);

    if (sum.a <= 0.0) {
        discard;
    }

    fragColor = sum;
    fragMaterial = vec4(kinds.rgb, sum.a);
    fragStones = vec4(kinds.a, pavings, 0.0, sum.a);
}
