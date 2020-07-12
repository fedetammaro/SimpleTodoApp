package it.unifi.simpletodoapp.repository.mongo;

import static org.assertj.core.api.Assertions.*;

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.bson.Document;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;

import de.bwaldvogel.mongo.MongoServer;
import de.bwaldvogel.mongo.backend.memory.MemoryBackend;
import it.unifi.simpletodoapp.model.Task;

public class TaskMongoRepositoryTest {
	private static MongoServer mongoServer;
	private static InetSocketAddress serverAddress;
	private MongoClient mongoClient;
	private TaskMongoRepository taskMongoRepository;
	private MongoCollection<Document> taskCollection;

	private static final String DB_NAME = "todoappdb";
	private static final String DB_COLLECTION = "tasks";

	@BeforeClass
	public static void startServer() {
		mongoServer = new MongoServer(new MemoryBackend());
		serverAddress = mongoServer.bind();
	}

	@AfterClass
	public static void stopServer() {
		mongoServer.shutdown();
	}

	@Before
	public void setUp() {
		mongoClient = new MongoClient(new ServerAddress(serverAddress));
		mongoClient.getDatabase(DB_NAME).drop();

		taskMongoRepository = new TaskMongoRepository(mongoClient, DB_NAME, DB_COLLECTION);
		taskCollection = mongoClient.getDatabase(DB_NAME)
				.getCollection(DB_COLLECTION);
	}

	@After
	public void tearDown() {
		mongoClient.close();
	}

	@Test
	public void testFindAllTasksWhenCollectionIsEmpty() {
		// Exercise and verify phases (no setup phase needed)
		assertThat(taskMongoRepository.findAll()).isEqualTo(Collections.emptyList());
	}

	@Test
	public void testFindAllTasksWhenCollectionIsNotEmpty() {
		// Setup phase
		Task task = new Task("1", "Buy groceries");
		addTaskToDatabase(task, Collections.emptyList());

		// Exercise phase
		List<Task> retrievedTasks = taskMongoRepository.findAll();

		// Verify phase
		assertThat(retrievedTasks).containsExactly(task);
	}

	@Test
	public void testFindTaskByIdWhenCollectionIsEmpty() {
		// Exercise and verify phases (no setup phase required)
		assertThat(taskMongoRepository.findById("1")).isNull();
	}

	@Test
	public void testFindTaskByIdWhenCollectionIsNotEmpty() {
		// Setup phase
		Task firstTask = new Task("1", "Buy groceries");
		Task secondTask = new Task("2", "Start using TDD");
		addTaskToDatabase(firstTask, Collections.emptyList());
		addTaskToDatabase(secondTask, Collections.emptyList());

		// Exercise and verify phases
		assertThat(taskMongoRepository.findById("2")).isEqualTo(secondTask);
	}

	@Test
	public void testSaveTask() {
		// Setup phase
		Task task = new Task("1", "Buy groceries");
		
		// Exercise phase
		taskMongoRepository.save(task);
		
		// Verify phase
		assertThat(getTasksFromDatabase()).isEqualTo(Collections.singletonList(task));
	}
	
	@Test
	public void testDeleteTask() {
		// Setup phase
		Task task = new Task("1", "Buy groceries");
		addTaskToDatabase(task, Collections.emptyList());
		
		// Exercise phase
		taskMongoRepository.delete(task);
		
		// Verify phase
		assertThat(getTasksFromDatabase()).isEmpty();
	}
	
	@Test
	public void testGetTagsWhenTagListIsEmpty() {
		// Setup phase
		Task task = new Task("1", "Start using TDD");
		addTaskToDatabase(task, Collections.emptyList());
		
		// Exercise phase
		List<String> retrievedTags = taskMongoRepository.getTagsByTaskId(task.getId());
		
		// Verify phase
		assertThat(retrievedTags).isEqualTo(Collections.emptyList());
	}
	
	@Test
	public void testGetTagsWhenTagListIsNotEmpty() {
		// Setup phase
		Task task = new Task("1", "Start using TDD");
		addTaskToDatabase(task, Collections.singletonList("1"));
		
		// Exercise phase
		List<String> retrievedTags = taskMongoRepository.getTagsByTaskId(task.getId());
		
		// Verify phase
		assertThat(retrievedTags).isEqualTo(Collections.singletonList("1"));
	}
	
	@Test
	public void testAddTagToTask() {
		// Setup phase
		Task task = new Task("1", "Start using TDD");
		addTaskToDatabase(task, Collections.emptyList());
		
		// Exercise phase
		taskMongoRepository.addTagToTask(task.getId(), "1");
		
		// Verify phase
		assertThat(taskMongoRepository.getTagsByTaskId(task.getId())).containsExactly("1");
	}
	
	@Test
	public void testRemoveTagFromTask() {
		// Setup phase
		Task task = new Task("1", "Start using TDD");
		addTaskToDatabase(task, Collections.singletonList("1"));
				
		// Exercise phase
		taskMongoRepository.removeTagFromTask(task.getId(), "1");
				
		// Verify phase
		assertThat(taskMongoRepository.getTagsByTaskId(task.getId())).isEmpty();
	}

	private void addTaskToDatabase(Task task, List<String> tags) {
		taskCollection.insertOne(new Document()
				.append("id", task.getId())
				.append("description", task.getDescription())
				.append("tags", tags));
	}

	private List<Task> getTasksFromDatabase() {
		return StreamSupport
				.stream(taskCollection.find().spliterator(), false)
				.map(d -> new Task(d.getString("id"), d.getString("description")))
				.collect(Collectors.toList());
	}
}
