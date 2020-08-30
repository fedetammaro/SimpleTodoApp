package it.unifi.simpletodoapp.repository.mongo;

import com.mongodb.MongoException;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import com.mongodb.client.TransactionBody;

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

		/* TransactionBody that simply applies the TaskTransactionCode with the
		 * given TaskMongoRepository and ClientSession */
		TransactionBody<T> transactionBody = 
				() -> code.apply(taskMongoRepository, clientSession);

				try {
					// Execute the transaction within the ClientSession
					value = clientSession.withTransaction(transactionBody);
				} catch(MongoException e) {
					throw new MongoException("Task transaction failed, aborting");
				} finally {
					clientSession.close();
				}

				return value;
	}

	@Override
	public <T> T doTagTransaction(TagTransactionCode<T> code) {
		ClientSession clientSession = mongoClient.startSession();
		T value = null;

		/* TransactionBody that simply applies the TagTransactionCode with the
		 * given TagMongoRepository and ClientSession */
		TransactionBody<T> transactionBody =
				() -> code.apply(tagMongoRepository, clientSession);

				try {
					// Execute the transaction within the ClientSession
					value = clientSession.withTransaction(transactionBody);
				} catch(MongoException e) {
					throw new MongoException("Tag transaction failed, aborting");
				} finally {
					clientSession.close();
				}

				return value;
	}

	@Override
	public <T> T doCompositeTransaction(CompositeTransactionCode<T> code) {
		ClientSession clientSession = mongoClient.startSession();
		T value = null;

		/* TransactionBody that simply applies the CompositeTransactionCode with the
		 * given TaskMongoRepository, TagMongoRepository and ClientSession */
		TransactionBody<T> transactionBody =
				() -> code.apply(taskMongoRepository, tagMongoRepository, clientSession);

				try {
					// Execute the transaction within the ClientSession
					value = clientSession.withTransaction(transactionBody);
				} catch(MongoException e) {
					throw new MongoException("Composite transaction failed, aborting");
				} finally {
					clientSession.close();
				}

				return value;
	}

}