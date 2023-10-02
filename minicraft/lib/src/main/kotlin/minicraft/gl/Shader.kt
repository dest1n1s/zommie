package minicraft.gl

import org.lwjgl.opengl.GL30.*

abstract class Shader(protected val source: String) {
    protected var id: Int? = null

    fun id() = id ?: throw IllegalStateException("Shader not compiled yet")

    abstract fun compile()

    fun destroy() {
        id?.let {
            glDeleteShader(it)
            id = null
        }
    }
}

class VertexShader(source: String) : Shader(source) {
    override fun compile() {
        if (id != null) return

        val shader = glCreateShader(GL_VERTEX_SHADER)
        glShaderSource(shader, source)
        glCompileShader(shader)
        if (glGetShaderi(shader, GL_COMPILE_STATUS) == GL_FALSE) {
            throw IllegalStateException("Failed to compile vertex shader: ${glGetShaderInfoLog(shader)}")
        }
        id = shader
    }

}

class FragmentShader(source: String) : Shader(source) {
    override fun compile() {
        if (id != null) return

        val shader = glCreateShader(GL_FRAGMENT_SHADER)
        glShaderSource(shader, source)
        glCompileShader(shader)
        if (glGetShaderi(shader, GL_COMPILE_STATUS) == GL_FALSE) {
            throw IllegalStateException("Failed to compile fragment shader: ${glGetShaderInfoLog(shader)}")
        }
        id = shader
    }
}

class Program(private val shaders: List<Shader>) {
    private var program: Int? = null

    operator fun get(name: String) =
        glGetUniformLocation(program ?: throw IllegalStateException("Program not linked yet"), name)

    fun link(destroyShadersAfterLink: Boolean = false) {
        if (program != null) return

        val program = glCreateProgram()
        this.program = program

        shaders.forEach { it.compile() }
        shaders.forEach { glAttachShader(program, it.id()) }
        glLinkProgram(program)
        if (glGetProgrami(program, GL_LINK_STATUS) == GL_FALSE) {
            throw IllegalStateException("Failed to link shader program: ${glGetProgramInfoLog(program)}")
        }

        if (destroyShadersAfterLink) shaders.forEach { it.destroy() }
    }

    fun destroy() {
        program?.let {
            shaders.forEach(Shader::destroy)
            glDeleteProgram(it)
            program = null
        }
    }

    fun use() = glUseProgram(program ?: throw IllegalStateException("Program not linked yet"))
}