package kr.redo.reverseRouter

data class ReverserRouterInformation(val beanName: String, val methodName: String,
                                     val pathVariables: Map<String, Any>,
                                     val parameterMap: Map<String, Array<out String>>,
                                     val requestURL: String, val contextPath: String,
                                     val urlPrefix: String) {
    val endpoint: String = "$beanName.$methodName"
}

val defaultReverserRouterInformation = ReverserRouterInformation(
        "", "", mapOf(), mapOf(), "", "", ""
)