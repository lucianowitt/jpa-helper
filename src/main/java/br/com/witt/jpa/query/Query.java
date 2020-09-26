package br.com.witt.jpa.query;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TemporalType;

public abstract class Query {

	protected EntityManager em;
	protected javax.persistence.Query query;

	public Query(EntityManager em) {
		this.em = em;
	}

	public Query setParameter(String name, Object value) {
		query.setParameter(name, value);
		return this;
	}

	public Query setParameter(String name, Calendar value, TemporalType type) {
		query.setParameter(name, value, type);
		return this;
	}

	public Query setParameter(String name, Date value, TemporalType type) {
		query.setParameter(name, value, type);
		return this;
	}

	public void setHint(String name, Object value) {
		query.setHint(name, value);
	}

	@SuppressWarnings("unchecked")
	public <T> T getSingleResult() {
		return (T) query.getSingleResult();
	}

	@SuppressWarnings("unchecked")
	public <T> List<T> getResultList() {
		return (List<T>) query.getResultList();
	}

	public <T> List<T> getResultPage(int pageNumber, int pageSize) {
		query.setFirstResult((pageNumber - 1) * pageSize);
		query.setMaxResults(pageSize);
		return getResultList();
	}

	public int executeUpdate() {
		return query.executeUpdate();
	}
}
