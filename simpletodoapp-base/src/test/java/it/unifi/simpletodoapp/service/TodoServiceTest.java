package it.unifi.simpletodoapp.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.AdditionalAnswers.answer;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.mongodb.client.ClientSession;

import it.unifi.simpletodoapp.model.Tag;
import it.unifi.simpletodoapp.model.Task;
import it.unifi.simpletodoapp.repository.CompositeTransactionCode;
import it.unifi.simpletodoapp.repository.TagTransactionCode;
import it.unifi.simpletodoapp.repository.TaskTransactionCode;
import it.unifi.simpletodoapp.repository.TransactionManager;
import it.unifi.simpletodoapp.repository.mongo.TagMongoRepository;
import it.unifi.simpletodoapp.repository.mongo.TaskMongoRepository;

public class TodoServiceTest {
	@Mock
	private TransactionManager transactionManager;

	@Mock
	private TaskMongoRepository taskRepository;

	@Mock
	private TagMongoRepository tagRepository;

	@InjectMocks
	private TodoService todoService;

	private ClientSession clientSession;

	@Before
	public void setUp() {
		/* Initializes all annotated fields (transactionManager, taskRepository)
		and injects the TransactionManager inside the TodoService */
		MockitoAnnotations.initMocks(this);

		/* Stub all transaction manager methods once to avoid unnecessary code
		 * duplication, since each method would have the same stub in every test*/
		when(transactionManager.doTaskTransaction(any()))
		.thenAnswer(answer(
				(TaskTransactionCode<?> code) -> code.apply(taskRepository, clientSession)
				));
		when(transactionManager.doTagTransaction(any()))
		.thenAnswer(answer(
				(TagTransactionCode<?> code) -> code.apply(tagRepository, clientSession)
				));
		when(transactionManager.doCompositeTransaction(any()))
		.thenAnswer(answer(
				(CompositeTransactionCode<?> code) -> code.apply(taskRepository, tagRepository, clientSession)
				));
	}

	@Test
	public void testAllTasksRetrieval() {
		// Setup phase
		List<Task> tasks = Arrays.asList(
				new Task("1", "Buy groceries"),
				new Task("2", "Start using TDD"));
		when(taskRepository.findAll(clientSession))
		.thenReturn(tasks);

		// Exercise phase
		List<Task> retrievedTasks = todoService.getAllTasks();

		// Verify phase
		InOrder inOrder = inOrder(transactionManager, taskRepository);
		inOrder.verify(transactionManager).doTaskTransaction(any());
		inOrder.verify(taskRepository).findAll(clientSession);
		inOrder.verifyNoMoreInteractions();

		assertThat(tasks)
		.isEqualTo(retrievedTasks);
	}

	@Test
	public void testFindTaskById() {
		// Setup phase
		Task task = new Task("1", "Buy groceries");
		when(taskRepository.findById(task.getId(), clientSession))
		.thenReturn(task);

		// Exercise phase
		Task retrievedTask = todoService.findTaskById(task.getId());

		// Verify phase
		InOrder inOrder = inOrder(transactionManager, taskRepository);
		inOrder.verify(transactionManager).doTaskTransaction(any());
		inOrder.verify(taskRepository).findById(task.getId(), clientSession);
		inOrder.verifyNoMoreInteractions();

		assertThat(task)
		.isEqualTo(retrievedTask);
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
		inOrder.verify(taskRepository).save(task, clientSession);
		inOrder.verifyNoMoreInteractions();
	}

	@Test
	public void testDeleteTask() {
		// Setup phase
		Task task = new Task("1", "Buy groceries");
		when(taskRepository.getTagsByTaskId(task.getId(), clientSession))
		.thenReturn(Collections.singletonList("1"));

		// Exercise phase
		todoService.deleteTask(task);

		// Verify phase
		InOrder inOrder = inOrder(transactionManager, taskRepository, tagRepository);
		inOrder.verify(transactionManager).doCompositeTransaction(any());
		inOrder.verify(taskRepository).getTagsByTaskId(task.getId(), clientSession);
		inOrder.verify(tagRepository).removeTaskFromTag("1", task.getId(), clientSession);
		inOrder.verify(taskRepository).delete(task, clientSession);
		inOrder.verifyNoMoreInteractions();
	}

