package kr.redo.reverseRouter.test.java;

import kr.redo.reverseRouter.ReverseRouter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {WebMvcJavaConfig.class})
@WebAppConfiguration
public class ReverseRouterJavaTest {
    @Inject
    private ReverseRouter router;
    @Inject
    private WebApplicationContext webApplicationContext;
    private MockMvc mockMvc;

    @Before
    public void before() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void testInitialize() {
        assertTrue(router.getInitialized());
    }

    @Test
    public void testIndex() {
        assertEquals("/", router.urlFor("main.index"));
    }

    @Test
    public void testUser() {
        assertEquals("/user/", router.urlFor("user.list"));
        assertEquals("/user/12", router.urlFor("user.show", "id", 12));

        assertEquals("/user/new/edit", router.urlFor("user.edit"));
        assertEquals("/user/12/edit", router.urlFor("user.edit", "id", 12));
    }

    @Test
    public void testHandleNullable() {
        assertEquals("/user/new/edit", router.urlFor("user.edit", "id", null));
    }

    @Test
    public void testCurrentFor() throws Exception {
        mockMvc.perform(get("/router/currentFor")).andExpect(content().string("/router/currentFor"));
        mockMvc.perform(get("/router/currentFor/12")).andExpect(content().string("/router/currentFor/12"));
    }

    @Test
    public void testAsExternal() throws Exception {
        mockMvc.perform(get("/router/external")).andExpect(content().string("http://localhost/router/external"));
    }
}
