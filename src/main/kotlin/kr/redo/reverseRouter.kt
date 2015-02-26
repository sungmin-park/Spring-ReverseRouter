package kr.redo.reverseRouter

import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping
import kotlin.properties.Delegates
import org.springframework.web.util.UriComponentsBuilder
import org.springframework.web.util.UriComponents
import org.springframework.context.ApplicationListener
import org.springframework.context.event.ContextRefreshedEvent
import java.util.regex.Pattern
import kr.redo.reverseRouter.utils.encodeQueryParams
import kr.redo.reverseRouter.utils.toVariableName
import kr.redo.reverseRouter.utils.join


class PatternCompiler(pattern: String) {
    class object {
        val NAMES_PATTERN = Pattern.compile("\\{([^/]+?)\\}")
    }

    val pattern: String
    val uriComponentsBuilder: UriComponents
    val pathVariables: List<String>

    {
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

class ReverseRouter : ApplicationListener<ContextRefreshedEvent> {
    override fun onApplicationEvent(event: ContextRefreshedEvent?) {
        if (event == null) {
            return
        }
        val requestMappingHandlerMapping =
                event.getApplicationContext().getBean("requestMappingHandlerMapping") as RequestMappingHandlerMapping
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
            val method = handlerMethod.getMethod()
            if (method == null) {
                continue
            }
            val beanName = handlerMethod.getBeanType().getSimpleName().replaceAll("Controller$", "").toVariableName()
            val endpoint = "$beanName.${method.getName()}"

            assert(!map.containsKey(endpoint))

            map.put(endpoint, info.getPatternsCondition().getPatterns().map { PatternCompiler(it) })
        }
    }

    fun urlFor(endpoint: String, vararg args: Pair<String, Any?>): String {
        [suppress("UNCHECKED_CAST")]
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

val reverseRouter = ReverseRouter()

fun urlFor(endpoint: String, vararg args: Pair<String, Any?>): String {
    return reverseRouter.urlFor(endpoint = endpoint, args = *args)
}
