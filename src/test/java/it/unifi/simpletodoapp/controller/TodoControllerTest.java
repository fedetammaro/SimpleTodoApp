package it.unifi.simpletodoapp.controller;

import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import it.unifi.simpletodoapp.model.Tag;
import it.unifi.simpletodoapp.model.Task;
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
		// Setup phase
		List<Task> tasks = new ArrayList<>();
		when(todoService.getAllTasks()).thenReturn(tasks);
		
		// Exercise phase
		todoController.getAllTasks();
		
		// Verify phase
		verify(todoService).getAllTasks();
		verify(todoView).showAllTasks(tasks);
	}
	
	@Test
	public void testTaskAdditionWithUniqueId() {
		// Setup phase
		Task task = new Task("1", "Buy groceries");
		when(todoService.findTaskById(task.getId()))
			.thenReturn(null);
		
		// Exercise phase
		todoController.addTask(task);
		
		// Verify phase: we also verify the order of the invocations
		InOrder inOrder = inOrder(todoService, todoView);
		inOrder.verify(todoService).saveTask(task);
		inOrder.verify(todoView).taskAdded(task);
		inOrder.verifyNoMoreInteractions();
	}
	
	@Test
	public void testTaskAdditionWithDuplicatedId() {
		// Setup phase
		Task task = new Task("1", "Buy groceries");
		Task duplicatedTask = new Task("1", "Buy even more groceries");
		when(todoService.findTaskById(duplicatedTask.getId()))
			.thenReturn(task);
		
		// Exercise phase
		todoController.addTask(duplicatedTask);
		
		// Verify phase: we also verify the order of the invocations
		InOrder inOrder = inOrder(todoService, todoView);
		inOrder.verify(todoService).findTaskById(duplicatedTask.getId());
		inOrder.verify(todoService, never()).saveTask(any());
		inOrder.verify(todoView).taskError("Cannot add task with duplicated ID " + duplicatedTask.getId());
		inOrder.verifyNoMoreInteractions();
	}
	
	@Test
	public void testSuccessfulTaskDeletion() {
		// Setup phase
		Task task = new Task("1", "Buy groceries");
		when(todoService.findTaskById(task.getId()))
			.thenReturn(task);
		
		// Exercise phase
		todoController.deleteTask(task);
		
		// Verify phase: we also verify the order of the invocations
		InOrder inOrder = inOrder(todoService, todoView);
		inOrder.verify(todoService).deleteTask(task);
		inOrder.verify(todoView).taskDeleted(task);
		inOrder.verifyNoMoreInteractions();
	}
	
	@Test
	public void testTaskDeletionWhenTaskAlreadyDeleted() {
		// Setup phase
		Task task = new Task("1", "Buy groceries");
		when(todoService.findTaskById(task.getId()))
			.thenReturn(null);
		
		// Exercise phase
		todoController.deleteTask(task);
		
		// Verify phase: we also verify the order of the invocations
		InOrder inOrder = inOrder(todoService, todoView);
		inOrder.verify(todoService).findTaskById(task.getId());
		inOrder.verify(todoService, never()).deleteTask(any());
		inOrder.verify(todoView).taskError("Task with ID " + task.getId() + " has already been removed");
		inOrder.verifyNoMoreInteractions();
	}
	
	@Test
	public void testAllTagsRetrieval() {
		// Setup phase
		List<Tag> tags = new ArrayList<>();
		when(todoService.getAllTags())
			.thenReturn(tags);
		
		// Exercise phase
		todoController.getAllTags();
		
		// Verify phase
		verify(todoService).getAllTags();
		verify(todoView).showAllTags(tags);
	}
	
	@Test
	public void testTagAdditionWithUniqueIdAndName() {
		// Setup phase
		Tag tag = new Tag("1", "Work");
		when(todoService.findTagById(tag.getId()))
			.thenReturn(null);
		
		// Exercise phase
		todoController.addTag(tag);
		
		// Verify phase: we also verify the order of the invocations
		InOrder inOrder = inOrder(todoService, todoView);
		inOrder.verify(todoService).saveTag(tag);
		inOrder.verify(todoView).tagAdded(tag);
		inOrder.verifyNoMoreInteractions();
	}
	
	@Test
	public void testTagAdditionWithExistingId() {
		// Setup phase
		Tag tag = new Tag("1", "Work");
		Tag duplicatedTag = new Tag("1", "Free time");
		when(todoService.findTagById(duplicatedTag.getId()))
			.thenReturn(tag);
		
		// Exercise phase
		todoController.addTag(duplicatedTag);
		
		// Verify phase: we also verify the order of the invocations
		InOrder inOrder = inOrder(todoService, todoView);
		inOrder.verify(todoService).findTagById(duplicatedTag.getId());
		inOrder.verify(todoService, never()).saveTag(any());
		inOrder.verify(todoView).tagError("Cannot add tag with duplicated ID " + duplicatedTag.getId());
		inOrder.verifyNoMoreInteractions();
	}
	
	@Test
	public void testTagAdditionWithExistingName() {
		// Setup phase
		Tag tag = new Tag("1", "Work");
		Tag duplicatedTag = new Tag("2", "Work");
		when(todoService.findTagById(duplicatedTag.getId()))
			.thenReturn(null);
		when(todoService.getAllTags())
			.thenReturn(Collections.singletonList(tag));
		
		// Exercise phase
		todoController.addTag(duplicatedTag);
		
		// Verify phase: we also verify the order of the invocations
		InOrder inOrder = inOrder(todoService, todoView);
		inOrder.verify(todoService).findTagById(duplicatedTag.getId());
		inOrder.verify(todoService).getAllTags();
		inOrder.verify(todoService, never()).saveTag(any());
		inOrder.verify(todoView).tagError("Cannot add tag with duplicated name \"" + duplicatedTag.getName() + "\"");
		inOrder.verifyNoMoreInteractions();
	}
	
	@Test
	public void testTagAdditionWithUniqueIdAndNameWithAlreadyPresentTags() {
		// Setup phase
		Tag tag = new Tag("3", "Housekeeping");
		when(todoService.findTagById(tag.getId()))
			.thenReturn(null);
		when(todoService.getAllTags())
			.thenReturn(Arrays.asList(
					new Tag("1", "Work"),
					new Tag("2", "Sport")
					));
		
		// Exercise phase
		todoController.addTag(tag);
		
		// Verify phase
		verify(todoService).saveTag(tag);
		verify(todoView).tagAdded(tag);
	}
	
	@Test
	public void testSuccessfulTagDeletion() {
		// Setup phase
		Tag tag = new Tag("1", "Work");
		when(todoService.findTagById(tag.getId()))
			.thenReturn(tag);
		
		// Exercise phase
		todoController.removeTag(tag);
		
		// Verify phase: we also verify the order of the invocations
		InOrder inOrder = inOrder(todoService, todoView);
		inOrder.verify(todoService).deleteTag(tag);
		inOrder.verify(todoView).tagRemoved(tag);
	}
	
	@Test
	public void testTagDeletionWhenAlreadyDeleted() {
		// Setup phase
		Tag tag = new Tag("1", "Work");
		when(todoService.findTagById(tag.getId()))
			.thenReturn(null);
		
		// Exercise phase
		todoController.removeTag(tag);
		
		// Verify phase: we also verify the order of the invocations
		InOrder inOrder = inOrder(todoService, todoView);
		inOrder.verify(todoService).findTagById(tag.getId());
		inOrder.verify(todoService, never()).deleteTag(any());
		inOrder.verify(todoView).tagError("Tag with ID " + tag.getId() + 
				" has already been removed");
		inOrder.verifyNoMoreInteractions();
	}
	
	@Test
	public void testSuccessfulTagAdditionToTask() {
		// Setup phase
		Task task = new Task("1", "Start using TDD");
		Tag tag = new Tag("1", "Work");
		when(todoService.findTaskById(task.getId()))
			.thenReturn(task);
		when(todoService.findTagById(tag.getId()))
			.thenReturn(tag);
		when(todoService.findTagsByTaskId(task.getId()))
			.thenReturn(Collections.<String>emptyList());
		
		// Exercise phase
		todoController.addTagToTask(task, tag);
		
		// Verify phase
		verify(todoService).addTagToTask(task.getId(), tag.getId());
		verify(todoView).showTaskTags(Collections.<Tag>emptyList());
	}
	
	@Test
	public void testSuccessfulTagAdditionToTaskWithOtherTags() {
		// Setup phase
		Task task = new Task("1", "Start using TDD");
		Tag previousTag = new Tag ("1", "Work");
		Tag tag = new Tag("2", "Important");
		when(todoService.findTaskById(task.getId()))
			.thenReturn(task);
		when(todoService.findTagById(tag.getId()))
			.thenReturn(tag);
		when(todoService.findTagById(previousTag.getId()))
			.thenReturn(previousTag);
		when(todoService.findTagsByTaskId(task.getId()))
			.thenReturn(Collections.singletonList(previousTag.getId()))
			.thenReturn(Arrays.asList(previousTag.getId(), tag.getId()));
		
		// Exercise phase
		todoController.addTagToTask(task, tag);

		// Verify phase: we also verify the order of the invocations
		InOrder inOrder = inOrder(todoService, todoView);
		inOrder.verify(todoService).findTagsByTaskId(task.getId());
		inOrder.verify(todoService).addTagToTask(task.getId(), tag.getId());
		inOrder.verify(todoService).findTagsByTaskId(task.getId());
		inOrder.verify(todoView).showTaskTags(Arrays.asList(previousTag, tag));
		inOrder.verifyNoMoreInteractions();
	}
	
	@Test
	public void testDuplicatedTagAdditionToTask() {
		// Setup phase
		Task task = new Task("1", "Start using TDD");
		Tag tag = new Tag("1", "Work");
		when(todoService.findTaskById(task.getId()))
			.thenReturn(task);
		when(todoService.findTagById(tag.getId()))
			.thenReturn(tag);
		when(todoService.findTagsByTaskId(task.getId()))
			.thenReturn(Collections.singletonList(tag.getId()));
		
		// Exercise phase
		todoController.addTagToTask(task, tag);
		
		// Verify phase: we also verify the order of the invocations
		InOrder inOrder = inOrder(todoService, todoView);
		inOrder.verify(todoService).findTagsByTaskId(task.getId());
		inOrder.verify(todoService, never()).addTagToTask(any(), any());
		inOrder.verify(todoView).tagError("Tag with ID " + tag.getId() + 
				" is already assigned to task with ID " + task.getId());
		inOrder.verifyNoMoreInteractions();
	}
	
	@Test
	public void testTagAdditionToTaskWhenTaskNonExistent() {
		// Setup phase
		Task task = new Task("1", "Start using TDD");
		Tag tag = new Tag("1", "Work");
		when(todoService.findTaskById(task.getId()))
			.thenReturn(null);
		
		// Exercise phase
		todoController.addTagToTask(task, tag);
		
		// Verify phase: we also verify the order of the invocations
		InOrder inOrder = inOrder(todoService, todoView);
		inOrder.verify(todoService).findTaskById(task.getId());
		inOrder.verify(todoService, never()).addTagToTask(any(), any());
		inOrder.verify(todoView).taskError("No task with ID " + task.getId());
		inOrder.verifyNoMoreInteractions();
	}
	
	@Test
	public void testTagAdditionToTaskWhenTagNonExistent() {
		// Setup phase
		Task task = new Task("1", "Start using TDD");
		Tag tag = new Tag("1", "Work");
		when(todoService.findTaskById(task.getId()))
			.thenReturn(task);
		when(todoService.findTagById(tag.getId()))
			.thenReturn(null);
				
		// Exercise phase
		todoController.addTagToTask(task, tag);
				
		// Verify phase: we also verify the order of the invocations
		InOrder inOrder = inOrder(todoService, todoView);
		inOrder.verify(todoService).findTagById(tag.getId());
		inOrder.verify(todoService, never()).addTagToTask(any(), any());
		inOrder.verify(todoView).tagError("No tag with ID " + tag.getId());
		inOrder.verifyNoMoreInteractions();	
	}
	
	@Test
	public void testTagRemovalFromTask() {
		// Setup phase
		Task task = new Task("1", "Start using TDD");
		Tag tag = new Tag("1", "Work");
		when(todoService.findTaskById(task.getId()))
			.thenReturn(task);
		when(todoService.findTagById(tag.getId()))
			.thenReturn(tag);
		when(todoService.findTagsByTaskId(task.getId()))
			.thenReturn(Collections.singletonList(tag.getId()))
			.thenReturn(Collections.<String>emptyList());
		
		// Exercise phase
		todoController.removeTagFromTask(task, tag);
		
		// Verify phase
		verify(todoService).removeTagFromTask(task.getId(), tag.getId());
		verify(todoView).showTaskTags(Collections.<Tag>emptyList());
	}
	
	@Test
	public void testTagRemovalFromTaskWithOtherTags() {
		// Setup phase
		Task task = new Task("1", "Start using TDD");
		Tag previousTag = new Tag("1", "Work");
		Tag tag = new Tag("2", "Important");
		List<String> previousTags = Arrays.asList(previousTag.getId(), tag.getId());
		when(todoService.findTaskById(task.getId()))
			.thenReturn(task);
		when(todoService.findTagById(tag.getId()))
			.thenReturn(tag);
		when(todoService.findTagById(previousTag.getId()))
			.thenReturn(previousTag);
		when(todoService.findTagsByTaskId(task.getId()))
			.thenReturn(previousTags)
			.thenReturn(Collections.singletonList(previousTag.getId()));
				
		// Exercise phase
		todoController.removeTagFromTask(task, tag);
		
		// Verify phase: we also verify the order of the invocations
		InOrder inOrder = inOrder(todoService, todoView);
		inOrder.verify(todoService).findTagsByTaskId(task.getId());
		inOrder.verify(todoService).removeTagFromTask(task.getId(), tag.getId());
		inOrder.verify(todoService).findTagById(task.getId());
		inOrder.verify(todoView).showTaskTags(Collections.singletonList(previousTag));
		inOrder.verifyNoMoreInteractions();
	}
	
	@Test
	public void testTagRemovalFromTaskWhenTagNotAssignedToTask() {
		// Setup phase
		Task task = new Task("1", "Start using TDD");
		Tag previousTag = new Tag("1", "Work");
		Tag tag = new Tag("2", "Important");
		when(todoService.findTaskById(task.getId()))
			.thenReturn(task);
		when(todoService.findTagById(tag.getId()))
			.thenReturn(tag);
		when(todoService.findTagsByTaskId(task.getId()))
			.thenReturn(Collections.singletonList(previousTag.getId()));
		
		// Exercise phase
		todoController.removeTagFromTask(task, tag);
		
		// Verify phase: we also verify the order of the invocations
		InOrder inOrder = inOrder(todoService, todoView);
		inOrder.verify(todoService).findTagsByTaskId(task.getId());
		inOrder.verify(todoService, never()).removeTagFromTask(any(), any());
		inOrder.verify(todoView).tagError("No tag with ID " + tag.getId() +
				" assigned to task with ID " + task.getId());
		inOrder.verifyNoMoreInteractions();
	}
	
	@Test
	public void testTagRemovalFromTaskWhenTaskNonExistent() {
		// Setup phase
		Task task = new Task("1", "Start using TDD");
		Tag tag = new Tag("1", "Work");
		when(todoService.findTaskById(task.getId()))
			.thenReturn(null);
		
		// Exercise phase
		todoController.removeTagFromTask(task, tag);
		
		// Verify phase: we also verify the order of the invocations
		InOrder inOrder = inOrder(todoService, todoView);
		inOrder.verify(todoService).findTaskById(task.getId());
		inOrder.verify(todoService, never()).removeTagFromTask(any(), any());
		inOrder.verify(todoView).taskError("No task with ID " + task.getId());
		inOrder.verifyNoMoreInteractions();
	}
	
	@Test
	public void testTagRemovalFromTaskWhenTagNonExistent() {
		// Setup phase
		Task task = new Task("1", "Start using TDD");
		Tag tag = new Tag("1", "Work");
		when(todoService.findTaskById(task.getId()))
			.thenReturn(task);
		when(todoService.findTagById(tag.getId()))
			.thenReturn(null);
		
		// Exercise phase
		todoController.removeTagFromTask(task, tag);
		
		// Verify phase: we also verify the order of the invocations
		InOrder inOrder = inOrder(todoService, todoView);
		inOrder.verify(todoService).findTagById(tag.getId());
		inOrder.verify(todoService, never()).removeTagFromTask(any(), any());
		inOrder.verify(todoView).tagError("No tag with ID " + tag.getId());
		inOrder.verifyNoMoreInteractions();
	}
	
	@Test
	public void testRetrieveTagsAssociatedToTask() {
		// Setup phase
		Task task = new Task("1", "Start using TDD");
		when(todoService.findTaskById(task.getId()))
			.thenReturn(task);
		when(todoService.findTagsByTaskId(task.getId()))
			.thenReturn(Collections.<String>emptyList());
		
		// Exercise phase
		todoController.getTagsByTask(task);
		
		// Verify phase
		verify(todoService).findTagsByTaskId(task.getId());
		verify(todoView).showTaskTags(Collections.<Tag>emptyList());
	}
	
	@Test
	public void testRetrieveTagsAssociatedToNonExistentTask() {
		// Setup phase
		Task task = new Task("1", "Start using TDD");
		when(todoService.findTaskById(task.getId()))
			.thenReturn(null);
		
		// Exercise phase
		todoController.getTagsByTask(task);
		
		// Verify phase: we also verify the order of the invocations
		InOrder inOrder = inOrder(todoService, todoView);
		inOrder.verify(todoService).findTaskById(task.getId());
		inOrder.verify(todoView, never()).showTaskTags(any());
		inOrder.verify(todoView).taskError("No task with ID " + task.getId());
		inOrder.verifyNoMoreInteractions();
	}
	
	@Test
	public void testRetrieveTasksAssociatedToTag() {
		// Setup phase
		Tag tag = new Tag("1", "Work");
		when(todoService.findTagById(tag.getId()))
			.thenReturn(tag);
		when(todoService.findTasksByTagId(tag.getId()))
			.thenReturn(Collections.<Task>emptyList());
		
		// Exercise phase
		todoController.getTasksByTag(tag);
		
		// Verify
		verify(todoService).findTasksByTagId(tag.getId());
		verify(todoView).showTagTasks(Collections.<Task>emptyList());
	}
	
	@Test
	public void testRetrieveTasksAssociatedToNonExistentTag() {
		Tag tag = new Tag("1", "Work");
		when(todoService.findTagById(tag.getId()))
			.thenReturn(null);
		
		// Exercise phase
		todoController.getTasksByTag(tag);
		
		// Verify: we also verify the order of the invocations
		InOrder inOrder = inOrder(todoService, todoView);
		inOrder.verify(todoService).findTagById(tag.getId());
		inOrder.verify(todoView, never()).showTagTasks(any());
		inOrder.verify(todoView).tagError("No tag with ID " + tag.getId());
		inOrder.verifyNoMoreInteractions();
	}
}