	@Test
	public void testAllTagsRetrieval() {
		// Setup phase
		List<Tag> tags = Arrays.asList(
				new Tag("1", "Work"),
				new Tag("2", "Important")
				);
		when(tagRepository.findAll(clientSession))
		.thenReturn(tags);

		// Exercise phase
		List<Tag> retrievedTags = todoService.getAllTags();

		// Verify phase
		InOrder inOrder = inOrder(transactionManager, tagRepository);
		inOrder.verify(transactionManager).doTagTransaction(any());
		inOrder.verify(tagRepository).findAll(clientSession);
		inOrder.verifyNoMoreInteractions();

		assertThat(tags)
		.isEqualTo(retrievedTags);
	}

	@Test
	public void testFindTagById() {
		// Setup phase
		Tag tag = new Tag("1", "Work");
		when(tagRepository.findById(tag.getId(), clientSession))
		.thenReturn(tag);

		// Exercise phase
		Tag retrievedTag = todoService.findTagById(tag.getId());

		// Verify phase
		InOrder inOrder = inOrder(transactionManager, tagRepository);
		inOrder.verify(transactionManager).doTagTransaction(any());
		inOrder.verify(tagRepository).findById(tag.getId(), clientSession);
		inOrder.verifyNoMoreInteractions();

		assertThat(tag)
		.isEqualTo(retrievedTag);
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
		inOrder.verify(tagRepository).save(tag, clientSession);
		inOrder.verifyNoMoreInteractions();
	}

	@Test
	public void testDeleteTag() {
		// Setup phase
		Tag tag = new Tag("1", "Work");
		when(tagRepository.getTasksByTagId(tag.getId(), clientSession))
		.thenReturn(Collections.singletonList("1"));

		// Exercise phase
		todoService.deleteTag(tag);

		// Verify phase
		InOrder inOrder = inOrder(transactionManager, tagRepository, taskRepository);
		inOrder.verify(transactionManager).doCompositeTransaction(any());
		inOrder.verify(tagRepository).getTasksByTagId(tag.getId(), clientSession);
		inOrder.verify(taskRepository).removeTagFromTask("1", tag.getId(), clientSession);
		inOrder.verify(tagRepository).delete(tag, clientSession);
		inOrder.verifyNoMoreInteractions();
	}

	@Test
	public void testFindTasksByTagId() {
		// Setup phase
		List<String> tasks = Collections.singletonList("1");
		Tag tag = new Tag("1", "Work");
		when(tagRepository.getTasksByTagId(tag.getId(), clientSession))
		.thenReturn(Collections.singletonList(tag.getId()));

		// Exercise phase
		List<String> retrievedTasks = todoService.findTasksByTagId(tag.getId());

		// Verify phase
		InOrder inOrder = inOrder(transactionManager, tagRepository);
		inOrder.verify(transactionManager).doTagTransaction(any());
		inOrder.verify(tagRepository).getTasksByTagId(tag.getId(), clientSession);
		inOrder.verifyNoMoreInteractions();

		assertThat(tasks)
		.isEqualTo(retrievedTasks);
	}

	@Test
	public void testFindTagsByTaskId() {
		// Setup phase
		List<String> tags = Collections.singletonList("1");
		Task task = new Task("1", "Start using TDD");
		when(taskRepository.getTagsByTaskId(task.getId(), clientSession))
		.thenReturn(Collections.singletonList(task.getId()));

		// Exercise phase
		List<String> retrievedTags = todoService.findTagsByTaskId(task.getId());

		// Verify phase
		InOrder inOrder = inOrder(transactionManager, taskRepository);
		inOrder.verify(transactionManager).doTaskTransaction(any());
		inOrder.verify(taskRepository).getTagsByTaskId(task.getId(), clientSession);
		inOrder.verifyNoMoreInteractions();

		assertThat(tags)
		.isEqualTo(retrievedTags);
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
		inOrder.verify(taskRepository).addTagToTask(task.getId(), tag.getId(), clientSession);
		inOrder.verify(tagRepository).addTaskToTag(tag.getId(), task.getId(), clientSession);
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
		inOrder.verify(taskRepository).removeTagFromTask(task.getId(), tag.getId(), clientSession);
		inOrder.verify(tagRepository).removeTaskFromTag(tag.getId(), task.getId(), clientSession);
		verifyNoMoreInteractions(transactionManager, taskRepository, tagRepository);
	}
}