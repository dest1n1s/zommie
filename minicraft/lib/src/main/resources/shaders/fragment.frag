#version 330
in vec3 originalPosition;
in vec2 texPosition;

out vec4 FragColor;

uniform sampler2D tex;

void main() {
    // add border lines
    int cnt = 0;
    if (originalPosition.x < -0.495 || originalPosition.x > 0.495) {
        cnt++;
    }
    if (originalPosition.y < -0.495 || originalPosition.y > 0.495) {
        cnt++;
    }
    if (originalPosition.z < -0.495 || originalPosition.z > 0.495) {
        cnt++;
    }
    if (cnt >= 2) {
        FragColor = vec4(0.0, 0.0, 0.0, 1.0);
        return;
    }
    FragColor = texture(tex, texPosition);
}