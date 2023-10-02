package minicraft.render

import org.joml.Matrix4f
import org.joml.Vector3f

class Camera(
    var eye: Vector3f = Vector3f(0f, 0f, 0f),
    var viewCenter: Vector3f = Vector3f(0f, 0f, -1f),
    var fovAngle: Float = 45f,
    var nearPlane: Float = 0.01f,
    var farPlane: Float = 100f,
) {
    fun projectionMatrix(aspectRatio: Float): Matrix4f =
        Matrix4f().perspective(fovAngle, aspectRatio, nearPlane, farPlane)

    fun viewMatrix(): Matrix4f =
        Matrix4f().lookAt(eye, viewCenter, Vector3f(0f, 1f, 0f))
}