package it.unifi.simpletodoapp.repository.mongo;

import com.mongodb.MongoClient;
import com.mongodb.client.ClientSession;

import it.unifi.simpletodoapp.repository.CompositeTransactionCode;
import it.unifi.simpletodoapp.repository.TagTransactionCode;
import it.unifi.simpletodoapp.repository.TaskTransactionCode;
import it.unifi.simpletodoapp.repository.TransactionManager;

public class TransactionManagerMongo implements TransactionManager {
	private MongoClient mongoClient;
	private TaskMongoRepository taskMongoRepository;
	private TagMongoRepository tagMongoRepository;
	
	public TransactionManagerMongo(MongoClient mongoClient, TaskMongoRepository taskMongoRepository,
			TagMongoRepository tagMongoRepository) {
		this.mongoClient = mongoClient;
		this.taskMongoRepository = taskMongoRepository;
		this.tagMongoRepository = tagMongoRepository;
	}

	@Override
	public <T> T doTaskTransaction(TaskTransactionCode<T> code) {
		ClientSession clientSession = mongoClient.startSession();
		T value = null;
		
		try {
			clientSession.startTransaction();
			value = code.apply(taskMongoRepository);
			clientSession.commitTransaction();
		} catch(Exception e) {
			clientSession.abortTransaction();
		} finally {
			clientSession.close();
		}
		
		return value;
	}

	@Override
	public <T> T doTagTransaction(TagTransactionCode<T> code) {
		ClientSession clientSession = mongoClient.startSession();
		T value = null;
		
		try {
			clientSession.startTransaction();
			value = code.apply(tagMongoRepository);
			clientSession.commitTransaction();
		} catch(Exception e) {
			clientSession.abortTransaction();
		} finally {
			clientSession.close();
		}
		
		return value;
	}

	@Override
	public <T> T doCompositeTransaction(CompositeTransactionCode<T> code) {
		ClientSession clientSession = mongoClient.startSession();
		T value = null;
		
		try {
			clientSession.startTransaction();
			value = code.apply(taskMongoRepository, tagMongoRepository);
			clientSession.commitTransaction();
		} catch(Exception e) {
			clientSession.abortTransaction();
		} finally {
			clientSession.close();
		}
		
		return value;
	}

}
