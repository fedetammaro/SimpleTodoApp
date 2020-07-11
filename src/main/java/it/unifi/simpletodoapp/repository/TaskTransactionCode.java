package it.unifi.simpletodoapp.repository;

import java.util.function.Function;

@FunctionalInterface
public interface TaskTransactionCode<T> extends Function<TaskRepository, T>{

}
