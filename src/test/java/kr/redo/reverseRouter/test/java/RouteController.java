package kr.redo.reverseRouter.test.java;

import kr.redo.reverseRouter.ReverseRouter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;

@Controller
@RequestMapping("/router")
public class RouteController {
    @Inject
    private ReverseRouter router;

    @RequestMapping("/currentFor")
    @ResponseBody
    public String currentFor() {
        return router.currentFor();
    }

    @RequestMapping("/external")
    @ResponseBody
    public String external() {
        return router.getBuilder().asExternal().toString();
    }

    @SuppressWarnings("MVCPathVariableInspection")
    @RequestMapping("/currentFor/{id}")
    @ResponseBody
    public String currentForId() {
        return router.currentFor("id", router.getCurrent().getPathVariables().get("id"));
    }
}
