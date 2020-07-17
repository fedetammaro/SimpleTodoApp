package it.unifi.simpletodoapp.repository.mongo;

import static org.assertj.core.api.Assertions.*;

import java.util.Arrays;
import java.util.Collections;
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

import it.unifi.simpletodoapp.model.Task;

public class TaskMongoRepositoryIT {
	@SuppressWarnings("rawtypes")
	@ClassRule
	public static final GenericContainer mongoContainer =
	new GenericContainer("mongo").withExposedPorts(27017);

	private MongoClient mongoClient;
	private TaskMongoRepository taskMongoRepository;
	private MongoCollection<Document> taskCollection;

	@Before
	public void setup() {
		mongoClient = new MongoClient(new ServerAddress(
				mongoContainer.getContainerIpAddress(),
				mongoContainer.getMappedPort(27017)));
		taskMongoRepository = new TaskMongoRepository(mongoClient, "todoapp", "tasks");

		MongoDatabase database = mongoClient.getDatabase("todoapp");

		database.drop();
		taskCollection = database.getCollection("tasks");
	}

	@After
	public void tearDown() {
		mongoClient.close();
	}

	@Test
	public void testFindAllTasks() {
		Task firstTask = new Task("1", "Buy groceries");
		Task secondTask = new Task("2", "Start using TDD");
		addTaskToDatabase(firstTask, Collections.emptyList());
		addTaskToDatabase(secondTask, Collections.emptyList());

		assertThat(taskMongoRepository.findAll()).containsExactly(firstTask, secondTask);
	}

	@Test
	public void testFindById() {
		Task firstTask = new Task("1", "Buy groceries");
		Task secondTask = new Task("2", "Start using TDD");
		addTaskToDatabase(firstTask, Collections.emptyList());
		addTaskToDatabase(secondTask, Collections.emptyList());

		assertThat(taskMongoRepository.findById(secondTask.getId())).isEqualTo(secondTask);
	}

	@Test
	public void testSave() {
		Task task = new Task("1", "Buy groceries");

		taskMongoRepository.save(task);

		assertThat(getAllTasksFromDatabase()).containsExactly(task);
	}

	@Test
	public void testDelete() {
		Task task = new Task("1", "Buy groceries");
		addTaskToDatabase(task, Collections.emptyList());
		
		taskMongoRepository.delete(task);

		assertThat(getAllTasksFromDatabase()).isEmpty();
	}

	@Test
	public void testGetTagsByTaskId() {
		Task task = new Task("1", "Buy groceries");
		addTaskToDatabase(task, Arrays.asList("1", "2"));

		assertThat(taskMongoRepository.getTagsByTaskId(task.getId()))
		.containsExactly("1", "2");
	}
	
	@Test
	public void testAddTagToTask() {
		Task task = new Task("1", "Buy groceries");
		addTaskToDatabase(task, Arrays.asList("1"));
		
		taskMongoRepository.addTagToTask(task.getId(), "2");
		
		assertThat(getTagsAssignedToTask(task)).containsExactly("1", "2");
	}
	
	@Test
	public void testRemoveTagFromTask() {
		Task task = new Task("1", "Buy groceries");
		addTaskToDatabase(task, Arrays.asList("1", "2"));
		
		taskMongoRepository.removeTagFromTask(task.getId(), "2");
		
		assertThat(getTagsAssignedToTask(task)).containsExactly("1");
	}

	private void addTaskToDatabase(Task task, List<String> tags) {
		taskCollection.insertOne(
				new Document()
				.append("id", task.getId())
				.append("description", task.getDescription())
				.append("tags", tags));
	}

	private List<Task> getAllTasksFromDatabase() {
		return StreamSupport
				.stream(taskCollection.find().spliterator(), false)
				.map(d -> new Task(d.getString("id"), d.getString("description")))
				.collect(Collectors.toList());
	}
	
	private List<String> getTagsAssignedToTask(Task task) {
		return taskCollection
				.find(Filters.eq("id", task.getId()))
				.first()
				.getList("tags", String.class);
	}
}
