package it.unifi.simpletodoapp.view.swing;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.List;

import org.assertj.swing.annotation.GUITest;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JPanelFixture;
import org.assertj.swing.fixture.JTabbedPaneFixture;
import org.assertj.swing.junit.runner.GUITestRunner;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.bson.Document;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.MongoDBContainer;

import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import it.unifi.simpletodoapp.controller.TodoController;
import it.unifi.simpletodoapp.model.Tag;
import it.unifi.simpletodoapp.model.Task;
import it.unifi.simpletodoapp.repository.mongo.TagMongoRepository;
import it.unifi.simpletodoapp.repository.mongo.TaskMongoRepository;
import it.unifi.simpletodoapp.repository.mongo.TransactionManagerMongo;
import it.unifi.simpletodoapp.service.TodoService;

@RunWith(GUITestRunner.class)
public class TodoSwingViewControllerIT extends AssertJSwingJUnitTestCase {
	private static final int MONGO_PORT = 27017;
	private static final String DB_NAME = "todoapp";
	private static final String TASKS_COLLECTION = "tasks";
	private static final String TAGS_COLLECTION = "tags";

	private TodoSwingView todoSwingView;
	private TodoService todoService;
	private TodoController todoController;
	private TransactionManagerMongo transactionManagerMongo;
	private TaskMongoRepository taskMongoRepository;
	private TagMongoRepository tagMongoRepository;

	private FrameFixture frameFixture;
	private JPanelFixture contentPanel;
	private JPanelFixture tasksPanel;
	private JPanelFixture tagsPanel;

	private MongoClient mongoClient;
	private ClientSession clientSession;
	private MongoCollection<Document> taskCollection;
	private MongoCollection<Document> tagCollection;

	@ClassRule
	public static final MongoDBContainer mongoContainer = new MongoDBContainer()
	.withExposedPorts(MONGO_PORT);
	
