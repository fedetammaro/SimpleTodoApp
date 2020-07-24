package it.unifi.simpletodoapp.repository.mongo;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

import it.unifi.simpletodoapp.model.Task;
import it.unifi.simpletodoapp.repository.TaskRepository;

public class TaskMongoRepository implements TaskRepository {
	private static final String ID = "id";
	private static final String DESCRIPTION = "description";
	private static final String TAGS = "tags";
	private MongoCollection<Document> taskCollection;

	public TaskMongoRepository(MongoClient mongoClient, String dbName, String dbCollection) {
		taskCollection = mongoClient.getDatabase(dbName)
				.getCollection(dbCollection);
	}

	@Override
	public List<Task> findAll() {
		return StreamSupport
				.stream(taskCollection.find().spliterator(), false)
				.map(this::createTaskFromMongoDocument)
				.collect(Collectors.toList());
	}

	@Override
	public Task findById(String taskId) {
		Document document = taskCollection.find(Filters.eq(ID, taskId)).first();
		
		if (document != null)
			return createTaskFromMongoDocument(document);
		else
			return null;
	}

	@Override
	public void save(Task task) {
		taskCollection.insertOne(new Document()
				.append(ID, task.getId())
				.append(DESCRIPTION, task.getDescription())
				.append(TAGS, Collections.emptyList())
				);		
	}

	@Override
	public void delete(Task task) {
		taskCollection.deleteOne(Filters.eq(ID, task.getId()));
	}

	@Override
	public List<String> getTagsByTaskId(String taskId) {
		return taskCollection.find(Filters.eq(ID, taskId)).first().getList(TAGS, String.class);
		
	}

	@Override
	public void addTagToTask(String taskId, String tagId) {
		taskCollection.updateOne(Filters.eq(ID, taskId), 
				Updates.push(TAGS, tagId));
	}

	@Override
	public void removeTagFromTask(String taskId, String tagId) {
		taskCollection.updateOne(Filters.eq(ID, taskId), 
				Updates.pull(TAGS, tagId));
	}
	
	private Task createTaskFromMongoDocument(Document document) {
		return new Task(document.getString(ID), document.getString(DESCRIPTION));
	}
}
