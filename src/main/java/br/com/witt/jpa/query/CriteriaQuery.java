package br.com.witt.jpa.query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;
import javax.persistence.criteria.Subquery;

/**
 * Facade for easy and fluent JPA 2.1 criteria query building and executing.<br>
 * Example:
 * 
 * <pre>
 * CriteriaQuery q = new CriteriaQuery(entityManager);
 * q.newQuery(MyEntity.class).from(MyEntity.class).selectDistinct();
 * q.where(q.cb().equal(q.get("id"), myEntityId));
 * MyEntity myEntity = q.getSingleResult();
 * </pre>
 * 
 * @author lucianowitt@gmail.com
 * 
 */
public class CriteriaQuery {

	private EntityManager em;
	private CriteriaBuilder cb;
	private javax.persistence.criteria.CriteriaQuery<?> query;
	private Map<String, From<?, ?>> entities;
	private Map<String, Object> hints;

	protected CriteriaQuery(EntityManager em) {
		this.em = em;
	}

	/**
	 * Creates a new query with the informed result class.
	 * 
	 * @param resultClass the class of the query result
	 * @return this {@link CriteriaQuery} instance
	 */
	protected CriteriaQuery newQuery(Class<?> resultClass) {
		cb = em.getCriteriaBuilder();
		if (Objects.isNull(resultClass)) {
			query = cb.createQuery();
		} else {
			query = cb.createQuery(resultClass);
		}
		entities = new LinkedHashMap<String, From<?, ?>>();
		hints = new HashMap<String, Object>();
		return this;
	}

	/**
	 * Creates a new query to return a scalar, as no result class is informed.
	 * 
	 * @return this {@link CriteriaQuery} instance
	 */
	protected CriteriaQuery newQuery() {
		return newQuery(null);
	}

	/**
	 * Gives access to the {@link CriteriaBuilder} used to build the
	 * {@link javax.persistence.criteria.CriteriaQuery}.
	 * 
	 * @return the {@link CriteriaBuilder}
	 */
	public CriteriaBuilder cb() {
		return cb;
	}

	/**
	 * Adds an entity class (table) to the from clause of the query, with the given
	 * alias.
	 * 
	 * @param entityClass the entity class (table)
	 * @param alias       the entity class (table) alias
	 * @return this {@link CriteriaQuery} instance
	 * @see {@link javax.persistence.criteria.CriteriaQuery#from(Class)}
	 */
	public CriteriaQuery from(Class<?> entityClass, String alias) {
		alias = getEntityAlias(alias);
		Root<?> root = query.from(entityClass);
		root.alias(alias);
		entities.put(alias, root);
		return this;
	}

	/**
	 * Adds an entity class (table) to the from clause of the query. If there will
	 * be more than one entity class (table) in the query, it is recommended to give
	 * it an alias by calling {@link CriteriaQuery#from(Class, String)} instead.
	 * 
	 * @param entityClass the entity class (table)
	 * @return this {@link CriteriaQuery} instance
	 */
	public CriteriaQuery from(Class<?> entityClass) {
		return from(entityClass, null);
	}

	/**
	 * Creates a join, of the join type informed, with the given path. The path
	 * should have the format <code>{alias}.{attribute}</code>. The alias part is
	 * optional if there is only one entity class (table) in the from clause and it
	 * is the path source.
	 * 
	 * @param path  the path from the source entity (table) to the target entity
	 *              (table)
	 * @param alias the target entity (table) alias
	 * @param type  the join type
	 * @return this {@link CriteriaQuery} instance
	 * @see {@link javax.persistence.criteria.From#join(String,JoinType)}
	 */
	public CriteriaQuery join(String path, String alias, JoinType type) {
		alias = getEntityAlias(alias);
		From<?, ?> from = null;
		checkPath(path);
		String[] pathParts = path.trim().split("\\.");
		int i;
		if (pathParts.length > 2) {
			throw new IllegalArgumentException("Invalid join path");
		} else if (pathParts.length == 1) {
			from = entities.values().iterator().next();
			i = 0;
		} else {
			from = entities.get(pathParts[0]);
			if (Objects.isNull(from)) {
				throw new IllegalArgumentException("No entity found in from clause whith the alias " + pathParts[0]);
			}
			i = 1;
		}
		Join<?, ?> join = from.join(pathParts[i], type);
		entities.put(alias, join);
		return this;
	}

