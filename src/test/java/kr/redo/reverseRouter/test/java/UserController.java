package kr.redo.reverseRouter.test.java;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/user")
public class UserController {
    @RequestMapping("/")
    public void list() {
    }

    @SuppressWarnings("MVCPathVariableInspection")
    @RequestMapping("/{id}")
    public void show() {
    }

    @SuppressWarnings("MVCPathVariableInspection")
    @RequestMapping(value = {"/{id}/edit", "/new/edit"})
    public void edit() {
    }
}
