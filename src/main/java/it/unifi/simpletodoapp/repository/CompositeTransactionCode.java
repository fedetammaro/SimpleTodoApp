package it.unifi.simpletodoapp.repository;

import java.util.function.BiFunction;

@FunctionalInterface
public interface CompositeTransactionCode<T> extends BiFunction<TaskRepository, TagRepository, T>{

}
