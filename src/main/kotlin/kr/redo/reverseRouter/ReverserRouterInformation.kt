package kr.redo.reverseRouter

import javax.servlet.http.HttpServletRequest

public data class ReverserRouterInformation(public val beanName: String, public val methodName: String,
                                            public val pathVariables: Map<String, Any>,
                                            public val requestURL: String, private val request: HttpServletRequest) {
    public val endpoint: String = "$beanName.$methodName"


    public val urlPrefix: String by lazy(LazyThreadSafetyMode.NONE) {
        "${request.scheme + "://" + request.serverName}${if (request.serverPort != 80) ":" + request.serverPort else ""}"
    }
}