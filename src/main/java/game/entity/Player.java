package game.entity;

import game.Camera;
import game.world.Block;
import game.world.World;
import org.joml.Vector3f;

public class Player {
    private static final float PLAYER_HEIGHT = 1.8f;
    private static final float PLAYER_WIDTH = 0.6f;
    private static final float GRAVITY = -20.0f;
    private static final float JUMP_FORCE = 8.0f;

    private Vector3f position;
    private Vector3f velocity;
    private boolean onGround;
    private Camera camera;
    private World world;
    private Inventory inventory;
    private int selectedSlot;

    public Player(World world, Camera camera) {
        this.world = world;
        this.camera = camera;
        this.position = new Vector3f(0, 10, 0); // Start lower
        this.velocity = new Vector3f();
        this.inventory = new Inventory();
        this.selectedSlot = 0;
        
        // Initialize inventory with some blocks
        inventory.addItem(new ItemStack(Block.GRASS, 64));
        inventory.addItem(new ItemStack(Block.DIRT, 64));
        inventory.addItem(new ItemStack(Block.STONE, 64));
    }

    public void update(float deltaTime) {
        // Apply gravity
        if (!onGround) {
            velocity.y += GRAVITY * deltaTime;
        }

        // Update position with velocity
        Vector3f newPosition = new Vector3f(position).add(
            new Vector3f(velocity).mul(deltaTime)
        );

        // Check collisions and adjust position
        moveWithCollision(newPosition);

        // Update camera position
        camera.getPosition().set(position.x, position.y + PLAYER_HEIGHT * 0.8f, position.z);
    }

    private void moveWithCollision(Vector3f newPosition) {
        // Check X axis collision
        if (!checkCollision(newPosition.x, position.y, position.z)) {
            position.x = newPosition.x;
        } else {
            velocity.x = 0;
        }

        // Check Y axis collision
        if (!checkCollision(position.x, newPosition.y, position.z)) {
            position.y = newPosition.y;
            onGround = false;
        } else {
            if (velocity.y < 0) {
                onGround = true;
            }
            velocity.y = 0;
        }

        // Check Z axis collision
        if (!checkCollision(position.x, position.y, newPosition.z)) {
            position.z = newPosition.z;
        } else {
            velocity.z = 0;
        }
    }

    private boolean checkCollision(float x, float y, float z) {
        // Check collision with blocks in a box around the player
        float minX = x - PLAYER_WIDTH / 2;
        float maxX = x + PLAYER_WIDTH / 2;
        float minY = y;
        float maxY = y + PLAYER_HEIGHT;
        float minZ = z - PLAYER_WIDTH / 2;
        float maxZ = z + PLAYER_WIDTH / 2;

        for (int bx = (int) Math.floor(minX); bx <= (int) Math.floor(maxX); bx++) {
            for (int by = (int) Math.floor(minY); by <= (int) Math.floor(maxY); by++) {
                for (int bz = (int) Math.floor(minZ); bz <= (int) Math.floor(maxZ); bz++) {
                    if (!Block.isTransparent(world.getBlock(bx, by, bz))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void jump() {
        if (onGround) {
            velocity.y = JUMP_FORCE;
            onGround = false;
        }
    }

    public void move(float dx, float dy, float dz) {
        velocity.x = dx;
        velocity.z = dz;
    }

    public void selectSlot(int slot) {
        if (slot >= 0 && slot < Inventory.HOTBAR_SIZE) {
            selectedSlot = slot;
        }
    }

    public boolean placeBlock() {
        ItemStack selectedStack = inventory.getItem(selectedSlot);
        if (selectedStack == null || selectedStack.isEmpty()) {
            return false;
        }

        // Get block position player is looking at
        Vector3f lookDir = new Vector3f(
            (float) Math.sin(Math.toRadians(camera.getYaw())) * (float) Math.cos(Math.toRadians(camera.getPitch())),
            (float) -Math.sin(Math.toRadians(camera.getPitch())),
            (float) Math.cos(Math.toRadians(camera.getYaw())) * (float) Math.cos(Math.toRadians(camera.getPitch()))
        );

        // Ray casting to find block position
        Vector3f pos = new Vector3f(camera.getPosition());
        Vector3f step = new Vector3f(lookDir).normalize().mul(0.05f);
        float maxDistance = 5.0f;
        float distance = 0;

        while (distance < maxDistance) {
            byte block = world.getBlock((int) pos.x, (int) pos.y, (int) pos.z);
            if (!Block.isTransparent(block)) {
                // Place block adjacent to the hit block
                pos.sub(step);
                world.setBlock((int) pos.x, (int) pos.y, (int) pos.z, selectedStack.getBlockType());
                selectedStack.decrease();
                return true;
            }
            pos.add(step);
            distance += 0.05f;
        }

        return false;
    }

    public boolean breakBlock() {
        // Similar to placeBlock, but removes the block instead
        Vector3f lookDir = new Vector3f(
            (float) Math.sin(Math.toRadians(camera.getYaw())) * (float) Math.cos(Math.toRadians(camera.getPitch())),
            (float) -Math.sin(Math.toRadians(camera.getPitch())),
            (float) Math.cos(Math.toRadians(camera.getYaw())) * (float) Math.cos(Math.toRadians(camera.getPitch()))
        );

        Vector3f pos = new Vector3f(camera.getPosition());
        Vector3f step = new Vector3f(lookDir).normalize().mul(0.05f);
        float maxDistance = 5.0f;
        float distance = 0;

        while (distance < maxDistance) {
            int x = (int) pos.x;
            int y = (int) pos.y;
            int z = (int) pos.z;
            byte block = world.getBlock(x, y, z);
            if (!Block.isTransparent(block)) {
                world.setBlock(x, y, z, Block.AIR);
                inventory.addItem(new ItemStack(block, 1));
                return true;
            }
            pos.add(step);
            distance += 0.05f;
        }

        return false;
    }

    public Vector3f getPosition() {
        return position;
    }

    public void setPosition(Vector3f position) {
        this.position = position;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    public int getSelectedSlot() {
        return selectedSlot;
    }
}
