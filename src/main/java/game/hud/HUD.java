package game.hud;

import game.entity.Player;
import game.entity.ItemStack;
import game.renderer.Shader;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;

public class HUD {
    private static final float[] QUAD_VERTICES = {
        -1.0f, -1.0f,  0.0f, 0.0f,
         1.0f, -1.0f,  1.0f, 0.0f,
         1.0f,  1.0f,  1.0f, 1.0f,
        -1.0f,  1.0f,  0.0f, 1.0f
    };

    private static final int[] QUAD_INDICES = {
        0, 1, 2,
        2, 3, 0
    };

    private int vaoId;
    private int vboId;
    private int eboId;
    private Shader shader;
    private Matrix4f projectionMatrix;
    private int width;
    private int height;

    public HUD(int width, int height) throws Exception {
        this.width = width;
        this.height = height;
        
        // Create shader
        shader = new Shader();
        shader.createVertexShader(loadShaderSource("/shaders/hud.vert"));
        shader.createFragmentShader(loadShaderSource("/shaders/hud.frag"));
        shader.link();

        // Create uniforms for HUD rendering
        shader.createUniform("projectionMatrix");
        shader.createUniform("useTexture");
        shader.createUniform("color");

        // Create projection matrix
        projectionMatrix = new Matrix4f().ortho2D(0, width, height, 0);

        // Create VAO
        vaoId = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(vaoId);

        // Create VBO
        vboId = GL15.glGenBuffers();
        FloatBuffer verticesBuffer = MemoryUtil.memAllocFloat(QUAD_VERTICES.length);
        verticesBuffer.put(QUAD_VERTICES).flip();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboId);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, verticesBuffer, GL15.GL_STATIC_DRAW);
        MemoryUtil.memFree(verticesBuffer);

        // Create EBO
        eboId = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, eboId);
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, QUAD_INDICES, GL15.GL_STATIC_DRAW);

        // Set up vertex attributes
        GL20.glVertexAttribPointer(0, 2, GL11.GL_FLOAT, false, 16, 0);
        GL20.glEnableVertexAttribArray(0);
        GL20.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, false, 16, 8);
        GL20.glEnableVertexAttribArray(1);
    }

    public void render(Player player) {
        shader.bind();
        GL30.glBindVertexArray(vaoId);

        // Set projection matrix
        shader.setUniform("projectionMatrix", projectionMatrix);
        shader.setUniform("useTexture", false);

        // Draw crosshair
        drawCrosshair();

        // Draw hotbar
        drawHotbar(player);

        // Draw debug info
        drawDebugInfo(player);

        GL30.glBindVertexArray(0);
        shader.unbind();
    }

    private void drawCrosshair() {
        float size = 10;
        float centerX = width / 2;
        float centerY = height / 2;

        shader.setUniform("color", 1.0f, 1.0f, 1.0f, 0.8f);

        // Horizontal line
        drawRect(centerX - size, centerY - 1, size * 2, 2);

        // Vertical line
        drawRect(centerX - 1, centerY - size, 2, size * 2);
    }

    private void drawHotbar(Player player) {
        float slotSize = 40;
        float padding = 4;
        float startX = (width - (slotSize * 9 + padding * 8)) / 2;
        float startY = height - slotSize - 10;

        ItemStack[] hotbar = player.getInventory().getHotbar();
        int selectedSlot = player.getSelectedSlot();

        // Draw hotbar background
        shader.setUniform("color", 0.0f, 0.0f, 0.0f, 0.5f);
        drawRect(startX - padding, startY - padding, 
                (slotSize * 9 + padding * 8) + padding * 2, 
                slotSize + padding * 2);

        // Draw slots
        for (int i = 0; i < 9; i++) {
            float x = startX + i * (slotSize + padding);
            
            // Draw slot background
            shader.setUniform("color", 0.3f, 0.3f, 0.3f, 0.7f);
            drawRect(x, startY, slotSize, slotSize);

            // Draw selection highlight
            if (i == selectedSlot) {
                shader.setUniform("color", 1.0f, 1.0f, 1.0f, 0.5f);
                drawRect(x - 2, startY - 2, slotSize + 4, slotSize + 4);
            }

            // Draw item count if exists
            ItemStack stack = hotbar[i];
            if (stack != null && !stack.isEmpty()) {
                // TODO: Draw item texture
                // For now, just draw the count
                drawText(String.valueOf(stack.getAmount()), 
                        x + slotSize - 10, startY + slotSize - 10);
            }
        }
    }

    private void drawDebugInfo(Player player) {
        shader.setUniform("color", 1.0f, 1.0f, 1.0f, 1.0f);
        
        // Draw coordinates
        String coords = String.format("XYZ: %.1f / %.1f / %.1f",
                player.getPosition().x,
                player.getPosition().y,
                player.getPosition().z);
        drawText(coords, 10, 10);
    }

    private void drawRect(float x, float y, float width, float height) {
        float x1 = (x / this.width) * 2 - 1;
        float y1 = (y / this.height) * 2 - 1;
        float x2 = ((x + width) / this.width) * 2 - 1;
        float y2 = ((y + height) / this.height) * 2 - 1;

        QUAD_VERTICES[0] = x1;  QUAD_VERTICES[1] = -y2;
        QUAD_VERTICES[4] = x2;  QUAD_VERTICES[5] = -y2;
        QUAD_VERTICES[8] = x2;  QUAD_VERTICES[9] = -y1;
        QUAD_VERTICES[12] = x1; QUAD_VERTICES[13] = -y1;

        FloatBuffer verticesBuffer = MemoryUtil.memAllocFloat(QUAD_VERTICES.length);
        verticesBuffer.put(QUAD_VERTICES).flip();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboId);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, verticesBuffer, GL15.GL_STATIC_DRAW);
        MemoryUtil.memFree(verticesBuffer);

        GL11.glDrawElements(GL11.GL_TRIANGLES, QUAD_INDICES.length, GL11.GL_UNSIGNED_INT, 0);
    }

    private void drawText(String text, float x, float y) {
        // TODO: Implement text rendering
        // For now, we'll skip actual text rendering as it requires font texture
    }

    public void cleanup() {
        shader.cleanup();
        GL20.glDisableVertexAttribArray(0);
        GL20.glDisableVertexAttribArray(1);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL15.glDeleteBuffers(vboId);
        GL15.glDeleteBuffers(eboId);
        GL30.glBindVertexArray(0);
        GL30.glDeleteVertexArrays(vaoId);
    }

    private String loadShaderSource(String path) {
        try (java.io.InputStream is = getClass().getResourceAsStream(path)) {
            if (is == null) {
                throw new RuntimeException("Could not find shader file: " + path);
            }
            try (java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(is))) {
                return reader.lines().collect(java.util.stream.Collectors.joining("\n"));
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load shader: " + path, e);
        }
    }
}
