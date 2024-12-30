#version 330 core

layout (location = 0) in vec3 position;
layout (location = 1) in vec2 texCoord;

out vec2 fragTexCoord;
out float lighting;
out vec3 fragNormal;
out vec3 fragPos;

uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;

void main() {
    vec4 worldPos = vec4(position, 1.0);
    vec4 viewPos = viewMatrix * worldPos;
    gl_Position = projectionMatrix * viewPos;
    fragTexCoord = texCoord;
    fragPos = position;
    
    // Calculate normal based on position within block
    vec3 center = floor(position) + vec3(0.5);
    fragNormal = normalize(position - center);
    
    // Simple lighting based on Y normal
    vec3 normal = vec3(0.0, 1.0, 0.0);
    vec3 lightDir = normalize(vec3(0.5, 1.0, 0.3));
    lighting = max(dot(normal, lightDir), 0.3); // Ambient light of 0.3
}
