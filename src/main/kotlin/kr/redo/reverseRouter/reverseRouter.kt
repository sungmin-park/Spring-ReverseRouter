package kr.redo.reverseRouter

import kr.redo.reverseRouter.utils.encodeQueryParams
import kr.redo.reverseRouter.utils.join
import kr.redo.reverseRouter.utils.toVariableName
import org.springframework.context.ApplicationListener
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping
import org.springframework.web.util.UriComponents
import org.springframework.web.util.UriComponentsBuilder
import java.util.regex.Pattern
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

open class ReverseRouter : ApplicationListener<ContextRefreshedEvent> {
    override fun onApplicationEvent(event: ContextRefreshedEvent?) {
        if (event == null) {
            return
        }
        val context = event.getApplicationContext()
        val requestMappingHandlerMapping = context.getBean(javaClass<RequestMappingHandlerMapping>())
        initialize(requestMappingHandlerMapping)
    }

    var initialized = false
    val map: Map<String, List<PatternCompiler>> by Delegates.lazy {
        assert(initialized)
        hashMapOf<String, List<PatternCompiler>>()
    }

    fun initialize(requestMappingHandlerMapping: RequestMappingHandlerMapping) {
        initialized = true
        val map: MutableMap<String, List<PatternCompiler>> = this.map as MutableMap

        val mappings = requestMappingHandlerMapping.getHandlerMethods()
        for ((info, handlerMethod) in mappings) {
            val method = handlerMethod.getMethod() ?: continue
            val beanName = handlerMethod
                    .getBeanType()
                    .getSimpleName()
                    .replace("Controller$".toRegex(), "")
                    .toVariableName()
            val endpoint = "$beanName.${method.getName()}"

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
}
