package sk.samuelkarabas.cassovia.entities;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

public abstract class AbstractModel<T extends AbstractModel<T>> {

    private SessionFactory sessionFactory;

    @FunctionalInterface
    public interface Todo<T> {
        T commit(Session session);
    }

    public AbstractModel() {
    }

    public AbstractModel(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    protected static <T extends AbstractModel<T>> T find(Class<T> tClass, SessionFactory sessionFactory, Integer id) {
        T result = transaction(session -> tClass.cast(session.get(tClass, id)), sessionFactory);
        result.setSessionFactory(sessionFactory);
        return result;
    }

    public void save() {
        transaction(session -> {
            session.saveOrUpdate(this);
            return null;
        }, sessionFactory);
    }

    public void delete() {
        transaction(session -> {
            session.remove(this);
            return null;
        }, sessionFactory);
    }

    protected static <T extends AbstractModel<T>> List<T> all(Class<T> tClass, SessionFactory sessionFactory) {

        List<T> result = transaction(session -> {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<T> cq = cb.createQuery(tClass);
            Root<T> root = cq.from(tClass);
            cq.select(root);
            Query<T> query = session.createQuery(cq);
            return query.getResultList();
        }, sessionFactory);

        result.forEach(t -> t.setSessionFactory(sessionFactory));

        return result;
    }

    private static <T> T transaction(Todo<T> todo, SessionFactory sessionFactory) {
        Session session = sessionFactory.openSession();
        Transaction tx = null;

        T result = null;

        try {
            tx = session.beginTransaction();
            result = todo.commit(session);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        } finally {
            session.close();
        }

        return result;
    }

    protected void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }
}
