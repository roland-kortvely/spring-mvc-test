package sk.rolandkortvely.cassovia.controllers;

import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Additional attributes for Thymeleaf
 */
@Component
public class Attributes {

    @ModelAttribute("author")
    public String author() {
        return "Roland Körtvely";
    }

    @ModelAttribute("title")
    public String title() {
        return "Cassovia Code";
    }
}
