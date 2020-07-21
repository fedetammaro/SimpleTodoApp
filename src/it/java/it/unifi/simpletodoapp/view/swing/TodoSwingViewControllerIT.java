package it.unifi.simpletodoapp.view.swing;

import static org.assertj.core.api.Assertions.*;

import java.util.Collections;
import java.util.List;

import org.assertj.swing.annotation.GUITest;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JPanelFixture;
import org.assertj.swing.junit.runner.GUITestRunner;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.bson.Document;
import org.junit.AfterClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.testcontainers.containers.GenericContainer;

import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import it.unifi.simpletodoapp.controller.TodoController;
import it.unifi.simpletodoapp.model.Task;
import it.unifi.simpletodoapp.repository.mongo.TagMongoRepository;
import it.unifi.simpletodoapp.repository.mongo.TaskMongoRepository;
import it.unifi.simpletodoapp.repository.mongo.TransactionManagerMongo;
import it.unifi.simpletodoapp.service.TodoService;

@RunWith(GUITestRunner.class)
public class TodoSwingViewControllerIT extends AssertJSwingJUnitTestCase {
	@SuppressWarnings("rawtypes")
	@ClassRule
	public static final GenericContainer mongoContainer =
	new GenericContainer("krnbr/mongo").withExposedPorts(27017);
	
	private TodoSwingView todoSwingView;
	private TodoService todoService;
	private TodoController todoController;
	private TransactionManagerMongo transactionManagerMongo;
	private TaskMongoRepository taskMongoRepository;
	private TagMongoRepository tagMongoRepository;
	
	private FrameFixture frameFixture;
	private JPanelFixture contentPanel;
	private JPanelFixture tasksPanel;
	private MongoClient mongoClient;
	
	private MongoCollection<Document> taskCollection;
	
	@Override
	public void onSetUp() {
		mongoClient = new MongoClient(new ServerAddress(
				mongoContainer.getContainerIpAddress(),
				mongoContainer.getMappedPort(27017)));
		taskMongoRepository = new TaskMongoRepository(mongoClient, "todoapp", "tasks");
		tagMongoRepository = new TagMongoRepository(mongoClient, "todoapp", "tags");
		transactionManagerMongo = new TransactionManagerMongo(mongoClient, taskMongoRepository, tagMongoRepository);
		
		MongoDatabase database = mongoClient.getDatabase("todoapp");

		database.drop();
		taskCollection = database.getCollection("tasks");
		
		GuiActionRunner.execute(() -> {
			todoService = new TodoService(transactionManagerMongo);
			todoSwingView = new TodoSwingView();
			todoController = new TodoController(todoService, todoSwingView);
			todoSwingView.setTodoController(todoController);
			return todoSwingView;
		});
		
		frameFixture = new FrameFixture(robot(), todoSwingView);
		frameFixture.show();
		
		contentPanel = frameFixture.panel("contentPane");
		tasksPanel = contentPanel.panel("tasksPanel");
	}
	
	@Override
	public void onTearDown() {
		mongoClient.close();
	}
	

	@AfterClass
	public static void stopContainer() {
		mongoContainer.stop();
	}
	
	@Test @GUITest
	public void testShowAllTasks() {
		addTaskToCollection(new Task("1", "Buy groceries"), Collections.emptyList());
		addTaskToCollection(new Task("2", "Start using TDD"), Collections.emptyList());
		addTaskToCollection(new Task("3", "Read some more"), Collections.emptyList());
		
		GuiActionRunner.execute(() -> todoController.getAllTasks());
		
		assertThat(tasksPanel.list("tasksTaskList").contents())
			.containsExactly(
					"#1 - Buy groceries",
					"#2 - Start using TDD",
					"#3 - Read some more"
					);
	}
	
	@Test @GUITest
	public void testAddTaskButtonAddsSuccessfully() {
		tasksPanel.textBox("taskIdTextField").enterText("1");
		tasksPanel.textBox("taskDescriptionTextField").enterText("Buy groceries");
		tasksPanel.button("btnAddTask").click();
		
		assertThat(tasksPanel.list("tasksTaskList").contents())
			.containsExactly("#1 - Buy groceries");
	}
	
	@Test @GUITest
	public void testAddTaskButtonThrowsError() {
		Task task = new Task("1", "Buy groceries");
		addTaskToCollection(task, Collections.emptyList());
		
		tasksPanel.textBox("taskIdTextField").enterText(task.getId());
		tasksPanel.textBox("taskDescriptionTextField").enterText(task.getDescription());
		tasksPanel.button("btnAddTask").click();
		
		assertThat(tasksPanel.list("tasksTaskList").contents()).isEmpty();
		assertThat(tasksPanel.label("tasksErrorLabel").text())
			.isEqualTo("Cannot add task with duplicated ID " + task.getId());
	}
	
	@Test @GUITest
	public void testDeleteButtonDeletesSuccessfully() {
		addTaskToCollection(new Task("1", "Buy groceries"), Collections.emptyList());
		addTaskToCollection(new Task("2", "Start using TDD"), Collections.emptyList());
		
		GuiActionRunner.execute(() -> todoController.getAllTasks());
		
		tasksPanel.list("tasksTaskList").selectItem(0);
		tasksPanel.button("btnDeleteTask").click();
		
		assertThat(tasksPanel.list("tasksTaskList").contents())
			.containsExactly("#2 - Start using TDD");
	}
	
	@Test @GUITest
	public void testDeleteButtonThrowsError() {
		Task task = new Task("1", "Buy groceries");
		addTaskToCollection(task, Collections.emptyList());
		
		GuiActionRunner.execute(() -> todoController.getAllTasks());
		taskMongoRepository.delete(task);
		
		tasksPanel.list("tasksTaskList").selectItem(0);
		tasksPanel.button("btnDeleteTask").click();
		
		assertThat(tasksPanel.label("tasksErrorLabel").text())
			.isEqualTo("Task with ID " + task.getId() + " has already been removed");
	}
	
	private void addTaskToCollection(Task task, List<String> tags) {
		taskCollection.insertOne(new Document()
				.append("id", task.getId())
				.append("description", task.getDescription())
				.append("tags", tags));
	}
}
