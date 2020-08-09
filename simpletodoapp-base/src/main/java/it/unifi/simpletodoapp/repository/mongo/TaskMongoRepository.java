package it.unifi.simpletodoapp.repository.mongo;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

import it.unifi.simpletodoapp.model.Task;

public class TaskMongoRepository {
	private static final String ID = "id";
	private static final String DESCRIPTION = "description";
	private static final String TAGS = "tags";

	private MongoCollection<Document> taskCollection;

	public TaskMongoRepository(MongoClient mongoClient, String dbName, String dbCollection) {
		taskCollection = mongoClient.getDatabase(dbName)
				.getCollection(dbCollection);
	}

	public List<Task> findAll(ClientSession clientSession) {
		return StreamSupport
				.stream(taskCollection.find(clientSession).spliterator(), false)
				.map(this::createTaskFromMongoDocument)
				.collect(Collectors.toList());
	}

	public Task findById(String taskId, ClientSession clientSession) {
		Document document = taskCollection.find(clientSession, Filters.eq(ID, taskId))
				.first();

		if (document != null)
			return createTaskFromMongoDocument(document);
		else
			return null;
	}

	public void save(Task task, ClientSession clientSession) {
		taskCollection.insertOne(clientSession, new Document()
				.append(ID, task.getId())
				.append(DESCRIPTION, task.getDescription())
				.append(TAGS, Collections.emptyList())
				);		
	}

	public void delete(Task task, ClientSession clientSession) {
		taskCollection.deleteOne(clientSession, Filters.eq(ID, task.getId()));
	}

	public List<String> getTagsByTaskId(String taskId, ClientSession clientSession) {
		return taskCollection.find(clientSession, Filters.eq(ID, taskId))
				.first()
				.getList(TAGS, String.class);

	}

	public void addTagToTask(String taskId, String tagId, ClientSession clientSession) {
		taskCollection.updateOne(clientSession, Filters.eq(ID, taskId), 
				Updates.push(TAGS, tagId));
	}

	public void removeTagFromTask(String taskId, String tagId, ClientSession clientSession) {
		taskCollection.updateOne(clientSession, Filters.eq(ID, taskId), 
				Updates.pull(TAGS, tagId));
	}

	private Task createTaskFromMongoDocument(Document document) {
		return new Task(document.getString(ID), document.getString(DESCRIPTION));
	}
}