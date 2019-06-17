package sk.rolandkortvely.cassovia.formatters;

import org.springframework.format.Formatter;
import org.springframework.stereotype.Component;
import sk.rolandkortvely.cassovia.models.User;

import java.util.Locale;

@Component
public class UserFormatter implements Formatter<User> {

    @Override
    public User parse(String id, Locale locale) {
        return User.find(Integer.parseInt(id));
    }

    @Override
    public String print(User user, Locale locale) {
        return user.getId() + "";
    }
}
