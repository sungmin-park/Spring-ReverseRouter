package kr.redo.reverseRouter.test.java;

import kr.redo.reverseRouter.ReverseRouter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {WebMvcJavaConfig.class})
@WebAppConfiguration
public class ReverseRouterJavaTest {
    @Inject
    private ReverseRouter reverseRouter;

    @Test
    public void testInitialize() {
        assertTrue(reverseRouter.getInitialized());
    }

    @Test
    public void testIndex() {
        assertEquals("/", reverseRouter.urlFor("main.index"));
    }

    @Test
    public void testUser() {
        assertEquals("/user/", reverseRouter.urlFor("user.list"));
        assertEquals("/user/12", reverseRouter.urlFor("user.show", "id", 12));

        assertEquals("/user/new/edit", reverseRouter.urlFor("user.edit"));
        assertEquals("/user/12/edit", reverseRouter.urlFor("user.edit", "id", 12));
    }

    @Test
    public void testHandleNullable() {
        assertEquals("/user/new/edit", reverseRouter.urlFor("user.edit", "id", null));
    }
}
