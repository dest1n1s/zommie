package minicraft.render

import minicraft.gl.Mesh
import minicraft.gl.RenderItem
import org.joml.Matrix4f
import org.joml.Vector3f

abstract class GameItem(
    protected var mesh: Mesh?,
) : RenderItem() {
    private val worldPos = Vector3f()
    private val rotation = Vector3f()
    private var scale = 1f

    fun modelMatrix() = Matrix4f()
        .translate(worldPos)
        .rotateX(rotation.x)
        .rotateY(rotation.y)
        .rotateZ(rotation.z)
        .scale(scale)

    override fun onSetup() {
        super.onSetup()
        mesh?.setup()
    }

    override fun onRender() {
        mesh?.render()
    }

    override fun onDestroy() {
        super.onDestroy()
        mesh?.destroy()
    }
}