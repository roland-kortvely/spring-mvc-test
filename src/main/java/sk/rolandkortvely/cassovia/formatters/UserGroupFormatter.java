package sk.rolandkortvely.cassovia.formatters;

import org.springframework.format.Formatter;
import org.springframework.stereotype.Component;
import sk.rolandkortvely.cassovia.models.UserGroup;

import java.util.Locale;

@Component
public class UserGroupFormatter implements Formatter<UserGroup> {


    @Override
    public UserGroup parse(String id, Locale locale) {
        return new UserGroup().find(Integer.parseInt(id));
    }

    @Override
    public String print(UserGroup userGroup, Locale locale) {
        return userGroup.getId() + "";
    }
}
