package it.unifi.simpletodoapp.service;

import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.AdditionalAnswers.answer;


import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import it.unifi.simpletodoapp.model.Task;
import it.unifi.simpletodoapp.repository.TaskRepository;
import it.unifi.simpletodoapp.repository.TaskTransactionCode;
import it.unifi.simpletodoapp.repository.TransactionManager;

public class TodoServiceTest {
	@Mock
	private TransactionManager transactionManager;
	
	@Mock
	private TaskRepository taskRepository;
	
	@InjectMocks
	private TodoService todoService;
	
	@Before
	public void setUp() {
		/* Initializes all annotated fields (transactionManager, taskRepository)
		and injects the TransactionManager inside the TodoService */
		MockitoAnnotations.initMocks(this);
		
		when(transactionManager.doTaskTransaction(any()))
			.thenAnswer(answer((TaskTransactionCode<?> code) -> code.apply(taskRepository)));
	}
	
	@Test
	public void testAllTasksRetrieval() {
		// Setup phase
		List<Task> tasks = Arrays.asList(
				new Task("1", "Buy groceries"),
				new Task("2", "Start using TDD"));
		when(taskRepository.findAll())
			.thenReturn(tasks);
		
		// Exercise phase
		List<Task> retrievedTasks = todoService.getAllTasks();
		
		// Verify
		InOrder inOrder = inOrder(transactionManager, taskRepository);
		inOrder.verify(transactionManager).doTaskTransaction(any());
		inOrder.verify(taskRepository).findAll();
		inOrder.verifyNoMoreInteractions();
		
		assertEquals(tasks, retrievedTasks);
	}
	
	@Test
	public void testFindTaskById() {
		// Setup phase
		Task task = new Task("1", "Buy groceries");
		when(taskRepository.findById(task.getId()))
			.thenReturn(task);
		
		// Exercise phase
		Task retrievedTask = todoService.findTaskById(task.getId());
		
		// Verify
		InOrder inOrder = inOrder(transactionManager, taskRepository);
		inOrder.verify(transactionManager).doTaskTransaction(any());
		inOrder.verify(taskRepository).findById(task.getId());
		inOrder.verifyNoMoreInteractions();
		assertEquals(task, retrievedTask);
	}
	
	@Test
	public void testSaveTask() {
		// Setup phase
		Task task = new Task("1", "Buy groceries");
		
		// Exercise phase
		todoService.saveTask(task);
		
		// Verify
		InOrder inOrder = inOrder(transactionManager, taskRepository);
		inOrder.verify(transactionManager).doTaskTransaction(any());
		inOrder.verify(taskRepository).save(task);
		inOrder.verifyNoMoreInteractions();
	}
	
	@Test
	public void testDeleteTask() {
		// Setup phase
		Task task = new Task("1", "Buy groceries");
		
		// Exercise phase
		todoService.deleteTask(task);
		
		// Verify
		InOrder inOrder = inOrder(transactionManager, taskRepository);
		inOrder.verify(transactionManager).doTaskTransaction(any());
		inOrder.verify(taskRepository).delete(task);
		inOrder.verifyNoMoreInteractions();
	}
}
