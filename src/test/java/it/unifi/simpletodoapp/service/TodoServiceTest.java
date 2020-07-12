package it.unifi.simpletodoapp.service;

import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.AdditionalAnswers.answer;


import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import it.unifi.simpletodoapp.model.Tag;
import it.unifi.simpletodoapp.model.Task;
import it.unifi.simpletodoapp.repository.CompositeTransactionCode;
import it.unifi.simpletodoapp.repository.TagRepository;
import it.unifi.simpletodoapp.repository.TaskRepository;
import it.unifi.simpletodoapp.repository.TagTransactionCode;
import it.unifi.simpletodoapp.repository.TaskTransactionCode;
import it.unifi.simpletodoapp.repository.TransactionManager;

public class TodoServiceTest {
	@Mock
	private TransactionManager transactionManager;

	@Mock
	private TaskRepository taskRepository;

	@Mock
	private TagRepository tagRepository;

	@InjectMocks
	private TodoService todoService;

	@Before
	public void setUp() {
		/* Initializes all annotated fields (transactionManager, taskRepository)
		and injects the TransactionManager inside the TodoService */
		MockitoAnnotations.initMocks(this);

		when(transactionManager.doTaskTransaction(any()))
			.thenAnswer(answer((TaskTransactionCode<?> code) -> code.apply(taskRepository)));
		when(transactionManager.doTagTransaction(any()))
			.thenAnswer(answer((TagTransactionCode<?> code) -> code.apply(tagRepository)));
		when(transactionManager.doCompositeTransaction(any()))
			.thenAnswer(answer((CompositeTransactionCode<?> code) -> code.apply(taskRepository, tagRepository)));
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

		// Verify phase
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

		// Verify phase
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

		// Verify phase
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

		// Verify phase
		InOrder inOrder = inOrder(transactionManager, taskRepository);
		inOrder.verify(transactionManager).doTaskTransaction(any());
		inOrder.verify(taskRepository).delete(task);
		inOrder.verifyNoMoreInteractions();
	}
	
	@Test
	public void testAllTagsRetrieval() {
		// Setup phase
		List<Tag> tags = Arrays.asList(
				new Tag("1", "Work"),
				new Tag("2", "Important"));
		when(tagRepository.findAll())
			.thenReturn(tags);

		// Exercise phase
		List<Tag> retrievedTags = todoService.getAllTags();

		// Verify phase
		InOrder inOrder = inOrder(transactionManager, tagRepository);
		inOrder.verify(transactionManager).doTagTransaction(any());
		inOrder.verify(tagRepository).findAll();
		inOrder.verifyNoMoreInteractions();

		assertEquals(tags, retrievedTags);
	}

	@Test
	public void testFindTagById() {
		// Setup phase
		Tag tag = new Tag("1", "Work");
		when(tagRepository.findById(tag.getId()))
			.thenReturn(tag);

		// Exercise phase
		Tag retrievedTag = todoService.findTagById(tag.getId());

		// Verify phase
		InOrder inOrder = inOrder(transactionManager, tagRepository);
		inOrder.verify(transactionManager).doTagTransaction(any());
		inOrder.verify(tagRepository).findById(tag.getId());
		inOrder.verifyNoMoreInteractions();

		assertEquals(tag, retrievedTag);
	}

	@Test
	public void testSaveTag() {
		// Setup phase
		Tag tag = new Tag("1", "Work");

		// Exercise phase
		todoService.saveTag(tag);

		// Verify phase
		InOrder inOrder = inOrder(transactionManager, tagRepository);
		inOrder.verify(transactionManager).doTagTransaction(any());
		inOrder.verify(tagRepository).save(tag);
		inOrder.verifyNoMoreInteractions();
	}
	
	@Test
	public void testDeleteTag() {
		// Setup phase
		Tag tag = new Tag("1", "Work");
		
		// Exercise phase
		todoService.deleteTag(tag);
		
		// Verify phase
		InOrder inOrder = inOrder(transactionManager, tagRepository);
		inOrder.verify(transactionManager).doTagTransaction(any());
		inOrder.verify(tagRepository).delete(tag);
		inOrder.verifyNoMoreInteractions();
	}
	
	@Test
	public void testFindTasksByTagId() {
		// Setup phase
		List<Task> tasks = Collections.singletonList(new Task("1", "Start using TDD"));
		Tag tag = new Tag("1", "Work");
		when(tagRepository.getTasksByTagId(tag.getId()))
			.thenReturn(tasks);
		
		// Exercise phase
		List<Task> retrievedTasks = todoService.findTasksByTagId(tag.getId());
		
		// Verify phase
		InOrder inOrder = inOrder(transactionManager, tagRepository);
		inOrder.verify(transactionManager).doTagTransaction(any());
		inOrder.verify(tagRepository).getTasksByTagId(tag.getId());
		inOrder.verifyNoMoreInteractions();
		
		assertEquals(tasks, retrievedTasks);
	}
	
	@Test
	public void testFindTagsByTaskId() {
		// Setup phase
		List<String> tags = Collections.singletonList("1");
		Task task = new Task("1", "Start using TDD");
		when(taskRepository.getTagsByTaskId(task.getId()))
			.thenReturn(Collections.singletonList(task.getId()));
		
		// Exercise phase
		List<String> retrievedTags = todoService.findTagsByTaskId(task.getId());
		
		// Verify phase
		InOrder inOrder = inOrder(transactionManager, taskRepository);
		inOrder.verify(transactionManager).doTaskTransaction(any());
		inOrder.verify(taskRepository).getTagsByTaskId(task.getId());
		inOrder.verifyNoMoreInteractions();
				
		assertEquals(tags, retrievedTags);
	}
	
	@Test
	public void testTagAdditionToTaskOrViceversa() {
		// Setup phase
		Task task = new Task("1", "Start using TDD");
		Tag tag = new Tag("1", "Work");
		
		// Exercise phase
		todoService.addTagToTask(task.getId(), tag.getId());
		
		// Verify phase
		InOrder inOrder = inOrder(transactionManager, taskRepository, tagRepository);
		inOrder.verify(transactionManager).doCompositeTransaction(any());
		inOrder.verify(taskRepository).addTagToTask(task.getId(), tag.getId());
		inOrder.verify(tagRepository).addTaskToTag(tag.getId(), task.getId());
		verifyNoMoreInteractions(transactionManager, taskRepository, tagRepository);
	}
	
	@Test
	public void testTagRemovalFromTaskOrViceversa() {
		// Setup phase
		Task task = new Task("1", "Start using TDD");
		Tag tag = new Tag("1", "Work");
				
		// Exercise phase
		todoService.removeTagFromTask(task.getId(), tag.getId());
				
		// Verify phase
		InOrder inOrder = inOrder(transactionManager, taskRepository, tagRepository);
		inOrder.verify(transactionManager).doCompositeTransaction(any());
		inOrder.verify(taskRepository).removeTagFromTask(task.getId(), tag.getId());
		inOrder.verify(tagRepository).removeTaskFromTag(tag.getId(), task.getId());
		verifyNoMoreInteractions(transactionManager, taskRepository, tagRepository);
	}
}
