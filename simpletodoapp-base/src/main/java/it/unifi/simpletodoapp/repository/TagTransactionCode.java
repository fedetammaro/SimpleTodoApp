package it.unifi.simpletodoapp.repository;

import java.util.function.BiFunction;

import com.mongodb.client.ClientSession;

import it.unifi.simpletodoapp.repository.mongo.TagMongoRepository;

@FunctionalInterface
public interface TagTransactionCode<T> extends BiFunction<TagMongoRepository, ClientSession, T>{

}