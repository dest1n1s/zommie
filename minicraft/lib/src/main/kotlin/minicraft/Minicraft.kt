package minicraft

import com.google.common.io.ByteStreams
import minicraft.gl.*
import minicraft.render.Camera
import minicraft.render.Cube
import minicraft.render.GameItem
import org.joml.Random
import org.joml.Vector3f
import org.lwjgl.BufferUtils
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL11.glReadPixels
import org.lwjgl.opengl.GL20.glUniformMatrix4fv
import org.lwjgl.opengl.GL30.*
import org.lwjgl.stb.STBImageWrite
import org.lwjgl.system.MemoryUtil
import java.io.File


class Minicraft(
    private val camera: Camera = Camera(),
    private val windowVisible: Boolean = true,
    private val keepRenderLoop: Boolean = true,
    private val windowWidth: Int = 3200,
    private val windowHeight: Int = 2400
) : Setupable, Destroyable {
    private lateinit var shaderProgram: Program
    private var window: Long = 0
    val gameItems: MutableList<GameItem> = mutableListOf()

    companion object {
        private const val BITMAP_ALIGNMENT_BYTES = 4
    }

    override fun setup() {
        if (!glfwInit()) {
            throw IllegalStateException("Unable to initialize GLFW")
        }
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3)
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3)
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE)
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE)
        glfwWindowHint(GLFW_VISIBLE, if (windowVisible) GLFW_TRUE else GLFW_FALSE)
        glfwWindowHint(GLFW_COCOA_MENUBAR, if (windowVisible) GLFW_TRUE else GLFW_FALSE)
        glfwWindowHint(GLFW_SAMPLES, 16)

        window = glfwCreateWindow(windowWidth, windowHeight, "Minicraft", 0, 0)
        if (window == 0L) {
            glfwTerminate()
            throw IllegalStateException("Failed to create window")
        }
        glfwMakeContextCurrent(window)

        GL.createCapabilities()
        glViewport(0, 0, windowWidth, windowHeight)
        glfwSetFramebufferSizeCallback(window) { _, width, height ->
            glViewport(0, 0, width, height)
        }
        glEnable(GL_MULTISAMPLE)
        glEnable(GL_DEPTH_TEST)
//        glEnable(GL_BLEND)
//        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)


        shaderProgram = Program(
            listOf(
                VertexShader(readSourceAsString("shaders/vertex.vert")),
                FragmentShader(readSourceAsString("shaders/fragment.frag"))
            )
        )
        shaderProgram.link(destroyShadersAfterLink = true)

        gameItems.forEach { it.setup() }

        while (!glfwWindowShouldClose(window) && keepRenderLoop) render()
    }


    fun renderFrame(): ByteArray {
        if (keepRenderLoop) throw IllegalStateException("Cannot render frame if we have a render loop")
        render()

        val width = BufferUtils.createIntBuffer(1)
        val height = BufferUtils.createIntBuffer(1)
        glfwGetFramebufferSize(window, width, height)
        val w = width.get()
        val h = height.get()

        val c = 3
        var stride = c * w
        stride += if (stride % BITMAP_ALIGNMENT_BYTES == 0) 0 else BITMAP_ALIGNMENT_BYTES - stride % BITMAP_ALIGNMENT_BYTES

        val size = stride * h
        val imgBuf = BufferUtils.createByteBuffer(size)
        glPixelStorei(GL_PACK_ALIGNMENT, BITMAP_ALIGNMENT_BYTES)
        glReadBuffer(GL_BACK) // Wait, why back buffer?
        glReadPixels(0, 0, w, h, GL_RGB, GL_UNSIGNED_BYTE, imgBuf)
        STBImageWrite.stbi_flip_vertically_on_write(true)

        val pngImgOutput = ByteStreams.newDataOutput()
        STBImageWrite.stbi_write_png_to_func({ _, data, lastSize ->
            val buf = MemoryUtil.memByteBufferSafe(data, lastSize)
                ?: throw IllegalStateException("Cannot read from memory buffer during PNG writing")
            if (buf.hasArray()) {
                pngImgOutput.write(buf.array())
            } else {
                val arr = ByteArray(lastSize)
                buf.get(arr)
                pngImgOutput.write(arr)
            }
        }, 0, w, h, c, imgBuf, stride)

        return pngImgOutput.toByteArray()
    }

    private fun render() {
        glClearColor(0.8f, 0.8f, 0.8f, 1.0f)
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

        shaderProgram.use()

        val cameraBuffer = BufferUtils.createFloatBuffer(16)
        val projBuffer = BufferUtils.createFloatBuffer(16)
        camera.viewMatrix().get(cameraBuffer)

        val viewPorts = intArrayOf(0, 0, 0, 0)
        GL11.glGetIntegerv(GL_VIEWPORT, viewPorts)
        val w = viewPorts[2].toFloat()
        val h = viewPorts[3].toFloat()
        camera.projectionMatrix(w / h).get(projBuffer)
        glUniformMatrix4fv(shaderProgram["camera"], false, cameraBuffer)
        glUniformMatrix4fv(shaderProgram["projection"], false, projBuffer)

        for (gameItem in gameItems) {
            val modelBuffer = BufferUtils.createFloatBuffer(16)

            gameItem.modelMatrix().get(modelBuffer)
            glUniformMatrix4fv(shaderProgram["model"], false, modelBuffer)

            gameItem.render()
        }

        glfwPollEvents()
        glfwSwapBuffers(window)
    }

    override fun destroy() {
        gameItems.forEach { it.destroy() }
        shaderProgram.destroy()
        glfwTerminate()
    }
}

fun main() {
    val cam = Camera(eye= Vector3f(0f, 3f, 3f))
    val minicraft = Minicraft(windowVisible = false, keepRenderLoop = false, camera = cam)
    for(i in -20..20) {
        for(j in -20..20) {
            minicraft.gameItems.add(Cube("textures/grass.png").apply {

                worldPos.set(i.toFloat(), Random().nextInt(2).toFloat(), j.toFloat())
            })
        }
    }
    minicraft.setup()
    val png = minicraft.renderFrame()
    println("Rendered frame: ${png.size} bytes")
    // write to file
    File("rendered.png").writeBytes(png)
    minicraft.destroy()
}