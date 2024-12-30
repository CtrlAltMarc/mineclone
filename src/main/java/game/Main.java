package game;

import game.renderer.Shader;
import game.world.World;
import game.world.WorldSaveManager;
import game.hud.HUD;
import game.entity.Player;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

public class Main {
    private static final int WIDTH = 1280;
    private static final int HEIGHT = 720;
    private long window;
    private Camera camera;
    private Input input;
    private World world;
    private Shader shader;
    private Player player;
    private HUD hud;
    private WorldSaveManager saveManager;
    private long lastFrameTime;
    private int frames;
    private long lastFPSTime;
    private int fps;

    public void run() {
        init();
        loop();
        cleanup();
    }

    private void init() {
        if (!GLFW.glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);

        window = GLFW.glfwCreateWindow(WIDTH, HEIGHT, "Minecraft Clone", 0, 0);
        if (window == 0) {
            throw new RuntimeException("Failed to create GLFW window");
        }

        GLFW.glfwSetWindowPos(
            window,
            (GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor()).width() - WIDTH) / 2,
            (GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor()).height() - HEIGHT) / 2
        );

        GLFW.glfwMakeContextCurrent(window);
        GLFW.glfwSwapInterval(1);
        GLFW.glfwShowWindow(window);
        
        GL.createCapabilities();
        
        // Initialize OpenGL
        GL11.glClearColor(0.529f, 0.808f, 0.922f, 0.0f);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glCullFace(GL11.GL_BACK);

        try {
            // Initialize shader
            shader = new Shader();
            shader.createVertexShader(loadShaderSource("/shaders/chunk.vert"));
            shader.createFragmentShader(loadShaderSource("/shaders/chunk.frag"));
            shader.link();
            
            // Create uniforms for 3D rendering
            shader.createUniform("projectionMatrix");
            shader.createUniform("viewMatrix");
            shader.createUniform("useTexture");
            shader.createUniform("color");
            
            // Initialize HUD
            hud = new HUD(WIDTH, HEIGHT);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }

        // Initialize camera and input
        camera = new Camera();
        world = new World();
        player = new Player(world, camera);
        input = new Input(window, camera, player);
        
        // Initialize save manager
        saveManager = new WorldSaveManager("world1");
        
        // Try to load existing save
        try {
            saveManager.loadWorld(world, player);
        } catch (Exception e) {
            System.out.println("No existing save found, starting new world");
        }
        
        lastFrameTime = System.currentTimeMillis();
        lastFPSTime = lastFrameTime;
    }

    private String loadShaderSource(String path) {
        try (InputStream is = getClass().getResourceAsStream(path)) {
            if (is == null) {
                throw new RuntimeException("Could not find shader file: " + path);
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                return reader.lines().collect(Collectors.joining("\n"));
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load shader: " + path, e);
        }
    }

    private void loop() {
        while (!GLFW.glfwWindowShouldClose(window)) {
            long currentTime = System.currentTimeMillis();
            float deltaTime = (currentTime - lastFrameTime) / 1000.0f;
            lastFrameTime = currentTime;
            
            // Update FPS counter
            frames++;
            if (currentTime - lastFPSTime > 1000) {
                fps = frames;
                frames = 0;
                lastFPSTime = currentTime;
            }

            // Update game state
            input.update(deltaTime);
            player.update(deltaTime);
            
            // Clear the screen
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
            
            // Render 3D world
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            GL11.glEnable(GL11.GL_CULL_FACE);
            GL11.glEnable(GL11.GL_LINE_SMOOTH);
            GL11.glLineWidth(1.0f);
            
            shader.bind();
            shader.setUniform("projectionMatrix", camera.getProjectionMatrix());
            shader.setUniform("viewMatrix", camera.getViewMatrix());
            shader.setUniform("useTexture", false);
            shader.setUniform("color", 0.5f, 0.8f, 0.3f, 1.0f);
            world.render();
            
            // Draw wireframe
            GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
            shader.setUniform("color", 0.0f, 0.0f, 0.0f, 1.0f);
            world.render();
            GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
            
            shader.unbind();
            
            // Render HUD
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            GL11.glDisable(GL11.GL_CULL_FACE);
            hud.render(player);
            
            GLFW.glfwSwapBuffers(window);
            GLFW.glfwPollEvents();
            
            // Auto-save every 5 minutes
            if (currentTime % (5 * 60 * 1000) < 100) {
                saveManager.saveWorld(world, player);
            }
        }
        
        // Save before exit
        saveManager.saveWorld(world, player);
    }

    private void cleanup() {
        input.cleanup();
        world.cleanup();
        shader.cleanup();
        hud.cleanup();
        GLFW.glfwDestroyWindow(window);
        GLFW.glfwTerminate();
    }

    public static void main(String[] args) {
        new Main().run();
    }
}
