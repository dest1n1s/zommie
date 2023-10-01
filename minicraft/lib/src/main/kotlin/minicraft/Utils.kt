package minicraft

import org.lwjgl.opengl.GL30.*

inline fun withVertexArray(vao: Int, vertexAttribs: IntArray = intArrayOf(), block: () -> Unit) {
    glBindVertexArray(vao)
    vertexAttribs.forEach { glEnableVertexAttribArray(it) }
    block()
    vertexAttribs.forEach { glDisableVertexAttribArray(it) }
    glBindVertexArray(0)
}

inline fun withBufferObject(type: Int, vbo: Int, block: () -> Unit) {
    glBindBuffer(type, vbo)
    block()
    if(type != GL_ELEMENT_ARRAY_BUFFER) {
        // Element array buffer is stored in a VAO and should not be unbound.
        glBindBuffer(type, 0)
    }
}

inline fun withTexture(type: Int = GL_TEXTURE_2D, texture: Int, block: () -> Unit) {
    glBindTexture(type, texture)
    block()
    glBindTexture(type, 0)
}

fun readSourceAsString(path: String): String = Utils::class.java.classLoader.getResource(path)?.readText()
    ?: throw IllegalStateException("Failed to read shader source")

fun readSourceAsBytes(path: String): ByteArray = Utils::class.java.classLoader.getResource(path)?.readBytes()
    ?: throw IllegalStateException("Failed to read shader source")

class Utils