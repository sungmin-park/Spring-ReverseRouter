package kr.redo.reverseRouter.kotlin

import kr.redo.reverseRouter.ReverseRouter

object reverseRouter : ReverseRouter()

fun urlFor(endpoint: String, vararg args: Pair<String, Any?>): String {
    return reverseRouter.urlFor(endpoint = endpoint, args = *args)
}

