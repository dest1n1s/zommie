#version 330
in vec3 originalPosition;
in vec2 texPosition;

out vec4 FragColor;

uniform sampler2D tex;

void main() {
    FragColor = texture(tex, texPosition);
}