package game;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWScrollCallback;
import game.entity.Player;

public class Input {
    private static final float MOUSE_SENSITIVITY = 0.1f;
    private static final float MOVEMENT_SPEED = 5.0f;
    
    private Camera camera;
    private Player player;
    private boolean[] keys;
    private boolean[] mouseButtons;
    private double lastX;
    private double lastY;
    private boolean firstMouse;
    
    private GLFWKeyCallback keyCallback;
    private GLFWCursorPosCallback mouseCallback;
    private GLFWMouseButtonCallback mouseButtonCallback;
    private GLFWScrollCallback scrollCallback;

    public Input(long window, Camera camera, Player player) {
        this.camera = camera;
        this.player = player;
        this.keys = new boolean[GLFW.GLFW_KEY_LAST];
        this.mouseButtons = new boolean[GLFW.GLFW_MOUSE_BUTTON_LAST];
        this.firstMouse = true;
        
        // Set up keyboard callback
        GLFW.glfwSetKeyCallback(window, keyCallback = new GLFWKeyCallback() {
            @Override
            public void invoke(long window, int key, int scancode, int action, int mods) {
                if (key >= 0 && key < keys.length) {
                    if (action == GLFW.GLFW_PRESS) {
                        keys[key] = true;
                    } else if (action == GLFW.GLFW_RELEASE) {
                        keys[key] = false;
                    }
                }
                
                // Number keys for inventory selection
                if (action == GLFW.GLFW_PRESS && key >= GLFW.GLFW_KEY_1 && key <= GLFW.GLFW_KEY_9) {
                    player.selectSlot(key - GLFW.GLFW_KEY_1);
                }
                
                if (key == GLFW.GLFW_KEY_ESCAPE && action == GLFW.GLFW_PRESS) {
                    GLFW.glfwSetWindowShouldClose(window, true);
                }
            }
        });
        
        // Set up mouse button callback
        GLFW.glfwSetMouseButtonCallback(window, mouseButtonCallback = new GLFWMouseButtonCallback() {
            @Override
            public void invoke(long window, int button, int action, int mods) {
                if (button >= 0 && button < mouseButtons.length) {
                    if (action == GLFW.GLFW_PRESS) {
                        mouseButtons[button] = true;
                    } else if (action == GLFW.GLFW_RELEASE) {
                        mouseButtons[button] = false;
                    }
                }
            }
        });
        
        // Set up mouse movement callback
        GLFW.glfwSetCursorPosCallback(window, mouseCallback = new GLFWCursorPosCallback() {
            @Override
            public void invoke(long window, double xpos, double ypos) {
                if (firstMouse) {
                    lastX = xpos;
                    lastY = ypos;
                    firstMouse = false;
                }
                
                float xoffset = (float) (xpos - lastX) * MOUSE_SENSITIVITY;
                float yoffset = (float) (ypos - lastY) * MOUSE_SENSITIVITY;
                
                lastX = xpos;
                lastY = ypos;
                
                camera.rotate(yoffset, xoffset);
            }
        });
        
        // Set up scroll callback for inventory selection
        GLFW.glfwSetScrollCallback(window, scrollCallback = new GLFWScrollCallback() {
            @Override
            public void invoke(long window, double xoffset, double yoffset) {
                int currentSlot = player.getSelectedSlot();
                if (yoffset > 0) {
                    currentSlot = (currentSlot - 1 + 9) % 9;
                } else if (yoffset < 0) {
                    currentSlot = (currentSlot + 1) % 9;
                }
                player.selectSlot(currentSlot);
            }
        });
        
        // Capture the cursor
        GLFW.glfwSetInputMode(window, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_DISABLED);
    }

    public void update(float deltaTime) {
        float dx = 0, dz = 0;
        
        if (keys[GLFW.GLFW_KEY_W]) {
            dz += MOVEMENT_SPEED;
        }
        if (keys[GLFW.GLFW_KEY_S]) {
            dz -= MOVEMENT_SPEED;
        }
        if (keys[GLFW.GLFW_KEY_A]) {
            dx -= MOVEMENT_SPEED;
        }
        if (keys[GLFW.GLFW_KEY_D]) {
            dx += MOVEMENT_SPEED;
        }
        
        // Apply movement relative to camera direction
        float yaw = (float) Math.toRadians(camera.getYaw());
        float moveX = (float) (dx * Math.cos(yaw) - dz * Math.sin(yaw));
        float moveZ = (float) (dx * Math.sin(yaw) + dz * Math.cos(yaw));
        
        player.move(moveX, 0, moveZ);
        
        if (keys[GLFW.GLFW_KEY_SPACE]) {
            player.jump();
        }
        
        // Handle block interaction
        if (mouseButtons[GLFW.GLFW_MOUSE_BUTTON_LEFT]) {
            player.breakBlock();
        }
        if (mouseButtons[GLFW.GLFW_MOUSE_BUTTON_RIGHT]) {
            player.placeBlock();
        }
    }

    public void cleanup() {
        keyCallback.free();
        mouseCallback.free();
        mouseButtonCallback.free();
        scrollCallback.free();
    }
}
