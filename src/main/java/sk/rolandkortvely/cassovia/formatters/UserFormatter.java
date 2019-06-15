package sk.rolandkortvely.cassovia.formatters;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.Formatter;
import org.springframework.stereotype.Component;
import sk.rolandkortvely.cassovia.models.User;

import java.util.Locale;

@Component
public class UserFormatter implements Formatter<User> {

    /**
     * Database context (MySQL)
     */
    @Autowired
    protected SessionFactory sessionFactory;

    @Override
    public User parse(String id, Locale locale) {
        return User.find(sessionFactory, Integer.parseInt(id));
    }

    @Override
    public String print(User user, Locale locale) {
        return user.getId() + "";
    }
}
