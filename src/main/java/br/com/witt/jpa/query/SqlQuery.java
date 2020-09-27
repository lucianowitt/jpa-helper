package br.com.witt.jpa.query;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.persistence.EntityManager;

import br.com.witt.jpa.util.TypeConverter;

/**
 * Facade for easy and fluent JPA 2.1 SQL query building and executing.<br>
 * 
 * @author lucianowitt@gmail.com
 *
 */
public class SqlQuery extends Query {

	private Class<?> resultClass;

	public SqlQuery(EntityManager em) {
		super(em);
	}

	/**
	 * Creates a new query with the informed result class.
	 * 
	 * @param sql         SQL string
	 * @param resultClass the class of the query result
	 * @return this {@link SqlQuery} instance
	 */
	public SqlQuery newQuery(String sql, Class<?> resultClass) {
		this.resultClass = resultClass;
		query = em.createNativeQuery(sql);
		return this;
	}

	/**
	 * Creates a new query to return a scalar, as no result class is informed.
	 * 
	 * @param sql SQL string
	 * @return this {@link SqlQuery} instance
	 */
	public SqlQuery newQuery(String sql) {
		return newQuery(sql, null);
	}

	/**
	 * 
	 * 
	 * @param <T>
	 * @param scalarClass
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> T getScalar(Class<T> scalarClass) {
		return (T) TypeConverter.convertValue(query.getSingleResult(), scalarClass);
	}

	/**
	 * Executes the query and returns a single result.
	 * 
	 * @param <T> the type of the result, resolved at runtime
	 * @return the single result of the query execution
	 * @see {@link javax.persistence.Query#getSingleResult()}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <T> T getSingleResult() {
		if (Objects.isNull(resultClass)) {
			return (T) query.getSingleResult();
		} else {
			Object[] result = (Object[]) query.getSingleResult();
			return TypeConverter.convert(result, resultClass);
		}
	}

	/**
	 * Executes the query and returns a list of results.
	 * 
	 * @param <T> the type of the result, resolved at runtime
	 * @return the list of results of the query execution
	 * @see {@link javax.persistence.Query#getResultList()}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <T> List<T> getResultList() {
		if (Objects.isNull(resultClass)) {
			return query.getResultList();
		} else {
			List<T> convertedList = new ArrayList<T>();
			List<Object[]> resultList = query.getResultList();
			for (Object[] result : resultList) {
				T converted = TypeConverter.convert(result, resultClass);
				convertedList.add(converted);
			}
			return convertedList;
		}
	}
}
