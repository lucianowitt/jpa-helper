package br.com.witt.jpa.query;

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

public class CriteriaQuery {

	private EntityManager em;
	private CriteriaBuilder cb;
	private javax.persistence.criteria.CriteriaQuery<?> query;
	private Map<String, From<?, ?>> entities;
	private Map<String, Object> hints;

	public CriteriaQuery(EntityManager em) {
		this.em = em;
	}

	public CriteriaQuery newQuery(Class<?> resultClass) {
		this.cb = em.getCriteriaBuilder();
		if (Objects.isNull(resultClass)) {
			query = cb.createQuery();
		} else {
			query = cb.createQuery(resultClass);
		}
		entities = new LinkedHashMap<String, From<?, ?>>();
		hints = new HashMap<String, Object>();
		return this;
	}

	public CriteriaQuery newQuery() {
		return newQuery(null);
	}

	public CriteriaBuilder cb() {
		return cb;
	}

	public CriteriaQuery from(Class<?> entityClass, String alias) {
		alias = getEntityAlias(alias);
		Root<?> root = query.from(entityClass);
		root.alias(alias);
		entities.put(alias, root);
		return this;
	}

	public CriteriaQuery from(Class<?> entityClass) {
		return from(entityClass, null);
	}

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
		Join<?, ?> join = from.join(pathParts[i]);
		entities.put(alias, join);
		return this;
	}

	public CriteriaQuery join(String path, String alias) {
		return join(path, alias, JoinType.INNER);
	}

	public CriteriaQuery leftJoin(String path, String alias) {
		return join(path, alias, JoinType.LEFT);
	}

	public CriteriaQuery rightJoin(String path, String alias) {
		return join(path, alias, JoinType.RIGHT);
	}

	public CriteriaQuery select(List<Selection<?>> selections) {
		query.multiselect(selections);
		return this;
	}

	public CriteriaQuery select(Selection<?>... selections) {
		query.multiselect(selections);
		return this;
	}

	public CriteriaQuery selectDistinct() {
		return distinct();
	}

	public CriteriaQuery distinct() {
		query.distinct(true);
		return this;
	}

	public CriteriaQuery where(Predicate... restrictions) {
		query.where(restrictions);
		return this;
	}

	public CriteriaQuery groupBy(List<Expression<?>> e) {
		query.groupBy(e);
		return this;
	}

	public CriteriaQuery groupBy(Expression<?>... e) {
		query.groupBy(e);
		return this;
	}

	public CriteriaQuery having(Predicate... restrictions) {
		query.having(restrictions);
		return this;
	}

	public CriteriaQuery orderBy(List<Order> o) {
		query.orderBy(o);
		return this;
	}

	public CriteriaQuery orderBy(Order... o) {
		query.orderBy(o);
		return this;
	}

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

	public void setHint(String name, Object value) {
		hints.put(name, value);
	}

	@SuppressWarnings("unchecked")
	public <T> T getSingleResult() {
		return (T) getTypedQuery().getSingleResult();
	}

	@SuppressWarnings("unchecked")
	public <T> List<T> getResultList() {
		return (List<T>) getTypedQuery().getResultList();
	}

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
		if (Objects.isNull(alias)) {
			alias = String.format("e%03d", entities.size());
		}
		return alias;
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
