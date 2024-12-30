package game;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Camera {
    private Vector3f position;
    private float pitch;
    private float yaw;
    private Matrix4f projectionMatrix;
    private Matrix4f viewMatrix;
    private boolean viewMatrixDirty;

    public Camera() {
        position = new Vector3f(0, 0, 0);
        pitch = 0;
        yaw = 0;
        projectionMatrix = new Matrix4f();
        viewMatrix = new Matrix4f();
        viewMatrixDirty = true;
        updateProjectionMatrix(1280, 720); // Default window size
    }

    public void move(float dx, float dy, float dz) {
        if (dz != 0) {
            position.x += Math.sin(Math.toRadians(yaw)) * -dz;
            position.z += Math.cos(Math.toRadians(yaw)) * dz;
        }
        if (dx != 0) {
            position.x += Math.sin(Math.toRadians(yaw - 90)) * -dx;
            position.z += Math.cos(Math.toRadians(yaw - 90)) * dx;
        }
        position.y += dy;
        viewMatrixDirty = true;
    }

    public void rotate(float deltaPitch, float deltaYaw) {
        this.pitch += deltaPitch;
        this.yaw += deltaYaw;
        
        // Limit pitch to avoid camera flipping
        if (pitch > 90) {
            pitch = 90;
        } else if (pitch < -90) {
            pitch = -90;
        }
        
        // Keep yaw between 0 and 360 degrees
        if (yaw >= 360) {
            yaw -= 360;
        } else if (yaw < 0) {
            yaw += 360;
        }
        viewMatrixDirty = true;
    }

    public void updateProjectionMatrix(int width, int height) {
        float aspectRatio = (float) width / height;
        projectionMatrix.identity();
        projectionMatrix.perspective((float) Math.toRadians(70.0f), aspectRatio, 0.1f, 1000.0f);
    }

    public Matrix4f getProjectionMatrix() {
        return projectionMatrix;
    }

    public Matrix4f getViewMatrix() {
        if (viewMatrixDirty) {
            updateViewMatrix();
        }
        return viewMatrix;
    }

    private void updateViewMatrix() {
        viewMatrix.identity();
        viewMatrix.rotate((float) Math.toRadians(pitch), new Vector3f(1, 0, 0))
                 .rotate((float) Math.toRadians(yaw), new Vector3f(0, 1, 0));
        viewMatrix.translate(-position.x, -position.y, -position.z);
        viewMatrixDirty = false;
    }

    public Vector3f getPosition() {
        return position;
    }

    public float getPitch() {
        return pitch;
    }

    public float getYaw() {
        return yaw;
    }
}
