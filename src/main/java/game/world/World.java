package game.world;

import game.renderer.ChunkRenderer;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;

public class World {
    private Map<Long, Chunk> chunks;
    private Map<Long, ChunkRenderer> chunkRenderers;
    
    public World() {
        chunks = new HashMap<>();
        chunkRenderers = new HashMap<>();
        generateInitialChunks();
    }
    
    private void generateInitialChunks() {
        // Generate a 3x3 chunk area
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                generateChunk(x, z);
            }
        }
    }
    
    private void generateChunk(int x, int z) {
        long key = getChunkKey(x, z);
        Chunk chunk = new Chunk(x, z);
        chunks.put(key, chunk);
        
        ChunkRenderer renderer = new ChunkRenderer();
        renderer.init(chunk);
        chunkRenderers.put(key, renderer);
    }
    
    public byte getBlock(int x, int y, int z) {
        int chunkX = Math.floorDiv(x, Chunk.SIZE);
        int chunkZ = Math.floorDiv(z, Chunk.SIZE);
        
        Chunk chunk = getChunk(chunkX, chunkZ);
        if (chunk == null) {
            return Block.AIR;
        }
        
        int localX = Math.floorMod(x, Chunk.SIZE);
        int localZ = Math.floorMod(z, Chunk.SIZE);
        return chunk.getBlock(localX, y, localZ);
    }
    
    public void setBlock(int x, int y, int z, byte blockType) {
        int chunkX = Math.floorDiv(x, Chunk.SIZE);
        int chunkZ = Math.floorDiv(z, Chunk.SIZE);
        
        Chunk chunk = getChunk(chunkX, chunkZ);
        if (chunk == null) {
            return;
        }
        
        int localX = Math.floorMod(x, Chunk.SIZE);
        int localZ = Math.floorMod(z, Chunk.SIZE);
        chunk.setBlock(localX, y, localZ, blockType);
        
        // Update chunk mesh
        ChunkRenderer renderer = chunkRenderers.get(getChunkKey(chunkX, chunkZ));
        if (renderer != null) {
            renderer.cleanup();
            renderer.init(chunk);
        }
    }
    
    public void render() {
        for (ChunkRenderer renderer : chunkRenderers.values()) {
            renderer.render();
        }
    }
    
    public void cleanup() {
        for (ChunkRenderer renderer : chunkRenderers.values()) {
            renderer.cleanup();
        }
    }
    
    private Chunk getChunk(int x, int z) {
        return chunks.get(getChunkKey(x, z));
    }
    
    private long getChunkKey(int x, int z) {
        return ((long)x << 32) | (z & 0xFFFFFFFFL);
    }
    
    public Map<Long, Chunk> getChunks() {
        return chunks;
    }
    
    public void loadChunk(int x, int z, byte[][][] blocks) {
        long key = getChunkKey(x, z);
        Chunk chunk = new Chunk(x, z);
        chunk.setBlocks(blocks);
        chunks.put(key, chunk);
        
        ChunkRenderer renderer = new ChunkRenderer();
        renderer.init(chunk);
        chunkRenderers.put(key, renderer);
    }
}
