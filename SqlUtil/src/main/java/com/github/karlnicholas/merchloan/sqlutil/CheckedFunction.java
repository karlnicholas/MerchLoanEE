package com.github.karlnicholas.merchloan.sqlutil;

import java.util.function.Function;

public interface CheckedFunction<T, R> extends Function<T, R> {

	@Override
	default R apply(T t) {

		try {
			return applyAndThrow(t);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	R applyAndThrow(T t) throws Exception;
}