package br.com.witt.jpa.query;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TemporalType;

/**
 * Facade for easy and fluent JPA 2.1 SQL and JPQL query building and
 * executing.<br>
 * 
 * @see {@link SqlQuery}, {@link JpqlQuery}
 * 
 * @author lucianowitt@gmail.com
 * 
 */
public abstract class Query {

	protected EntityManager em;
	protected javax.persistence.Query query;

	public Query(EntityManager em) {
		this.em = em;
	}

	/**
	 * Binds the given value to the parameter with the given name.
	 * 
	 * @param name  parameter name
	 * @param value parameter value
	 * @return this {@link Query} instance
	 * @see {@link javax.persistence.Query#setParameter(String, Object)}
	 */
	public Query setParameter(String name, Object value) {
		query.setParameter(name, value);
		return this;
	}

	/**
	 * Binds the given {@link Calendar} instance to the parameter with the given
	 * name.
	 * 
	 * @param name  parameter name
	 * @param value parameter value
	 * @param type
	 * @return this {@link Query} instance
	 * @see {@link javax.persistence.Query#setParameter(String, Calendar, TemporalType)}
	 */
	public Query setParameter(String name, Calendar value, TemporalType type) {
		query.setParameter(name, value, type);
		return this;
	}

	/**
	 * Binds the given {@link Date} instance to the parameter with the given name.
	 * 
	 * @param name  parameter name
	 * @param value parameter value
	 * @param type
	 * @return this {@link Query} instance
	 * @see {@link javax.persistence.Query#setParameter(String, Date, TemporalType)}
	 */
	public Query setParameter(String name, Date value, TemporalType type) {
		query.setParameter(name, value, type);
		return this;
	}

	/**
	 * Sets a query property or hint.
	 * 
	 * @param name  property/hint name
	 * @param value property/hint value
	 * @see {@link javax.persistence.Query#setHint(String, Object)}
	 */
	public void setHint(String name, Object value) {
		query.setHint(name, value);
	}

	/**
	 * Executes the query and returns a single result.
	 * 
	 * @param <T> the type of the result, resolved at runtime
	 * @return the single result of the query execution
	 * @see {@link javax.persistence.Query#getSingleResult()}
	 */
	@SuppressWarnings("unchecked")
	public <T> T getSingleResult() {
		return (T) query.getSingleResult();
	}

	/**
	 * Executes the query and returns a list of results.
	 * 
	 * @param <T> the type of the result, resolved at runtime
	 * @return the list of results of the query execution
	 * @see {@link javax.persistence.Query#getResultList()}
	 */
	@SuppressWarnings("unchecked")
	public <T> List<T> getResultList() {
		return (List<T>) query.getResultList();
	}

	/**
	 * Executes the query and returns a page of results.
	 * 
	 * @param <T>        the type of the results, resolved at runtime
	 * @param pageNumber the number of the page
	 * @param pageSize   the size of the page
	 * @return a page of results
	 */
	public <T> List<T> getResultPage(int pageNumber, int pageSize) {
		query.setFirstResult((pageNumber - 1) * pageSize);
		query.setMaxResults(pageSize);
		return getResultList();
	}

	/**
	 * Executes an update command.
	 * 
	 * @return the number of entites affected.
	 * @see {@link javax.persistence.Query#executeUpdate()}
	 */
	public int executeUpdate() {
		return query.executeUpdate();
	}
}
