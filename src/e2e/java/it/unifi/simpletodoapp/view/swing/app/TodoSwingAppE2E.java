package it.unifi.simpletodoapp.view.swing.app;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.swing.launcher.ApplicationLauncher.*;
import static org.awaitility.Awaitility.*;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import javax.swing.JFrame;

import org.assertj.swing.annotation.GUITest;
import org.assertj.swing.core.GenericTypeMatcher;
import org.assertj.swing.finder.WindowFinder;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JPanelFixture;
import org.assertj.swing.fixture.JTabbedPaneFixture;
import org.assertj.swing.junit.runner.GUITestRunner;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.bson.Document;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.testcontainers.containers.GenericContainer;

import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

import it.unifi.simpletodoapp.model.Tag;
import it.unifi.simpletodoapp.model.Task;

@RunWith(GUITestRunner.class)
public class TodoSwingAppE2E extends AssertJSwingJUnitTestCase {
	private static final int MONGO_PORT = 27017;
	private static final String DB_NAME = "todoapp";
	private static final String TASKS_COLLECTION = "tasks";
	private static final String TAGS_COLLECTION = "tags";
	private static final String TASK_1_ID = "1";
	private static final String TASK_1_DESCRIPTION = "Buy groceries";
	private static final String TASK_2_ID = "2";
	private static final String TASK_2_DESCRIPTION = "Start using TDD";
	private static final String TAG_1_ID = "1";
	private static final String TAG_1_NAME = "Work";
	private static final String TAG_2_ID = "2";
	private static final String TAG_2_NAME = "Important";
	
	@SuppressWarnings("rawtypes")
	@ClassRule
	public static final GenericContainer mongoContainer =
	new GenericContainer("krnbr/mongo").withExposedPorts(MONGO_PORT);

	private FrameFixture frameFixture;
	private MongoClient mongoClient;

	private JPanelFixture contentPanel;

	private JTabbedPaneFixture tabPanel;

	@Override
	protected void onSetUp() {
		/* Creates the mongo client by connecting it to the mongodb instance, empties
		 * the database before each test, adds some example tasks and tags to their
		 * relative collections, starts the application and its Swing view and waits
		 * to find the JFrame of the Swing view, so that tests won't fail because
		 * they start before the application window has been created and shown */
		mongoClient = new MongoClient(new ServerAddress(
				mongoContainer.getContainerIpAddress(),
				mongoContainer.getMappedPort(MONGO_PORT)));

		mongoClient.getDatabase(DB_NAME).drop();
		
		addTaskToDatabase(
				new Task(TASK_1_ID, TASK_1_DESCRIPTION),
				Arrays.asList(TAG_2_ID)
				);
		addTaskToDatabase(
				new Task(TASK_2_ID, TASK_2_DESCRIPTION),
				Arrays.asList(TAG_1_ID)
				);
		addTagToDatabase(
				new Tag(TAG_1_ID, TAG_1_NAME),
				Arrays.asList(TASK_2_ID)
				);
		addTagToDatabase(
				new Tag(TAG_2_ID, TAG_2_NAME),
				Arrays.asList(TASK_1_ID)
				);

		application("it.unifi.simpletodoapp.TodoApplication")
			.withArgs(
					"--mongo-host=" + mongoContainer.getContainerIpAddress(),
					"--mongo-port=" + mongoContainer.getMappedPort(MONGO_PORT),
					"--db-name=" + DB_NAME,
					"--db-tasksCollection=" + TASKS_COLLECTION,
					"--db-tagsCollection=" + TAGS_COLLECTION
					)
			.start();
		
		GenericTypeMatcher<JFrame> frameMatcher = new GenericTypeMatcher<JFrame>(JFrame.class) {
			@Override
			protected boolean isMatching(JFrame frame) {
				return "Simple Todo Application".equals(frame.getTitle()) && frame.isShowing();
			}
		};
		
		/* Need to wait until the TodoSwingView is created, otherwise these references
		 * will not be valid to use during the tests, leading to a ComponentLookupException
		 */
		await().atMost(2, TimeUnit.SECONDS).until(() -> {
			try {
				frameFixture = WindowFinder.findFrame(frameMatcher).using(robot());
				contentPanel = frameFixture.panel("contentPane");
				tabPanel = contentPanel.tabbedPane("tabbedPane");
				return true;
			} catch(Exception e) {
				return false;
			}
		});
	}
	
