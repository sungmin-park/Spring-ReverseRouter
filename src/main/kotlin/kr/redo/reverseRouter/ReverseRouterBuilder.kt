package kr.redo.reverseRouter

class ReverseRouterBuilder(private val reverseRouter: ReverseRouter, val endpoint: String) {
    private val params = arrayListOf<Pair<String, Any?>>()
    fun add(name: String, value: Any?): ReverseRouterBuilder {
        params.add(name to value)
        return this
    }

    override fun toString(): String {
        return reverseRouter.urlFor(endpoint, *params.toTypedArray())
    }

    fun set(name: String, value: Any?): ReverseRouterBuilder {
        params.removeAll(params.filter { it.first == name })
        return add(name, value)
    }
}