package br.com.witt.jpa.query;

import java.util.Objects;

import javax.persistence.EntityManager;

/**
 * Facade for easy and fluent JPA 2.1 JPQL query building and executing.
 * 
 * @author lucianowitt@gmail.com
 *
 */
public class JpqlQuery extends Query {

	protected JpqlQuery(EntityManager em) {
		super(em);
	}

	/**
	 * Creates a new query with the informed result class.
	 * 
	 * @param jpql        JPQL string
	 * @param resultClass the class of the query result
	 * @return this {@link JpqlQuery} instance
	 */
	protected JpqlQuery newQuery(String jpql, Class<?> resultClass) {
		if (Objects.isNull(resultClass)) {
			query = em.createQuery(jpql);
		} else {
			query = em.createQuery(jpql, resultClass);
		}
		return this;
	}

	/**
	 * Creates a new query to return a scalar, as no result class is informed.
	 * 
	 * @param jpql JPQL string
	 * @return this {@link JpqlQuery} instance
	 */
	protected JpqlQuery newQuery(String jpql) {
		return newQuery(jpql, null);
	}
}