	@Override
	public void onTearDown() {
		/* Close the client connection after each test so that it can
		 * be created anew in the next test */
		mongoClient.close();
	}
	
	@Test @GUITest
	public void testAlreadySavedDataIsPresent() {
		// Verify phase (no setup or exercise phases required)
		JPanelFixture tasksPanel = getTasksPanel();
		assertThat(tasksPanel.list("tasksTaskList").contents())
			.anySatisfy(i -> assertThat(i).contains(TASK_1_ID, TASK_1_DESCRIPTION))
			.anySatisfy(i -> assertThat(i).contains(TASK_2_ID, TASK_2_DESCRIPTION));
		
		assertThat(tasksPanel.comboBox("tagComboBox").contents())
			.anySatisfy(i -> assertThat(i).contains(TAG_1_ID, TAG_1_NAME))
			.anySatisfy(i -> assertThat(i).contains(TAG_2_ID, TAG_2_NAME));
		
		tasksPanel.list("tasksTaskList").selectItem(Pattern.compile(".*" + TASK_1_DESCRIPTION + ".*"));
		assertThat(tasksPanel.list("assignedTagsList").contents())
			.anySatisfy(i -> assertThat(i).contains(TAG_2_ID, TAG_2_NAME));
		
		tasksPanel.list("tasksTaskList").selectItem(Pattern.compile(".*Start using TDD.*"));
		assertThat(tasksPanel.list("assignedTagsList").contents())
			.anySatisfy(i -> assertThat(i).contains(TAG_1_ID, TAG_1_NAME));
		
		JPanelFixture tagsPanel = getTagsPanel();
		assertThat(tagsPanel.list("tagsTagList").contents())
			.anySatisfy(i -> assertThat(i).contains(TAG_1_ID, TAG_1_NAME))
			.anySatisfy(i -> assertThat(i).contains(TAG_2_ID, TAG_2_NAME));
		
		tagsPanel.list("tagsTagList").selectItem(Pattern.compile(".*Work.*"));
		assertThat(tagsPanel.list("assignedTasksList").contents())
			.anySatisfy(i -> assertThat(i).contains(TASK_2_ID, TASK_2_DESCRIPTION));
		
		tagsPanel.list("tagsTagList").selectItem(Pattern.compile(".*Important.*"));
		assertThat(tagsPanel.list("assignedTasksList").contents())
			.anySatisfy(i -> assertThat(i).contains(TASK_1_ID, TASK_1_DESCRIPTION));
		
	}
	
	@Test @GUITest
	public void testAddTaskSuccess() {
		// Setup phase
		JPanelFixture tasksPanel = getTasksPanel();
		tasksPanel.textBox("taskIdTextField").enterText("3");
		tasksPanel.textBox("taskDescriptionTextField").enterText("Test this application");
		
		// Exercise phase
		tasksPanel.button("btnAddTask").click();
		
		// Verify phase
		assertThat(tasksPanel.list("tasksTaskList").contents())
			.anySatisfy(i -> assertThat(i).contains("3", "Test this application"));
	}
	
	@Test @GUITest
	public void testAddTaskError() {
		// Setup phase
		JPanelFixture tasksPanel = getTasksPanel();
		tasksPanel.textBox("taskIdTextField").enterText(TASK_1_ID);
		tasksPanel.textBox("taskDescriptionTextField").enterText(TASK_1_DESCRIPTION);
		
		// Exercise phase
		tasksPanel.button("btnAddTask").click();
		
		// Verify phase
		assertThat(tasksPanel.label("tasksErrorLabel").text())
			.contains(TASK_1_ID);
	}
	
	@Test @GUITest
	public void testDeleteTaskSuccess() {
		// Setup phase
		JPanelFixture tasksPanel = getTasksPanel();
		tasksPanel.list("tasksTaskList").selectItem(Pattern.compile(".*" + TASK_1_DESCRIPTION + ".*"));
		
		// Exercise phase
		tasksPanel.button("btnDeleteTask").click();
		
		// Verify phase
		assertThat(tasksPanel.list("tasksTaskList").contents())
			.noneMatch(i -> i.contains(TASK_1_DESCRIPTION));
	}
	

	@Test @GUITest
	public void testDeleteTaskError() {
		// Setup phase
		JPanelFixture tasksPanel = getTasksPanel();
		tasksPanel.list("tasksTaskList").selectItem(Pattern.compile(".*" + TASK_1_DESCRIPTION + ".*"));
		removeTaskFromDatabase(TASK_1_ID);
		
		// Exercise phase
		tasksPanel.button("btnDeleteTask").click();
		
		// Verify phase
		assertThat(tasksPanel.label("tasksErrorLabel").text())
			.contains(TASK_1_ID);
	}
	
