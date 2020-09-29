package br.com.witt.jpa.query;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

/**
 * Facade for easy and fluent JPA 2.1 criteria subquery building.
 *
 * @param <T> the subquery return type
 * 
 * @author lucianowitt@gmail.com
 */
public class CriteriaSubquery<T> {

	private CriteriaBuilder cb;
	private Subquery<T> query;
	private Map<String, From<?, ?>> entities;

	protected CriteriaSubquery(CriteriaBuilder cb, Subquery<T> query) {
		this.cb = cb;
		this.query = query;
		this.entities = new LinkedHashMap<String, From<?, ?>>();
	}

	/**
	 * Gives access to the {@link CriteriaBuilder} used to build the
	 * {@link Subquery}.
	 * 
	 * @return the {@link CriteriaBuilder}
	 */
	public CriteriaBuilder cb() {
		return cb;
	}

	/**
	 * Adds an entity class (table) to the from clause of the subquery. If there
	 * will be more than one entity class (table) in the subquery, it is recommended
	 * to give it an alias by calling {@link CriteriaSubquery#from(Class, String)}
	 * instead.
	 * 
	 * @param entityClass the entity class (table)
	 * @return this {@link Subquery} instance
	 */
	public CriteriaSubquery<T> from(Class<?> entityClass) {
		return from(entityClass, null);
	}

