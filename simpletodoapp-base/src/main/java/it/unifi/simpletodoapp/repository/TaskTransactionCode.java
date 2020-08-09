package it.unifi.simpletodoapp.repository;

import java.util.function.BiFunction;

import com.mongodb.client.ClientSession;

import it.unifi.simpletodoapp.repository.mongo.TaskMongoRepository;

@FunctionalInterface
public interface TaskTransactionCode<T> extends BiFunction<TaskMongoRepository, ClientSession, T>{

}