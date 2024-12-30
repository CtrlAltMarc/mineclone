package game.world;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import game.entity.Player;
import org.joml.Vector3f;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class WorldSaveManager {
    private static final String SAVE_DIR = "saves";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    
    private final String worldName;
    private final Path savePath;
    
    public WorldSaveManager(String worldName) {
        this.worldName = worldName;
        this.savePath = Paths.get(SAVE_DIR, worldName);
        createSaveDirectory();
    }
    
    private void createSaveDirectory() {
        try {
            Files.createDirectories(savePath);
        } catch (IOException e) {
            throw new RuntimeException("Could not create save directory", e);
        }
    }
    
    public void saveWorld(World world, Player player) {
        // Save player data
        savePlayer(player);
        
        // Save chunks
        for (Map.Entry<Long, Chunk> entry : world.getChunks().entrySet()) {
            saveChunk(entry.getValue());
        }
        
        // Save world metadata
        saveWorldMeta();
    }
    
    private void savePlayer(Player player) {
        PlayerData data = new PlayerData();
        data.position = player.getPosition();
        data.inventory = player.getInventory();
        
        String json = GSON.toJson(data);
        Path playerPath = savePath.resolve("player.json");
        
        try {
            Files.write(playerPath, json.getBytes());
        } catch (IOException e) {
            throw new RuntimeException("Could not save player data", e);
        }
    }
    
    private void saveChunk(Chunk chunk) {
        ChunkData data = new ChunkData();
        data.x = chunk.getX();
        data.z = chunk.getZ();
        data.blocks = chunk.getBlocks();
        
        String json = GSON.toJson(data);
        Path chunkPath = savePath.resolve(String.format("chunk_%d_%d.json", chunk.getX(), chunk.getZ()));
        
        try {
            Files.write(chunkPath, json.getBytes());
        } catch (IOException e) {
            throw new RuntimeException("Could not save chunk data", e);
        }
    }
    
    private void saveWorldMeta() {
        WorldMetaData meta = new WorldMetaData();
        meta.name = worldName;
        meta.version = 1;
        meta.lastPlayed = System.currentTimeMillis();
        
        String json = GSON.toJson(meta);
        Path metaPath = savePath.resolve("world.json");
        
        try {
            Files.write(metaPath, json.getBytes());
        } catch (IOException e) {
            throw new RuntimeException("Could not save world metadata", e);
        }
    }
    
    public void loadWorld(World world, Player player) {
        // Load player data
        loadPlayer(player);
        
        // Load chunks
        try {
            Files.list(savePath)
                .filter(path -> path.toString().startsWith("chunk_"))
                .forEach(path -> loadChunk(world, path));
        } catch (IOException e) {
            throw new RuntimeException("Could not load chunks", e);
        }
    }
    
    private void loadPlayer(Player player) {
        Path playerPath = savePath.resolve("player.json");
        
        try {
            String json = new String(Files.readAllBytes(playerPath));
            PlayerData data = GSON.fromJson(json, PlayerData.class);
            
            player.setPosition(data.position);
            player.setInventory(data.inventory);
        } catch (IOException e) {
            throw new RuntimeException("Could not load player data", e);
        }
    }
    
    private void loadChunk(World world, Path path) {
        try {
            String json = new String(Files.readAllBytes(path));
            ChunkData data = GSON.fromJson(json, ChunkData.class);
            
            world.loadChunk(data.x, data.z, data.blocks);
        } catch (IOException e) {
            throw new RuntimeException("Could not load chunk data", e);
        }
    }
    
    private static class PlayerData {
        Vector3f position;
        game.entity.Inventory inventory;
    }
    
    private static class ChunkData {
        int x;
        int z;
        byte[][][] blocks;
    }
    
    private static class WorldMetaData {
        String name;
        int version;
        long lastPlayed;
    }
}
