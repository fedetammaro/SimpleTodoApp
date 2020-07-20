package it.unifi.simpletodoapp.service;

import static org.assertj.core.api.Assertions.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.bson.Document;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.GenericContainer;

import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import it.unifi.simpletodoapp.model.Tag;
import it.unifi.simpletodoapp.model.Task;
import it.unifi.simpletodoapp.repository.mongo.TagMongoRepository;
import it.unifi.simpletodoapp.repository.mongo.TaskMongoRepository;
import it.unifi.simpletodoapp.repository.mongo.TransactionManagerMongo;

public class TodoServiceTransactionsIT {
	@SuppressWarnings("rawtypes")
	@ClassRule
	public static final GenericContainer mongoContainer =
	new GenericContainer("krnbr/mongo").withExposedPorts(27017);
	
	private TodoService todoService;
	private TransactionManagerMongo transactionManagerMongo;
	private TaskMongoRepository taskMongoRepository;
	private TagMongoRepository tagMongoRepository;
	
	private MongoClient mongoClient;
	
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
		
		todoService = new TodoService(transactionManagerMongo);

		MongoDatabase database = mongoClient.getDatabase("todoapp");

		database.drop();
		taskCollection = database.getCollection("tasks");
		tagCollection = database.getCollection("tags");
	}
	
	@After
	public void tearDown() {
		mongoClient.close();
	}
	

	@AfterClass
	public static void stopContainer() {
		mongoContainer.stop();
	}
	
	@Test
	public void testGetAllTasks() {
		Task firstTask = new Task("1", "Buy groceries");
		Task secondTask = new Task("2", "Start using TDD");
		addTaskToCollection(firstTask, Collections.emptyList());
		addTaskToCollection(secondTask, Collections.emptyList());
		
		List<Task> retrievedTasks = todoService.getAllTasks();
		
		assertThat(retrievedTasks).containsExactly(firstTask, secondTask);
	}
	
	@Test
	public void testFindTaskById() {
		Task firstTask = new Task("1", "Buy groceries");
		Task secondTask = new Task("2", "Start using TDD");
		addTaskToCollection(firstTask, Collections.emptyList());
		addTaskToCollection(secondTask, Collections.emptyList());
		
		Task retrievedTask = todoService.findTaskById(firstTask.getId());
		
		assertThat(retrievedTask).isEqualTo(firstTask);
	}
	
	@Test
	public void testSaveTask() {
		Task task = new Task("1", "Buy groceries");
		
		todoService.saveTask(task);
		
		assertThat(getAllTasksFromDatabase()).containsExactly(task);
	}
	
	@Test
	public void testDeleteTask() {
		Task task = new Task("1", "Buy groceries");
		addTaskToCollection(task, Collections.emptyList());
		
		todoService.deleteTask(task);
		
		assertThat(getAllTasksFromDatabase()).isEmpty();
	}
	
	@Test
	public void testGetAllTags() {
		Tag firstTag = new Tag("1", "Work");
		Tag secondTag = new Tag("2", "Important");
		addTagToCollection(firstTag, Collections.emptyList());
		addTagToCollection(secondTag, Collections.emptyList());
		
		List<Tag> retrievedTags = todoService.getAllTags();
		
		assertThat(retrievedTags).containsExactly(firstTag, secondTag);
	}
	
	@Test
	public void testFindTagById() {
		Tag firstTag = new Tag("1", "Work");
		Tag secondTag = new Tag("2", "Important");
		addTagToCollection(firstTag, Collections.emptyList());
		addTagToCollection(secondTag, Collections.emptyList());
		
		Tag retrievedTag = todoService.findTagById(firstTag.getId());
		
		assertThat(retrievedTag).isEqualTo(firstTag);
	}
	
	@Test
	public void testSaveTag() {
		Tag tag = new Tag("1", "Work");
		
		todoService.saveTag(tag);
		
		assertThat(getAllTagsFromDatabase()).containsExactly(tag);
	}
	
	@Test
	public void testDeleteTag() {
		Tag tag = new Tag("1", "Work");
		addTagToCollection(tag, Collections.emptyList());
		
		todoService.deleteTag(tag);
		
		assertThat(getAllTagsFromDatabase()).isEmpty();
	}
	
	private void addTaskToCollection(Task task, List<Tag> tags) {
		taskCollection.insertOne(new Document()
				.append("id", task.getId())
				.append("description", task.getDescription())
				.append("tags", tags));
	}
	
	private void addTagToCollection(Tag tag, List<Task> tasks) {
		taskCollection.insertOne(new Document()
				.append("id", tag.getId())
				.append("name", tag.getName())
				.append("tasks", tasks));
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
}
