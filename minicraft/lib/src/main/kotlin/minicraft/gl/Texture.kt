package minicraft.gl

import minicraft.readSourceAsBytes
import minicraft.withTexture
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11.glTexImage2D
import org.lwjgl.opengl.GL30.*
import org.lwjgl.stb.STBImage
import java.nio.ByteBuffer

class Texture(
    private val bytes: ByteArray
) : Setupable, Destroyable {
    private var id: Int? = null

    fun id() = id ?: throw IllegalStateException("Texture not initialized")

    override fun setup() {
        if (id != null) return
        val h = BufferUtils.createIntBuffer(1)
        val w = BufferUtils.createIntBuffer(1)
        val c = BufferUtils.createIntBuffer(1)

        val heapImgBuf = ByteBuffer.wrap(bytes)
        val directImgBuf = ByteBuffer.allocateDirect(heapImgBuf.limit()).put(heapImgBuf).flip()
        val texBuf = STBImage.stbi_load_from_memory(directImgBuf, w, h, c, 0)
            ?: throw IllegalStateException("Failed to load texture: ${STBImage.stbi_failure_reason()}")

        val format = when (c.get()) {
            3 -> GL_RGB
            4 -> GL_RGBA
            else -> throw IllegalStateException("Unsupported number of channels: ${c.get()}")
        }

        val id = glGenTextures()

        withTexture(texture = id) {
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST)

            glTexImage2D(GL_TEXTURE_2D, 0, format, w.get(), h.get(), 0, format, GL_UNSIGNED_BYTE, texBuf)
            glGenerateMipmap(GL_TEXTURE_2D)
        }

        STBImage.stbi_image_free(texBuf)

        this.id = id
    }

    override fun destroy() {
        id?.let {
            glDeleteTextures(it)
            id = null
        }
    }

    companion object {
        private val caches: HashMap<String, RefCount<Texture>> = hashMapOf()
        fun fromResource(resourcePath: String): RefCount<Texture> =
            caches.getOrPut(resourcePath) { return@getOrPut RefCount(Texture(readSourceAsBytes(resourcePath))) }
    }
}
