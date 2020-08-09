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

import it.unifi.simpletodoapp.model.Tag;

public class TagMongoRepository {
	private static final String ID = "id";
	private static final String NAME = "name";
	private static final String TASKS = "tasks";

	private MongoCollection<Document> tagCollection;

	public TagMongoRepository(MongoClient mongoClient, String dbName, String dbCollection) {
		tagCollection = mongoClient.getDatabase(dbName)
				.getCollection(dbCollection);
	}

	public List<Tag> findAll(ClientSession clientSession) {
		return StreamSupport
				.stream(tagCollection.find(clientSession).spliterator(), false)
				.map(this::createTagFromMongoDocument)
				.collect(Collectors.toList());
	}

	public Tag findById(String tagId, ClientSession clientSession) {
		Document document = tagCollection.find(clientSession, Filters.eq(ID, tagId))
				.first();

		if (document != null)
			return createTagFromMongoDocument(document);
		else
			return null;
	}

	public void save(Tag tag, ClientSession clientSession) {
		tagCollection.insertOne(clientSession, new Document()
				.append(ID, tag.getId())
				.append(NAME, tag.getName())
				.append(TASKS, Collections.emptyList())
				);		
	}

	public void delete(Tag tag, ClientSession clientSession) {
		tagCollection.deleteOne(clientSession, Filters.eq(ID, tag.getId()));

	}

	public List<String> getTasksByTagId(String tagId, ClientSession clientSession) {
		return tagCollection.find(clientSession, Filters.eq(ID, tagId))
				.first()
				.getList(TASKS, String.class);
	}

	public void addTaskToTag(String tagId, String taskId, ClientSession clientSession) {
		tagCollection.updateOne(clientSession, Filters.eq(ID, tagId), 
				Updates.push(TASKS, taskId));
	}

	public void removeTaskFromTag(String tagId, String taskId, ClientSession clientSession) {
		tagCollection.updateOne(clientSession, Filters.eq(ID, tagId), 
				Updates.pull(TASKS, taskId));
	}

	private Tag createTagFromMongoDocument(Document document) {
		return new Tag(document.getString(ID), document.getString(NAME));
	}
}