package kr.redo.reverseRouter.utils

import java.net.URLEncoder
import java.util.regex.Pattern
import javax.servlet.http.HttpServletRequest

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

fun String.toVariableName(): String {
    return "${substring(0, 1).toLowerCase()}${substring(1)}"
}

fun encodeQueryParams(params: List<Pair<String, Any>>): String {
    return "&".join(
            params.map { param ->
                "=".join(listOf(param.first, param.second.toString()).map { URLEncoder.encode(it, "UTF-8") })
            }
    )
}

fun HttpServletRequest.urlPrefix(): String {
    val scheme = findCfVisitorScheme() ?: getHeader("X-Forwarded-Proto") ?: scheme
    return "${"$scheme://$serverName"}${if (serverPort != 80) ":$serverPort" else ""}"
}

private fun HttpServletRequest.findCfVisitorScheme(): String? {
    val header = getHeader("CF-Visitor") ?: return null
    val matcher = cfVisitorSchemePattern.matcher(header)
    if (!matcher.matches()) {
        return null
    }
    return matcher.group(1)
}

private val cfVisitorSchemePattern = Pattern.compile(".*\"scheme\":\"([^\"]+)\".*")