	/**
	 * Adds an entity class (table) to the from clause of the subquery, with the
	 * given alias.
	 * 
	 * @param entityClass the entity class (table)
	 * @param alias       the entity class (table) alias
	 * @return this {@link CriteriaSubquery} instance
	 * @see {@link javax.persistence.criteria.Subquery#from(Class)}
	 */
	public CriteriaSubquery<T> from(Class<?> entityClass, String alias) {
		alias = getEntityAlias(alias);
		Root<?> root = query.from(entityClass);
		root.alias(alias);
		entities.put(alias, root);
		return this;
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
	 * @return this {@link CriteriaSubquery} instance
	 * @see {@link javax.persistence.criteria.From#join(String,JoinType)}
	 */
	public CriteriaSubquery<T> join(String path, String alias, JoinType type) {
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
	 * @return this {@link CriteriaSubquery} instance
	 */
	public CriteriaSubquery<T> innerJoin(String path, String alias) {
		return join(path, alias, JoinType.INNER);
	}

	/**
	 * Creates an left outer join with the given path. The path should have the
	 * format <code>{alias}.{attribute}</code>. The alias part is optional if there
	 * is only one entity class (table) in the from clause and it is the path
	 * source.
	 * 
	 * @param path  the path from the source entity (table) to the target entity
	 *              (table)
	 * @param alias the target entity (table) alias
	 * @return this {@link CriteriaSubquery} instance
	 */
	public CriteriaSubquery<T> leftJoin(String path, String alias) {
		return join(path, alias, JoinType.LEFT);
	}

	/**
	 * Creates an roght outer join with the given path. The path should have the
	 * format <code>{alias}.{attribute}</code>. The alias part is optional if there
	 * is only one entity class (table) in the from clause and it is the path
	 * source.
	 * 
	 * @param path  the path from the source entity (table) to the target entity
	 *              (table)
	 * @param alias the target entity (table) alias
	 * @return this {@link CriteriaSubquery} instance
	 */
	public CriteriaSubquery<T> rightJoin(String path, String alias) {
		return join(path, alias, JoinType.RIGHT);
	}

	/**
	 * Specifies the selection to be returned by the subquery.
	 * 
	 * @param selection the selection
	 * @return this {@link CriteriaSubquery} instance
	 * @see {@link CriteriaSubquery#select(Expression)}
	 */
	public CriteriaSubquery<T> select(Expression<T> selection) {
		query.select(selection);
		return this;
	}

	/**
	 * Specifies that duplicated results will be discarded.
	 * 
	 * @return this {@link CriteriaSubquery} instance
	 * @see {@link CriteriaSubquery#distinct())}
	 */
	public CriteriaSubquery<T> distinct() {
		query.distinct(true);
		return this;
	}

	/**
	 * Specifies the expression to be counted.
	 * 
	 * @param x the expression to be counted
	 * @return this {@link CriteriaSubquery} instance
	 * @see {@link javax.persistence.criteria.CriteriaBuilder#count(Expression)}
	 */
	@SuppressWarnings("unchecked")
	public CriteriaSubquery<T> count(Expression<T> x) {
		return select((Expression<T>) cb.count(x));
	}

	/**
	 * Specifies the expression to be counted, and that duplicated results wont be
	 * considered.
	 * 
	 * @param x the expression to be counted
	 * @return this {@link CriteriaSubquery} instance
	 * @see {@link javax.persistence.criteria.CriteriaBuilder#countDistinct(Expression)}
	 */
	@SuppressWarnings("unchecked")
	public CriteriaSubquery<T> countDistinct(Expression<T> x) {
		return select((Expression<T>) cb.countDistinct(x));
	}

	/**
	 * Convenience method to create a new list of predicates to later pass to the
	 * {@link CriteriaSubquery#where(Predicate...)} method. Use the
	 * {@link CriteriaBuilder} to create predicates to add to this list.
	 * 
	 * @return new list of restrictions (predicates)
	 * @see {@link CriteriaSubquery#cb()}
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
	 * @return this {@link CriteriaSubquery} instance
	 * @see {@link javax.persistence.criteria.CriteriaSubquery#where(Predicate...)}
	 */
	public CriteriaSubquery<T> where(List<Predicate> restrictions) {
		return where(restrictions.toArray(new Predicate[restrictions.size()]));
	}

	/**
	 * Specifies the restrictions for the where clause. If more than one is
	 * informed, they will be all arguments of a conjunction (AND operator)
	 * predicate.
	 * 
	 * @param restrictions the criteria restrictions
	 * @return this {@link CriteriaSubquery} instance
	 * @see {@link javax.persistence.criteria.CriteriaSubquery#where(Predicate...)}
	 */
	public CriteriaSubquery<T> where(Predicate... restrictions) {
		query.where(restrictions);
		return this;
	}

	/**
	 * Specifies the group by expressions.
	 * 
	 * @param e list of group by expressions
	 * @return this {@link CriteriaSubquery} instance
	 * @see {@link javax.persistence.criteria.CriteriaSubquery#groupBy(List))}
	 */
	public CriteriaSubquery<T> groupBy(List<Expression<?>> e) {
		query.groupBy(e);
		return this;
	}

	/**
	 * Specifies the group by expressions.
	 * 
	 * @param e one or more group by expressions
	 * @return this {@link CriteriaSubquery} instance
	 * @see {@link javax.persistence.criteria.CriteriaSubquery#groupBy(Expression...)}
	 */
	public CriteriaSubquery<T> groupBy(Expression<?>... groupings) {
		query.groupBy(groupings);
		return this;
	}

	/**
	 * Specifies the restrictions for the having clause. If more than one is
	 * informed, they will be all arguments of a conjunction (AND operator)
	 * predicate.
	 * 
	 * @param restrictions the criteria restrictions
	 * @return this {@link CriteriaSubquery} instance
	 * @see {@link javax.persistence.criteria.CriteriaSubquery#having(Predicate...)}
	 */
	public CriteriaSubquery<T> having(Predicate... restrictions) {
		query.having(restrictions);
		return this;
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
	 * Creates a new subquery with the informed result class.
	 * 
	 * @param <T>         the type of the subquery result, resolved at runtime
	 * @param resultClass the class of the subquery result
	 * @return the {@link CriteriaSubquery} instance
	 * @see {@link javax.persistence.criteria.CommonAbstractCriteria#subquery(Class)}
	 */
	public <E> Subquery<E> newSubquery(Class<E> resultClass) {
		return query.subquery(resultClass);
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