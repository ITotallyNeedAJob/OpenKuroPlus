#version 330

uniform sampler2D InSampler;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

in vec2 texCoord;

out vec4 fragColor;

void main() {
    vec2 oneTexel = 1.0 / InSize;

    vec4 center = textureLod(InSampler, texCoord, 0.0);
    vec4 left = textureLod(InSampler, texCoord - vec2(oneTexel.x, 0.0), 0.0);
    vec4 right = textureLod(InSampler, texCoord + vec2(oneTexel.x, 0.0), 0.0);
    vec4 up = textureLod(InSampler, texCoord - vec2(0.0, oneTexel.y), 0.0);
    vec4 down = textureLod(InSampler, texCoord + vec2(0.0, oneTexel.y), 0.0);

    float edge = abs(center.a - left.a) + abs(center.a - right.a)
            + abs(center.a - up.a) + abs(center.a - down.a);
    edge = clamp(edge, 0.0, 1.0);

    vec3 colour = center.rgb * center.a + left.rgb * left.a + right.rgb * right.a
            + up.rgb * up.a + down.rgb * down.a;
    float weight = center.a + left.a + right.a + up.a + down.a;
    colour = weight > 0.0 ? colour / weight : vec3(0.0);

    fragColor = vec4(colour, edge);
}
