package sk.samuelkarabas.cassovia.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;

@Controller
public abstract class AbstractController {

    @ModelAttribute("author")
    public String author() {
        return "Roland Körtvely";
    }

    @ModelAttribute("title")
    public String title() {
        return "Cassovia code";
    }
}
