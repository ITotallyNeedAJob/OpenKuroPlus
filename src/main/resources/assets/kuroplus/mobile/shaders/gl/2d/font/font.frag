#version 330 core

uniform sampler2D tex;
uniform float pxRange;

in vec2 vUV;
in vec4 vColor;

out vec4 fragColor;

float median(float r, float g, float b) {
    return max(min(r, g), min(max(r, g), b));
}

float screenPxRange() {
    float range = pxRange > 0.1 ? pxRange : 8.0;
    vec2 texSize = vec2(textureSize(tex, 0));
    if (texSize.x <= 0.0 || texSize.y <= 0.0) {
        texSize = vec2(828.0, 828.0);
    }
    vec2 unitRange = vec2(range) / texSize;
    vec2 fw = max(fwidth(vUV), vec2(0.000001));
    vec2 screenTexSize = vec2(1.0) / fw;
    return clamp(0.5 * dot(unitRange, screenTexSize), 1.0, 40.0);
}

void main() {
    vec4 msd = texture(tex, vUV);
    float sd = median(msd.r, msd.g, msd.b);
    float screenPxDistance = screenPxRange() * (sd - 0.5);
    float alpha = clamp(screenPxDistance + 0.5, 0.0, 1.0);

    if (alpha <= 0.01) discard;
    fragColor = vec4(vColor.rgb, vColor.a * alpha);
}
