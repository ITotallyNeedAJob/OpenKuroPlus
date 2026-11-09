#version 330

uniform sampler2D InSampler;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

layout(std140) uniform BlurConfig {
    vec2 BlurDir;
    float Radius;
};

in vec2 texCoord;

out vec4 fragColor;

void main() {

    vec2 step = (1.0 / InSize) * BlurDir * (Radius * 0.5);

    vec4 sum = textureLod(InSampler, texCoord, 0.0) * 0.4;
    float weight = 0.4;
    for (int i = -2; i <= 2; i++) {
        if (i == 0) continue;
        float w = 1.0 - abs(float(i)) / 3.0;
        sum += textureLod(InSampler, texCoord + step * float(i), 0.0) * w;
        weight += w;
    }

    fragColor = sum / weight;
}
