package sk.rolandkortvely.cassovia.controllers;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

@Controller
public class ErrorsController extends AbstractController implements ErrorController {

    @RequestMapping("/error")
    public String errors(HttpServletRequest httpRequest, Model model) {

        String error;

        switch (getErrorCode(httpRequest)) {
            case 400:
                error = "Bad Request";
                break;

            case 401:
                error = "Unauthorized";
                break;

            case 404:
                error = "Resource not found";
                break;

            case 405:
                error = "Method not allowed";
                break;

            case 500:
                error = "Internal Server Error";
                break;
            default:
                error = "Unknown error";
        }

        model.addAttribute("error", error);
        model.addAttribute("code", getErrorCode(httpRequest) + "");

        return "error";
    }

    private int getErrorCode(HttpServletRequest httpRequest) {
        return (Integer) httpRequest
                .getAttribute("javax.servlet.error.status_code");
    }

    @Override
    public String getErrorPath() {
        return null;
    }
}
