#version 330 core

in vec2 fragTexCoord;
in float lighting;
in vec3 fragNormal;
in vec3 fragPos;

out vec4 fragColor;

uniform sampler2D textureSampler;
uniform bool useTexture;
uniform vec4 color;

void main() {
    vec4 baseColor;
    if (useTexture) {
        baseColor = texture(textureSampler, fragTexCoord);
    } else {
        baseColor = color;
    }
    
    // Calculate edge factor
    vec3 absNormal = abs(fragNormal);
    float edgeFactor = max(max(absNormal.x, absNormal.y), absNormal.z);
    float edge = smoothstep(0.95, 1.0, edgeFactor);
    
    // Darken edges
    vec4 edgeColor = vec4(0.0, 0.0, 0.0, 1.0);
    vec4 finalColor = mix(baseColor * lighting, edgeColor, edge * 0.3);
    
    fragColor = finalColor;
}
