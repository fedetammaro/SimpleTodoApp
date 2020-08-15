package it.unifi.simpletodoapp.controller;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

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
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testcontainers.containers.MongoDBContainer;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

import it.unifi.simpletodoapp.model.Tag;
import it.unifi.simpletodoapp.model.Task;
import it.unifi.simpletodoapp.repository.mongo.TagMongoRepository;
import it.unifi.simpletodoapp.repository.mongo.TaskMongoRepository;
import it.unifi.simpletodoapp.repository.mongo.TransactionManagerMongo;
import it.unifi.simpletodoapp.service.TodoService;
import it.unifi.simpletodoapp.view.swing.TodoSwingView;

public class TodoControllerServiceIT {
	private static final int MONGO_PORT = 27017;
	private static final String DB_NAME = "todoapp";
	private static final String TASKS_COLLECTION = "tasks";
	private static final String TAGS_COLLECTION = "tags";

	@ClassRule
	public static final MongoDBContainer mongoContainer = new MongoDBContainer()
	.withExposedPorts(MONGO_PORT);

	private TransactionManagerMongo transactionManagerMongo;
	private TodoService todoService;
	private TodoController todoController;

	@Mock
	private TodoSwingView todoSwingView;

	private MongoClient mongoClient;
	private TaskMongoRepository taskMongoRepository;
	private TagMongoRepository tagMongoRepository;
	private MongoCollection<Document> taskCollection;
	private MongoCollection<Document> tagCollection;

	@Before
	public void setup() {
		/* Initializes the view mock, creates the mongo client by connecting it to the mongodb
		 * instance, both repositories and collections; also empties the database before each
		 * test */
		MockitoAnnotations.initMocks(this);

		String mongoRsUrl = mongoContainer.getReplicaSetUrl();
		mongoClient = MongoClients.create(mongoRsUrl);
		
		taskMongoRepository = new TaskMongoRepository(mongoClient, DB_NAME, TASKS_COLLECTION);
		tagMongoRepository = new TagMongoRepository(mongoClient, DB_NAME, TAGS_COLLECTION);

		transactionManagerMongo = new TransactionManagerMongo(mongoClient, taskMongoRepository, tagMongoRepository);
		todoService = new TodoService(transactionManagerMongo);
		todoController = new TodoController(todoService, todoSwingView);

		MongoDatabase database = mongoClient.getDatabase(DB_NAME);

		database.drop();
		database.createCollection(TASKS_COLLECTION);
		database.createCollection(TAGS_COLLECTION);
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
		Task task = new Task("1", "Buy groceries");
		addTaskToCollection(task, Collections.emptyList());
		
		// Exercise phase
		todoController.getAllTasks();
		
		// Verify phase
		verify(todoSwingView).showAllTasks(Collections.singletonList(task));
	}

	@Test
	public void testAddTask() {
		// Setup phase
		Task task = new Task("1", "Buy groceries");
		
		// Exercise phase
		todoController.addTask(task);
		
		// Verify phase
		assertThat(getAllTasksFromDatabase())
		.containsExactly(task);
		verify(todoSwingView).taskAdded(task);
	}

	@Test
	public void testDeleteTask() {
		// Setup phase
		Task task = new Task("1", "Buy groceries");
		addTaskToCollection(task, Collections.emptyList());

		// Exercise phase
		todoController.deleteTask(task);

		// Verify phase
		assertThat(getAllTasksFromDatabase())
		.isEmpty();
		verify(todoSwingView).taskDeleted(task);
	}

	@Test
	public void testGetAllTags() {
		// Setup phase
		Tag tag = new Tag("1", "Work");
		addTagToCollection(tag, Collections.emptyList());

		// Exercise phase
		todoController.getAllTags();
		
		// Verify phase
		verify(todoSwingView).showAllTags(Collections.singletonList(tag));
	}

	@Test
	public void testAddTag() {
		// Setup phase
		Tag tag = new Tag("1", "Work");

		// Exercise phase
		todoController.addTag(tag);

		// Verify phase
		assertThat(getAllTagsFromDatabase())
		.containsExactly(tag);
		verify(todoSwingView).tagAdded(tag);
	}

	@Test
	public void testDeleteTag() {
		// Setup phase
		Tag tag = new Tag("1", "Work");
		addTagToCollection(tag, Collections.emptyList());

		// Exercise phase
		todoController.removeTag(tag);

		// Verify phase
		assertThat(getAllTagsFromDatabase())
		.isEmpty();
		verify(todoSwingView).tagRemoved(tag);
	}

	@Test
	public void testAddTagToTask() {
		// Setup phase
		Task task = new Task("1", "Start using TDD");
		Tag tag = new Tag("1", "Work");
		addTaskToCollection(task, Collections.emptyList());
		addTagToCollection(tag, Collections.emptyList());

		// Exercise phase
		todoController.addTagToTask(task, tag);

		// Verify phase
		assertThat(getTagsAssignedToTask(task))
		.containsExactly("1");
		assertThat(getTasksAssignedToTag(tag))
		.containsExactly("1");
		verify(todoSwingView).tagAddedToTask(tag);
	}

	@Test
	public void testRemoveTagFromTask() {
		// Setup phase
		Task task = new Task("1", "Start using TDD");
		Tag tag = new Tag("1", "Work");
		addTaskToCollection(task, Collections.singletonList(tag.getId()));
		addTagToCollection(tag, Collections.singletonList(task.getId()));

		// Exercise phase
		todoController.removeTagFromTask(task, tag);

		// Verify phase
		assertThat(getTagsAssignedToTask(task))
		.isEmpty();
		assertThat(getTasksAssignedToTag(tag))
		.isEmpty();
		verify(todoSwingView).tagRemovedFromTask(tag);
	}

	@Test
	public void testGetTagsByTask() {
		// Setup phase
		Task task = new Task("1", "Start using TDD");
		Tag tag = new Tag("1", "Work");
		addTaskToCollection(task, Collections.singletonList(tag.getId()));
		addTagToCollection(tag, Collections.singletonList(task.getId()));

		// Exercise phase
		todoController.getTagsByTask(task);

		// Verify phase
		verify(todoSwingView).showTaskTags(Collections.singletonList(tag));
	}

	@Test
	public void testGetTasksByTag() {
		// Setup phase
		Task task = new Task("1", "Start using TDD");
		Tag tag = new Tag("1", "Work");
		addTaskToCollection(task, Collections.singletonList(tag.getId()));
		addTagToCollection(tag, Collections.singletonList(task.getId()));

		// Exercise phase
		todoController.getTasksByTag(tag);

		// Verify phase
		verify(todoSwingView).showTagTasks(Collections.singletonList(task));
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