package kr.redo.reverseRouter

import javax.servlet.http.HttpServletRequest
import kotlin.properties.Delegates

public data class ReverserRouterInformation(public val beanName: String, public val methodName: String,
                                            public val pathVariables: Map<String, Any>,
                                            public val requestURL: String, private val request: HttpServletRequest) {
    public val endpoint: String = "$beanName.$methodName"


    public val urlPrefix: String by Delegates.lazy {
        "${request.getScheme() + "://" + request.getServerName()}${if (request.getServerPort() != 80) ":" + request.getServerPort() else ""}"
    }
}