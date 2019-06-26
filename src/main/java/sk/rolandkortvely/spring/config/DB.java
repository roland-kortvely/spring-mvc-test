package sk.rolandkortvely.spring.config;

import org.hibernate.SessionFactory;
import org.springframework.stereotype.Component;

/**
 * Database Context
 */
@Component
public class DB
{

    public static SessionFactory sessionFactory;

    public DB(SessionFactory sessionFactory)
    {
        DB.sessionFactory = sessionFactory;
    }
}
