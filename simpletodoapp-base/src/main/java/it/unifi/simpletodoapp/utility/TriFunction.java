package it.unifi.simpletodoapp.utility;

@FunctionalInterface
public interface TriFunction<T, U, V, R> {
	
	R apply(T t, U u, V v);
}