	/**
	 * Creates an inner join with the given path. The path should have the format
	 * <code>{alias}.{attribute}</code>. The alias part is optional if there is only
	 * one entity class (table) in the from clause and it is the path source.
	 * 
	 * @param path  the path from the source entity (table) to the target entity
	 *              (table)
	 * @param alias the target entity (table) alias
	 * @return this {@link CriteriaQuery} instance
	 */
	public CriteriaQuery innerJoin(String path, String alias) {
		return join(path, alias, JoinType.INNER);
	}

	/**
	 * Creates a left outer join with the given path. The path should have the
	 * format <code>{alias}.{attribute}</code>. The alias part is optional if there
	 * is only one entity class (table) in the from clause and it is the path
	 * source.
	 * 
	 * @param path  the path from the source entity (table) to the target entity
	 *              (table)
	 * @param alias the target entity (table) alias
	 * @return this {@link CriteriaQuery} instance
	 */
	public CriteriaQuery leftJoin(String path, String alias) {
		return join(path, alias, JoinType.LEFT);
	}

	/**
	 * Creates a right outer join with the given path. The path should have the
	 * format <code>{alias}.{attribute}</code>. The alias part is optional if there
	 * is only one entity class (table) in the from clause and it is the path
	 * source.
	 * 
	 * @param path  the path from the source entity (table) to the target entity
	 *              (table)
	 * @param alias the target entity (table) alias
	 * @return this {@link CriteriaQuery} instance
	 */
	public CriteriaQuery rightJoin(String path, String alias) {
		return join(path, alias, JoinType.RIGHT);
	}

	/**
	 * Specifies the list of selections to be returned by the query.
	 * 
	 * @param selections the list of selections.
	 * @return this {@link CriteriaQuery} instance
	 * @see {@link javax.persistence.criteria.CriteriaQuery#multiselect(List)}
	 */
	public CriteriaQuery select(List<Selection<?>> selections) {
		query.multiselect(selections);
		return this;
	}

	/**
	 * Specifies one or more selections to be returned by the query.
	 * 
	 * @param selections one or more selections.
	 * @return this {@link CriteriaQuery} instance
	 * @see {@link javax.persistence.criteria.CriteriaQuery#multiselect(Selection...)}
	 */
	public CriteriaQuery select(Selection<?>... selections) {
		query.multiselect(selections);
		return this;
	}

	/**
	 * Specifies that duplicated results will be discarded. The same as the method
	 * {@link CriteriaQuery#distinct()}, but with a name that sounds better when no
	 * selection will be informed.
	 * 
	 * @return this {@link CriteriaQuery} instance
	 */
	public CriteriaQuery selectDistinct() {
		return distinct();
	}

	/**
	 * Specifies that duplicated results will be discarded.
	 * 
	 * @return this {@link CriteriaQuery} instance
	 * @see {@link javax.persistence.criteria.CriteriaQuery#distinct(boolean)}
	 */
	public CriteriaQuery distinct() {
		query.distinct(true);
		return this;
	}

	/**
	 * Specifies the expression to be counted.
	 * 
	 * @param x the expression to be counted
	 * @return this {@link CriteriaQuery} instance
	 * @see {@link javax.persistence.criteria.CriteriaBuilder#count(Expression)}
	 */
	public CriteriaQuery count(Expression<?> x) {
		return select(cb.count(x));
	}

	/**
	 * Specifies the expression to be counted, and that duplicated results wont be
	 * considered.
	 * 
	 * @param x the expression to be counted
	 * @return this {@link CriteriaQuery} instance
	 * @see {@link javax.persistence.criteria.CriteriaBuilder#countDistinct(Expression)}
	 */
	public CriteriaQuery countDistinct(Expression<?> x) {
		return select(cb.countDistinct(x));
	}

