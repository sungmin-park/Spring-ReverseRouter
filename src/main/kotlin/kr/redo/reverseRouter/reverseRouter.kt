package kr.redo.reverseRouter

import kr.redo.reverseRouter.utils.encodeQueryParams
import kr.redo.reverseRouter.utils.join
import kr.redo.reverseRouter.utils.toVariableName
import org.springframework.context.ApplicationListener
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping
import org.springframework.web.util.UriComponents
import org.springframework.web.util.UriComponentsBuilder
import java.util.regex.Pattern
import javax.inject.Inject
import javax.naming.ConfigurationException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import kotlin.properties.Delegates


class PatternCompiler(pattern: String) {
    companion object {
        val NAMES_PATTERN = Pattern.compile("\\{([^/]+?)\\}")
    }

    val pattern: String
    val uriComponentsBuilder: UriComponents
    val pathVariables: List<String>

    init {
        this.pattern = pattern
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
        val queryParams = arrayListOf<Pair<String, Any>>();
        args.forEach {
            if (it.first in pathVariables) {
                pathMap.put(it.first, it.second.toString())
            } else {
                queryParams.add(it)
            }
        }
        val uri = uriComponentsBuilder.expand(pathMap).toString()
        if (queryParams.size() == 0) {
            return uri;
        }
        val separator = if ("?" in uri) "&" else "?"
        return separator.join(listOf(uri, encodeQueryParams(params = queryParams)));
    }
}

open class ReverseRouter : ApplicationListener<ContextRefreshedEvent>, HandlerInterceptor {
    Inject private var request: HttpServletRequest? = null

    var initialized = false
    private val map: Map<String, List<PatternCompiler>> by Delegates.lazy {
        assert(initialized)
        hashMapOf<String, List<PatternCompiler>>()
    }
    private val REVERSER_ROUTER_INFORMATION = javaClass<ReverserRouterInformation>().getTypeName()

    public val current: ReverserRouterInformation
        get() {
            val attribute = request!!.getAttribute(REVERSER_ROUTER_INFORMATION) ?:
                    throw ConfigurationException("ReverseRouter dose not configured as an interceptor.")
            return attribute as ReverserRouterInformation
        }

    override fun onApplicationEvent(event: ContextRefreshedEvent?) {
        if (event == null) {
            return
        }
        val context = event.getApplicationContext()
        initialize(context.getBean(javaClass<RequestMappingHandlerMapping>()))
    }

    override fun preHandle(request: HttpServletRequest?, response: HttpServletResponse?, handler: Any?): Boolean {
        if (handler is HandlerMethod) {
            val (baseName, methodName) = handler.endpoint
            request?.setAttribute(REVERSER_ROUTER_INFORMATION, ReverserRouterInformation(baseName, methodName))
        }
        return true;
    }

    override fun postHandle(request: HttpServletRequest?, response: HttpServletResponse?, handler: Any?, modelAndView: ModelAndView?) {
    }

    override fun afterCompletion(request: HttpServletRequest?, response: HttpServletResponse?, handler: Any?, ex: Exception?) {
    }

    fun initialize(requestMappingHandlerMapping: RequestMappingHandlerMapping) {
        initialized = true
        val map: MutableMap<String, List<PatternCompiler>> = this.map as MutableMap

        val mappings = requestMappingHandlerMapping.getHandlerMethods()
        for ((info, handlerMethod) in mappings) {
            handlerMethod.getMethod() ?: continue
            val (beanName, methodName) = handlerMethod.endpoint
            val endpoint = "$beanName.$methodName"

            assert(!map.containsKey(endpoint))

            map.put(endpoint, info.getPatternsCondition().getPatterns().map { PatternCompiler(it) })
        }
    }

    fun urlFor(endpoint: String, vararg args: Pair<String, Any?>): String {
        @suppress("UNCHECKED_CAST")
        val params = args.filter { it.second != null } as List<Pair<String, Any>>
        val patterns = map.get(endpoint)!!
        for (pattern in patterns) {
            if (pattern.canCompile(params)) {
                return pattern.compile(params)
            }
        }
        throw IllegalArgumentException()
    }

    private val HandlerMethod.endpoint: Pair<String, String>
        get() {
            return getBeanType().getSimpleName().replace("Controller$".toRegex(), "").toVariableName() to getMethod().getName()
        }
}
