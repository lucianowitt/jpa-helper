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
    private EntityManager em;

    public List<Department> searchActiveDepartments(String name) {
        CriteriaQuery q = new CriteriaQuery(em);
        q.newQuery(Department.class).from(Department.class).selectDistinct();
        List<Predicate> restrictions = q.newRestrictions();
        restrictions.add(q.cb().like(q.get("name").as(String.class), "%" + name + "%"));
        restrictions.add(q.cb().equal(q.get("active"), Boolean.TRUE);
        q.where(restrictions);
        q.orderBy(q.cb().asc(a.get("name")));
        return q.getResultList();
    }

    public List<Department> listActiveDepartments(int pageNumber, int pageSize) {
        JpqlQuery q = new JpqlQuery(em);
        String jpql = "select d from Department d where d.active = :active order by d.name";
        q.newQuery(jpql, Department.class);
        q.setParameter("active", Boolean.TRUE);
        return q.getResultPage(pageNumber, pageSize);
    }

    public Long countActiveDepartments() {
        SqlQuery q = new SqlQuery(em);
        String sql = "select count(*) as total from Department where active = :active";
        q.newQuery(sql);
        q.setParameter("active", Boolean.TRUE);
        return q.getScalar(Long.class);
    }
}
```
