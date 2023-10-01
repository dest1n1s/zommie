package minicraft.gl

interface Destroyable {
    fun destroy()
}

interface Setupable {
    fun setup()
}

class RefCount<T>(private val value: T) : Destroyable, Setupable
        where T : Destroyable, T : Setupable {
    private var refCount = 0

    fun get(): T = value
    override fun destroy() {
        refCount--
        if (refCount == 0) {
            value.destroy()
        }
    }

    override fun setup() {
        refCount++
        if (refCount == 1) {
            value.setup()
        }
    }
}