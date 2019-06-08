package sk.rolandkortvely.cassovia.entities;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.jetbrains.annotations.NotNull;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.stream.Stream;

public abstract class AbstractModel {

    private SessionFactory sessionFactory;

    @FunctionalInterface
    public interface SessionTransaction<T> {
        T commit(Session session);
    }

    @FunctionalInterface
    public interface QueryTransaction<T, E> {
        T commit(Session session, Query<E> query);
    }

    public AbstractModel() {
    }

    public AbstractModel(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    protected static <T extends AbstractModel> T find(Class<T> tClass, SessionFactory sessionFactory, Integer id) {
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

    protected static <T extends AbstractModel> List<T> all(Class<T> tClass, SessionFactory sessionFactory) {

        List<T> result = query((session, query) -> query.getResultList(),tClass, sessionFactory);

        result.forEach(t -> t.setSessionFactory(sessionFactory));

        return result;
    }

    protected static <T extends AbstractModel> T first(Class<T> tClass, SessionFactory sessionFactory) {

        T result = query((session, query) -> query.getResultList().stream().findFirst().orElse(null), tClass, sessionFactory);

        if (result != null) {
            result.setSessionFactory(sessionFactory);
        }

        return result;
    }

    protected static <T extends AbstractModel> Stream<T> stream(Class<T> tClass, SessionFactory sessionFactory) {
        return query((session, query) -> query.getResultList().stream(), tClass, sessionFactory);
    }

    private static <T,E> T query(QueryTransaction<T,E> queryTransaction, Class<E> tClass, SessionFactory sessionFactory) {
        return transaction(session -> {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<E> cq = cb.createQuery(tClass);
            Root<E> root = cq.from(tClass);
            cq.select(root);
            return queryTransaction.commit(session, session.createQuery(cq));
        }, sessionFactory);
    }

    private static <T> T transaction(SessionTransaction<T> sessionTransaction, @NotNull SessionFactory sessionFactory) {
        Session session = sessionFactory.openSession();
        Transaction tx = null;

        T result = null;

        try {
            tx = session.beginTransaction();
            result = sessionTransaction.commit(session);
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