	/**
	 * Convenience method to create a new list of predicates to later pass to the
	 * {@link CriteriaQuery#where(Predicate...)} method. Use the
	 * {@link CriteriaBuilder} to create predicates to add to this list.
	 * 
	 * @return new list of restrictions (predicates)
	 * @see {@link CriteriaQuery#cb()}
	 */
	public List<Predicate> newRestrictions() {
		return new ArrayList<Predicate>();
	}
	
	/**
	 * Specifies the restrictions for the where clause. If more than one is
	 * informed, they will be all arguments of a conjunction (AND operator)
	 * predicate.
	 * 
	 * @param restrictions the criteria restrictions
	 * @return this {@link CriteriaQuery} instance
	 * @see {@link javax.persistence.criteria.CriteriaQuery#where(Predicate...)}
	 */
	public CriteriaQuery where(List<Predicate> restrictions) {
		query.where(restrictions.toArray(new Predicate[restrictions.size()]));
		return this;
	}

	/**
	 * Specifies the restrictions for the where clause. If more than one is
	 * informed, they will be all arguments of a conjunction (AND operator)
	 * predicate.
	 * 
	 * @param restrictions the criteria restrictions
	 * @return this {@link CriteriaQuery} instance
	 * @see {@link javax.persistence.criteria.CriteriaQuery#where(Predicate...)}
	 */
	public CriteriaQuery where(Predicate... restrictions) {
		query.where(restrictions);
		return this;
	}

	/**
	 * Specifies the group by expressions.
	 * 
	 * @param e list of group by expressions
	 * @return this {@link CriteriaQuery} instance
	 * @see {@link javax.persistence.criteria.CriteriaQuery#groupBy(List))}
	 */
	public CriteriaQuery groupBy(List<Expression<?>> e) {
		query.groupBy(e);
		return this;
	}

	/**
	 * Specifies the group by expressions.
	 * 
	 * @param e one or more group by expressions
	 * @return this {@link CriteriaQuery} instance
	 * @see {@link javax.persistence.criteria.CriteriaQuery#groupBy(Expression...)}
	 */
	public CriteriaQuery groupBy(Expression<?>... e) {
		query.groupBy(e);
		return this;
	}

	/**
	 * Specifies the restrictions for the having clause. If more than one is
	 * informed, they will be all arguments of a conjunction (AND operator)
	 * predicate.
	 * 
	 * @param restrictions the criteria restrictions
	 * @return this {@link CriteriaQuery} instance
	 * @see {@link javax.persistence.criteria.CriteriaQuery#having(Predicate...)}
	 */
	public CriteriaQuery having(Predicate... restrictions) {
		query.having(restrictions);
		return this;
	}

	/**
	 * Specifies the query ordering.
	 * 
	 * @param o list of {@link Order} expressions
	 * @return this {@link CriteriaQuery} instance
	 * @see {@link javax.persistence.criteria.CriteriaQuery#orderBy(List)}
	 */
	public CriteriaQuery orderBy(List<Order> o) {
		query.orderBy(o);
		return this;
	}

	/**
	 * Specifies the query ordering.
	 * 
	 * @param o one or more {@link Order} expressions
	 * @return this {@link CriteriaQuery} instance
	 * @see {@link javax.persistence.criteria.CriteriaQuery#orderBy(Order...)}
	 */
	public CriteriaQuery orderBy(Order... o) {
		query.orderBy(o);
		return this;
	}

	/**
	 * Creates a new subquery with the informed result class.
	 * 
	 * @param <T>         the type of the subquery result, resolved at runtime
	 * @param resultClass the class of the subquery result
	 * @return the {@link CriteriaSubquery} instance
	 * @see {@link javax.persistence.criteria.CommonAbstractCriteria#subquery(Class)}
	 */
	public <T> CriteriaSubquery<T> newSubquery(Class<T> resultClass) {
		Subquery<T> subquery = query.subquery(resultClass);
		return new CriteriaSubquery<T>(cb, subquery);
	}

