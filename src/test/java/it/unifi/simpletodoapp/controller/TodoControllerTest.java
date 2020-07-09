package it.unifi.simpletodoapp.controller;

import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
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
}
