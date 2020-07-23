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
		/* Creates the mongo client by connecting it to the mongodb instance, both
		 * repositories and collections and the transaction manager; also empties
		 * the database before each test */
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
		/* Close the client connection after each test so that it can
		 * be created anew in the next test */
		mongoClient.close();
	}
	

	@AfterClass
	public static void stopContainer() {
		// Stops the container after all methods have been executed
		mongoContainer.stop();
	}
	
	@Test
	public void testGetAllTasks() {
		// Setup phase
		Task firstTask = new Task("1", "Buy groceries");
		Task secondTask = new Task("2", "Start using TDD");
		addTaskToCollection(firstTask, Collections.emptyList());
		addTaskToCollection(secondTask, Collections.emptyList());

		// Exercise phase
		List<Task> retrievedTasks = todoService.getAllTasks();
		
		// Verify phase
		assertThat(retrievedTasks).containsExactly(firstTask, secondTask);
	}
	
	@Test
	public void testFindTaskById() {
		// Setup phase
		Task firstTask = new Task("1", "Buy groceries");
		Task secondTask = new Task("2", "Start using TDD");
		addTaskToCollection(firstTask, Collections.emptyList());
		addTaskToCollection(secondTask, Collections.emptyList());

		// Exercise phase
		Task retrievedTask = todoService.findTaskById(firstTask.getId());

		// Verify phase
		assertThat(retrievedTask).isEqualTo(firstTask);
	}
	
	@Test
	public void testSaveTask() {
		// Setup phase
		Task task = new Task("1", "Buy groceries");

		// Exercise phase
		todoService.saveTask(task);

		// Verify phase
		assertThat(getAllTasksFromDatabase()).containsExactly(task);
	}
	
	@Test
	public void testDeleteTask() {
		// Setup phase
		Task task = new Task("1", "Buy groceries");
		addTaskToCollection(task, Collections.emptyList());

		// Exercise phase
		todoService.deleteTask(task);

		// Verify phase
		assertThat(getAllTasksFromDatabase()).isEmpty();
	}
	
	@Test
	public void testGetAllTags() {
		// Setup phase
		Tag firstTag = new Tag("1", "Work");
		Tag secondTag = new Tag("2", "Important");
		addTagToCollection(firstTag, Collections.emptyList());
		addTagToCollection(secondTag, Collections.emptyList());

		// Exercise phase
		List<Tag> retrievedTags = todoService.getAllTags();

		// Verify phase
		assertThat(retrievedTags).containsExactly(firstTag, secondTag);
	}
	
	@Test
	public void testFindTagById() {
		// Setup phase
		Tag firstTag = new Tag("1", "Work");
		Tag secondTag = new Tag("2", "Important");
		addTagToCollection(firstTag, Collections.emptyList());
		addTagToCollection(secondTag, Collections.emptyList());

		// Exercise phase
		Tag retrievedTag = todoService.findTagById(firstTag.getId());

		// Verify phase
		assertThat(retrievedTag).isEqualTo(firstTag);
	}
	
	@Test
	public void testSaveTag() {
		// Setup phase
		Tag tag = new Tag("1", "Work");

		// Exercise phase
		todoService.saveTag(tag);

		// Verify phase
		assertThat(getAllTagsFromDatabase()).containsExactly(tag);
	}
	
	@Test
	public void testDeleteTag() {
		// Setup phase
		Tag tag = new Tag("1", "Work");
		addTagToCollection(tag, Collections.emptyList());

		// Exercise phase
		todoService.deleteTag(tag);

		// Verify phase
		assertThat(getAllTagsFromDatabase()).isEmpty();
	}
	
	@Test
	public void testAddTagToTask() {
		// Setup phase
		Task task = new Task("1", "Start using TDD");
		Tag tag = new Tag("1", "Work");
		addTaskToCollection(task, Collections.emptyList());
		addTagToCollection(tag, Collections.emptyList());

		// Exercise phase
		todoService.addTagToTask(task.getId(), tag.getId());

		// Verify phase
		assertThat(getTagsAssignedToTask(task)).containsExactly(tag.getId());
		assertThat(getTasksAssignedToTag(tag)).containsExactly(task.getId());
	}
	
	@Test
	public void testFindTagsByTaskId() {
		// Setup phase
		Task task = new Task("1", "Start using TDD");
		Tag tag = new Tag("1", "Work");
		addTaskToCollection(task, Collections.singletonList(tag.getId()));
		addTagToCollection(tag, Collections.singletonList(task.getId()));

		// Exercise phase
		List<String> retrievedTags = todoService.findTagsByTaskId(task.getId());

		// Verify phase
		assertThat(retrievedTags).containsExactly("1");
	}
	
	@Test
	public void testRemoveTagFromTask() {
		// Setup phase
		Task task = new Task("1", "Start using TDD");
		Tag tag = new Tag("1", "Work");
		addTaskToCollection(task, Collections.singletonList(tag.getId()));
		addTagToCollection(tag, Collections.singletonList(task.getId()));

		// Exercise phase
		todoService.removeTagFromTask(task.getId(), tag.getId());

		// Verify phase
		assertThat(getTagsAssignedToTask(task)).isEmpty();
		assertThat(getTasksAssignedToTag(tag)).isEmpty();
	}
	
	@Test
	public void testFindTasksByTagId() {
		// Setup phase
		Task task = new Task("1", "Start using TDD");
		Tag tag = new Tag("1", "Work");
		addTaskToCollection(task, Collections.singletonList(tag.getId()));
		addTagToCollection(tag, Collections.singletonList(task.getId()));
		
		// Exercise phase
		List<String> retrievedTasks = todoService.findTasksByTagId(tag.getId());

		// Verify phase
		assertThat(retrievedTasks).containsExactly("1");
	}
	
	private void addTaskToCollection(Task task, List<String> tags) {
		// Private method to directly insert a task in the collection
		taskCollection.insertOne(new Document()
				.append("id", task.getId())
				.append("description", task.getDescription())
				.append("tags", tags)
				);
	}

	private void addTagToCollection(Tag tag, List<String> tasks) {
		// Private method to directly insert a tag in the collection
		tagCollection.insertOne(new Document()
				.append("id", tag.getId())
				.append("name", tag.getName())
				.append("tasks", tasks)
				);
	}

	private List<Task> getAllTasksFromDatabase() {
		// Private method to directly retrieve all tasks from the collection
		return StreamSupport
				.stream(taskCollection.find().spliterator(), false)
				.map(d -> new Task(d.getString("id"), d.getString("description")))
				.collect(Collectors.toList());
	}

	private List<Tag> getAllTagsFromDatabase() {
		// Private method to directly retrieve all tags from the collection
		return StreamSupport
				.stream(tagCollection.find().spliterator(), false)
				.map(d -> new Tag(d.getString("id"), d.getString("name")))
				.collect(Collectors.toList());
	}

	private List<String> getTagsAssignedToTask(Task task) {
		/* Private method to directly retrieve all tags assigned to a task 
		 * from the collection */
		return taskCollection
				.find(Filters.eq("id", task.getId()))
				.first()
				.getList("tags", String.class);
	}

	private List<String> getTasksAssignedToTag(Tag tag) {
		/* Private method to directly retrieve all tasks assigned to a tag 
		 * from the collection */
		return tagCollection
				.find(Filters.eq("id", tag.getId()))
				.first()
				.getList("tasks", String.class);
	}
}
