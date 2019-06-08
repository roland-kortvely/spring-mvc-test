package sk.rolandkortvely.cassovia.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

@Controller
public class ErrorController extends AbstractController implements org.springframework.boot.web.servlet.error.ErrorController {

    @RequestMapping("/error")
    public String errors(HttpServletRequest httpRequest, Model model) {

        String error = "";

        switch (getErrorCode(httpRequest)) {
            case 400: {
                error = "Http Error Code: 400. Bad Request";
                break;
            }
            case 401: {
                error = "Http Error Code: 401. Unauthorized";
                break;
            }
            case 404: {
                error = "Http Error Code: 404. Resource not found";
                break;
            }
            case 500: {
                error = "Http Error Code: 500. Internal Server Error";
                break;
            }
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
