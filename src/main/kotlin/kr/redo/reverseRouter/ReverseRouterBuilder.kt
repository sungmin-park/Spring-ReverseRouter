package kr.redo.reverseRouter

data class ReverseRouterBuilder(private val reverseRouter: ReverseRouter, val endpoint: String, private val params: List<Pair<String, Any?>> = listOf<Pair<String, Any?>>()) {
    fun add(name: String, value: Any?): ReverseRouterBuilder {
        return ReverseRouterBuilder(reverseRouter, endpoint, params + (name to value))
    }

    override fun toString(): String {
        return reverseRouter.urlFor(endpoint, *params.toTypedArray())
    }

    fun asExternal(): ReverseRouterBuilder {
        return set("_external", true)
    }

    fun set(name: String, value: Any?): ReverseRouterBuilder {
        return ReverseRouterBuilder(reverseRouter, endpoint, params.filter { it.first != name } + (name to value))
    }
}