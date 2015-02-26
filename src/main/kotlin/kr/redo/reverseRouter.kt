package kr.redo.reverseRouter

import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping
import kotlin.properties.Delegates
import org.springframework.web.util.UriComponentsBuilder
import org.springframework.web.util.UriComponents
import org.springframework.context.ApplicationListener
import org.springframework.context.event.ContextRefreshedEvent


class PatternCompiler(pattern: String) {
    val pattern: String
    val uriComponentsBuilder: UriComponents
    {
        this.pattern = pattern
        uriComponentsBuilder = UriComponentsBuilder.fromPath(pattern).build()
    }

    fun compile(): String? {
        return uriComponentsBuilder.expand(hashMapOf()).toString();
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
            val beanName = handlerMethod.getBeanType().getSimpleName().replaceAll("Controller$", "")
            val endpoint = "$beanName.${method.getName()}"

            assert(!map.containsKey(endpoint))

            map.put(endpoint, info.getPatternsCondition().getPatterns().map { PatternCompiler(it) })
        }
    }

    fun urlFor(endpoint: String): String {
        val patterns = map.get(endpoint)!!
        for (pattern in patterns) {
            val url = pattern.compile()
            if (url != null) {
                return url
            }
        }
        throw IllegalArgumentException()
    }
}
