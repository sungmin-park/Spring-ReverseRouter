package kr.redo.reverseRouter.kotlin

import kr.redo.reverseRouter.ReverseRouter
import org.springframework.web.servlet.ModelAndView

object reverseRouter : ReverseRouter()

fun urlFor(endpoint: String, vararg args: Pair<String, Any?>): String {
    return reverseRouter.urlFor(endpoint = endpoint, args = *args)
}

fun redirect(url: String): ModelAndView {
    return ModelAndView("redirect:$url")
}

fun redirectFor(endpoint: String, vararg args: Pair<String, Any?>): ModelAndView {
    return ModelAndView("redirect:${urlFor(endpoint = endpoint, args = *args)}")
}