	@Test @GUITest
	public void testAssignTagSuccess() {
		// Setup phase
		JPanelFixture tasksPanel = getTasksPanel();
		tasksPanel.list("tasksTaskList").selectItem(Pattern.compile(".*Start using TDD.*"));
		tasksPanel.comboBox("tagComboBox").selectItem(Pattern.compile(".*Important.*"));
		
		// Exercise phase
		tasksPanel.button("btnAssignTag").click();
		
		// Verify phase
		assertThat(tasksPanel.list("assignedTagsList").contents())
			.anySatisfy(i -> assertThat(i).contains(TAG_2_NAME));
	}
	
	@Test @GUITest
	public void testAssignTagError() {
		// Setup phase
		JPanelFixture tasksPanel = getTasksPanel();
		tasksPanel.list("tasksTaskList").selectItem(Pattern.compile(".*Start using TDD.*"));
		tasksPanel.comboBox("tagComboBox").selectItem(Pattern.compile(".*Work.*"));
		
		// Exercise phase
		tasksPanel.button("btnAssignTag").click();
		
		// Verify phase
		assertThat(tasksPanel.label("tasksErrorLabel").text())
			.contains(TASK_2_ID, TAG_1_ID);
	}
	
	@Test @GUITest
	public void testRemoveTagFromTaskSuccess() {
		// Setup phase
		JPanelFixture tasksPanel = getTasksPanel();
		tasksPanel.list("tasksTaskList").selectItem(Pattern.compile(".*Start using TDD.*"));
		tasksPanel.list("assignedTagsList").selectItem(Pattern.compile(".*Work.*"));
		
		// Exercise phase
		tasksPanel.button("btnRemoveTag").click();
		
		// Verify phase
		assertThat(tasksPanel.list("assignedTagsList").contents())
			.noneMatch(i -> i.contains(TAG_1_NAME));
	}
	
	@Test @GUITest
	public void testRemoveTagFromTaskError() {
		// Setup phase
		JPanelFixture tasksPanel = getTasksPanel();
		tasksPanel.list("tasksTaskList").selectItem(Pattern.compile(".*Start using TDD.*"));
		tasksPanel.list("assignedTagsList").selectItem(Pattern.compile(".*Work.*"));
		removeTagFromTaskDatabase(TAG_2_ID, TAG_1_ID);
		
		// Exercise phase
		tasksPanel.button("btnRemoveTag").click();
		
		// Verify phase
		assertThat(tasksPanel.label("tasksErrorLabel").text())
			.contains(TASK_2_ID, TAG_1_ID);
	}
	
	@Test @GUITest
	public void testAddTagSuccess() {
		// Setup phase
		JPanelFixture tagsPanel = getTagsPanel();
		tagsPanel.textBox("tagIdTextField").enterText("3");
		tagsPanel.textBox("tagNameTextField").enterText("Free time");
		
		// Exercise phase
		tagsPanel.button("btnAddTag").click();
		
		// Verify phase
		assertThat(tagsPanel.list("tagsTagList").contents())
			.anySatisfy(i -> assertThat(i).contains("3", "Free time"));
	}
	
	@Test @GUITest
	public void testAddTagError() {
		// Setup phase
		JPanelFixture tagsPanel = getTagsPanel();
		tagsPanel.textBox("tagIdTextField").enterText(TAG_1_ID);
		tagsPanel.textBox("tagNameTextField").enterText(TAG_1_NAME);
		
		// Exercise phase
		tagsPanel.button("btnAddTag").click();
		
		// Verify phase
		assertThat(tagsPanel.label("tagsErrorLabel").text())
			.contains(TAG_1_ID);
	}
	
	@Test @GUITest
	public void testDeleteTagSuccess() {
		// Setup phase
		JPanelFixture tagsPanel = getTagsPanel();
		tagsPanel.list("tagsTagList").selectItem(Pattern.compile(".*Work.*"));
		
		// Exercise phase
		tagsPanel.button("btnDeleteTag").click();
		
		// Verify phase
		assertThat(tagsPanel.list("tagsTagList").contents())
			.noneMatch(i -> i.contains(TAG_1_NAME));
	}
	

