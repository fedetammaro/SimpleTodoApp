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

import it.unifi.simpletodoapp.model.Tag;
import it.unifi.simpletodoapp.repository.TagRepository;

public class TagMongoRepository implements TagRepository {
	private MongoCollection<Document> tagCollection;

	public TagMongoRepository(MongoClient mongoClient, String dbName, String dbCollection) {
		tagCollection = mongoClient.getDatabase(dbName)
				.getCollection(dbCollection);
	}

	@Override
	public List<Tag> findAll() {
		return StreamSupport
				.stream(tagCollection.find().spliterator(), false)
				.map(this::createTagFromMongoDocument)
				.collect(Collectors.toList());
	}

	@Override
	public Tag findById(String tagId) {
		Document document = tagCollection.find(Filters.eq("id", tagId)).first();

		if (document != null)
			return createTagFromMongoDocument(document);
		else
			return null;
	}

	@Override
	public void save(Tag tag) {
		tagCollection.insertOne(new Document()
				.append("id", tag.getId())
				.append("name", tag.getName())
				.append("tasks", Collections.emptyList()));		
	}

	@Override
	public void delete(Tag tag) {
		tagCollection.deleteOne(Filters.eq("id", tag.getId()));

	}

	@Override
	public List<String> getTasksByTagId(String tagId) {
		return tagCollection.find(Filters.eq("id", tagId)).first().getList("tasks", String.class);
	}

	@Override
	public void addTaskToTag(String tagId, String taskId) {
		tagCollection.updateOne(Filters.eq("id", tagId), 
				Updates.push("tasks", taskId));
	}

	@Override
	public void removeTaskFromTag(String tagId, String taskId) {
		tagCollection.updateOne(Filters.eq("id", tagId), 
				Updates.pull("tasks", taskId));
	}

	private Tag createTagFromMongoDocument(Document document) {
		return new Tag(document.getString("id"), document.getString("name"));
	}
}
