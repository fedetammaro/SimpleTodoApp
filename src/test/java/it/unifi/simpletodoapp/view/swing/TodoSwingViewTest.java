package it.unifi.simpletodoapp.view.swing;

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
	}
	
	@Test @GUITest
	public void testAddButtonDisabledUntilBothFieldsAreNotEmpty() {
		JTextComponentFixture idField = tasksPanel.textBox("tasksIdTextField");
		JTextComponentFixture descriptionField = tasksPanel.textBox("tasksDescriptionTextField");
		JButtonFixture addButton = tasksPanel.button("btnAddTask");
		
		idField.enterText("1");
		addButton.requireDisabled();
		
		idField.setText("");
		descriptionField.setText("Buy groceries");
		addButton.requireDisabled();
	}
	
	@Test @GUITest
	public void testAddButtonDisabledWhenSpacesAreInput() {
		JTextComponentFixture idField = tasksPanel.textBox("tasksIdTextField");
		JTextComponentFixture descriptionField = tasksPanel.textBox("tasksDescriptionTextField");
		JButtonFixture addButton = tasksPanel.button("btnAddTask");
		
		idField.enterText(" ");
		descriptionField.enterText("Buy groceries");
		addButton.requireDisabled();
		
		idField.setText("");
		descriptionField.setText("");
		idField.enterText("1");
		descriptionField.enterText(" ");
		addButton.requireDisabled();
	}
	
	@Test @GUITest
	public void testAddButtonEnabledWhenBothFieldsAreNotEmpty() {
		JTextComponentFixture idField = tasksPanel.textBox("tasksIdTextField");
		JTextComponentFixture descriptionField = tasksPanel.textBox("tasksDescriptionTextField");
		JButtonFixture addButton = tasksPanel.button("btnAddTask");
		
		idField.enterText("1");
		descriptionField.enterText("Buy groceries");
		addButton.requireEnabled();
	}
	
	@Test @GUITest
	public void testAddButtonControllerInvocationWhenPressed() {
		JTextComponentFixture idField = tasksPanel.textBox("tasksIdTextField");
		JTextComponentFixture descriptionField = tasksPanel.textBox("tasksDescriptionTextField");
		JButtonFixture addButton = tasksPanel.button("btnAddTask");
		
		idField.enterText("1");
		descriptionField.enterText("Buy groceries");
		addButton.click();
		
		verify(todoController).addTask(new Task("1", "Buy groceries"));;
	}
}
