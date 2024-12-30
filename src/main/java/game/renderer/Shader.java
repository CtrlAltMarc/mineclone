package game.renderer;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL20;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL20.*;

public class Shader {
    private final int programId;
    private int vertexShaderId;
    private int fragmentShaderId;
    private final Map<String, Integer> uniforms;

    public Shader() throws Exception {
        programId = glCreateProgram();
        if (programId == 0) {
            throw new Exception("Could not create Shader");
        }
        uniforms = new HashMap<>();
    }

    public void createVertexShader(String shaderCode) throws Exception {
        vertexShaderId = createShader(shaderCode, GL_VERTEX_SHADER);
    }

    public void createFragmentShader(String shaderCode) throws Exception {
        fragmentShaderId = createShader(shaderCode, GL_FRAGMENT_SHADER);
    }

    protected int createShader(String shaderCode, int shaderType) throws Exception {
        int shaderId = glCreateShader(shaderType);
        if (shaderId == 0) {
            throw new Exception("Error creating shader. Type: " + shaderType);
        }

        glShaderSource(shaderId, shaderCode);
        glCompileShader(shaderId);

        if (glGetShaderi(shaderId, GL_COMPILE_STATUS) == 0) {
            throw new Exception("Error compiling Shader code: " + glGetShaderInfoLog(shaderId, 1024));
        }

        glAttachShader(programId, shaderId);

        return shaderId;
    }

    public void link() throws Exception {
        glLinkProgram(programId);
        if (glGetProgrami(programId, GL_LINK_STATUS) == 0) {
            throw new Exception("Error linking Shader code: " + glGetProgramInfoLog(programId, 1024));
        }

        if (vertexShaderId != 0) {
            glDetachShader(programId, vertexShaderId);
        }
        if (fragmentShaderId != 0) {
            glDetachShader(programId, fragmentShaderId);
        }

        glValidateProgram(programId);
        if (glGetProgrami(programId, GL_VALIDATE_STATUS) == 0) {
            System.err.println("Warning validating Shader code: " + glGetProgramInfoLog(programId, 1024));
        }
    }

    public void createUniform(String uniformName) throws Exception {
        bind();
        int uniformLocation = glGetUniformLocation(programId, uniformName);
        if (uniformLocation < 0) {
            System.err.println("Warning: Could not find uniform: " + uniformName);
            return;
        }
        uniforms.put(uniformName, uniformLocation);
        unbind();
    }

    public void setUniform(String uniformName, Matrix4f value) {
        Integer location = uniforms.get(uniformName);
        if (location != null) {
            try (MemoryStack stack = MemoryStack.stackPush()) {
                FloatBuffer fb = stack.mallocFloat(16);
                value.get(fb);
                glUniformMatrix4fv(location, false, fb);
            }
        }
    }

    public void setUniform(String uniformName, int value) {
        Integer location = uniforms.get(uniformName);
        if (location != null) {
            glUniform1i(location, value);
        }
    }

    public void setUniform(String uniformName, float value) {
        Integer location = uniforms.get(uniformName);
        if (location != null) {
            glUniform1f(location, value);
        }
    }

    public void setUniform(String uniformName, boolean value) {
        Integer location = uniforms.get(uniformName);
        if (location != null) {
            glUniform1i(location, value ? 1 : 0);
        }
    }

    public void setUniform(String uniformName, float x, float y, float z, float w) {
        Integer location = uniforms.get(uniformName);
        if (location != null) {
            glUniform4f(location, x, y, z, w);
        }
    }

    public void bind() {
        glUseProgram(programId);
    }

    public void unbind() {
        glUseProgram(0);
    }

    public void cleanup() {
        unbind();
        if (programId != 0) {
            glDeleteProgram(programId);
        }
    }
}
