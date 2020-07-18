package it.unifi.simpletodoapp.repository.mongo;

import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.bson.Document;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.GenericContainer;

import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

import it.unifi.simpletodoapp.model.Tag;
import it.unifi.simpletodoapp.model.Task;

public class TransactionManagerMongoIT {
	@SuppressWarnings("rawtypes")
	@ClassRule
	public static final GenericContainer mongoContainer =
	new GenericContainer("krnbr/mongo").withExposedPorts(27017);
	
	private TransactionManagerMongo transactionManagerMongo;
	
	private MongoClient mongoClient;
	private TaskMongoRepository taskMongoRepository;
	private TagMongoRepository tagMongoRepository;
	private MongoCollection<Document> taskCollection;
	private MongoCollection<Document> tagCollection;

	@Before
	public void setup() {
		mongoClient = new MongoClient(new ServerAddress(
				mongoContainer.getContainerIpAddress(),
				mongoContainer.getMappedPort(27017)));
		taskMongoRepository = new TaskMongoRepository(mongoClient, "todoapp", "tasks");
		tagMongoRepository = new TagMongoRepository(mongoClient, "todoapp", "tags");
		
		transactionManagerMongo = new TransactionManagerMongo(mongoClient, taskMongoRepository, tagMongoRepository);

		MongoDatabase database = mongoClient.getDatabase("todoapp");

		database.drop();
		taskCollection = database.getCollection("tasks");
		tagCollection = database.getCollection("tags");
	}

	@After
	public void tearDown() {
		mongoClient.close();
	}
	
	@Test
	public void testTaskTransaction() {
		Task task = new Task("1", "Start using TDD");
		
		transactionManagerMongo.doTaskTransaction(taskRepository -> {
			taskRepository.save(task);
			return null;
		});
		
		assertThat(getAllTasksFromDatabase()).containsExactly(task);
	}
	
	@Test
	public void testTagTransaction() {
		Tag tag = new Tag("1", "Work");
		
		transactionManagerMongo.doTagTransaction(tagRepository -> {
			tagRepository.save(tag);
			return null;
		});
		
		assertThat(getAllTagsFromDatabase()).containsExactly(tag);
	}
	
	@Test
	public void testCompositeTransaction() {
		Task task = new Task("1", "Start using TDD");
		Tag tag = new Tag("1", "Work");
		
		transactionManagerMongo.doCompositeTransaction((taskRepository, tagRepository) -> {
			taskRepository.save(task);
			tagRepository.save(tag);
			
			taskRepository.addTagToTask(task.getId(), tag.getId());
			tagRepository.addTaskToTag(tag.getId(), task.getId());
			return null;
		});
		
		assertThat(getTagsAssignedToTask(task)).containsExactly("1");
		assertThat(getTasksAssignedToTag(tag)).containsExactly("1");
	}
	
	private List<Task> getAllTasksFromDatabase() {
		return StreamSupport
				.stream(taskCollection.find().spliterator(), false)
				.map(d -> new Task(d.getString("id"), d.getString("description")))
				.collect(Collectors.toList());
	}
	
	private List<Tag> getAllTagsFromDatabase() {
		return StreamSupport
				.stream(tagCollection.find().spliterator(), false)
				.map(d -> new Tag(d.getString("id"), d.getString("name")))
				.collect(Collectors.toList());
	}
	
	private List<String> getTagsAssignedToTask(Task task) {
		return taskCollection
				.find(Filters.eq("id", task.getId()))
				.first()
				.getList("tags", String.class);
	}
	
	private List<String> getTasksAssignedToTag(Tag tag) {
		return tagCollection
				.find(Filters.eq("id", tag.getId()))
				.first()
				.getList("tasks", String.class);
	}
}
