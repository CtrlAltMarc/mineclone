#version 330 core

in vec2 fragTexCoord;

out vec4 fragColor;

uniform sampler2D textureSampler;
uniform vec4 color;
uniform bool useTexture;

void main() {
    if (useTexture) {
        fragColor = texture(textureSampler, fragTexCoord) * color;
    } else {
        fragColor = color;
    }
}
