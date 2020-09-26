package br.com.witt.jpa.query;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.persistence.EntityManager;

import br.com.witt.jpa.util.TypeConverter;

public class SqlQuery extends Query {

	private Class<?> resultClass;

	public SqlQuery(EntityManager em) {
		super(em);
	}

	public SqlQuery newQuery(String sql, Class<?> resultClass) {
		this.resultClass = resultClass;
		query = em.createNativeQuery(sql);
		return this;
	}

	public SqlQuery newQuery(String sql) {
		return newQuery(sql, null);
	}

	@SuppressWarnings("unchecked")
	public <T> T getScalar(Class<T> scalarClass) {
		return (T) TypeConverter.convertValue(query.getSingleResult(), scalarClass);
	}

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
