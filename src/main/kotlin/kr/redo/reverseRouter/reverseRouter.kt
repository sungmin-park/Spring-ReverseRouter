package kr.redo.reverseRouter

import kr.redo.reverseRouter.utils.encodeQueryParams
import kr.redo.reverseRouter.utils.join
import kr.redo.reverseRouter.utils.toVariableName
import kr.redo.reverseRouter.utils.urlPrefix
import org.springframework.context.ApplicationListener
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.servlet.HandlerMapping
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping
import org.springframework.web.util.UriComponents
import org.springframework.web.util.UriComponentsBuilder
import java.util.regex.Pattern
import javax.inject.Inject
import javax.naming.ConfigurationException
import javax.servlet.DispatcherType
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


class PatternCompiler(val pattern: String) {
    companion object {
        val NAMES_PATTERN = Pattern.compile("\\{([^/]+?)}")!!
    }

    val uriComponentsBuilder: UriComponents
    val pathVariables: List<String>

    init {
        val matcher = NAMES_PATTERN.matcher(pattern)
        val pathVariables = arrayListOf<String>()
        while (matcher.find()) {
            pathVariables.add(matcher.group(1))
        }
        this.pathVariables = pathVariables
        uriComponentsBuilder = UriComponentsBuilder.fromPath(pattern).build()
    }

    fun canCompile(args: List<Pair<String, Any>>): Boolean {
        val keys = args.map { it.first }
        return pathVariables.firstOrNull({ it !in keys }) == null
    }

    fun compile(args: List<Pair<String, Any>>): String {
        val pathMap = hashMapOf<String, String>()
        val queryParams = arrayListOf<Pair<String, Any>>()
        args.forEach {
            if (it.first in pathVariables) {
                pathMap.put(it.first, it.second.toString())
            } else {
                queryParams.add(it)
            }
        }
        val uri = uriComponentsBuilder.expand(pathMap).encode().toString()
        if (queryParams.size == 0) {
            return uri
        }
        val separator = if ("?" in uri) "&" else "?"
        return separator.join(listOf(uri, encodeQueryParams(params = queryParams)))
    }
}

open class ReverseRouter : ApplicationListener<ContextRefreshedEvent>, HandlerInterceptor {
    @Inject
    private lateinit var request: HttpServletRequest

    var initialized = false
    private val map: Map<String, List<PatternCompiler>> by lazy(LazyThreadSafetyMode.NONE) {
        assert(initialized)
        hashMapOf<String, List<PatternCompiler>>()
    }
    private val REVERSER_ROUTER_INFORMATION = ReverserRouterInformation::class.java.name

    val current: ReverserRouterInformation
        get() {
            val attribute = request.getAttribute(REVERSER_ROUTER_INFORMATION)
                    ?: throw ConfigurationException("ReverseRouter dose not configured as an interceptor.")
            return attribute as ReverserRouterInformation
        }

    val builder: ReverseRouterBuilder get() = builderFor(current.endpoint)
    val currentBuilder: ReverseRouterBuilder
        get() {
            val pathVariableBuilder = current.pathVariables.entries
                    .fold(builder) { builder, (name, value) ->
                        builder.add(name, value)
                    }
            return current.parameterMap.entries.fold(pathVariableBuilder) { builder, (name, parameters) ->
                parameters.fold(builder) { parameterBuilder, parameter -> parameterBuilder.add(name, parameter) }
            }
        }

    override fun onApplicationEvent(event: ContextRefreshedEvent?) {
        if (event == null) {
            return
        }
        val context = event.applicationContext
        initialize(context.getBean(RequestMappingHandlerMapping::class.java))
    }

