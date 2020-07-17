package it.unifi.simpletodoapp.repository.mongo;

import static org.assertj.core.api.Assertions.*;

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

import it.unifi.simpletodoapp.model.Task;

public class TaskMongoRepositoryIT {
	@SuppressWarnings("rawtypes")
	@ClassRule
	public static final GenericContainer mongoContainer =
	new GenericContainer("mongo:4.2.3").withExposedPorts(27017);
	
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
		addTaskToDatabase(firstTask);
		addTaskToDatabase(secondTask);
		
		assertThat(taskMongoRepository.findAll()).containsExactly(firstTask, secondTask);
	}

	private void addTaskToDatabase(Task task) {
		taskCollection.insertOne(
				new Document()
				.append("id", task.getId())
				.append("description", task.getDescription()));
	}
}
