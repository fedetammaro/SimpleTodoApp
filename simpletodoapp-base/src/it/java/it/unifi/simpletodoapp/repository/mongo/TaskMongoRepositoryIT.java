package it.unifi.simpletodoapp.repository.mongo;

import static org.assertj.core.api.Assertions.*;

import java.util.Arrays;
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
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

import it.unifi.simpletodoapp.model.Task;

public class TaskMongoRepositoryIT {
	private static final int MONGO_PORT = 27017;
	private static final String DB_NAME = "todoapp";
	private static final String TASKS_COLLECTION = "tasks";

	@SuppressWarnings("rawtypes")
	@ClassRule
	public static final GenericContainer mongoContainer =
	new GenericContainer("krnbr/mongo").withExposedPorts(MONGO_PORT);

	private MongoClient mongoClient;
	private ClientSession clientSession;
	private TaskMongoRepository taskMongoRepository;
	private MongoCollection<Document> taskCollection;

	@Before
	public void setup() {
		/* Creates the mongo client by connecting it to the mongodb instance, the task
		 * repository and collection; also empties the database before each test */
		mongoClient = new MongoClient(new ServerAddress(
				mongoContainer.getContainerIpAddress(),
				mongoContainer.getMappedPort(MONGO_PORT))
				);
		clientSession = mongoClient.startSession();
		taskMongoRepository = new TaskMongoRepository(mongoClient, DB_NAME, TASKS_COLLECTION);

		MongoDatabase database = mongoClient.getDatabase(DB_NAME);

		database.drop();
		database.createCollection(TASKS_COLLECTION);
		taskCollection = database.getCollection(TASKS_COLLECTION);
	}

	@After
	public void tearDown() {
		/* Close the client connection after each test so that it can
		 * be created anew in the next test */
		clientSession.close();
		mongoClient.close();
	}

	@AfterClass
	public static void stopContainer() {
		// Stops the container after all methods have been executed
		mongoContainer.stop();
	}

	@Test
	public void testFindAllTasks() {
		// Setup phase
		Task firstTask = new Task("1", "Buy groceries");
		Task secondTask = new Task("2", "Start using TDD");
		addTaskToDatabase(firstTask, Collections.emptyList());
		addTaskToDatabase(secondTask, Collections.emptyList());

		// Exercise and verify phases
		assertThat(taskMongoRepository.findAll(clientSession))
		.containsExactly(firstTask, secondTask);
	}

	@Test
	public void testFindById() {
		// Setup phase
		Task firstTask = new Task("1", "Buy groceries");
		Task secondTask = new Task("2", "Start using TDD");
		addTaskToDatabase(firstTask, Collections.emptyList());
		addTaskToDatabase(secondTask, Collections.emptyList());

		// Exercise and verify phases
		assertThat(taskMongoRepository.findById(secondTask.getId(), clientSession))
		.isEqualTo(secondTask);
	}

	@Test
	public void testSave() {
		// Setup phase
		Task task = new Task("1", "Buy groceries");

		// Exercise phase
		taskMongoRepository.save(task, clientSession);

		// Verify phase
		assertThat(getAllTasksFromDatabase())
		.containsExactly(task);
	}

	@Test
	public void testDelete() {
		// Setup phase
		Task task = new Task("1", "Buy groceries");
		addTaskToDatabase(task, Collections.emptyList());

		// Exercise phase
		taskMongoRepository.delete(task, clientSession);

		// Verify phase
		assertThat(getAllTasksFromDatabase())
		.isEmpty();
	}

	@Test
	public void testGetTagsByTaskId() {
		// Setup phase
		Task task = new Task("1", "Buy groceries");
		addTaskToDatabase(task, Arrays.asList("1", "2"));

		// Exercise and verify phases
		assertThat(taskMongoRepository.getTagsByTaskId(task.getId(), clientSession))
		.containsExactly("1", "2");
	}

	@Test
	public void testAddTagToTask() {
		// Setup phase
		Task task = new Task("1", "Buy groceries");
		addTaskToDatabase(task, Arrays.asList("1"));

		// Exercise phase
		taskMongoRepository.addTagToTask(task.getId(), "2", clientSession);

		// Verify phase
		assertThat(getTagsAssignedToTask(task))
		.containsExactly("1", "2");
	}

	@Test
	public void testRemoveTagFromTask() {
		// Setup phase
		Task task = new Task("1", "Buy groceries");
		addTaskToDatabase(task, Arrays.asList("1", "2"));

		// Exercise phase
		taskMongoRepository.removeTagFromTask(task.getId(), "2", clientSession);

		// Verify phase
		assertThat(getTagsAssignedToTask(task))
		.containsExactly("1");
	}

	private void addTaskToDatabase(Task task, List<String> tags) {
		// Private method to directly insert a task in the collection
		taskCollection.insertOne(new Document()
				.append("id", task.getId())
				.append("description", task.getDescription())
				.append("tags", tags)
				);
	}

	private List<Task> getAllTasksFromDatabase() {
		// Private method to directly retrieve all tasks from the collection
		return StreamSupport
				.stream(taskCollection.find().spliterator(), false)
				.map(d -> new Task(d.getString("id"), d.getString("description")))
				.collect(Collectors.toList());
	}

	private List<String> getTagsAssignedToTask(Task task) {
		/* Private method to directly retrieve all tags assigned to a tag 
		 * from the collection */
		return taskCollection
				.find(Filters.eq("id", task.getId()))
				.first()
				.getList("tags", String.class);
	}
}