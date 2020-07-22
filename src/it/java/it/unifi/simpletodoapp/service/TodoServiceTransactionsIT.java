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
import com.mongodb.client.model.Filters;

import it.unifi.simpletodoapp.model.Tag;
import it.unifi.simpletodoapp.model.Task;
import it.unifi.simpletodoapp.repository.mongo.TagMongoRepository;
import it.unifi.simpletodoapp.repository.mongo.TaskMongoRepository;
import it.unifi.simpletodoapp.repository.mongo.TransactionManagerMongo;

public class TodoServiceTransactionsIT {
	private static final int MONGO_PORT = 27017;
	private static final String DB_NAME = "todoapp";
	private static final String TASKS_COLLECTION = "tasks";
	private static final String TAGS_COLLECTION = "tags";

	@SuppressWarnings("rawtypes")
	@ClassRule
	public static final GenericContainer mongoContainer =
	new GenericContainer("krnbr/mongo").withExposedPorts(MONGO_PORT);
	
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
				mongoContainer.getMappedPort(MONGO_PORT))
				);
		taskMongoRepository = new TaskMongoRepository(mongoClient, DB_NAME, TASKS_COLLECTION);
		tagMongoRepository = new TagMongoRepository(mongoClient, DB_NAME, TAGS_COLLECTION);
		transactionManagerMongo = new TransactionManagerMongo(mongoClient, taskMongoRepository, tagMongoRepository);
		
		todoService = new TodoService(transactionManagerMongo);

		MongoDatabase database = mongoClient.getDatabase(DB_NAME);

		database.drop();
		taskCollection = database.getCollection(TASKS_COLLECTION);
		tagCollection = database.getCollection(TAGS_COLLECTION);
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
	
	@Test
	public void testAddTagToTask() {
		Task task = new Task("1", "Start using TDD");
		Tag tag = new Tag("1", "Work");
		addTaskToCollection(task, Collections.emptyList());
		addTagToCollection(tag, Collections.emptyList());
		
		todoService.addTagToTask(task.getId(), tag.getId());
		
		assertThat(getTagsAssignedToTask(task)).containsExactly(tag.getId());
		assertThat(getTasksAssignedToTag(tag)).containsExactly(task.getId());
	}
	
	@Test
	public void testFindTagsByTaskId() {
		Task task = new Task("1", "Start using TDD");
		Tag tag = new Tag("1", "Work");
		addTaskToCollection(task, Collections.singletonList(tag.getId()));
		addTagToCollection(tag, Collections.singletonList(task.getId()));
		
		List<String> retrievedTags = todoService.findTagsByTaskId(task.getId());
		
		assertThat(retrievedTags).containsExactly("1");
	}
	
	@Test
	public void testRemoveTagFromTask() {
		Task task = new Task("1", "Start using TDD");
		Tag tag = new Tag("1", "Work");
		addTaskToCollection(task, Collections.singletonList(tag.getId()));
		addTagToCollection(tag, Collections.singletonList(task.getId()));
		
		todoService.removeTagFromTask(task.getId(), tag.getId());
		
		assertThat(getTagsAssignedToTask(task)).isEmpty();
		assertThat(getTasksAssignedToTag(tag)).isEmpty();
	}
	
	@Test
	public void testFindTasksByTagId() {
		Task task = new Task("1", "Start using TDD");
		Tag tag = new Tag("1", "Work");
		addTaskToCollection(task, Collections.singletonList(tag.getId()));
		addTagToCollection(tag, Collections.singletonList(task.getId()));
		
		List<String> retrievedTasks = todoService.findTasksByTagId(tag.getId());
		
		assertThat(retrievedTasks).containsExactly("1");
	}
	
	private void addTaskToCollection(Task task, List<String> tags) {
		taskCollection.insertOne(new Document()
				.append("id", task.getId())
				.append("description", task.getDescription())
				.append(TAGS_COLLECTION, tags)
				);
	}
	
	private void addTagToCollection(Tag tag, List<String> tasks) {
		tagCollection.insertOne(new Document()
				.append("id", tag.getId())
				.append("name", tag.getName())
				.append(TASKS_COLLECTION, tasks)
				);
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
				.getList(TAGS_COLLECTION, String.class);
	}
	
	private List<String> getTasksAssignedToTag(Tag tag) {
		return tagCollection
				.find(Filters.eq("id", tag.getId()))
				.first()
				.getList(TASKS_COLLECTION, String.class);
	}
}
