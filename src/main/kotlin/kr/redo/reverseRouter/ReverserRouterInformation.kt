package kr.redo.reverseRouter

import java.util.regex.Pattern
import javax.servlet.http.HttpServletRequest

data class ReverserRouterInformation(val beanName: String, val methodName: String,
                                     val pathVariables: Map<String, Any>,
                                     val parameterMap: Map<String, Array<out String>>,
                                     val requestURL: String, private val request: HttpServletRequest) {
    val endpoint: String = "$beanName.$methodName"

    val urlPrefix: String by lazy(LazyThreadSafetyMode.NONE) {
        val scheme = findCfVisitorScheme() ?: request.getHeader("X-Forwarded-Proto") ?: request.scheme
        "${scheme + "://" + request.serverName}${if (request.serverPort != 80) ":" + request.serverPort else ""}"
    }

    private val cfVisitorSchemePattern = Pattern.compile(".*\"scheme\":\"([^\"]+)\".*")
    private fun findCfVisitorScheme(): String? {
        val header = request.getHeader("CF-Visitor") ?: return null
        val matcher = cfVisitorSchemePattern.matcher(header)
        if (!matcher.matches()) {
            return null
        }
        return matcher.group(1)
    }
}