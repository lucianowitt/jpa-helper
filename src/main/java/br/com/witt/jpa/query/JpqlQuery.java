package br.com.witt.jpa.query;

import java.util.Objects;

import javax.persistence.EntityManager;

public class JpqlQuery extends Query {

	public JpqlQuery(EntityManager em) {
		super(em);
	}

	public JpqlQuery newQuery(String jpql, Class<?> resultClass) {
		if (Objects.isNull(resultClass)) {
			query = em.createQuery(jpql);
		} else {
			query = em.createQuery(jpql, resultClass);
		}
		return this;
	}

	public JpqlQuery newQuery(String sql) {
		return newQuery(sql, null);
	}
}
