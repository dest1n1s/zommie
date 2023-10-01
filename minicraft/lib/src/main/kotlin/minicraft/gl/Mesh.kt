package minicraft.gl

import minicraft.withBufferObject
import minicraft.withTexture
import minicraft.withVertexArray
import org.lwjgl.opengl.GL30.*

class Mesh(
    private val vertices: FloatArray,
    private val indices: IntArray,
    private val textureCoords: FloatArray,
    private val texture: RefCount<Texture>
) : RenderItem() {

    private var vao: Int = 0
    private val vbos: ArrayList<Int> = ArrayList()
    override fun onSetup() {
        vao = glGenVertexArrays()
        withVertexArray(vao) {
            val vbo = glGenBuffers()
            withBufferObject(GL_ARRAY_BUFFER, vbo) {
                glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW)
                glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0)
            }
            vbos.add(vbo)

            val tbo = glGenBuffers()
            withBufferObject(GL_ARRAY_BUFFER, tbo) {
                glBufferData(GL_ARRAY_BUFFER, textureCoords, GL_STATIC_DRAW)
                glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0)
            }
            vbos.add(tbo)

            val ibo = glGenBuffers()
            withBufferObject(GL_ELEMENT_ARRAY_BUFFER, ibo) {
                glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW)
            }
            vbos.add(ibo)
        }
        texture.setup()
    }

    override fun onRender() {
        glActiveTexture(GL_TEXTURE0)
        withTexture(texture = texture.get().id()) {
            withVertexArray(vao, vertexAttribs = intArrayOf(0, 1)) {
                glDrawElements(GL_TRIANGLES, indices.size, GL_UNSIGNED_INT, 0)
            }
        }
    }

    override fun onDestroy() {
        vbos.forEach { glDeleteBuffers(it) }
        vbos.clear()
        texture.destroy()
        glDeleteVertexArrays(vao)
    }

}