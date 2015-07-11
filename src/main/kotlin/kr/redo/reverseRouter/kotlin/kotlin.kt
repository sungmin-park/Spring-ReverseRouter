package kr.redo.reverseRouter.kotlin

import kr.redo.reverseRouter.ReverseRouter
import org.springframework.web.servlet.ModelAndView

public object reverseRouter : ReverseRouter()

public fun urlFor(endpoint: String, vararg args: Pair<String, Any?>): String {
    return reverseRouter.urlFor(endpoint = endpoint, args = *args)
}

public fun currentFor(vararg args: Pair<String, Any?>): String {
    return reverseRouter.currentFor(*args)
}

public fun redirect(url: String): String {
    return "redirect:$url"
}

public fun redirectFor(endpoint: String, vararg args: Pair<String, Any?>): String {
    return "redirect:${urlFor(endpoint = endpoint, args = *args)}"
}