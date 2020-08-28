package it.unifi.simpletodoapp.view.swing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

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
	@Mock
	private TodoController todoController;

	@InjectMocks
	private TodoSwingView todoSwingView;

	private FrameFixture frameFixture;
	private JPanelFixture contentPanel;
	private JPanelFixture tasksPanel;
	private JPanelFixture tagsPanel;

	@Override
	protected void onSetUp() {
		/* Setup phase to create the application window, show it and
		 * get the FrameFixture to conduct tests on */
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
		// Verify phase
		JTabbedPaneFixture tabPanel = contentPanel.tabbedPane("tabbedPane");
		tabPanel.requireTabTitles("Tasks", "Tags");
	}

	@Test @GUITest
	public void testTasksTabControlsArePresent() {
		// Verify phase
		tasksPanel.label("taskIdLabel");
		tasksPanel.textBox("taskIdTextField").requireEnabled();
		tasksPanel.label("taskDescriptionLabel");
		tasksPanel.textBox("taskDescriptionTextField").requireEnabled();
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
		// Setup phase
		JTextComponentFixture idField =
				tasksPanel.textBox("taskIdTextField");
		JTextComponentFixture descriptionField =
				tasksPanel.textBox("taskDescriptionTextField");
		JButtonFixture addTaskButton =
				tasksPanel.button("btnAddTask");

		// Exercise and verify phases
		idField.enterText("1");
		addTaskButton.requireDisabled();

		idField.setText("");
		descriptionField.setText("Buy groceries");
		addTaskButton.requireDisabled();
	}

	@Test @GUITest
	public void testAddTaskButtonDisabledWhenSpacesAreInput() {
		// Setup phase
		JTextComponentFixture idField =
				tasksPanel.textBox("taskIdTextField");
		JTextComponentFixture descriptionField =
				tasksPanel.textBox("taskDescriptionTextField");
		JButtonFixture addTaskButton = tasksPanel.button("btnAddTask");

		// Exercise and verify phases
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
		// Setup phase
		JTextComponentFixture idField =
				tasksPanel.textBox("taskIdTextField");
		JTextComponentFixture descriptionField =
				tasksPanel.textBox("taskDescriptionTextField");
		JButtonFixture addTaskButton =
				tasksPanel.button("btnAddTask");

		// Exercise and verify phases
		idField.enterText("1");
		descriptionField.enterText("Buy groceries");
		addTaskButton.requireEnabled();
	}

	@Test @GUITest
	public void testAddTaskButtonControllerInvocationWhenPressed() {
		// Setup phase
		JTextComponentFixture idField =
				tasksPanel.textBox("taskIdTextField");
		JTextComponentFixture descriptionField =
				tasksPanel.textBox("taskDescriptionTextField");
		JButtonFixture addTaskButton =
				tasksPanel.button("btnAddTask");

		// Exercise phase
		idField.enterText("1");
		descriptionField.enterText("Buy groceries");
		addTaskButton.click();

		// Verify phase
		verify(todoController).addTask(new Task("1", "Buy groceries"));
	}

	@Test @GUITest
	public void testTaskAddedAddsToTheTaskList() {
		// Setup phase
		Task task = new Task("1", "Buy groceries");

		// Exercise phase
		GuiActionRunner.execute(
				() -> todoSwingView.taskAdded(task)
				);
		
		// Verify phase
		assertThat(tasksPanel.list("tasksTaskList").contents())
		.containsExactly("#1 - Buy groceries");
	}

	@Test @GUITest
	public void testTaskErrorMessageLabel() {
		// Setup phase
		String errorMessage = "This is an error message";

		// Exercise phase
		GuiActionRunner.execute(
				() -> todoSwingView.taskError(errorMessage)
				);
		
		// Verify phase
		tasksPanel.label("tasksErrorLabel").requireText(errorMessage);
	}

	@Test @GUITest
	public void testTaskAdditionRemovesErrorMessage() {
		// Setup phase
		String errorMessage = "This is an error message";
		Task task = new Task("1", "Buy groceries");

		// Exercise phase
		GuiActionRunner.execute(
				() -> {
					todoSwingView.taskError(errorMessage);
					todoSwingView.taskAdded(task);
				});

		// Verify phase
		tasksPanel.label("tasksErrorLabel").requireText(" ");
	}

	@Test @GUITest
	public void testSelectingTaskFromListEnablesDeleteTaskButton() {
		// Setup phase
		Task task = new Task("1", "Buy groceries");

		// Exercise and verify phases
		GuiActionRunner.execute(
				() -> todoSwingView.taskAdded(task)
				);

		tasksPanel.list("tasksTaskList").clickItem(0);
		tasksPanel.button("btnDeleteTask").requireEnabled();
		tasksPanel.list("tasksTaskList").clearSelection();
		tasksPanel.button("btnDeleteTask").requireDisabled();
	}

	@Test @GUITest
	public void testDeleteTaskButtonControllerInvocationWhenPressed() {
		// Setup phase
		Task task = new Task("1", "Buy groceries");

		// Exercise phase
		GuiActionRunner.execute(
				() -> todoSwingView.taskAdded(task)
				);

		tasksPanel.list("tasksTaskList").clickItem(0);
		tasksPanel.button("btnDeleteTask").click();

		// Verify phase
		verify(todoController).deleteTask(task);
	}

	@Test @GUITest
	public void testTaskDeletedRemovesFromTheTaskList() {
		// Setup phase
		Task task = new Task("1", "Buy groceries");

		// Exercise phase
		GuiActionRunner.execute(
				() -> {
					todoSwingView.taskAdded(task);
					todoSwingView.taskDeleted(task);
				});

		// Verify phase
		assertThat(tasksPanel.list("tasksTaskList").contents())
		.isEmpty();
	}

	@Test @GUITest
	public void testTaskDeletedRemovesErrorMessage() {
		// Setup phase
		Task task = new Task("1", "Buy groceries");

		// Exercise phase
		GuiActionRunner.execute(
				() -> {
					todoSwingView.taskAdded(task);
					todoSwingView.taskError("This is an error message");
					todoSwingView.taskDeleted(task);
				});

		// Verify phase
		tasksPanel.label("tasksErrorLabel").requireText(" ");
	}

	@Test @GUITest
	public void testShowAllTasksAddsToTheTaskList() {
		// Setup phase
		Task firstTask = new Task("1", "Buy groceries");
		Task secondTask = new Task("2", "Start using TDD");

		// Exercise phase
		GuiActionRunner.execute(
				() -> todoSwingView.showAllTasks(Arrays.asList(firstTask, secondTask))
				);

		// Verify phase
		assertThat(tasksPanel.list("tasksTaskList").contents())
		.containsExactly("#1 - Buy groceries", "#2 - Start using TDD");
	}

	@Test @GUITest
	public void testControllerInvocationWithTaskListSelection() {
		// Setup phase
		Task firstTask = new Task("1", "Buy groceries");
		Task secondTask = new Task("2", "Start using TDD");

		// Exercise phase
		GuiActionRunner.execute(
				() -> todoSwingView.showAllTasks(Arrays.asList(firstTask, secondTask))
				);

		tasksPanel.list("tasksTaskList").clickItem(0);
		
		// Verify phase
		verify(todoController).getTagsByTask(firstTask);
	}

	@Test @GUITest
	public void testTagComboBoxEnabledWhenTaskIsSelected() {
		// Setup phase
		Task task = new Task("1", "Buy groceries");

		// Exercise and verify phases
		GuiActionRunner.execute(
				() -> todoSwingView.taskAdded(task)
				);

		tasksPanel.list("tasksTaskList").clickItem(0);
		tasksPanel.comboBox("tagComboBox").requireEnabled();
		tasksPanel.list("tasksTaskList").clearSelection();
		tasksPanel.comboBox("tagComboBox").requireDisabled();
	}

	@Test @GUITest
	public void testTagComboBoxContainsTags() {
		// Setup phase
		Tag firstTag = new Tag("1", "Work");
		Tag secondTag = new Tag("2", "Important");

		// Exercise phase
		GuiActionRunner.execute(
				() -> todoSwingView.showAllTags(Arrays.asList(firstTag, secondTag))
				);

		// Verify phase
		assertThat(tasksPanel.comboBox("tagComboBox").contents())
		.containsExactly("(1) Work", "(2) Important");
	}

	@Test @GUITest
	public void testAssignTagEnabledWhenTaskIsSelected() {
		// Setup phase
		Task task = new Task("1", "Buy groceries");

		// Exercise and verify phases
		GuiActionRunner.execute(
				() -> todoSwingView.taskAdded(task)
				);

		tasksPanel.list("tasksTaskList").clickItem(0);
		tasksPanel.button("btnAssignTag").requireEnabled();
		tasksPanel.list("tasksTaskList").clearSelection();
		tasksPanel.button("btnAssignTag").requireDisabled();
	}

	@Test @GUITest
	public void testAssingTagButtonControllerInvocationWhenPressed() {
		// Setup phase
		Task task = new Task("1", "Start using TDD");
		Tag tag = new Tag("1", "Work");

		// Exercise phase
		GuiActionRunner.execute(
				() -> {
					todoSwingView.taskAdded(task);
					todoSwingView.showAllTags(Arrays.asList(tag));
				});

		tasksPanel.list("tasksTaskList").clickItem(0);
		tasksPanel.button("btnAssignTag").click();
		
		// Verify phase
		verify(todoController).addTagToTask(task, tag);
	}

	@Test @GUITest
	public void testTagAddedAddsToTheAssignedTagList() {
		// Setup phase
		Tag tag = new Tag("1", "Work");

		// Exercise phase
		GuiActionRunner.execute(
				() -> todoSwingView.tagAddedToTask(tag)
				);

		// Verify phase
		assertThat(tasksPanel.list("assignedTagsList").contents())
		.containsExactly("(1) Work");
	}

	@Test @GUITest
	public void testTagAddedRemovesErrorMessage() {
		// Setup phase
		Tag tag = new Tag("1", "Work");

		// Exercise phase
		GuiActionRunner.execute(
				() -> {
					todoSwingView.taskError("This is an error message");
					todoSwingView.tagAddedToTask(tag);
				});

		// Verify phase
		assertThat(tasksPanel.label("tasksErrorLabel").text())
		.isEqualTo(" ");
	}

	@Test @GUITest
	public void testShowTaskTagsAddsToAssignedTagsList() {
		// Setup phase
		Tag firstTag = new Tag("1", "Work");
		Tag secondTag = new Tag("2", "Important");

		// Exercise phase
		GuiActionRunner.execute(
				() -> todoSwingView.showTaskTags(Arrays.asList(firstTag, secondTag))
				);

		// Verify phase
		assertThat(tasksPanel.list("assignedTagsList").contents())
		.containsExactly("(1) Work", "(2) Important");
	}


	@Test @GUITest
	public void testAssignedTagsListIsClearedWhenTaskSelectionIsCleared() {
		// Setup phase
		Task task = new Task("1", "Start using TDD");
		Tag tag = new Tag("1", "Work");

		// Exercise phase
		GuiActionRunner.execute(
				() -> {
					todoSwingView.showAllTasks(Collections.singletonList(task));
					todoSwingView.showTaskTags(Collections.singletonList(tag));
				});

		tasksPanel.list("tasksTaskList").clickItem(0);
		tasksPanel.list("tasksTaskList").clearSelection();

		// Verify phase
		assertThat(tasksPanel.list("assignedTagsList").contents())
		.isEmpty();
	}

	@Test @GUITest
	public void testRemoveTagButtonIsEnabledWhenTagIsSelectedFromList() {
		// Setup phase
		Tag tag = new Tag("1", "Work");

		// Exercise and verify phases
		GuiActionRunner.execute(
				() -> {
					todoSwingView.showTaskTags(Collections.singletonList(tag));
				});

		tasksPanel.list("assignedTagsList").clickItem(0);
		tasksPanel.button("btnRemoveTag").requireEnabled();
		tasksPanel.list("assignedTagsList").clearSelection();
		tasksPanel.button("btnRemoveTag").requireDisabled();
	}

	@Test @GUITest
	public void testRemoveTagButtonControllerInvocationWhenPressed() {
		// Setup phase
		Task task = new Task("1", "Start using TDD");
		Tag tag = new Tag("1", "Work");

		// Exercise phase
		GuiActionRunner.execute(
				() -> {
					todoSwingView.taskAdded(task);
					todoSwingView.showTaskTags(Collections.singletonList(tag));
				});

		tasksPanel.list("tasksTaskList").clickItem(0);
		tasksPanel.list("assignedTagsList").clickItem(0);
		tasksPanel.button("btnRemoveTag").click();

		// Verify phase
		verify(todoController).removeTagFromTask(task, tag);
	}

	@Test @GUITest
	public void testTagRemovedFromTaskRemovesFromTheAssignedTagList() {
		// Setup phase
		Tag tag = new Tag("1", "Work");

		// Exercise phase
		GuiActionRunner.execute(
				() -> {
					todoSwingView.showTaskTags(Collections.singletonList(tag));
					todoSwingView.tagRemovedFromTask(tag);
				});

		// Verify phase
		String[] assignedTags = tasksPanel.list("assignedTagsList").contents();
		assertThat(assignedTags)
		.isEmpty();
	}

	@Test @GUITest
	public void testTagRemovedFromTaskRemovesErrorMessage() {
		// Setup phase
		Tag tag = new Tag("1", "Work");

		// Exercise phase
		GuiActionRunner.execute(
				() -> {
					todoSwingView.tagAddedToTask(tag);
					todoSwingView.taskError("This is an error message");
					todoSwingView.tagRemovedFromTask(tag);
				});

		// Verify phase
		assertThat(tasksPanel.label("tasksErrorLabel").text())
		.isEqualTo(" ");
	}

	@Test @GUITest
	public void testTagTabControlsArePresent() {
		// Setup phase
		getTagsPanel();

		// Verify phase
		tagsPanel.label("tagIdLabel");
		tagsPanel.textBox("tagIdTextField");
		tagsPanel.label("tagNameLabel");
		tagsPanel.textBox("tagNameTextField");
		tagsPanel.button("btnAddTag").requireDisabled();
		tagsPanel.label("tagsTagListLabel");
		tagsPanel.list("tagsTagList");
		tagsPanel.button("btnDeleteTag").requireDisabled();
		tagsPanel.label("assignedTasksListLabel");
		tagsPanel.list("assignedTasksList");
		tagsPanel.button("btnRemoveTask").requireDisabled();
		tagsPanel.label("tagsErrorLabel");
	}

	@Test @GUITest
	public void testAddTagButtonDisabledUntilBothFieldsAreNotEmpty() {
		// Setup phase
		getTagsPanel();

		JTextComponentFixture idField =
				tagsPanel.textBox("tagIdTextField");
		JTextComponentFixture nameField =
				tagsPanel.textBox("tagNameTextField");
		JButtonFixture addTagButton =
				tagsPanel.button("btnAddTag");
		
		// Exercise and verify phases
		idField.enterText("1");
		addTagButton.requireDisabled();

		idField.setText("");
		nameField.setText("Work");
		addTagButton.requireDisabled();
	}

	@Test @GUITest
	public void testAddTagButtonDisabledWhenSpacesAreInput() {
		// Setup phase
		getTagsPanel();

		JTextComponentFixture idField =
				tagsPanel.textBox("tagIdTextField");
		JTextComponentFixture nameField =
				tagsPanel.textBox("tagNameTextField");
		JButtonFixture addTagButton =
				tagsPanel.button("btnAddTag");
		
		// Exercise and verify phases
		idField.enterText(" ");
		nameField.enterText("Work");
		addTagButton.requireDisabled();

		idField.setText("");
		nameField.setText("");
		idField.enterText("1");
		nameField.enterText(" ");
		addTagButton.requireDisabled();
	}

	@Test @GUITest
	public void testAddTagButtonEnabledWhenBothFieldsAreNotEmpty() {
		// Setup phase
		getTagsPanel();

		JTextComponentFixture idField =
				tagsPanel.textBox("tagIdTextField");
		JTextComponentFixture nameField =
				tagsPanel.textBox("tagNameTextField");
		JButtonFixture addtagButton =
				tagsPanel.button("btnAddTag");
		
		// Exercise phase
		idField.enterText("1");
		nameField.enterText("Work");
		
		// Verify phase
		addtagButton.requireEnabled();
	}

	@Test @GUITest
	public void testAddTagButtonControllerInvocationWhenPressed() {
		// Setup phase
		getTagsPanel();

		JTextComponentFixture idField =
				tagsPanel.textBox("tagIdTextField");
		JTextComponentFixture nameField =
				tagsPanel.textBox("tagNameTextField");
		JButtonFixture addTagButton =
				tagsPanel.button("btnAddTag");
		
		// Exercise phase
		idField.enterText("1");
		nameField.enterText("Work");
		addTagButton.click();

		// Verify phase
		verify(todoController).addTag(new Tag("1", "Work"));
	}

	@Test @GUITest
	public void testTagAddedAddsToTheTagList() {
		// Setup phase
		getTagsPanel();

		Tag tag = new Tag("1", "Work");

		// Exercise phase
		GuiActionRunner.execute(
				() -> todoSwingView.tagAdded(tag)
				);

		// Verify phase
		assertThat(tagsPanel.list("tagsTagList").contents())
		.containsExactly("(1) Work");
	}

	@Test @GUITest
	public void testTagErrorMessageLabel() {
		// Setup phase
		getTagsPanel();

		String errorMessage = "This is an error message";

		// Exercise phase
		GuiActionRunner.execute(
				() -> todoSwingView.tagError(errorMessage)
				);
		
		// Verify phase
		tagsPanel.label("tagsErrorLabel").requireText(errorMessage);
	}

	@Test @GUITest
	public void testTagAdditionRemovesErrorMessage() {
		// Setup phase
		getTagsPanel();

		String errorMessage = "This is an error message";
		Tag tag = new Tag("1", "Work");

		// Exercise phase
		GuiActionRunner.execute(
				() -> {
					todoSwingView.tagError(errorMessage);
					todoSwingView.tagAdded(tag);
				});

		// Verify phase
		tagsPanel.label("tagsErrorLabel").requireText(" ");
	}

	@Test @GUITest
	public void testSelectingTagFromListEnablesDeleteTagButton() {
		// Setup phase
		getTagsPanel();

		Tag tag = new Tag("1", "Work");

		// Exercise and verify phases
		GuiActionRunner.execute(
				() -> todoSwingView.tagAdded(tag)
				);

		tagsPanel.list("tagsTagList").clickItem(0);
		tagsPanel.button("btnDeleteTag").requireEnabled();
		tagsPanel.list("tagsTagList").clearSelection();
		tagsPanel.button("btnDeleteTag").requireDisabled();
	}

	@Test @GUITest
	public void testDeleteTagButtonControllerInvocationWhenPressed() {
		// Setup phase
		getTagsPanel();

		Tag tag = new Tag("1", "Work");

		// Exercise phase
		GuiActionRunner.execute(
				() -> todoSwingView.tagAdded(tag)
				);

		tagsPanel.list("tagsTagList").clickItem(0);
		tagsPanel.button("btnDeleteTag").click();

		// Verify phase
		verify(todoController).deleteTag(tag);
	}

	@Test @GUITest
	public void testTagDeletedRemovesFromTheTagList() {
		// Setup phase
		getTagsPanel();

		Tag tag = new Tag("1", "Work");

		// Exercise phase
		GuiActionRunner.execute(
				() -> {
					todoSwingView.tagAdded(tag);
					todoSwingView.tagDeleted(tag);
				});

		// Verify phase
		assertThat(tagsPanel.list("tagsTagList").contents())
		.isEmpty();
	}

	@Test @GUITest
	public void testTagDeletedRemovesErrorMessage() {
		// Setup phase
		getTagsPanel();

		Tag tag = new Tag("1", "Work");

		// Exercise phase
		GuiActionRunner.execute(
				() -> {
					todoSwingView.tagAdded(tag);
					todoSwingView.tagError("This is an error message");
					todoSwingView.tagDeleted(tag);
				});

		// Verify phase
		tagsPanel.label("tagsErrorLabel").requireText(" ");
	}

	@Test @GUITest
	public void testShowAllTagsAddsToTheTagList() {
		// Setup phase
		getTagsPanel();

		Tag firstTag = new Tag("1", "Work");
		Tag secondTag = new Tag("2", "Important");

		// Exercise phase
		GuiActionRunner.execute(
				() -> todoSwingView.showAllTags(Arrays.asList(firstTag, secondTag))
				);

		// Verify phase
		assertThat(tagsPanel.list("tagsTagList").contents())
		.containsExactly("(1) Work", "(2) Important");
	}

	@Test @GUITest
	public void testControllerInvocationWithTagListSelection() {
		// Setup phase
		getTagsPanel();

		Tag firstTag = new Tag("1", "Work");
		Tag secondTag = new Tag("2", "Important");

		// Exercise phase
		GuiActionRunner.execute(
				() -> todoSwingView.showAllTags(Arrays.asList(firstTag, secondTag))
				);

		tagsPanel.list("tagsTagList").clickItem(0);
		
		// Verify phase
		verify(todoController).getTasksByTag(firstTag);
	}

	@Test @GUITest
	public void testShowTagTasksAddsToAssignedTasksList() {
		// Setup phase
		getTagsPanel();

		Task firstTask = new Task("1", "Buy groceries");
		Task secondTask = new Task("2", "Start using TDD");

		// Exercise phase
		GuiActionRunner.execute(
				() -> todoSwingView.showTagTasks(Arrays.asList(firstTask, secondTask))
				);

		// Verify phase
		assertThat(tagsPanel.list("assignedTasksList").contents())
		.containsExactly("#1 - Buy groceries", "#2 - Start using TDD");
	}


	@Test @GUITest
	public void testAssignedTasksListIsClearedWhenTagSelectionIsCleared() {
		// Setup phase
		getTagsPanel();

		Tag tag = new Tag("1", "Work");
		Task task = new Task("1", "Start using TDD");

		// Exercise phase
		GuiActionRunner.execute(
				() -> {
					todoSwingView.showAllTags(Collections.singletonList(tag));
					todoSwingView.showTagTasks(Collections.singletonList(task));
				});

		tagsPanel.list("tagsTagList").clickItem(0);
		tagsPanel.list("tagsTagList").clearSelection();

		// Verify phase
		assertThat(tagsPanel.list("assignedTasksList").contents())
		.isEmpty();
	}

	@Test @GUITest
	public void testRemoveTaskButtonIsEnabledWhenTaskIsSelectedFromList() {
		// Setup phase
		getTagsPanel();

		Task task = new Task("1", "Buy groceries");

		// Exercise and verify phases
		GuiActionRunner.execute(
				() -> {
					todoSwingView.showTagTasks(Collections.singletonList(task));
				});

		tagsPanel.list("assignedTasksList").clickItem(0);
		tagsPanel.button("btnRemoveTask").requireEnabled();
		tagsPanel.list("assignedTasksList").clearSelection();
		tagsPanel.button("btnRemoveTask").requireDisabled();
	}

	@Test @GUITest
	public void testRemoveTaskButtonControllerInvocationWhenPressed() {
		// Setup phase
		getTagsPanel();

		Tag tag = new Tag("1", "Work");
		Task task = new Task("1", "Start using TDD");

		// Exercise phase
		GuiActionRunner.execute(
				() -> {
					todoSwingView.tagAdded(tag);
					todoSwingView.showTagTasks(Collections.singletonList(task));
				});

		tagsPanel.list("tagsTagList").clickItem(0);
		tagsPanel.list("assignedTasksList").clickItem(0);
		tagsPanel.button("btnRemoveTask").click();

		// Verify phase
		verify(todoController).removeTaskFromTag(tag, task);
	}

	@Test @GUITest
	public void testTaskRemovedFromTagRemovesFromTheAssignedTasksList() {
		// Setup phase
		getTagsPanel();

		Task task = new Task("1", "Buy groceries");

		// Exercise phase
		GuiActionRunner.execute(
				() -> {
					todoSwingView.showTagTasks(Collections.singletonList(task));
					todoSwingView.taskRemovedFromTag(task);
				});

		// Verify phase
		assertThat(tagsPanel.list("assignedTasksList").contents())
		.isEmpty();
	}

	@Test @GUITest
	public void testTaskRemovedFromTagRemovesErrorMessage() {
		// Setup phase
		getTagsPanel();

		Task task = new Task("1", "Buy groceries");

		// Exercise phase
		GuiActionRunner.execute(
				() -> {
					todoSwingView.showTagTasks(Collections.singletonList(task));
					todoSwingView.tagError("This is an error message");
					todoSwingView.taskRemovedFromTag(task);
				});

		// Verify phase
		assertThat(tagsPanel.label("tagsErrorLabel").text())
		.isEqualTo(" ");
	}

	private void getTagsPanel() {
		JTabbedPaneFixture tabPanel = contentPanel.tabbedPane("tabbedPane");
		tabPanel.selectTab("Tags");
		tagsPanel = contentPanel.panel("tagsPanel");
	}
}