#version 330 core

in vec2 vCoord;
in vec4 vColor;
in vec2 vSize;
in vec4 vRadius;
in float vThickness;

out vec4 fragColor;

float sdf(in vec2 p, in vec2 b, in vec4 r) {
    r.xy = (p.x > 0.0) ? r.xz : r.yw;
    r.x  = (p.y > 0.0) ? r.x  : r.y;
    vec2 q = abs(p) - b + r.x;
    return min(max(q.x, q.y), 0.0) + length(max(q, 0.0)) - r.x;
}

void main() {
    vec2 halfSize = vSize * 0.5;
    vec2 localPos = halfSize - (vCoord * vSize);
    float dist = sdf(localPos, halfSize, vRadius);
    float edge = fwidth(dist) * 0.5;
    float outer = smoothstep(edge, -edge, dist);
    float inner = smoothstep(-edge, edge, dist + vThickness);
    float alpha = outer * inner;

    if (alpha <= 0.01) discard;
    fragColor = vec4(vColor.rgb, vColor.a * alpha);
}
