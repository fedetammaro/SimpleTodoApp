package it.unifi.simpletodoapp.repository.mongo;

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
import org.testcontainers.containers.MongoDBContainer;

import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import it.unifi.simpletodoapp.model.Task;

public class TaskMongoRepositoryTest {
	private static final String DB_NAME = "todoappdb";
	private static final String DB_COLLECTION = "tasks";
	private static final int MONGO_PORT = 27017;

	private MongoClient mongoClient;
	private ClientSession clientSession;
	private TaskMongoRepository taskMongoRepository;
	private MongoCollection<Document> taskCollection;

	@ClassRule
	public static final MongoDBContainer mongoContainer = new MongoDBContainer()
	.withExposedPorts(MONGO_PORT);

	@Before
	public void setup() {
		/* Creates the mongo client by connecting it to the mongodb instance, the task
		 * repository and collection; also empties the database before each test */
		String mongoRsUrl = mongoContainer.getReplicaSetUrl();
		mongoClient = MongoClients.create(mongoRsUrl);
		clientSession = mongoClient.startSession();
		
		taskMongoRepository = new TaskMongoRepository(mongoClient, DB_NAME, DB_COLLECTION);

		MongoDatabase database = mongoClient.getDatabase(DB_NAME);

		database.drop();
		database.createCollection(DB_COLLECTION);
		taskCollection = database.getCollection(DB_COLLECTION);
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
	public void testFindAllTasksWhenCollectionIsEmpty() {
		// Exercise and verify phases (no setup phase needed)
		assertThat(taskMongoRepository.findAll(clientSession))
		.isEqualTo(Collections.emptyList());
	}

	@Test
	public void testFindAllTasksWhenCollectionIsNotEmpty() {
		// Setup phase
		Task task = new Task("1", "Buy groceries");
		addTaskToDatabase(task, Collections.emptyList());

		// Exercise phase
		List<Task> retrievedTasks = taskMongoRepository.findAll(clientSession);

		// Verify phase
		assertThat(retrievedTasks)
		.containsExactly(task);
	}

	@Test
	public void testFindTaskByIdWhenCollectionIsEmpty() {
		// Exercise and verify phases (no setup phase required)
		assertThat(taskMongoRepository.findById("1", clientSession))
		.isNull();
	}

	@Test
	public void testFindTaskByIdWhenCollectionIsNotEmpty() {
		// Setup phase
		Task firstTask = new Task("1", "Buy groceries");
		Task secondTask = new Task("2", "Start using TDD");
		addTaskToDatabase(firstTask, Collections.emptyList());
		addTaskToDatabase(secondTask, Collections.emptyList());

		// Exercise and verify phases
		assertThat(taskMongoRepository.findById("2", clientSession))
		.isEqualTo(secondTask);
	}

	@Test
	public void testSaveTask() {
		// Setup phase
		Task task = new Task("1", "Buy groceries");

		// Exercise phase
		taskMongoRepository.save(task, clientSession);

		// Verify phase
		assertThat(getTasksFromDatabase())
		.isEqualTo(Collections.singletonList(task));
	}

	@Test
	public void testDeleteTask() {
		// Setup phase
		Task task = new Task("1", "Buy groceries");
		addTaskToDatabase(task, Collections.emptyList());

		// Exercise phase
		taskMongoRepository.delete(task, clientSession);

		// Verify phase
		assertThat(getTasksFromDatabase())
		.isEmpty();
	}

	@Test
	public void testGetTagsWhenTagListIsEmpty() {
		// Setup phase
		Task task = new Task("1", "Start using TDD");
		addTaskToDatabase(task, Collections.emptyList());

		// Exercise phase
		List<String> retrievedTags = taskMongoRepository.getTagsByTaskId(task.getId(), clientSession);

		// Verify phase
		assertThat(retrievedTags)
		.isEqualTo(Collections.emptyList());
	}

	@Test
	public void testGetTagsWhenTagListIsNotEmpty() {
		// Setup phase
		Task task = new Task("1", "Start using TDD");
		addTaskToDatabase(task, Collections.singletonList("1"));

		// Exercise phase
		List<String> retrievedTags = taskMongoRepository.getTagsByTaskId(task.getId(), clientSession);

		// Verify phase
		assertThat(retrievedTags)
		.isEqualTo(Collections.singletonList("1"));
	}

	@Test
	public void testAddTagToTask() {
		// Setup phase
		Task task = new Task("1", "Start using TDD");
		addTaskToDatabase(task, Collections.emptyList());

		// Exercise phase
		taskMongoRepository.addTagToTask(task.getId(), "1", clientSession);

		// Verify phase
		assertThat(taskMongoRepository.getTagsByTaskId(task.getId(), clientSession))
		.containsExactly("1");
	}

	@Test
	public void testRemoveTagFromTask() {
		// Setup phase
		Task task = new Task("1", "Start using TDD");
		addTaskToDatabase(task, Collections.singletonList("1"));

		// Exercise phase
		taskMongoRepository.removeTagFromTask(task.getId(), "1", clientSession);

		// Verify phase
		assertThat(taskMongoRepository.getTagsByTaskId(task.getId(), clientSession))
		.isEmpty();
	}

	private void addTaskToDatabase(Task task, List<String> tags) {
		// Private method to directly insert a task in the collection
		taskCollection.insertOne(new Document()
				.append("id", task.getId())
				.append("description", task.getDescription())
				.append("tags", tags)
				);
	}

	private List<Task> getTasksFromDatabase() {
		// Private method to directly retrieve all tasks from the collection
		return StreamSupport
				.stream(taskCollection.find().spliterator(), false)
				.map(d -> new Task(d.getString("id"), d.getString("description")))
				.collect(Collectors.toList());
	}
}