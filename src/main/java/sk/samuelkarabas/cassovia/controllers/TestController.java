package sk.samuelkarabas.cassovia.controllers;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.WebApplicationContext;
import sk.samuelkarabas.cassovia.entities.User;

@Controller
@Scope(WebApplicationContext.SCOPE_SESSION)
@RequestMapping("/")
public class TestController {

    @Autowired
    private SessionFactory sessionFactory;

    @RequestMapping
    public String index(Model model)
    {

        model.addAttribute("users", User.all(sessionFactory));

        return "index";
    }
}