    override fun preHandle(request: HttpServletRequest?, response: HttpServletResponse?, handler: Any?): Boolean {
        if (handler is HandlerMethod) {
            val (baseName, methodName) = handler.endpoint
            @Suppress("UNCHECKED_CAST")
            val pathVariables =
                    request!!.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE) as Map<String, Any>?
                            ?: mapOf()

            val requestURL: String
            if (request.dispatcherType == DispatcherType.INCLUDE) {
                requestURL = request.getAttribute("javax.servlet.include.request_uri") as String
            } else {
                if (request.queryString != null) {
                    requestURL = request.requestURI + "?" + request.queryString
                } else {
                    requestURL = request.requestURI
                }
            }
            request.setAttribute(
                    REVERSER_ROUTER_INFORMATION,
                    ReverserRouterInformation(
                            baseName, methodName, pathVariables, request.parameterMap.toMap(), requestURL,
                            request.contextPath, request.urlPrefix()
                    )
            )
        }
        return true
    }

    override fun postHandle(request: HttpServletRequest?, response: HttpServletResponse?, handler: Any?, modelAndView: ModelAndView?) {
    }

    override fun afterCompletion(request: HttpServletRequest?, response: HttpServletResponse?, handler: Any?, ex: Exception?) {
    }

    fun initialize(requestMappingHandlerMapping: RequestMappingHandlerMapping) {
        initialized = true
        val map: MutableMap<String, List<PatternCompiler>> = this.map as MutableMap

        val mappings = requestMappingHandlerMapping.handlerMethods
        for ((info, handlerMethod) in mappings) {
            handlerMethod.method ?: continue
            val (beanName, methodName) = handlerMethod.endpoint
            val endpoint = "$beanName.$methodName"

            assert(!map.containsKey(endpoint))

            map.put(endpoint, info.patternsCondition.patterns.map { PatternCompiler(it) })
        }
    }

    fun urlFor(endpoint: String): String {
        return urlFor(endpoint, *arrayOf())
    }

    fun urlFor(endpoint: String, name: String, value: Any?, vararg values: Any?): String {
        val params = mergeToArrayOfPairs(name, value, values)
        return urlFor(endpoint, *params)
    }

    private fun mergeToArrayOfPairs(name: String, value: Any?, values: Array<out Any?>) =
            (arrayOf(name to value) + (0..(values.size / 2) - 1)
                    .map { it * 2 }
                    .map { values[it] as String to values[it + 1] })

    fun urlFor(endpoint: String, vararg args: Pair<String, Any?>): String {
        if (endpoint.startsWith('.')) {
            return urlFor("${current.beanName}$endpoint", *args)
        }
        var external = false
        @Suppress("UNCHECKED_CAST")
        val params = args
                .filter { it.second != null }
                .filter {
                    if (it.first == "_external") {
                        external = true
                        return@filter false
                    }
                    true
                } as List<Pair<String, Any>>
        val patterns = map[endpoint] ?: throw IllegalArgumentException("Not found $endpoint")
        println("request.contextPath = ${request.contextPath}")
        patterns
                .filter { it.canCompile(params) }
                .map { it.compile(params) }
                .forEach { return "${if (external) current.urlPrefix else ""}${current.contextPath}$it" }
        throw IllegalArgumentException("Can not compile $endpoint with $params for ${patterns.map { it.pattern }.joinToString()}")
    }

    private val HandlerMethod.endpoint: Pair<String, String>
        get() {
            return beanType.simpleName.replace("Controller$".toRegex(), "").toVariableName() to method.name
        }

    fun currentFor(vararg args: Pair<String, Any?>): String {
        return urlFor(current.endpoint, *args)
    }

    fun currentFor(): String {
        return currentFor(*arrayOf())
    }

    fun currentFor(name: String, value: Any?, vararg values: Any?): String {
        return currentFor(*mergeToArrayOfPairs(name, value, values))
    }

    fun builderFor(endpoint: String): ReverseRouterBuilder {
        return ReverseRouterBuilder(this, endpoint)
    }

    // offline or test 용 기능
    fun setReverserRouterInformation(information: ReverserRouterInformation) {
        request.setAttribute(REVERSER_ROUTER_INFORMATION, information)
    }
}


