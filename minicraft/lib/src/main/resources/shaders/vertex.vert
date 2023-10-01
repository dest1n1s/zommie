#version 330

layout (location = 0) in vec3 position;
layout (location = 1) in vec2 texturePosition;

out vec3 originalPosition;
out vec2 texPosition;

uniform mat4 model;
uniform mat4 camera;
uniform mat4 projection;

void main()
{
    originalPosition = position;
    texPosition = texturePosition;
    gl_Position = projection * camera * model * vec4(position, 1.0);
}