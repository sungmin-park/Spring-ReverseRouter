package kr.redo.reverseRouter

import javax.servlet.http.HttpServletRequest

data class ReverserRouterInformation(val beanName: String, val methodName: String,
                                            val pathVariables: Map<String, Any>,
                                            val requestURL: String, private val request: HttpServletRequest) {
    val endpoint: String = "$beanName.$methodName"

    val urlPrefix: String by lazy(LazyThreadSafetyMode.NONE) {
        val scheme = request.getHeader("X-Forwarded-Proto") ?: request.scheme
        "${scheme + "://" + request.serverName}${if (request.serverPort != 80) ":" + request.serverPort else ""}"
    }
}