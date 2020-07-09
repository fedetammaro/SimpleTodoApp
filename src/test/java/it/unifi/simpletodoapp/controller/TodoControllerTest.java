package it.unifi.simpletodoapp.controller;

import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

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
}
