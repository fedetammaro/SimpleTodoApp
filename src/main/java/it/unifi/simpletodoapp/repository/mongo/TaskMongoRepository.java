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
		Document document = taskCollection.find(Filters.eq("id", taskId)).first();
		
		if (document != null)
			return createTaskFromMongoDocument(document);
		else
			return null;
	}

	@Override
	public void save(Task task) {
		taskCollection.insertOne(new Document()
				.append("id", task.getId())
				.append("description", task.getDescription())
				.append("tags", Collections.emptyList()));		
	}

	@Override
	public void delete(Task task) {
		taskCollection.deleteOne(Filters.eq("id", task.getId()));
	}

	@Override
	public List<String> getTagsByTaskId(String taskId) {
		return taskCollection.find(Filters.eq("id", taskId)).first().getList("tags", String.class);
		
	}

	@Override
	public void addTagToTask(String taskId, String tagId) {
		taskCollection.updateOne(Filters.eq("id", taskId), 
				Updates.push("tags", tagId));
	}

	@Override
	public void removeTagFromTask(String taskId, String tagId) {
		taskCollection.updateOne(Filters.eq("id", taskId), 
				Updates.pull("tags", tagId));
	}
	
	private Task createTaskFromMongoDocument(Document document) {
		return new Task(document.getString("id"), document.getString("description"));
	}

}
