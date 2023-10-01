package minicraft.gl

enum class RenderItemState {
    INIT,
    SETUP,
    DESTROYED
}

abstract class RenderItem: Setupable, Destroyable {
    private var state: RenderItemState = RenderItemState.INIT
    override fun setup() {
        if (state != RenderItemState.INIT) return
        onSetup()
        state = RenderItemState.SETUP
    }

    fun render() {
        if (state != RenderItemState.SETUP) return
        onRender()
    }

    override fun destroy() {
        if (state != RenderItemState.SETUP) return
        onDestroy()
        state = RenderItemState.DESTROYED
    }

    open fun onSetup() {}
    abstract fun onRender()
    open fun onDestroy() {}
}