	@Test @GUITest
	public void testDeleteTagError() {
		// Setup phase
		JPanelFixture tagsPanel = getTagsPanel();
		tagsPanel.list("tagsTagList").selectItem(Pattern.compile(".*Work.*"));
		removeTagFromDatabase(TAG_1_ID);
		
		// Exercise phase
		tagsPanel.button("btnDeleteTag").click();
		
		// Verify phase
		assertThat(tagsPanel.label("tagsErrorLabel").text())
			.contains(TAG_1_ID);
	}
	
	@Test @GUITest
	public void testRemoveTaskFromTagSuccess() {
		// Setup phase
		JPanelFixture tagsPanel = getTagsPanel();
		tagsPanel.list("tagsTagList").selectItem(Pattern.compile(".*Work.*"));
		tagsPanel.list("assignedTasksList").selectItem(Pattern.compile(".*Start using TDD.*"));
		
		// Exercise phase
		tagsPanel.button("btnRemoveTask").click();
		
		// Verify phase
		assertThat(tagsPanel.list("assignedTasksList").contents())
			.noneMatch(i -> i.contains(TASK_2_DESCRIPTION));
	}
	
	@Test @GUITest
	public void testRemoveTaskFromTagError() {
		// Setup phase
		JPanelFixture tagsPanel = getTagsPanel();
		tagsPanel.list("tagsTagList").selectItem(Pattern.compile(".*Work.*"));
		tagsPanel.list("assignedTasksList").selectItem(Pattern.compile(".*Start using TDD.*"));
		removeTaskFromTagDatabase(TAG_1_ID, TASK_2_ID);
		
		// Exercise phase
		tagsPanel.button("btnRemoveTask").click();
		
		// Verify phase
		assertThat(tagsPanel.label("tagsErrorLabel").text())
			.contains(TASK_2_ID, TAG_1_ID);
	}
	
	private void addTaskToDatabase(Task task, List<String> tags) {
		// Private method to directly insert a task in the collection
		mongoClient.getDatabase(DB_NAME)
			.getCollection(TASKS_COLLECTION)
			.insertOne(new Document()
					.append("id", task.getId())
					.append("description", task.getDescription())
					.append("tags", tags)
					);
	}
	
	private void addTagToDatabase(Tag tag, List<String> tasks) {
		// Private method to directly insert a tag in the collection
		mongoClient.getDatabase(DB_NAME)
			.getCollection(TAGS_COLLECTION)
			.insertOne(new Document()
					.append("id", tag.getId())
					.append("name", tag.getName())
					.append("tasks", tasks)
					);
	}
	
	private void removeTaskFromDatabase(String taskId) {
		// Private method to directly remove a task from the collection
		mongoClient.getDatabase(DB_NAME)
			.getCollection(TASKS_COLLECTION)
			.deleteOne(Filters.eq("id", taskId));
	}
	
	private void removeTagFromDatabase(String tagId) {
		// Private method to directly remove a tag from the collection
		mongoClient.getDatabase(DB_NAME)
			.getCollection(TAGS_COLLECTION)
			.deleteOne(Filters.eq("id", tagId));
	}
	
	private void removeTagFromTaskDatabase(String taskId, String tagId) {
		// Private method to directly remove a tag from a task in the collection
		mongoClient.getDatabase(DB_NAME)
			.getCollection(TASKS_COLLECTION)
			.updateOne(Filters.eq("id", taskId), Updates.pull(TAGS_COLLECTION, tagId));
	}
	
	private void removeTaskFromTagDatabase(String tagId, String taskId) {
		// Private method to directly remove a task from a tag in the collection
		mongoClient.getDatabase(DB_NAME)
			.getCollection(TAGS_COLLECTION)
			.updateOne(Filters.eq("id", tagId), Updates.pull(TASKS_COLLECTION, taskId));
	}
	
	private JPanelFixture getTasksPanel() {
		// Private method that returns a reference to the tasks panel
		return contentPanel.panel("tasksPanel");
	}
	
	private JPanelFixture getTagsPanel() {
		/* This is also necessary since the tab, when clicked,
		 * might not always be immediately available */
		await().atMost(2, TimeUnit.SECONDS).until(() -> {
			try {
				tabPanel.selectTab("Tags");
				contentPanel.panel("tagsPanel");
				return true;
			} catch(Exception e) {
				return false;
			}
		});
		
		return contentPanel.panel("tagsPanel");
	}
}
