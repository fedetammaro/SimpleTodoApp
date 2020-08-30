package it.unifi.simpletodoapp.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import it.unifi.simpletodoapp.model.Tag;
import it.unifi.simpletodoapp.model.Task;
import it.unifi.simpletodoapp.repository.TagRepositoryException;
import it.unifi.simpletodoapp.repository.TaskRepositoryException;
import it.unifi.simpletodoapp.service.TodoService;
import it.unifi.simpletodoapp.view.TodoView;

public class TodoControllerTest {
	@Mock
	private TodoView todoView;

	@Mock
	private TodoService todoService;

	@InjectMocks
	private TodoController todoController;

	@Before
	public void setUp() {
		/* Initializes all annotated fields (todoView, todoService, todoController)
		and injects them inside the TodoController */

		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testAllTasksRetrieval() {
		// Exercise phase
		todoController.getAllTasks();

		// Verify phase: we also verify the order of the invocation
		InOrder inOrder = inOrder(todoService, todoView);
		inOrder.verify(todoService).getAllTasks();
		inOrder.verify(todoView).showAllTasks(Collections.emptyList());
		inOrder.verifyNoMoreInteractions();
	}

	@Test
	public void testSuccessfulTaskAddition() {
		// Setup phase
		Task task = new Task("1", "Buy groceries");

		// Exercise phase
		todoController.addTask(task);

		// Verify phase: we also verify the order of the invocations
		InOrder inOrder = inOrder(todoService, todoView);
		inOrder.verify(todoService).saveTask(task);
		inOrder.verify(todoView).taskAdded(task);
		inOrder.verifyNoMoreInteractions();
	}

	@Test
	public void testTaskAdditionException() {
		// Setup phase
		Task task = new Task("1", "Buy groceries");
		doThrow(new TaskRepositoryException("Cannot add task with duplicated ID " + task.getId()))
		.when(todoService)
		.saveTask(task);

		// Exercise phase
		todoController.addTask(task);

		// Verify phase: we also verify the order of the invocations
		InOrder inOrder = inOrder(todoService, todoView);
		inOrder.verify(todoService).saveTask(task);
		inOrder.verify(todoView, never()).taskAdded(any());
		inOrder.verify(todoView).taskError(
				"Cannot add task with duplicated ID " + task.getId());
		inOrder.verifyNoMoreInteractions();
	}

	@Test
	public void testSuccessfulTaskDeletion() {
		// Setup phase
		Task task = new Task("1", "Buy groceries");

		// Exercise phase
		todoController.deleteTask(task);

		// Verify phase: we also verify the order of the invocations
		InOrder inOrder = inOrder(todoService, todoView);
		inOrder.verify(todoService).deleteTask(task);
		inOrder.verify(todoView).taskDeleted(task);
		inOrder.verifyNoMoreInteractions();
	}

	@Test
	public void testTaskDeletionException() {
		// Setup phase
		Task task = new Task("1", "Buy groceries");
		doThrow(new TaskRepositoryException("Task with ID " + task.getId() + " has already been deleted"))
		.when(todoService)
		.deleteTask(task);

		// Exercise phase
		todoController.deleteTask(task);

		// Verify phase: we also verify the order of the invocations
		InOrder inOrder = inOrder(todoService, todoView);
		inOrder.verify(todoService).deleteTask(task);
		inOrder.verify(todoView, never()).taskDeleted(any());
		inOrder.verify(todoView).taskError(
				"Task with ID " + task.getId() + " has already been deleted");
		inOrder.verifyNoMoreInteractions();
	}

	@Test
	public void testAllTagsRetrieval() {
		// Exercise phase
		todoController.getAllTags();

		// Verify phase: we also verify the order of the invocations
		InOrder inOrder = inOrder(todoService, todoView);
		inOrder.verify(todoService).getAllTags();
		inOrder.verify(todoView).showAllTags(Collections.emptyList());
		inOrder.verifyNoMoreInteractions();
	}

	@Test
	public void testSuccessfulTagAddition() {
		// Setup phase
		Tag tag = new Tag("1", "Work");

		// Exercise phase
		todoController.addTag(tag);

		// Verify phase: we also verify the order of the invocations
		InOrder inOrder = inOrder(todoService, todoView);
		inOrder.verify(todoService).saveTag(tag);
		inOrder.verify(todoView).tagAdded(tag);
		inOrder.verifyNoMoreInteractions();
	}

	@Test
	public void testTagAdditionException() {
		// Setup phase
		Tag tag = new Tag("1", "Work");
		doThrow(new TagRepositoryException("Cannot add tag with duplicated ID " + tag.getId()))
		.when(todoService)
		.saveTag(tag);

		// Exercise phase
		todoController.addTag(tag);

		// Verify phase: we also verify the order of the invocations
		InOrder inOrder = inOrder(todoService, todoView);
		inOrder.verify(todoService).saveTag(tag);
		inOrder.verify(todoView, never()).tagAdded(any());
		inOrder.verify(todoView).tagError(
				"Cannot add tag with duplicated ID " + tag.getId());
		inOrder.verifyNoMoreInteractions();
	}

	@Test
	public void testSuccessfulTagDeletion() {
		// Setup phase
		Tag tag = new Tag("1", "Work");

		// Exercise phase
		todoController.deleteTag(tag);

		// Verify phase: we also verify the order of the invocations
		InOrder inOrder = inOrder(todoService, todoView);
		inOrder.verify(todoService).deleteTag(tag);
		inOrder.verify(todoView).tagDeleted(tag);
		inOrder.verifyNoMoreInteractions();
	}

	@Test
	public void testTagDeletionException() {
		// Setup phase
		Tag tag = new Tag("1", "Work");
		doThrow(new TagRepositoryException("Tag with ID " + tag.getId() + " has already been deleted"))
		.when(todoService)
		.deleteTag(tag);

		// Exercise phase
		todoController.deleteTag(tag);

		// Verify phase: we also verify the order of the invocations
		InOrder inOrder = inOrder(todoService, todoView);
		inOrder.verify(todoService).deleteTag(tag);
		inOrder.verify(todoView, never()).tagDeleted(any());
		inOrder.verify(todoView).tagError(
				"Tag with ID " + tag.getId() + " has already been deleted");
		inOrder.verifyNoMoreInteractions();
	}

	@Test
	public void testSuccessfulTagAdditionToTask() {
		// Setup phase
		Task task = new Task("1", "Start using TDD");
		Tag tag = new Tag("1", "Work");

		// Exercise phase
		todoController.addTagToTask(task, tag);

		// Verify phase: we also verify the order of the invocations
		InOrder inOrder = inOrder(todoService, todoView);
		inOrder.verify(todoService).addTagToTask(task.getId(), tag.getId());
		inOrder.verify(todoView).tagAddedToTask(tag);
		inOrder.verifyNoMoreInteractions();
	}

	@Test
	public void testTagAdditionToTaskExceptionTask() {
		// Setup phase
		Task task = new Task("1", "Start using TDD");
		Tag tag = new Tag("1", "Work");
		doThrow(new TaskRepositoryException("No task with ID " + task.getId()))
		.when(todoService)
		.addTagToTask(task.getId(), tag.getId());

		// Exercise phase
		todoController.addTagToTask(task, tag);

		// Verify phase: we also verify the order of the invocations
		InOrder inOrder = inOrder(todoService, todoView);
		inOrder.verify(todoService).addTagToTask(task.getId(), tag.getId());
		inOrder.verify(todoView, never()).tagAddedToTask(any());
		inOrder.verify(todoView).taskError("No task with ID " + task.getId());
		inOrder.verifyNoMoreInteractions();
	}

	@Test
	public void testTagAdditionToTaskExceptionTag() {
		// Setup phase
		Task task = new Task("1", "Start using TDD");
		Tag tag = new Tag("1", "Work");
		doThrow(new TagRepositoryException("No tag with ID " + tag.getId()))
		.when(todoService)
		.addTagToTask(task.getId(), tag.getId());

		// Exercise phase
		todoController.addTagToTask(task, tag);

		// Verify phase: we also verify the order of the invocations
		InOrder inOrder = inOrder(todoService, todoView);
		inOrder.verify(todoService).addTagToTask(task.getId(), tag.getId());
		inOrder.verify(todoView, never()).tagAddedToTask(any());
		inOrder.verify(todoView).tagError("No tag with ID " + tag.getId());
		inOrder.verifyNoMoreInteractions();
	}

	@Test
	public void testSuccessfulTagRemovalFromTask() {
		// Setup phase
		Task task = new Task("1", "Start using TDD");
		Tag tag = new Tag("1", "Work");

		// Exercise phase
		todoController.removeTagFromTask(task, tag);

		// Verify phase: we also verify the order of the invocations
		InOrder inOrder = inOrder(todoService, todoView);
		inOrder.verify(todoService).removeTagFromTask(task.getId(), tag.getId());
		inOrder.verify(todoView).tagRemovedFromTask(tag);
		inOrder.verifyNoMoreInteractions();
	}

	@Test
	public void testTagRemovalFromTaskExceptionTask() {
		// Setup phase
		Task task = new Task("1", "Start using TDD");
		Tag tag = new Tag("1", "Work");
		doThrow(new TaskRepositoryException("No task with ID " + task.getId()))
		.when(todoService)
		.removeTagFromTask(task.getId(), tag.getId());

		// Exercise phase
		todoController.removeTagFromTask(task, tag);

		// Verify phase: we also verify the order of the invocations
		InOrder inOrder = inOrder(todoService, todoView);
		inOrder.verify(todoService).removeTagFromTask(task.getId(), tag.getId());
		inOrder.verify(todoView, never()).tagRemovedFromTask(any());
		inOrder.verify(todoView).taskError("No task with ID " + task.getId());
		inOrder.verifyNoMoreInteractions();
	}

	@Test
	public void testTagRemovalFromTaskExceptionTag() {
		// Setup phase
		Task task = new Task("1", "Start using TDD");
		Tag tag = new Tag("1", "Work");
		doThrow(new TagRepositoryException("No tag with ID " + tag.getId()))
		.when(todoService)
		.removeTagFromTask(task.getId(), tag.getId());

		// Exercise phase
		todoController.removeTagFromTask(task, tag);

		// Verify phase: we also verify the order of the invocations
		InOrder inOrder = inOrder(todoService, todoView);
		inOrder.verify(todoService).removeTagFromTask(task.getId(), tag.getId());
		inOrder.verify(todoView, never()).tagRemovedFromTask(any());
		inOrder.verify(todoView).tagError("No tag with ID " + tag.getId());
		inOrder.verifyNoMoreInteractions();
	}

	@Test
	public void testSuccessfulTaskRemovalFromTag() {
		// Setup phase
		Task task = new Task("1", "Start using TDD");
		Tag tag = new Tag("1", "Work");

		// Exercise phase
		todoController.removeTaskFromTag(tag, task);

		// Verify phase: we also verify the order of the invocations
		InOrder inOrder = inOrder(todoService, todoView);
		inOrder.verify(todoService).removeTaskFromTag(tag.getId(), task.getId());
		inOrder.verify(todoView).taskRemovedFromTag(task);
		inOrder.verifyNoMoreInteractions();
	}

	@Test
	public void testTaskRemovalFromTagExceptionTask() {
		// Setup phase
		Task task = new Task("1", "Start using TDD");
		Tag tag = new Tag("1", "Work");
		doThrow(new TaskRepositoryException("No task with ID " + task.getId()))
		.when(todoService)
		.removeTaskFromTag(tag.getId(), task.getId());

		// Exercise phase
		todoController.removeTaskFromTag(tag, task);

		// Verify phase: we also verify the order of the invocations
		InOrder inOrder = inOrder(todoService, todoView);
		inOrder.verify(todoService).removeTaskFromTag(tag.getId(), task.getId());
		inOrder.verify(todoView, never()).taskRemovedFromTag(any());
		inOrder.verify(todoView).taskError("No task with ID " + task.getId());
		inOrder.verifyNoMoreInteractions();
	}

	@Test
	public void testTaskRemovalFromTagExceptionTag() {
		// Setup phase
		Task task = new Task("1", "Start using TDD");
		Tag tag = new Tag("1", "Work");
		doThrow(new TagRepositoryException("No tag with ID " + tag.getId()))
		.when(todoService)
		.removeTaskFromTag(tag.getId(), task.getId());

		// Exercise phase
		todoController.removeTaskFromTag(tag, task);

		// Verify phase: we also verify the order of the invocations
		InOrder inOrder = inOrder(todoService, todoView);
		inOrder.verify(todoService).removeTaskFromTag(tag.getId(), task.getId());
		inOrder.verify(todoView, never()).tagRemovedFromTask(any());
		inOrder.verify(todoView).tagError("No tag with ID " + tag.getId());
		inOrder.verifyNoMoreInteractions();
	}

	@Test
	public void testRetrieveTagsAssociatedToTask() {
		// Setup phase
		Task task = new Task("1", "Start using TDD");
		when(todoService.findTagsByTaskId(task.getId()))
		.thenReturn(Collections.singletonList("1"));
		when(todoService.findTagById("1"))
		.thenReturn(new Tag("1", "Work"));

		// Exercise phase
		todoController.getTagsByTask(task);

		// Verify phase
		InOrder inOrder = inOrder(todoService, todoView);
		inOrder.verify(todoService).findTagsByTaskId(task.getId());
		inOrder.verify(todoView).showTaskTags(Collections.singletonList(new Tag("1", "Work")));
		inOrder.verifyNoMoreInteractions();
	}

	@Test
	public void testRetrieveTagsAssociatedToTaskException() {
		// Setup phase
		Task task = new Task("1", "Start using TDD");
		doThrow(new TaskRepositoryException("No task with ID " + task.getId()))
		.when(todoService)
		.findTagsByTaskId(task.getId());

		// Exercise phase
		todoController.getTagsByTask(task);

		// Verify phase
		InOrder inOrder = inOrder(todoService, todoView);
		inOrder.verify(todoService).findTagsByTaskId(task.getId());
		inOrder.verify(todoView, never()).showTaskTags(any());
		inOrder.verify(todoView).taskError("No task with ID " + task.getId());
		inOrder.verifyNoMoreInteractions();
	}

	@Test
	public void testRetrieveTasksAssociatedToTag() {
		// Setup phase
		Tag tag = new Tag("1", "Work");
		when(todoService.findTasksByTagId(tag.getId()))
		.thenReturn(Collections.singletonList("1"));
		when(todoService.findTaskById("1"))
		.thenReturn(new Task("1", "Start using TDD"));

		// Exercise phase
		todoController.getTasksByTag(tag);

		// Verify phase
		InOrder inOrder = inOrder(todoService, todoView);
		inOrder.verify(todoService).findTasksByTagId(tag.getId());
		inOrder.verify(todoView).showTagTasks(
				Collections.singletonList(new Task("1", "Start using TDD"))
				);
		inOrder.verifyNoMoreInteractions();
	}

	@Test
	public void testRetrieveTasksAssociatedToTagException() {
		// Setup phase
		Tag tag = new Tag("1", "Work");
		doThrow(new TagRepositoryException("No tag with ID " + tag.getId()))
		.when(todoService)
		.findTasksByTagId(tag.getId());

		// Exercise phase
		todoController.getTasksByTag(tag);

		// Verify phase
		InOrder inOrder = inOrder(todoService, todoView);
		inOrder.verify(todoService).findTasksByTagId(tag.getId());
		inOrder.verify(todoView, never()).showTagTasks(any());
		inOrder.verify(todoView).tagError("No tag with ID " + tag.getId());
		inOrder.verifyNoMoreInteractions();
	}
}