	@Override
	public void onSetUp() {
		/* Creates the mongo client by connecting it to the mongodb instance, both
		 * repositories and collections and the transaction manager; also empties
		 * the database before each test and creates the application window on
		 * which tests will be executed */
		String mongoRsUrl = mongoContainer.getReplicaSetUrl();
		mongoClient = MongoClients.create(mongoRsUrl);
		clientSession = mongoClient.startSession();
		
		taskMongoRepository = new TaskMongoRepository(mongoClient, DB_NAME, TASKS_COLLECTION);
		tagMongoRepository = new TagMongoRepository(mongoClient, DB_NAME, TAGS_COLLECTION);
		transactionManagerMongo = new TransactionManagerMongo(mongoClient, taskMongoRepository, tagMongoRepository);

		MongoDatabase database = mongoClient.getDatabase(DB_NAME);

		database.drop();
		database.createCollection(TASKS_COLLECTION);
		database.createCollection(TAGS_COLLECTION);
		taskCollection = database.getCollection(TASKS_COLLECTION);
		tagCollection = database.getCollection(TAGS_COLLECTION);

		GuiActionRunner.execute(
				() -> {
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
		/* Close the client connection after each test so that it can
		 * be created anew in the next test */
		clientSession.close();
		mongoClient.close();
	}
	
	@BeforeClass
	public static void setupMongoLogger() {
		LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
		Logger rootLogger = loggerContext.getLogger("org.mongodb.driver");
		rootLogger.setLevel(Level.INFO);
	}


	@AfterClass
	public static void stopContainer() {
		// Stops the container after all methods have been executed
		mongoContainer.stop();
	}

	@Test @GUITest
	public void testShowAllTasks() {
		// Setup phase
		addTaskToCollection(new Task("1", "Buy groceries"), Collections.emptyList());
		addTaskToCollection(new Task("2", "Start using TDD"), Collections.emptyList());
		addTaskToCollection(new Task("3", "Read some more"), Collections.emptyList());

		// Exercise phase
		GuiActionRunner.execute(
				() -> todoController.getAllTasks()
				);

		// Verify phase
		assertThat(tasksPanel.list("tasksTaskList").contents())
		.containsExactly(
				"#1 - Buy groceries",
				"#2 - Start using TDD",
				"#3 - Read some more"
				);
	}

	@Test @GUITest
	public void testAddTaskButtonAddsSuccessfully() {
		// Exercise phase (no setup phase required)
		tasksPanel.textBox("taskIdTextField").enterText("1");
		tasksPanel.textBox("taskDescriptionTextField").enterText("Buy groceries");
		tasksPanel.button("btnAddTask").click();

		// Verify phase
		assertThat(tasksPanel.list("tasksTaskList").contents())
		.containsExactly("#1 - Buy groceries");
	}

	@Test @GUITest
	public void testAddTaskButtonThrowsError() {
		// Setup phase
		Task task = new Task("1", "Buy groceries");
		addTaskToCollection(task, Collections.emptyList());

		// Exercise phase
		tasksPanel.textBox("taskIdTextField").enterText(task.getId());
		tasksPanel.textBox("taskDescriptionTextField").enterText(task.getDescription());
		tasksPanel.button("btnAddTask").click();

		// Verify phase
		assertThat(tasksPanel.list("tasksTaskList").contents())
		.isEmpty();
		assertThat(tasksPanel.label("tasksErrorLabel").text())
		.isEqualTo("Cannot add task with duplicated ID " + task.getId());
	}

	@Test @GUITest
	public void testDeleteTaskButtonDeletesSuccessfully() {
		// Setup phase
		addTaskToCollection(new Task("1", "Buy groceries"), Collections.emptyList());
		addTaskToCollection(new Task("2", "Start using TDD"), Collections.emptyList());

		GuiActionRunner.execute(
				() -> todoController.getAllTasks()
				);

		// Exercise phase
		tasksPanel.list("tasksTaskList").selectItem(0);
		tasksPanel.button("btnDeleteTask").click();

		// Verify phase
		assertThat(tasksPanel.list("tasksTaskList").contents())
		.containsExactly("#2 - Start using TDD");
	}

	@Test @GUITest
	public void testDeleteTaskButtonThrowsError() {
		// Setup phase
		Task task = new Task("1", "Buy groceries");
		addTaskToCollection(task, Collections.emptyList());

		GuiActionRunner.execute(
				() -> todoController.getAllTasks()
				);

		taskMongoRepository.delete(task, clientSession);

		// Exercise phase
		tasksPanel.list("tasksTaskList").selectItem(0);
		tasksPanel.button("btnDeleteTask").click();

		// Verify phase
		assertThat(tasksPanel.label("tasksErrorLabel").text())
		.isEqualTo("Task with ID " + task.getId() + " has already been deleted");
	}

	@Test @GUITest
	public void testAssignTagButtonAssignsSuccessfully() {
		// Setup phase
		Tag tag = new Tag("1", "Work");
		addTaskToCollection(new Task("1", "Start using TDD"), Collections.emptyList());
		addTagToCollection(tag, Collections.emptyList());

		GuiActionRunner.execute(
				() -> {
					todoController.getAllTasks();
					todoController.getAllTags();
				});

		// Exercise phase
		tasksPanel.list("tasksTaskList").selectItem(0);
		tasksPanel.button("btnAssignTag").click();

		// Verify phase
		assertThat(tasksPanel.list("assignedTagsList").contents())
		.containsExactly("(1) Work");
	}

	@Test @GUITest
	public void testAssignTagButtonThrowsError() {
		// Setup phase
		Task task = new Task("1", "Start using TDD");
		Tag tag = new Tag("1", "Work");
		addTaskToCollection(task, Collections.singletonList(tag.getId()));
		addTagToCollection(tag, Collections.singletonList(task.getId()));

		GuiActionRunner.execute(
				() -> {
					todoController.getAllTasks();
					todoController.getAllTags();
				});

		// Exercise phase
		tasksPanel.list("tasksTaskList").selectItem(0);
		tasksPanel.button("btnAssignTag").click();

		// Verify phase
		assertThat(tasksPanel.label("tasksErrorLabel").text())
		.isEqualTo("Tag with ID " + tag.getId() +
				" is already assigned to task with ID " + task.getId());
	}

	@Test @GUITest
	public void testClickOnTaskShowsAssignedTags() {
		// Setup phase
		Task task = new Task("1", "Start using TDD");
		Tag tag = new Tag("1", "Work");
		addTaskToCollection(task, Collections.singletonList(tag.getId()));
		addTagToCollection(tag, Collections.singletonList(task.getId()));

		GuiActionRunner.execute(
				() -> {
					todoController.getAllTasks();
					todoController.getAllTags();
				});

		// Exercise phase
		tasksPanel.list("tasksTaskList").selectItem(0);

		// Verify phase
		assertThat(tasksPanel.list("assignedTagsList").contents())
		.containsExactly("(1) Work");
	}

	@Test @GUITest
	public void testRemoveTagButtonRemovesTagFromTaskSuccessfully() {
		// Setup phase
		Task task = new Task("1", "Start using TDD");
		Tag tag = new Tag("1", "Work");
		addTaskToCollection(task, Collections.singletonList(tag.getId()));
		addTagToCollection(tag, Collections.singletonList(task.getId()));

		GuiActionRunner.execute(
				() -> {
					todoController.getAllTasks();
					todoController.getAllTags();
				});

		// Exercise phase
		tasksPanel.list("tasksTaskList").selectItem(0);
		tasksPanel.list("assignedTagsList").selectItem(0);
		tasksPanel.button("btnRemoveTag").click();

		// Verify phase
		assertThat(tasksPanel.list("assignedTagsList").contents())
		.isEmpty();
	}

	@Test @GUITest
	public void testRemoveTagButtonThrowsError() {
		// Setup phase
		Task task = new Task("1", "Start using TDD");
		Tag tag = new Tag("1", "Work");
		addTaskToCollection(task, Collections.singletonList(tag.getId()));
		addTagToCollection(tag, Collections.singletonList(task.getId()));

		GuiActionRunner.execute(
				() -> {
					todoController.getAllTasks();
					todoController.getAllTags();
				});

		// Exercise phase
		tasksPanel.list("tasksTaskList").selectItem(0);
		tasksPanel.list("assignedTagsList").selectItem(0);

		taskMongoRepository.removeTagFromTask(task.getId(), tag.getId(), clientSession);

		tasksPanel.button("btnRemoveTag").click();

		// Verify phase
		assertThat(tasksPanel.label("tasksErrorLabel").text())
		.isEqualTo("No tag with ID " + tag.getId() + " assigned to task with ID " + task.getId());
	}

	@Test @GUITest
	public void testShowAllTags() {
		// Setup phase
		addTagToCollection(new Tag("1", "Work"), Collections.emptyList());
		addTagToCollection(new Tag("2", "Important"), Collections.emptyList());
		addTagToCollection(new Tag("3", "Free time"), Collections.emptyList());

		// Exercise phase
		GuiActionRunner.execute(
				() -> todoController.getAllTags()
				);

		// Verify phase
		assertThat(tasksPanel.comboBox("tagComboBox").contents())
		.containsExactly(
				"(1) Work",
				"(2) Important",
				"(3) Free time"
				);

		getTagsPanel();

		assertThat(tagsPanel.list("tagsTagList").contents())
		.containsExactly(
				"(1) Work",
				"(2) Important",
				"(3) Free time"
				);
	}

	@Test @GUITest
	public void testAddTagButtonAddsSuccessfully() {
		// Setup phase
		getTagsPanel();

		// Exercise phase
		tagsPanel.textBox("tagIdTextField").enterText("1");
		tagsPanel.textBox("tagNameTextField").enterText("Work");
		tagsPanel.button("btnAddTag").click();

		// Verify phase
		assertThat(tagsPanel.list("tagsTagList").contents())
		.containsExactly("(1) Work");
	}

	@Test @GUITest
	public void testAddTagButtonThrowsError() {
		// Setup phase
		getTagsPanel();

		Tag tag = new Tag("1", "Work");
		addTagToCollection(tag, Collections.emptyList());

		// Exercise phase
		tagsPanel.textBox("tagIdTextField").enterText(tag.getId());
		tagsPanel.textBox("tagNameTextField").enterText(tag.getName());
		tagsPanel.button("btnAddTag").click();

		// Verify phase
		assertThat(tagsPanel.list("tagsTagList").contents())
		.isEmpty();
		assertThat(tagsPanel.label("tagsErrorLabel").text())
		.isEqualTo("Cannot add tag with duplicated ID " + tag.getId());
	}

	@Test @GUITest
	public void testDeleteTagButtonDeletesSuccessfully() {
		// Setup phase
		getTagsPanel();

		addTagToCollection(new Tag("1", "Work"), Collections.emptyList());
		addTagToCollection(new Tag("2", "Important"), Collections.emptyList());

		GuiActionRunner.execute(
				() -> todoController.getAllTags()
				);

		// Exercise phase
		tagsPanel.list("tagsTagList").selectItem(0);
		tagsPanel.button("btnDeleteTag").click();

		// Verify phase
		assertThat(tagsPanel.list("tagsTagList").contents())
		.containsExactly("(2) Important");
	}

	@Test @GUITest
	public void testDeleteTagButtonThrowsError() {
		// Setup phase
		getTagsPanel();

		Tag tag = new Tag("1", "Work");
		addTagToCollection(tag, Collections.emptyList());

		GuiActionRunner.execute(
				() -> todoController.getAllTags()
				);
		tagMongoRepository.delete(tag, clientSession);

		// Exercise phase
		tagsPanel.list("tagsTagList").selectItem(0);
		tagsPanel.button("btnDeleteTag").click();

		// Verify phase
		assertThat(tagsPanel.label("tagsErrorLabel").text())
		.isEqualTo("Tag with ID " + tag.getId() + " has already been deleted");
	}

	@Test @GUITest
	public void testClickOnTagShowsAssignedTasks() {
		// Setup phase
		getTagsPanel();

		Task task = new Task("1", "Start using TDD");
		Tag tag = new Tag("1", "Work");
		addTaskToCollection(task, Collections.singletonList(tag.getId()));
		addTagToCollection(tag, Collections.singletonList(task.getId()));

		GuiActionRunner.execute(
				() -> {
					todoController.getAllTasks();
					todoController.getAllTags();
				});

		// Exercise phase
		tagsPanel.list("tagsTagList").selectItem(0);

		// Verify phase
		assertThat(tagsPanel.list("assignedTasksList").contents())
		.containsExactly("#1 - Start using TDD");
	}


	@Test @GUITest
	public void testRemoveTaskButtonRemovesTaskFromTagSuccessfully() {
		// Setup phase
		getTagsPanel();

		Task task = new Task("1", "Start using TDD");
		Tag tag = new Tag("1", "Work");
		addTaskToCollection(task, Collections.singletonList(tag.getId()));
		addTagToCollection(tag, Collections.singletonList(task.getId()));

		GuiActionRunner.execute(
				() -> {
					todoController.getAllTasks();
					todoController.getAllTags();
				});

		// Exercise phase
		tagsPanel.list("tagsTagList").selectItem(0);
		tagsPanel.list("assignedTasksList").selectItem(0);
		tagsPanel.button("btnRemoveTask").click();

		// Verify phase
		assertThat(tagsPanel.list("assignedTasksList").contents())
		.isEmpty();
	}

	@Test @GUITest
	public void testRemoveTaskButtonThrowsError() {
		// Setup phase
		getTagsPanel();

		Task task = new Task("1", "Start using TDD");
		Tag tag = new Tag("1", "Work");
		addTaskToCollection(task, Collections.singletonList(tag.getId()));
		addTagToCollection(tag, Collections.singletonList(task.getId()));

		GuiActionRunner.execute(
				() -> {
					todoController.getAllTasks();
					todoController.getAllTags();
				});

		// Exercise phase
		tagsPanel.list("tagsTagList").selectItem(0);
		tagsPanel.list("assignedTasksList").selectItem(0);

		tagMongoRepository.removeTaskFromTag(tag.getId(), task.getId(), clientSession);

		tagsPanel.button("btnRemoveTask").click();

		// Verify phase
		assertThat(tagsPanel.label("tagsErrorLabel").text())
		.isEqualTo("No task with ID " + task.getId() + " assigned to tag with ID " + tag.getId());
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

	private void getTagsPanel() {
		// Private method that returns a reference to the tags panel
		JTabbedPaneFixture tabPanel = contentPanel.tabbedPane("tabbedPane");
		tabPanel.selectTab("Tags");
		tagsPanel = contentPanel.panel("tagsPanel");
	}
}