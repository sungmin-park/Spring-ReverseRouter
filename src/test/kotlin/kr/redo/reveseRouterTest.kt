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
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.context.annotation.ComponentScan
import kr.redo.reverseRouter.kotlin.reverseRouter
import kr.redo.reverseRouter.kotlin.urlFor

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

    RequestMapping(array("/{id}"))
    fun show() {
    }

    RequestMapping(array("/{id}/edit", "/new/edit"))
    fun edit() {
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
}

