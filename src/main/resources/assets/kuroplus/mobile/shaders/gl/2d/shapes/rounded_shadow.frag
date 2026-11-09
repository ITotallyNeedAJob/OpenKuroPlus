#version 330 core

in vec2 vLocalPos;
in vec2 vHalfSize;
in vec4 vColor;
in vec4 vRadius;
in float vShadowRadius;

out vec4 fragColor;

float sdf(in vec2 p, in vec2 b, in vec4 r) {
    r.xy = (p.x > 0.0) ? r.xz : r.yw;
    r.x  = (p.y > 0.0) ? r.x  : r.y;
    vec2 q = abs(p) - b + r.x;
    return min(max(q.x, q.y), 0.0) + length(max(q, 0.0)) - r.x;
}

void main() {
    float dist = sdf(vLocalPos, vHalfSize, vRadius);
    float alpha = 0.0;
    if (dist <= 0.0) {
        alpha = 1.0;
    } else {
        float factor = clamp(1.0 - (dist / max(1.0, vShadowRadius)), 0.0, 1.0);
        alpha = factor * factor * (3.0 - 2.0 * factor);
    }
    if (alpha <= 0.002) discard;
    fragColor = vec4(vColor.rgb, vColor.a * alpha);
}
