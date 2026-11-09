#version 330 core

layout(location = 0) in mat4 modelMat;
layout(location = 4) in vec4 rect;
layout(location = 5) in vec4 radius;
layout(location = 6) in ivec4 color;
layout(location = 7) in float z;
layout(location = 8) in float shadowRadius;
layout(std140) uniform Camera {
    mat4 proj;
};

out vec2 vLocalPos;
out vec2 vHalfSize;
out vec4 vColor;
out vec4 vRadius;
out float vShadowRadius;

const vec2[4] quad = vec2[](
    vec2(0.0, 0.0),
    vec2(1.0, 0.0),
    vec2(0.0, 1.0),
    vec2(1.0, 1.0)
);

vec4 getColor(int c) {
    float a = float((c >> 24) & 0xFF) / 255.0;
    float r = float((c >> 16) & 0xFF) / 255.0;
    float g = float((c >> 8) & 0xFF) / 255.0;
    float b = float(c & 0xFF) / 255.0;
    return vec4(r, g, b, a);
}

void main() {
    int id = gl_VertexID % 4;

    vec2 coord = quad[id];
    vHalfSize = rect.zw * 0.5;
    vRadius = radius;
    vShadowRadius = shadowRadius;
    vColor = getColor(color[id]);

    vec2 expandedSize = rect.zw + vec2(shadowRadius * 2.0);
    vec2 expandedPos = rect.xy - vec2(shadowRadius);
    vec2 local = (coord * expandedSize) + expandedPos;

    vLocalPos = (coord - vec2(0.5)) * expandedSize;

    gl_Position = proj * modelMat * vec4(local, z, 1.0);
}
