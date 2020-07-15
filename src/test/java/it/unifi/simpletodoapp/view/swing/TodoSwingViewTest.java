package it.unifi.simpletodoapp.view.swing;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.assertj.swing.annotation.GUITest;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JButtonFixture;
import org.assertj.swing.fixture.JPanelFixture;
import org.assertj.swing.fixture.JTabbedPaneFixture;
import org.assertj.swing.fixture.JTextComponentFixture;
import org.assertj.swing.junit.runner.GUITestRunner;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import it.unifi.simpletodoapp.controller.TodoController;
import it.unifi.simpletodoapp.model.Task;

@RunWith(GUITestRunner.class)
public class TodoSwingViewTest extends AssertJSwingJUnitTestCase {
	private FrameFixture frameFixture;
	
	@Mock
	private TodoController todoController;
	
	@InjectMocks
	private TodoSwingView todoSwingView;
	
	private JPanelFixture contentPanel;
	private JPanelFixture tasksPanel;

	@Override
	protected void onSetUp() {
		GuiActionRunner.execute(() -> {
			todoSwingView = new TodoSwingView();
			return todoSwingView;
		});
		
		MockitoAnnotations.initMocks(this);
		
		frameFixture = new FrameFixture(robot(), todoSwingView);
		frameFixture.show();
		
		contentPanel = frameFixture.panel("contentPane");
		tasksPanel = contentPanel.panel("tasksPanel");
	}

	@Test @GUITest
	public void testTabsArePresent() {
		JTabbedPaneFixture tabPanel = contentPanel.tabbedPane("tabbedPane");
		
		tabPanel.requireTabTitles("Tasks", "Tags");
	}
	
	@Test @GUITest
	public void testTasksTabControlsArePresent() {
		tasksPanel.label("tasksIdLabel");
		tasksPanel.textBox("tasksIdTextField").requireEnabled();
		tasksPanel.label("tasksDescriptionLabel");
		tasksPanel.textBox("tasksDescriptionTextField").requireEnabled();
		tasksPanel.button("btnAddTask").requireDisabled();
		tasksPanel.label("tasksTaskListLabel");
		tasksPanel.list("tasksTaskList");
		tasksPanel.button("btnDeleteTask").requireDisabled();
		tasksPanel.comboBox("tagComboBox").requireDisabled();
		tasksPanel.button("btnAssignTag").requireDisabled();
		tasksPanel.list("assignedTagsList");
		tasksPanel.button("btnRemoveTag");
		tasksPanel.label("tasksErrorLabel");
	}
	
	@Test @GUITest
	public void testAddTaskButtonDisabledUntilBothFieldsAreNotEmpty() {
		JTextComponentFixture idField = tasksPanel.textBox("tasksIdTextField");
		JTextComponentFixture descriptionField = tasksPanel.textBox("tasksDescriptionTextField");
		JButtonFixture addTaskButton = tasksPanel.button("btnAddTask");
		
		idField.enterText("1");
		addTaskButton.requireDisabled();
		
		idField.setText("");
		descriptionField.setText("Buy groceries");
		addTaskButton.requireDisabled();
	}
	
	@Test @GUITest
	public void testAddButtonDisabledWhenSpacesAreInput() {
		JTextComponentFixture idField = tasksPanel.textBox("tasksIdTextField");
		JTextComponentFixture descriptionField = tasksPanel.textBox("tasksDescriptionTextField");
		JButtonFixture addTaskButton = tasksPanel.button("btnAddTask");
		
		idField.enterText(" ");
		descriptionField.enterText("Buy groceries");
		addTaskButton.requireDisabled();
		
		idField.setText("");
		descriptionField.setText("");
		idField.enterText("1");
		descriptionField.enterText(" ");
		addTaskButton.requireDisabled();
	}
	
	@Test @GUITest
	public void testAddTaskButtonEnabledWhenBothFieldsAreNotEmpty() {
		JTextComponentFixture idField = tasksPanel.textBox("tasksIdTextField");
		JTextComponentFixture descriptionField = tasksPanel.textBox("tasksDescriptionTextField");
		JButtonFixture addTaskButton = tasksPanel.button("btnAddTask");
		
		idField.enterText("1");
		descriptionField.enterText("Buy groceries");
		addTaskButton.requireEnabled();
	}
	
	@Test @GUITest
	public void testAddTaskButtonControllerInvocationWhenPressed() {
		JTextComponentFixture idField = tasksPanel.textBox("tasksIdTextField");
		JTextComponentFixture descriptionField = tasksPanel.textBox("tasksDescriptionTextField");
		JButtonFixture addTaskButton = tasksPanel.button("btnAddTask");
		
		idField.enterText("1");
		descriptionField.enterText("Buy groceries");
		addTaskButton.click();
		
		verify(todoController).addTask(new Task("1", "Buy groceries"));
	}
	
	@Test @GUITest
	public void testTaskAddedAddsToTheTaskList() {
		Task task = new Task("1", "Buy groceries");
		
		GuiActionRunner.execute(() -> todoSwingView.taskAdded(task));
		
		String[] taskList = tasksPanel.list("tasksTaskList").contents();
		assertThat(taskList).containsExactly("#1 - Buy groceries");
	}
	
	@Test @GUITest
	public void testTaskErrorMessageLabel() {
		String errorMessage = "This is an error message";
		
		GuiActionRunner.execute(() -> todoSwingView.taskError(errorMessage));
		tasksPanel.label("tasksErrorLabel").requireText(errorMessage);
	}
	
	@Test @GUITest
	public void testTaskAdditionRemovesErrorMessage() {
		String errorMessage = "This is an error message";
		Task task = new Task("1", "Buy groceries");
		
		GuiActionRunner.execute(() -> {
			todoSwingView.taskError(errorMessage);
			todoSwingView.taskAdded(task);
		});
		
		tasksPanel.label("tasksErrorLabel").requireText(" ");
	}
	
	@Test @GUITest
	public void testSelectingTaskFromListEnablesDeleteTaskButton() {
		Task task = new Task("1", "Buy groceries");
		
		GuiActionRunner.execute(() -> todoSwingView.taskAdded(task));
		
		tasksPanel.list("tasksTaskList").clickItem(0);
		tasksPanel.button("btnDeleteTask").requireEnabled();
		tasksPanel.list("tasksTaskList").clearSelection();
		tasksPanel.button("btnDeleteTask").requireDisabled();
	}
	
	@Test @GUITest
	public void testDeleteTaskButtonControllerInvocationWhenPressed() {
		Task task = new Task("1", "Buy groceries");
		
		GuiActionRunner.execute(() -> todoSwingView.taskAdded(task));
		
		tasksPanel.list("tasksTaskList").clickItem(0);
		tasksPanel.button("btnDeleteTask").click();
		
		verify(todoController).deleteTask(task);
	}
	
	@Test @GUITest
	public void testTaskDeletedRemovesFromTheTaskList() {
		Task task = new Task("1", "Buy groceries");
		
		GuiActionRunner.execute(() -> {
			todoSwingView.taskAdded(task);
			todoSwingView.taskDeleted(task);
		});
		
		String[] taskList = tasksPanel.list("tasksTaskList").contents();
		assertThat(taskList).isEmpty();
	}
	
	@Test @GUITest
	public void testTaskDeletedRemovesErrorMessage() {
		Task task = new Task("1", "Buy groceries");
		
		GuiActionRunner.execute(() -> {
			todoSwingView.taskAdded(task);
			todoSwingView.taskError("This is an error message");
			todoSwingView.taskDeleted(task);
		});
		
		tasksPanel.label("tasksErrorLabel").requireText(" ");
	}
}
