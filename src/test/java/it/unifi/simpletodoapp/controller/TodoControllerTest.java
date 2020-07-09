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
		inOrder.verify(todoView).taskError("Cannot add task with duplicated ID " + duplicatedTask.getId());
		inOrder.verify(todoService, never()).saveTask(duplicatedTask);
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
		inOrder.verify(todoView).taskError("Task with ID " + task.getId() + " has already been removed");
		inOrder.verify(todoService, never()).deleteTask(task);
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
		inOrder.verify(todoView).tagError("Cannot add tag with duplicated ID " + duplicatedTag.getId());
		inOrder.verify(todoService, never()).saveTag(duplicatedTag);
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
		inOrder.verify(todoView).tagError("Cannot add tag with duplicated name \"" + duplicatedTag.getName() + "\"");
		inOrder.verify(todoService, never()).saveTag(duplicatedTag);
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
		inOrder.verify(todoView).tagError("Tag with ID " + tag.getId() + " has already been removed");
		inOrder.verify(todoService, never()).deleteTag(tag);
	}
}
