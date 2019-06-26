package sk.rolandkortvely.spring.config;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Exception handler for frontend
 */
@Controller
public class ErrorsController extends AbstractController implements ErrorController
{

    /**
     * Handle error and return Thymeleaf view
     *
     * @param model Instance of empty object for Thymeleaf, you fill model with data you want to share with View
     * @return Error View
     */
    @RequestMapping("/error")
    public String errors(Model model)
    {

        String error;

        switch (getErrorCode()) {
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
        model.addAttribute("code", getErrorCode());

        return "error";
    }

    /**
     * Parse error core from request
     *
     * @return error code
     */
    private int getErrorCode()
    {
        return (Integer) request.getAttribute("javax.servlet.error.status_code");
    }

    @Override
    public String getErrorPath()
    {
        return null;
    }
}
