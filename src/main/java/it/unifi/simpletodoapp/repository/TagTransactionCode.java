package it.unifi.simpletodoapp.repository;

import java.util.function.Function;

@FunctionalInterface
public interface TagTransactionCode<T> extends Function<TagRepository, T>{

}
