package kr.redo.reverseRouter.test.kotlin

import kr.redo.reverseRouter.ReverseRouter
import kr.redo.reverseRouter.kotlin
import kr.redo.reverseRouter.kotlin.*
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.context.ApplicationListener
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Controller
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.context.web.WebAppConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.context.WebApplicationContext
import org.springframework.web.servlet.config.annotation.EnableWebMvc
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter
import javax.inject.Inject
import kotlin.properties.Delegates

Controller
class MainController {
    RequestMapping("/")
    fun index() {
    }
}

Controller
RequestMapping("/user")
class UserController {
    RequestMapping("/")
    fun list() {
    }

    RequestMapping("/{id}")
    fun show() {
    }

    RequestMapping("/{id}/edit", "/new/edit")
    fun edit() {
    }
}

Controller
RequestMapping("/router")
class RouterController {
    RequestMapping("/endpoint")
    ResponseBody
    fun endpoint() = reverseRouter.current.endpoint

    RequestMapping("/dotNotation")
    ResponseBody
    fun dotNotation() = urlFor(".dotNotation")

    RequestMapping("/currentUrl")
    ResponseBody
    fun currentUrl() = currentFor()

    RequestMapping("/current/{id}")
    ResponseBody
    fun current() = currentFor("id" to reverseRouter.current.pathVariables["id"])

    RequestMapping("/builder")
    ResponseBody
    fun builder() = reverseRouter.builder.toString()

    RequestMapping("/currentRequestURL")
    ResponseBody
    fun currentRequestURL() = reverseRouter.current.requestURL
}

Configuration
ComponentScan
EnableWebMvc
open class WebMvcConfig : WebMvcConfigurerAdapter() {
    Bean open fun reverseRouter(): ReverseRouter = kotlin.reverseRouter

    Bean open fun applicationListener(): ApplicationListener<*> = reverseRouter()

    override fun addInterceptors(registry: InterceptorRegistry?) {
        registry?.addInterceptor(reverseRouter())
        super.addInterceptors(registry)
    }
}

fun get(urlTemplate: String, vararg urlVariables: Any) = MockMvcRequestBuilders.get(urlTemplate, *urlVariables)!!

fun content() = MockMvcResultMatchers.content()

RunWith(SpringJUnit4ClassRunner::class)
ContextConfiguration(classes = arrayOf(WebMvcConfig::class))
WebAppConfiguration
class ReverseRouterTest {
    Inject
    private var wac: WebApplicationContext? = null
    private var mockMvc by Delegates.notNull<MockMvc>()

    Before
    public fun before() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build()
    }


    Test
    fun testInitialize() {
        Assert.assertTrue(reverseRouter.initialized)
    }

    Test
    fun testIndex() {
        Assert.assertEquals("/", urlFor("main.index"))
    }

    Test
    fun testUser() {
        Assert.assertEquals("/user/", urlFor("user.list"))
        Assert.assertEquals("/user/12", urlFor("user.show", "id" to 12))

        Assert.assertEquals("/user/new/edit", urlFor("user.edit"))
        Assert.assertEquals("/user/12/edit", urlFor("user.edit", "id" to 12))
    }

    Test
    fun testHandleNullable() {
        Assert.assertEquals("/user/new/edit", urlFor("user.edit", "id" to null))
    }

    Test
    fun testExtraParams() {
        Assert.assertEquals("/user/?page=10", urlFor("user.list", "page" to 10))
    }

    Test
    fun testRedirect() {
        Assert.assertEquals("redirect:/user/", redirect(urlFor("user.list")))
    }

    Test
    fun restRedirectFor() {
        Assert.assertEquals("redirect:/user/", redirectFor("user.list"))
    }

    Test
    fun testCurrentEndpoint() {
        mockMvc.perform(get(urlFor("router.endpoint"))).andExpect(content().string("router.endpoint"))
    }

    Test
    fun testDotNotation() {
        mockMvc.perform(get(urlFor("router.dotNotation"))).andExpect(content().string("/router/dotNotation"))
    }

    Test
    fun testCurrentFor() {
        mockMvc.perform(get(urlFor("router.currentUrl"))).andExpect(content().string("/router/currentUrl"))
        mockMvc.perform(get(urlFor("router.current", "id" to 1))).andExpect(content().string("/router/current/1"))
    }

    Test
    fun testBuilderFor() {
        val builder = reverseRouter.builderFor("user.edit")
        Assert.assertEquals("/user/new/edit", builder.toString())
        Assert.assertEquals("/user/new/edit?name=john", builder.add("name", "john").toString())
        Assert.assertEquals("/user/new/edit?name=john&name=john", builder.add("name", "john").toString())
        Assert.assertEquals("/user/new/edit?name=jane", builder.set("name", "jane").toString())
    }

    Test
    fun testBuilder() {
        mockMvc.perform(get("/router/builder")).andExpect(content().string("/router/builder"))
    }

    Test fun testCurrentRequestURL() {
        mockMvc.perform(get("/router/currentRequestURL")).andExpect(content().string("/router/currentRequestURL"))
        mockMvc.perform(get("/router/currentRequestURL?param=value"))
                .andExpect(content().string("/router/currentRequestURL?param=value"))
    }
}