	/**
	 * Creates a {@link Path} to an entity attribute.
	 * 
	 * @param path string path in the format
	 *             <code>{alias}.{attribute}.{attribute}...</code>
	 * @return the {@link Path}
	 */
	public Path<?> get(String path) {
		From<?, ?> from = null;
		checkPath(path);
		String[] pathParts = path.trim().split("\\.");
		int i;
		if (pathParts.length == 1) {
			from = entities.values().iterator().next();
			i = 0;
		} else {
			from = entities.get(pathParts[0]);
			if (Objects.isNull(from)) {
				throw new IllegalArgumentException("No entity found in from clause whith the alias " + pathParts[0]);
			}
			i = 1;
		}
		Path<?> result = from;
		for (; i < pathParts.length; i++) {
			result = result.get(pathParts[i]);
		}
		return result;
	}

	/**
	 * Creates a string expression of concatenated string expressions.
	 * 
	 * @param parts the string expression to concatenate
	 * @return the concatenated string expression
	 * @see {@link CriteriaBuilder#concat(Expression, Expression)}
	 */
	@SuppressWarnings("unchecked")
	public Expression<String> concat(Expression<String>... parts) {
		if (parts == null || parts.length == 0) {
			return null;
		}
		Expression<String> e = parts[0];
		if (parts.length > 1) {
			for (int i = 1; i < parts.length; i++) {
				e = cb.concat(e, parts[i]);
			}
		}
		return e;
	}

	/**
	 * Sets a query property or hint.
	 * 
	 * @param name  property/hint name
	 * @param value property/hint value
	 * @see {@link javax.persistence.TypedQuery#setHint(String, Object)}
	 */
	public void setHint(String name, Object value) {
		hints.put(name, value);
	}

	/**
	 * Executes the query and returns a single result.
	 * 
	 * @param <T> the type of the result, resolved at runtime
	 * @return the single result of the query execution
	 * @see {@link javax.persistence.TypedQuery#getSingleResult()}
	 */
	@SuppressWarnings("unchecked")
	public <T> T getSingleResult() {
		return (T) getTypedQuery().getSingleResult();
	}

	/**
	 * Executes the query and returns a list of results.
	 * 
	 * @param <T> the type of the result, resolved at runtime
	 * @return the list of results of the query execution
	 * @see {@link javax.persistence.TypedQuery#getResultList()}
	 */
	@SuppressWarnings("unchecked")
	public <T> List<T> getResultList() {
		TypedQuery<?> typedQuery = getTypedQuery();
		return (List<T>) typedQuery.getResultList();
	}

	/**
	 * Executes the query and returns a page of results.
	 * 
	 * @param <T>        the type of the results, resolved at runtime
	 * @param pageNumber the number of the page
	 * @param pageSize   the size of the page
	 * @return a page of results
	 */
	@SuppressWarnings("unchecked")
	public <T> List<T> getResultPage(int pageNumber, int pageSize) {
		TypedQuery<?> typedQuery = getTypedQuery();
		typedQuery.setFirstResult((pageNumber - 1) * pageSize);
		typedQuery.setMaxResults(pageSize);
		return (List<T>) typedQuery.getResultList();
	}

	private TypedQuery<?> getTypedQuery() {
		TypedQuery<?> typedQuery = em.createQuery(query);
		for (Map.Entry<String, Object> entry : hints.entrySet()) {
			typedQuery.setHint(entry.getKey(), entry.getValue());
		}
		return typedQuery;
	}

	private String getEntityAlias(String alias) {
		if (Objects.isNull(alias) || alias.trim().isEmpty()) {
			alias = String.format("e%03d", entities.size());
		}
		return alias.trim();
	}

	private void checkPath(String path) {
		if (Objects.isNull(entities) || entities.isEmpty()) {
			throw new IllegalArgumentException("No entity in from clause");
		}
		if (Objects.isNull(path) || path.trim().isEmpty()) {
			throw new IllegalArgumentException("Path cannot be null");
		}
	}
}
