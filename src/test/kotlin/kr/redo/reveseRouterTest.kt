package kr.redo.reverseRouter

import org.junit.Test
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.junit.runner.RunWith
import org.springframework.test.context.web.WebAppConfiguration
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.EnableWebMvc
import org.junit.Assert
import org.springframework.context.ApplicationListener
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.context.annotation.Bean
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.context.annotation.ComponentScan

val reverseRouter = ReverseRouter()

Controller
class MainController {
    RequestMapping(array("/"))
    fun index() {
    }
}

Controller
RequestMapping(array("/user"))
class UserController {
    RequestMapping(array("/"))
    fun list() {
    }
}

Configuration
ComponentScan
EnableWebMvc
open class WebMvcConfig : WebMvcConfigurerAdapter() {
    Bean
    open fun applicationListener(): ApplicationListener<*> {
        return reverseRouter
    }
}


RunWith(javaClass<SpringJUnit4ClassRunner>())
ContextConfiguration(classes = array(javaClass<WebMvcConfig>()))
WebAppConfiguration
class ReverseRouterTest {
    Test
    fun testInitialize() {
        Assert.assertTrue(reverseRouter.initialized)
    }

    Test
    fun testIndex() {
        Assert.assertEquals("/", reverseRouter.urlFor("Main.index"))
    }

    Test
    fun testUser() {
        Assert.assertEquals("/user/", reverseRouter.urlFor("User.list"))
    }
}

