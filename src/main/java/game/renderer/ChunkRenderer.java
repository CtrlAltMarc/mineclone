package game.renderer;

import game.world.Chunk;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;

public class ChunkRenderer {
    private int vaoId;
    private int vboId;
    private int eboId;
    private int vertexCount;

    public void init(Chunk chunk) {
        // Create mesh from chunk data
        List<Float> meshData = chunk.generateMesh();
        
        // Create VAO
        vaoId = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(vaoId);

        // Create VBO
        vboId = GL15.glGenBuffers();
        FloatBuffer verticesBuffer = null;
        try {
            verticesBuffer = MemoryUtil.memAllocFloat(meshData.size());
            for (float f : meshData) {
                verticesBuffer.put(f);
            }
            verticesBuffer.flip();
            vertexCount = (meshData.size() / 5) * 6 / 4; // Convert quads to triangles

            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboId);
            GL15.glBufferData(GL15.GL_ARRAY_BUFFER, verticesBuffer, GL15.GL_STATIC_DRAW);

            // Position attribute
            GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 20, 0);
            GL20.glEnableVertexAttribArray(0);

            // Texture coordinate attribute
            GL20.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, false, 20, 12);
            GL20.glEnableVertexAttribArray(1);

            // Create indices for converting quads to triangles
            int numQuads = meshData.size() / 20; // 20 floats per quad (5 per vertex * 4 vertices)
            IntBuffer indicesBuffer = MemoryUtil.memAllocInt(numQuads * 6);
            for (int i = 0; i < numQuads; i++) {
                int baseVertex = i * 4;
                indicesBuffer.put(baseVertex);
                indicesBuffer.put(baseVertex + 1);
                indicesBuffer.put(baseVertex + 2);
                indicesBuffer.put(baseVertex + 2);
                indicesBuffer.put(baseVertex + 3);
                indicesBuffer.put(baseVertex);
            }
            indicesBuffer.flip();

            // Create and bind EBO
            eboId = GL15.glGenBuffers();
            GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, eboId);
            GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL15.GL_STATIC_DRAW);
            MemoryUtil.memFree(indicesBuffer);

        } finally {
            if (verticesBuffer != null) {
                MemoryUtil.memFree(verticesBuffer);
            }
        }
    }

    public void render() {
        GL30.glBindVertexArray(vaoId);
        GL11.glDrawElements(GL11.GL_TRIANGLES, vertexCount, GL11.GL_UNSIGNED_INT, 0);
    }

    public void cleanup() {
        GL20.glDisableVertexAttribArray(0);
        GL20.glDisableVertexAttribArray(1);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL15.glDeleteBuffers(vboId);

        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
        GL15.glDeleteBuffers(eboId);

        GL30.glBindVertexArray(0);
        GL30.glDeleteVertexArrays(vaoId);
    }
}
