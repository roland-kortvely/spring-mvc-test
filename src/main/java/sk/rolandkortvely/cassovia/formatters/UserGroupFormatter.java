package sk.rolandkortvely.cassovia.formatters;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.Formatter;
import org.springframework.stereotype.Component;
import sk.rolandkortvely.cassovia.models.UserGroup;

import java.util.Locale;

@Component
public class UserGroupFormatter implements Formatter<UserGroup> {

    /**
     * Database context (MySQL)
     */
    @Autowired
    protected SessionFactory sessionFactory;

    @Override
    public UserGroup parse(String id, Locale locale) {
        return UserGroup.find(sessionFactory, Integer.parseInt(id));
    }

    @Override
    public String print(UserGroup userGroup, Locale locale) {
        return userGroup.getId() + "";
    }
}
