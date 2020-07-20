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
import org.testcontainers.containers.GenericContainer;

import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import it.unifi.simpletodoapp.model.Task;
import it.unifi.simpletodoapp.repository.mongo.TagMongoRepository;
import it.unifi.simpletodoapp.repository.mongo.TaskMongoRepository;
import it.unifi.simpletodoapp.repository.mongo.TransactionManagerMongo;
import it.unifi.simpletodoapp.service.TodoService;
import it.unifi.simpletodoapp.view.swing.TodoSwingView;

public class TodoControllerServiceIT {
	@SuppressWarnings("rawtypes")
	@ClassRule
	public static final GenericContainer mongoContainer =
	new GenericContainer("krnbr/mongo").withExposedPorts(27017);
	
	private TransactionManagerMongo transactionManagerMongo;
	private TodoService todoService;
	private TodoController todoController;
	
	@Mock
	private TodoSwingView todoSwingView;
	
	private MongoClient mongoClient;
	private TaskMongoRepository taskMongoRepository;
	private TagMongoRepository tagMongoRepository;
	private MongoCollection<Document> taskCollection;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		
		mongoClient = new MongoClient(new ServerAddress(
				mongoContainer.getContainerIpAddress(),
				mongoContainer.getMappedPort(27017)));
		taskMongoRepository = new TaskMongoRepository(mongoClient, "todoapp", "tasks");
		tagMongoRepository = new TagMongoRepository(mongoClient, "todoapp", "tags");
		
		transactionManagerMongo = new TransactionManagerMongo(mongoClient, taskMongoRepository, tagMongoRepository);
		todoService = new TodoService(transactionManagerMongo);
		todoController = new TodoController(todoService, todoSwingView);
		
		MongoDatabase database = mongoClient.getDatabase("todoapp");

		database.drop();
		taskCollection = database.getCollection("tasks");
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
		Task task = new Task("1", "Buy groceries");
		addTaskToCollection(task, Collections.emptyList());
		
		todoController.getAllTasks();
		
		verify(todoSwingView).showAllTasks(Collections.singletonList(task));
	}
	
	@Test
	public void testAddTask() {
		Task task = new Task("1", "Buy groceries");
		
		todoController.addTask(task);
		
		assertThat(getAllTasksFromDatabase()).containsExactly(task);
		verify(todoSwingView).taskAdded(task);
	}
	
	@Test
	public void testDeleteTask() {
		Task task = new Task("1", "Buy groceries");
		addTaskToCollection(task, Collections.emptyList());
		
		todoController.deleteTask(task);
		
		assertThat(getAllTasksFromDatabase()).isEmpty();
		verify(todoSwingView).taskDeleted(task);
	}
	
	private void addTaskToCollection(Task task, List<String> tags) {
		taskCollection.insertOne(new Document()
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
}
