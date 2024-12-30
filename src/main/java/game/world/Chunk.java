package game.world;

import java.util.ArrayList;
import java.util.List;

public class Chunk {
    public static final int SIZE = 16;
    private byte[][][] blocks;
    private int x, z; // Chunk coordinates
    private boolean isDirty; // Whether the chunk needs to be re-rendered
    
    public Chunk(int x, int z) {
        this.x = x;
        this.z = z;
        this.blocks = new byte[SIZE][SIZE][SIZE];
        this.isDirty = true;
        generateTerrain();
    }
    
    private void generateTerrain() {
        // Simple flat terrain generation
        for (int x = 0; x < SIZE; x++) {
            for (int z = 0; z < SIZE; z++) {
                int height = 4; // Lower terrain height
                
                for (int y = 0; y < SIZE; y++) {
                    if (y > height) {
                        blocks[x][y][z] = Block.AIR;
                    } else if (y == height) {
                        blocks[x][y][z] = Block.GRASS;
                    } else if (y > height - 4) {
                        blocks[x][y][z] = Block.DIRT;
                    } else {
                        blocks[x][y][z] = Block.STONE;
                    }
                }
            }
        }
    }
    
    public byte getBlock(int x, int y, int z) {
        if (x < 0 || x >= SIZE || y < 0 || y >= SIZE || z < 0 || z >= SIZE) {
            return Block.AIR;
        }
        return blocks[x][y][z];
    }
    
    public void setBlock(int x, int y, int z, byte blockType) {
        if (x < 0 || x >= SIZE || y < 0 || y >= SIZE || z < 0 || z >= SIZE) {
            return;
        }
        blocks[x][y][z] = blockType;
        isDirty = true;
    }
    
    public List<Float> generateMesh() {
        List<Float> meshData = new ArrayList<>();
        
        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < SIZE; y++) {
                for (int z = 0; z < SIZE; z++) {
                    byte blockType = blocks[x][y][z];
                    if (blockType == Block.AIR) continue;
                    
                    // Check each face
                    // Only add face if adjacent block is transparent
                    float worldX = x + this.x * SIZE;
                    float worldY = y;
                    float worldZ = z + this.z * SIZE;
                    
                    // Front face (positive Z)
                    if (isTransparent(x, y, z + 1)) {
                        addFaceToMesh(meshData, blockType, 0, worldX, worldY, worldZ);
                    }
                    // Back face (negative Z)
                    if (isTransparent(x, y, z - 1)) {
                        addFaceToMesh(meshData, blockType, 1, worldX, worldY, worldZ);
                    }
                    // Top face (positive Y)
                    if (isTransparent(x, y + 1, z)) {
                        addFaceToMesh(meshData, blockType, 2, worldX, worldY, worldZ);
                    }
                    // Bottom face (negative Y)
                    if (isTransparent(x, y - 1, z)) {
                        addFaceToMesh(meshData, blockType, 3, worldX, worldY, worldZ);
                    }
                    // Right face (positive X)
                    if (isTransparent(x + 1, y, z)) {
                        addFaceToMesh(meshData, blockType, 4, worldX, worldY, worldZ);
                    }
                    // Left face (negative X)
                    if (isTransparent(x - 1, y, z)) {
                        addFaceToMesh(meshData, blockType, 5, worldX, worldY, worldZ);
                    }
                }
            }
        }
        
        isDirty = false;
        return meshData;
    }
    
    private boolean isTransparent(int x, int y, int z) {
        return Block.isTransparent(getBlock(x, y, z));
    }
    
    private void addFaceToMesh(List<Float> meshData, byte blockType, int face, float x, float y, float z) {
        float[] vertices = Block.getVertices();
        float[] texCoords = Block.getTextureCoords(blockType, face);
        
        // Add four vertices for the face
        for (int i = 0; i < 4; i++) {
            int baseIndex = face * 12 + i * 3;
            // Position
            meshData.add(vertices[baseIndex] + x);
            meshData.add(vertices[baseIndex + 1] + y);
            meshData.add(vertices[baseIndex + 2] + z);
            // Texture coordinates
            meshData.add(texCoords[i * 2]);
            meshData.add(texCoords[i * 2 + 1]);
        }
    }
    
    public boolean isDirty() {
        return isDirty;
    }
    
    public int getX() {
        return x;
    }
    
    public int getZ() {
        return z;
    }
    
    public byte[][][] getBlocks() {
        return blocks;
    }
    
    public void setBlocks(byte[][][] blocks) {
        if (blocks != null && 
            blocks.length == SIZE && 
            blocks[0].length == SIZE && 
            blocks[0][0].length == SIZE) {
            this.blocks = blocks;
            this.isDirty = true;
        }
    }
}
