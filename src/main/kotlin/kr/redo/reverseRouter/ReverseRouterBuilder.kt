package kr.redo.reverseRouter

public class ReverseRouterBuilder(private val reverseRouter: ReverseRouter, public val endpoint: String) {
    private val params = arrayListOf<Pair<String, Any?>>()
    public fun add(name: String, value: Any?): ReverseRouterBuilder {
        params.add(name to value)
        return this
    }

    override fun toString(): String {
        return reverseRouter.urlFor(endpoint, *params.toTypedArray())
    }
}