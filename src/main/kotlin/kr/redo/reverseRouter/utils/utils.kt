package kr.redo.reverseRouter.utils

import java.net.URLEncoder

fun String.join(strings: List<String>): String {
    val builder = StringBuilder()
    strings.forEachIndexed { i, s ->
        if (i != 0) {
            builder.append(this)
        }
        builder.append(s)
    }
    return builder.toString()
}

fun String.toVariableName():String {
    return "${substring(0, 1).toLowerCase()}${substring(1)}"
}

fun encodeQueryParams(params: List<Pair<String, Any>>): String {
    return "&".join(
            params.map { param ->
                "=".join(listOf(param.first, param.second.toString()).map { URLEncoder.encode(it, "UTF-8") })
            }
    )
}
