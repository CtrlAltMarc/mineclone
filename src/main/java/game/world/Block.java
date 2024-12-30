package game.world;

public class Block {
    public static final byte AIR = 0;
    public static final byte GRASS = 1;
    public static final byte DIRT = 2;
    public static final byte STONE = 3;

    private static final float[] VERTICES = {
        // Front face
        -0.5f, -0.5f,  0.5f,
         0.5f, -0.5f,  0.5f,
         0.5f,  0.5f,  0.5f,
        -0.5f,  0.5f,  0.5f,
        
        // Back face
        -0.5f, -0.5f, -0.5f,
        -0.5f,  0.5f, -0.5f,
         0.5f,  0.5f, -0.5f,
         0.5f, -0.5f, -0.5f,
        
        // Top face
        -0.5f,  0.5f, -0.5f,
        -0.5f,  0.5f,  0.5f,
         0.5f,  0.5f,  0.5f,
         0.5f,  0.5f, -0.5f,
        
        // Bottom face
        -0.5f, -0.5f, -0.5f,
         0.5f, -0.5f, -0.5f,
         0.5f, -0.5f,  0.5f,
        -0.5f, -0.5f,  0.5f,
        
        // Right face
         0.5f, -0.5f, -0.5f,
         0.5f,  0.5f, -0.5f,
         0.5f,  0.5f,  0.5f,
         0.5f, -0.5f,  0.5f,
        
        // Left face
        -0.5f, -0.5f, -0.5f,
        -0.5f, -0.5f,  0.5f,
        -0.5f,  0.5f,  0.5f,
        -0.5f,  0.5f, -0.5f
    };

    private static final int[] INDICES = {
        0,  1,  2,  2,  3,  0,  // Front
        4,  5,  6,  6,  7,  4,  // Back
        8,  9,  10, 10, 11, 8,  // Top
        12, 13, 14, 14, 15, 12, // Bottom
        16, 17, 18, 18, 19, 16, // Right
        20, 21, 22, 22, 23, 20  // Left
    };

    public static float[] getVertices() {
        return VERTICES;
    }

    public static int[] getIndices() {
        return INDICES;
    }

    public static boolean isTransparent(byte blockType) {
        return blockType == AIR;
    }

    public static float[] getTextureCoords(byte blockType, int face) {
        float textureX = 0;
        float textureY = 0;

        switch (blockType) {
            case GRASS:
                if (face == 2) { // Top face
                    textureX = 0;
                    textureY = 0;
                } else if (face == 3) { // Bottom face
                    textureX = 2;
                    textureY = 0;
                } else { // Side faces
                    textureX = 1;
                    textureY = 0;
                }
                break;
            case DIRT:
                textureX = 2;
                textureY = 0;
                break;
            case STONE:
                textureX = 3;
                textureY = 0;
                break;
        }

        float unit = 1.0f / 16.0f; // Assuming texture atlas is 16x16
        float[] coords = new float[] {
            textureX * unit, textureY * unit,
            (textureX + 1) * unit, textureY * unit,
            (textureX + 1) * unit, (textureY + 1) * unit,
            textureX * unit, (textureY + 1) * unit
        };
        return coords;
    }
}
