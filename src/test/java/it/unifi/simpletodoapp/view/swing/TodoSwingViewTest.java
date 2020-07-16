package it.unifi.simpletodoapp.view.swing;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collections;

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
import it.unifi.simpletodoapp.model.Tag;
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
	private JPanelFixture tagsPanel;

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
		tasksPanel.button("btnRemoveTag").requireDisabled();
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
	
	@Test @GUITest
	public void testShowAllTasksAddsToTheTaskList() {
		Task firstTask = new Task("1", "Buy groceries");
		Task secondTask = new Task("2", "Start using TDD");
		
		GuiActionRunner.execute(() -> todoSwingView.showAllTasks(Arrays.asList(firstTask, secondTask)));
		
		String[] taskList = tasksPanel.list("tasksTaskList").contents();
		assertThat(taskList).containsExactly("#1 - Buy groceries", "#2 - Start using TDD");
	}
	
	@Test @GUITest
	public void testControllerInvocationWithTaskListSelection() {
		Task firstTask = new Task("1", "Buy groceries");
		Task secondTask = new Task("2", "Start using TDD");
		
		GuiActionRunner.execute(() -> todoSwingView.showAllTasks(Arrays.asList(firstTask, secondTask)));
		
		tasksPanel.list("tasksTaskList").clickItem(0);
		verify(todoController).getTagsByTask(firstTask);
	}
	
	@Test @GUITest
	public void testTagComboBoxEnabledWhenTaskIsSelected() {
		Task task = new Task("1", "Buy groceries");
		
		GuiActionRunner.execute(() -> todoSwingView.taskAdded(task));
		
		tasksPanel.list("tasksTaskList").clickItem(0);
		tasksPanel.comboBox("tagComboBox").requireEnabled();
		tasksPanel.list("tasksTaskList").clearSelection();
		tasksPanel.comboBox("tagComboBox").requireDisabled();
	}
	
	@Test @GUITest
	public void testTagComboBoxContainsTags() {
		Tag firstTag = new Tag("1", "Work");
		Tag secondTag = new Tag("2", "Important");
		
		GuiActionRunner.execute(() -> todoSwingView.showAllTags(Arrays.asList(firstTag, secondTag)));
		
		String[] tagsInComboBox = tasksPanel.comboBox("tagComboBox").contents();
		assertThat(tagsInComboBox).containsExactly("(1) Work", "(2) Important");
	}
	
	@Test @GUITest
	public void testAssignTagEnabledWhenTaskIsSelected() {
		Task task = new Task("1", "Buy groceries");
		
		GuiActionRunner.execute(() -> todoSwingView.taskAdded(task));
		
		tasksPanel.list("tasksTaskList").clickItem(0);
		tasksPanel.button("btnAssignTag").requireEnabled();
		tasksPanel.list("tasksTaskList").clearSelection();
		tasksPanel.button("btnAssignTag").requireDisabled();
	}
	
	@Test @GUITest
	public void testAssingTagButtonControllerInvocationWhenPressed() {
		Task task = new Task("1", "Start using TDD");
		Tag tag = new Tag("1", "Work");
		
		GuiActionRunner.execute(() -> {
			todoSwingView.taskAdded(task);
			todoSwingView.showAllTags(Arrays.asList(tag));
		});
		
		tasksPanel.list("tasksTaskList").clickItem(0);
		tasksPanel.button("btnAssignTag").click();
		verify(todoController).addTagToTask(task, tag);
	}
	
	@Test @GUITest
	public void testTagAddedAddsToTheTagList() {
		Tag tag = new Tag("1", "Work");
		
		GuiActionRunner.execute(() -> todoSwingView.tagAddedToTask(tag));
		
		String[] taskTagsList = tasksPanel.list("assignedTagsList").contents();
		assertThat(taskTagsList).containsExactly("(1) Work");
	}
	
	@Test @GUITest
	public void testTagAddedRemovesErrorMessage() {
		Tag tag = new Tag("1", "Work");
		
		GuiActionRunner.execute(() -> {
			todoSwingView.taskError("This is an error message");
			todoSwingView.tagAddedToTask(tag);
		});
		
		assertThat(tasksPanel.label("tasksErrorLabel").text()).isEqualTo(" ");
	}
	
	@Test @GUITest
	public void testShowTaskTagsAddsToAssignedTagsList() {
		Tag firstTag = new Tag("1", "Work");
		Tag secondTag = new Tag("2", "Important");
		
		GuiActionRunner.execute(() -> todoSwingView.showTaskTags(Arrays.asList(firstTag, secondTag)));
		
		String[] tagsList = tasksPanel.list("assignedTagsList").contents();
		assertThat(tagsList).containsExactly("(1) Work", "(2) Important");
	}
	
	
	@Test @GUITest
	public void testAssignedTagsListIsClearedWhenTaskSelectionIsCleared() { 
		Task task = new Task("1", "Start using TDD");
		Tag tag = new Tag("1", "Work");
		
		GuiActionRunner.execute(() -> {
			todoSwingView.showAllTasks(Collections.singletonList(task));
			todoSwingView.showTaskTags(Collections.singletonList(tag));
		});
		
		tasksPanel.list("tasksTaskList").clickItem(0);
		tasksPanel.list("tasksTaskList").clearSelection();
		
		String[] tagsList = tasksPanel.list("assignedTagsList").contents();
		assertThat(tagsList).isEmpty();
	}
	
	@Test @GUITest
	public void testRemoveTagButtonIsEnabledWhenTagIsSelectedFromList() {
		Tag tag = new Tag("1", "Work");
		
		GuiActionRunner.execute(() -> {
			todoSwingView.showTaskTags(Collections.singletonList(tag));
		});
		
		tasksPanel.list("assignedTagsList").clickItem(0);
		tasksPanel.button("btnRemoveTag").requireEnabled();
		tasksPanel.list("assignedTagsList").clearSelection();
		tasksPanel.button("btnRemoveTag").requireDisabled();
	}
	
	@Test @GUITest
	public void testRemoveTagButtonControllerInvocationWhenPressed() {
		Task task = new Task("1", "Start using TDD");
		Tag tag = new Tag("1", "Work");
		
		GuiActionRunner.execute(() -> {
			todoSwingView.taskAdded(task);
			todoSwingView.showTaskTags(Collections.singletonList(tag));
		});
		
		tasksPanel.list("tasksTaskList").clickItem(0);
		tasksPanel.list("assignedTagsList").clickItem(0);
		tasksPanel.button("btnRemoveTag").click();
		
		verify(todoController).removeTagFromTask(task, tag);
	}
	
	@Test @GUITest
	public void testTagRemovedFromTaskRemovesFromTheAssignedTagList() {
		Tag tag = new Tag("1", "Work");
		
		GuiActionRunner.execute(() -> {
			todoSwingView.showTaskTags(Collections.singletonList(tag));
			todoSwingView.tagRemovedFromTask(tag);
		});
		
		String[] assignedTags = tasksPanel.list("assignedTagsList").contents();
		assertThat(assignedTags).isEmpty();
	}
	
	@Test @GUITest
	public void testTagRemovedFromTaskRemovesErrorMessage() {
		Tag tag = new Tag("1", "Work");
		
		GuiActionRunner.execute(() -> {
			todoSwingView.tagAddedToTask(tag);
			todoSwingView.taskError("This is an error message");
			todoSwingView.tagRemovedFromTask(tag);
		});
		
		assertThat(tasksPanel.label("tasksErrorLabel").text()).isEqualTo(" ");
	}
	
	@Test @GUITest
	public void testTagTabControlsArePresent() {
		getTagsPanel();
		
		tagsPanel.label("tagIdLabel");
		tagsPanel.textBox("tagIdTextField");
		tagsPanel.label("tagNameLabel");
		tagsPanel.textBox("tagNameTextField");
		tagsPanel.button("btnAddTag").requireDisabled();
		tagsPanel.label("tagsTagListLabel");
		tagsPanel.list("tagsTagList");
		tagsPanel.button("btnDeleteTag").requireDisabled();
		tagsPanel.label("tagsTaskListLabel");
		tagsPanel.list("assignedTaskList");
		tagsPanel.button("btnRemoveTask").requireDisabled();
		tagsPanel.label("tagsErrorLabel");
	}
	
	private void getTagsPanel() {
		JTabbedPaneFixture tabPanel = contentPanel.tabbedPane("tabbedPane");
		tabPanel.selectTab("Tags");
		tagsPanel = contentPanel.panel("tagsPanel");
	}
}
