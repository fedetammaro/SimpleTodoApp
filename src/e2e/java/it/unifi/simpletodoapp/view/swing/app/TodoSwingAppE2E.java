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
	@SuppressWarnings("rawtypes")
	@ClassRule
	public static final GenericContainer mongoContainer =
	new GenericContainer("krnbr/mongo").withExposedPorts(27017);

	private FrameFixture frameFixture;
	private MongoClient mongoClient;

	private JPanelFixture contentPanel;

	private JTabbedPaneFixture tabPanel;

	@Override
	protected void onSetUp() {
		mongoClient = new MongoClient(new ServerAddress(
				mongoContainer.getContainerIpAddress(),
				mongoContainer.getMappedPort(27017)));

		mongoClient.getDatabase("todoapp").drop();
		
		addTaskToDatabase(
				new Task("1", "Buy groceries"),
				Arrays.asList("2")
				);
		addTaskToDatabase(
				new Task("2", "Start using TDD"),
				Arrays.asList("1")
				);
		addTagToDatabase(
				new Tag("1", "Work"),
				Arrays.asList("2")
				);
		addTagToDatabase(
				new Tag("2", "Important"),
				Arrays.asList("1")
				);

		application("it.unifi.simpletodoapp.TodoApplication")
			.withArgs(
					"--mongo-host=" + mongoContainer.getContainerIpAddress(),
					"--mongo-port=" + mongoContainer.getMappedPort(27017),
					"--db-name=" + "todoapp",
					"--db-tasksCollection=" + "tasks",
					"--db-tagsCollection=" + "tags"
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
	protected void onTearDown() {
		mongoClient.close();
	}
	
	@Test @GUITest
	public void testAlreadySavedDataIsPresent() {
		JPanelFixture tasksPanel = getTasksPanel();
		assertThat(tasksPanel.list("tasksTaskList").contents())
			.anySatisfy(i -> assertThat(i).contains("1", "Buy groceries"))
			.anySatisfy(i -> assertThat(i).contains("2", "Start using TDD"));
		
		assertThat(tasksPanel.comboBox("tagComboBox").contents())
			.anySatisfy(i -> assertThat(i).contains("1", "Work"))
			.anySatisfy(i -> assertThat(i).contains("2", "Important"));
		
		tasksPanel.list("tasksTaskList").selectItem(Pattern.compile(".*Buy groceries.*"));
		assertThat(tasksPanel.list("assignedTagsList").contents())
			.anySatisfy(i -> assertThat(i).contains("2", "Important"));
		
		tasksPanel.list("tasksTaskList").selectItem(Pattern.compile(".*Start using TDD.*"));
		assertThat(tasksPanel.list("assignedTagsList").contents())
			.anySatisfy(i -> assertThat(i).contains("1", "Work"));
		
		JPanelFixture tagsPanel = getTagsPanel();
		assertThat(tagsPanel.list("tagsTagList").contents())
			.anySatisfy(i -> assertThat(i).contains("1", "Work"))
			.anySatisfy(i -> assertThat(i).contains("2", "Important"));
		
		tagsPanel.list("tagsTagList").selectItem(Pattern.compile(".*Work.*"));
		assertThat(tagsPanel.list("assignedTasksList").contents())
			.anySatisfy(i -> assertThat(i).contains("2", "Start using TDD"));
		
		tagsPanel.list("tagsTagList").selectItem(Pattern.compile(".*Important.*"));
		assertThat(tagsPanel.list("assignedTasksList").contents())
			.anySatisfy(i -> assertThat(i).contains("1", "Buy groceries"));
		
	}
	
	@Test @GUITest
	public void testAddTaskSuccess() {
		JPanelFixture tasksPanel = getTasksPanel();
		tasksPanel.textBox("taskIdTextField").enterText("3");
		tasksPanel.textBox("taskDescriptionTextField").enterText("Test this application");
		tasksPanel.button("btnAddTask").click();
		
		assertThat(tasksPanel.list("tasksTaskList").contents())
			.anySatisfy(i -> assertThat(i).contains("3", "Test this application"));
	}
	
	@Test @GUITest
	public void testAddTaskError() {
		JPanelFixture tasksPanel = getTasksPanel();
		tasksPanel.textBox("taskIdTextField").enterText("1");
		tasksPanel.textBox("taskDescriptionTextField").enterText("Buy groceries");
		tasksPanel.button("btnAddTask").click();
		
		assertThat(tasksPanel.label("tasksErrorLabel").text())
			.contains("1");
	}
	
	@Test @GUITest
	public void testDeleteTaskSuccess() {
		JPanelFixture tasksPanel = getTasksPanel();
		tasksPanel.list("tasksTaskList").selectItem(Pattern.compile(".*Buy groceries.*"));
		tasksPanel.button("btnDeleteTask").click();
		
		assertThat(tasksPanel.list("tasksTaskList").contents())
			.noneMatch(i -> i.contains("Buy groceries"));
	}
	

	@Test @GUITest
	public void testDeleteTaskError() {
		JPanelFixture tasksPanel = getTasksPanel();
		tasksPanel.list("tasksTaskList").selectItem(Pattern.compile(".*Buy groceries.*"));
		removeTaskFromDatabase("1");
		tasksPanel.button("btnDeleteTask").click();
		
		assertThat(tasksPanel.label("tasksErrorLabel").text())
			.contains("1");
	}
	
	@Test @GUITest
	public void testAssignTagSuccess() {
		JPanelFixture tasksPanel = getTasksPanel();
		tasksPanel.list("tasksTaskList").selectItem(Pattern.compile(".*Start using TDD.*"));
		tasksPanel.comboBox("tagComboBox").selectItem(Pattern.compile(".*Important.*"));
		tasksPanel.button("btnAssignTag").click();
		
		assertThat(tasksPanel.list("assignedTagsList").contents())
			.anySatisfy(i -> assertThat(i).contains("Important"));
	}
	
	@Test @GUITest
	public void testAssignTagError() {
		JPanelFixture tasksPanel = getTasksPanel();
		tasksPanel.list("tasksTaskList").selectItem(Pattern.compile(".*Start using TDD.*"));
		tasksPanel.comboBox("tagComboBox").selectItem(Pattern.compile(".*Work.*"));
		tasksPanel.button("btnAssignTag").click();
		
		assertThat(tasksPanel.label("tasksErrorLabel").text())
			.contains("2", "1");
	}
	
	@Test @GUITest
	public void testRemoveTagFromTaskSuccess() {
		JPanelFixture tasksPanel = getTasksPanel();
		tasksPanel.list("tasksTaskList").selectItem(Pattern.compile(".*Start using TDD.*"));
		tasksPanel.list("assignedTagsList").selectItem(Pattern.compile(".*Work.*"));
		tasksPanel.button("btnRemoveTag").click();
		
		assertThat(tasksPanel.list("assignedTagsList").contents())
			.noneMatch(i -> i.contains("Work"));
	}
	
	@Test @GUITest
	public void testRemoveTagFromTaskError() {
		JPanelFixture tasksPanel = getTasksPanel();
		tasksPanel.list("tasksTaskList").selectItem(Pattern.compile(".*Start using TDD.*"));
		tasksPanel.list("assignedTagsList").selectItem(Pattern.compile(".*Work.*"));
		removeTagFromTaskDatabase("2", "1");
		tasksPanel.button("btnRemoveTag").click();
		
		assertThat(tasksPanel.label("tasksErrorLabel").text())
			.contains("1", "2");
	}
	
	@Test @GUITest
	public void testAddTagSuccess() {
		JPanelFixture tagsPanel = getTagsPanel();
		tagsPanel.textBox("tagIdTextField").enterText("3");
		tagsPanel.textBox("tagNameTextField").enterText("Free time");
		tagsPanel.button("btnAddTag").click();
		
		assertThat(tagsPanel.list("tagsTagList").contents())
			.anySatisfy(i -> assertThat(i).contains("3", "Free time"));
	}
	
	@Test @GUITest
	public void testAddTagError() {
		JPanelFixture tagsPanel = getTagsPanel();
		tagsPanel.textBox("tagIdTextField").enterText("1");
		tagsPanel.textBox("tagNameTextField").enterText("Work");
		tagsPanel.button("btnAddTag").click();
		
		assertThat(tagsPanel.label("tagsErrorLabel").text())
			.contains("1");
	}
	
	@Test @GUITest
	public void testDeleteTagSuccess() {
		JPanelFixture tagsPanel = getTagsPanel();
		tagsPanel.list("tagsTagList").selectItem(Pattern.compile(".*Work.*"));
		tagsPanel.button("btnDeleteTag").click();
		
		assertThat(tagsPanel.list("tagsTagList").contents())
			.noneMatch(i -> i.contains("Work"));
	}
	

	@Test @GUITest
	public void testDeleteTagError() {
		JPanelFixture tagsPanel = getTagsPanel();
		tagsPanel.list("tagsTagList").selectItem(Pattern.compile(".*Work.*"));
		removeTagFromDatabase("1");
		tagsPanel.button("btnDeleteTag").click();
		
		assertThat(tagsPanel.label("tagsErrorLabel").text())
			.contains("1");
	}
	
	@Test @GUITest
	public void testRemoveTaskFromTagSuccess() {
		JPanelFixture tagsPanel = getTagsPanel();
		tagsPanel.list("tagsTagList").selectItem(Pattern.compile(".*Work.*"));
		tagsPanel.list("assignedTasksList").selectItem(Pattern.compile(".*Start using TDD.*"));
		tagsPanel.button("btnRemoveTask").click();
		
		assertThat(tagsPanel.list("assignedTasksList").contents())
			.noneMatch(i -> i.contains("Start using TDD"));
	}
	
	@Test @GUITest
	public void testRemoveTaskFromTagError() {
		JPanelFixture tagsPanel = getTagsPanel();
		tagsPanel.list("tagsTagList").selectItem(Pattern.compile(".*Work.*"));
		tagsPanel.list("assignedTasksList").selectItem(Pattern.compile(".*Start using TDD.*"));
		removeTaskFromTagDatabase("1", "2");
		tagsPanel.button("btnRemoveTask").click();
		
		assertThat(tagsPanel.label("tagsErrorLabel").text())
			.contains("2", "1");
	}
	
	private void addTaskToDatabase(Task task, List<String> tags) {
		mongoClient.getDatabase("todoapp")
			.getCollection("tasks")
			.insertOne(new Document()
					.append("id", task.getId())
					.append("description", task.getDescription())
					.append("tags", tags)
					);
	}
	
	private void removeTaskFromDatabase(String taskId) {
		mongoClient.getDatabase("todoapp")
			.getCollection("tasks")
			.deleteOne(Filters.eq("id", taskId));
	}
	
	private void removeTagFromDatabase(String tagId) {
		mongoClient.getDatabase("todoapp")
			.getCollection("tags")
			.deleteOne(Filters.eq("id", tagId));
	}
	
	private void removeTagFromTaskDatabase(String taskId, String tagId) {
		mongoClient.getDatabase("todoapp")
			.getCollection("tasks")
			.updateOne(Filters.eq("id", taskId), Updates.pull("tags", tagId));
	}
	
	private void removeTaskFromTagDatabase(String tagId, String taskId) {
		mongoClient.getDatabase("todoapp")
			.getCollection("tags")
			.updateOne(Filters.eq("id", tagId), Updates.pull("tasks", taskId));
	}
	
	private void addTagToDatabase(Tag tag, List<String> tasks) {
		mongoClient.getDatabase("todoapp")
			.getCollection("tags")
			.insertOne(new Document()
					.append("id", tag.getId())
					.append("name", tag.getName())
					.append("tasks", tasks)
					);
	}
	
	private JPanelFixture getTasksPanel() {
		return contentPanel.panel("tasksPanel");
	}
	
	private JPanelFixture getTagsPanel() {
		tabPanel.selectTab("Tags");
		
		return contentPanel.panel("tagsPanel");
	}
}
