package it.unifi.simpletodoapp.repository;

import com.mongodb.client.ClientSession;

import it.unifi.simpletodoapp.repository.mongo.TagMongoRepository;
import it.unifi.simpletodoapp.repository.mongo.TaskMongoRepository;
import it.unifi.simpletodoapp.utility.TriFunction;

@FunctionalInterface
public interface CompositeTransactionCode<T> extends TriFunction<TaskMongoRepository, TagMongoRepository, ClientSession, T>{

}