#version 330

uniform sampler2D InSampler;
uniform sampler2D EdgeSampler;
uniform sampler2D BodySampler;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

layout(std140) uniform EspConfig {
    float FillAlpha;
    float RimStrength;
    float GlowStrength;
};

in vec2 texCoord;

out vec4 fragColor;

void main() {
    vec4 body = textureLod(BodySampler, texCoord, 0.0);

    if (body.a > 0.0) {
        fragColor = vec4(body.rgb, body.a * FillAlpha);
        return;
    }

    vec4 edge = textureLod(EdgeSampler, texCoord, 0.0);
    vec4 halo = textureLod(InSampler, texCoord, 0.0);

    float rim = edge.a * RimStrength;
    float glow = halo.a * halo.a * GlowStrength;
    float alpha = max(rim, glow);
    if (alpha <= 0.0) {
        fragColor = vec4(0.0);
        return;
    }

    fragColor = vec4(rim >= glow ? edge.rgb : halo.rgb, alpha);
}
