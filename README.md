# JPA2 query

Facade classes for easy and fluent build and execution of JPA 2.1 criteria, JPQL and SQL queries.


See below an example for each type of query.


```java
package myapp.repositories;

import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import br.com.witt.jpa.query.CriteriaQuery;
import br.com.witt.jpa.query.JpqlQuery;
import br.com.witt.jpa.query.SqlQuery;

import myapp.entities.Department;

@Stateless
public class DepartmentRepository {

    @Inject
    private JpaQueryFactory jpaQueryFactory;

    public List<Department> searchActiveDepartments(String name) {
        CriteriaQuery q = jpaQueryFactory.createCriteriaQuery(Department.class);
        q.from(Department.class).selectDistinct();
        List<Predicate> restrictions = q.newRestrictions();
        restrictions.add(q.cb().like(q.get("name").as(String.class), "%" + name + "%"));
        restrictions.add(q.cb().equal(q.get("active"), Boolean.TRUE);
        q.where(restrictions);
        q.orderBy(q.cb().asc(a.get("name")));
        return q.getResultList();
    }

    public List<Department> listActiveDepartments(int pageNumber, int pageSize) {
        String jpql = "select d from Department d where d.active = :active order by d.name";
        JpqlQuery q = jpaQueryFactory.createJpqlQuery(jpql, Department.class);
        q.setParameter("active", Boolean.TRUE);
        return q.getResultPage(pageNumber, pageSize);
    }

    public Long countActiveDepartments() {
        String sql = "select count(*) as total from Department where active = :active";
        SqlQuery q = jpaQueryFactory.createSqlQuery(sql);
        q.setParameter("active", Boolean.TRUE);
        return q.getScalar(Long.class);
    }
}
```

Where, for example, the JpaQueryFactory is provided by:

```
package myapp.config;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import br.com.witt.jpa.query.JpaQueryFactory;

@ApplicationScoped
public class ResourceProvider {

    @PersistenceContext
    private EntityManager entityManager;

    @Produces
    public EntityManager getEntityManager() {
        return entityManager;
    }

    @Produces
    @Dependent
    public JpaQueryFactory getJpaQueryFactory() {
        return new JpaQueryFactory(entityManager);
    }
}
```