package sk.rolandkortvely.cassovia.models;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class QueryStream<AModel extends Model> extends Model {

    private CriteriaBuilder cb;
    private Transaction tx;
    private Session session;
    private Root<AModel> model;
    private CriteriaQuery<AModel> f;

    private Order order = Order.ASC;
    private String orderBy = "id";

    private Predicate predicateResult;
    private List<Predicate> predicates = new ArrayList<>();

    public enum Order {
        ASC, DESC
    }

    QueryStream(Class<AModel> modelClass, SessionFactory sessionFactory) {

        session = sessionFactory.openSession();
        tx = session.beginTransaction();
        cb = session.getCriteriaBuilder();

        CriteriaQuery<AModel> cq = cb.createQuery(modelClass);

        model = cq.from(modelClass);
        f = cq.select(model);
    }

    QueryStream(Class<AModel> modelClass, SessionFactory sessionFactory, String orderBy) {
        this(modelClass, sessionFactory);
        this.orderBy = orderBy;
    }

    QueryStream(Class<AModel> modelClass, SessionFactory sessionFactory, String orderBy, Order order) {
        this(modelClass, sessionFactory);
        this.orderBy = orderBy;
        this.order = order;
    }

    private void concat(Predicate predicate) {
        if (predicateResult == null) {
            predicateResult = predicate;
        } else {
            predicates.add(predicate);
        }
    }

    public QueryStream<AModel> where(String c, Object s) {

        if (c.length() < 1) {
            return this;
        }

        if (s == null) {
            return isNull(c);
        } else {
            concat(cb.equal(model.get(c), s));
        }

        return this;
    }

    public QueryStream<AModel> whereNot(String c, Object s) {

        if (c.length() < 1) {
            return this;
        }

        if (s == null) {
            return isNotNull(c);
        } else {
            concat(cb.notEqual(model.get(c), s));
        }

        return this;
    }

    public QueryStream<AModel> isNull(String c) {

        if (c.length() < 1) {
            return this;
        }

        concat(cb.isNull(model.get(c)));
        return this;
    }

    public QueryStream<AModel> isNotNull(String c) {

        if (c.length() < 1) {
            return this;
        }

        concat(cb.isNotNull(model.get(c)));
        return this;
    }

    public QueryStream<AModel> orderBy(String c, Order order) {

        if (c.length() < 1) {
            return this;
        }

        this.order = order;
        this.orderBy = c;

        return this;
    }

    public Stream<AModel> stream() {

        if (predicateResult != null) {

            if (predicates.size() > 0) {
                predicates.forEach(predicate -> predicateResult = cb.and(predicateResult, predicate));
            }

            f.where(predicateResult);
        }

        if (orderBy != null) {
            if (order == Order.ASC) {
                f = f.orderBy(cb.asc(model.get(orderBy)));
            } else {
                f = f.orderBy(cb.desc(model.get(orderBy)));
            }
        }

        List<AModel> result = new ArrayList<>(session.createQuery(f).getResultList());

        tx.commit();
        session.close();

        result.forEach(AModel -> AModel.setSessionFactory(sessionFactory));

        return result.stream();
    }
}
