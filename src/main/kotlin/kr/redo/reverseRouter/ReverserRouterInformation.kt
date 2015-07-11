package kr.redo.reverseRouter

public data class ReverserRouterInformation(public val beanName: String, public val methodName: String,
                                            public val pathVariables: Map<String, Any>,
                                            public val requestURL: String) {
    public val endpoint: String = "$beanName.$methodName"
}