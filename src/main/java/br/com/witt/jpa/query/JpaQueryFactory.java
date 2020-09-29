package br.com.witt.jpa.query;

import javax.persistence.EntityManager;

public class JpaQueryFactory {

	private EntityManager entityManager;

	public JpaQueryFactory(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	public CriteriaQuery createCriteriaQuery() {
		return new CriteriaQuery(entityManager).newQuery();
	}

	public CriteriaQuery createCriteriaQuery(Class<?> resultClass) {
		return new CriteriaQuery(entityManager).newQuery(resultClass);
	}

	public JpqlQuery createJpqlQuery(String jpql) {
		return new JpqlQuery(entityManager).newQuery(jpql);
	}

	public JpqlQuery createJpqlQuery(String jpql, Class<?> resultClass) {
		return new JpqlQuery(entityManager).newQuery(jpql, resultClass);
	}

	public SqlQuery createSqlQuery(String sql) {
		return new SqlQuery(entityManager).newQuery(sql);
	}

	public SqlQuery createSqlQuery(String sql, Class<?> resultClass) {
		return new SqlQuery(entityManager).newQuery(sql, resultClass);
	}
}
