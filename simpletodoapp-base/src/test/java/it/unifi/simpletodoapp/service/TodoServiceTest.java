package it.unifi.simpletodoapp.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.AdditionalAnswers.answer;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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
import it.unifi.simpletodoapp.repository.TagRepositoryException;
import it.unifi.simpletodoapp.repository.TagTransactionCode;
import it.unifi.simpletodoapp.repository.TaskRepositoryException;
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
	public void testSaveTaskWithUniqueId() {
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
	public void testSaveTaskWithDuplicatedId() {
		// Setup phase
		Task task = new Task("1", "Buy groceries");
		when(taskRepository.findById(task.getId(), clientSession))
		.thenReturn(task);

		// Exercise and verify phases
		TaskRepositoryException exception = assertThrows(TaskRepositoryException.class,
				() -> todoService.saveTask(task));
		assertThat(exception.getMessage())
		.isEqualTo("Cannot add task with duplicated ID " + task.getId());
		verify(taskRepository, never()).save(task, clientSession);
	}

	@Test
	public void testDeleteExistingTask() {
		// Setup phase
		Task task = new Task("1", "Buy groceries");
		when(taskRepository.findById(task.getId(), clientSession))
		.thenReturn(task);
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
	public void testDeleteNonExistingTask() {
		// Setup phase
		Task task = new Task("1", "Buy groceries");
		when(taskRepository.findById(task.getId(), clientSession))
		.thenReturn(null);

		// Exercise and verify phases
		TaskRepositoryException exception = assertThrows(TaskRepositoryException.class,
				() -> todoService.deleteTask(task));
		assertThat(exception.getMessage())
		.isEqualTo("Task with ID " + task.getId() + " has already been deleted");
		verify(taskRepository, never()).delete(task, clientSession);
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
	public void testSaveTagWithUniqueIdAndName() {
		// Setup phase
		Tag tag = new Tag("1", "Work");
		when(tagRepository.findById(tag.getId(), clientSession))
		.thenReturn(null);
		when(tagRepository.findAll(clientSession))
		.thenReturn(Collections.emptyList());

		// Exercise phase
		todoService.saveTag(tag);

		// Verify phase
		InOrder inOrder = inOrder(transactionManager, tagRepository);
		inOrder.verify(transactionManager).doTagTransaction(any());
		inOrder.verify(tagRepository).save(tag, clientSession);
		inOrder.verifyNoMoreInteractions();
	}

	@Test
	public void testSaveTagWithDuplicatedId() {
		// Setup phase
		Tag tag = new Tag("1", "Work");
		when(tagRepository.findById(tag.getId(), clientSession))
		.thenReturn(tag);

		// Exercise and verify phases
		TagRepositoryException exception = assertThrows(TagRepositoryException.class,
				() -> todoService.saveTag(tag));
		assertThat(exception.getMessage())
		.isEqualTo("Cannot add tag with duplicated ID " + tag.getId());
		verify(tagRepository, never()).save(tag, clientSession);
	}

	@Test
	public void testSaveTagWithDuplicatedName() {
		// Setup phase
		Tag tag = new Tag("1", "Work");
		when(tagRepository.findById(tag.getId(), clientSession))
		.thenReturn(null);
		when(tagRepository.findAll(clientSession))
		.thenReturn(Arrays.asList(new Tag("2", "Work")));

		// Exercise and verify phases
		TagRepositoryException exception = assertThrows(TagRepositoryException.class,
				() -> todoService.saveTag(tag));
		assertThat(exception.getMessage())
		.isEqualTo("Cannot add tag with duplicated name \"" + tag.getName() + "\"");
		verify(tagRepository, never()).save(tag, clientSession);
	}

	@Test
	public void testSaveTagWithUniqueIdAndNameWithOtherTags() {
		Tag tag = new Tag("1", "Work");
		when(tagRepository.findById(tag.getId(), clientSession))
		.thenReturn(null);
		when(tagRepository.findAll(clientSession))
		.thenReturn(Arrays.asList(
				new Tag("2", "Important"),
				new Tag("3", "Free time")
				));

		// Exercise phase
		todoService.saveTag(tag);

		// Verify phase
		InOrder inOrder = inOrder(transactionManager, tagRepository);
		inOrder.verify(transactionManager).doTagTransaction(any());
		inOrder.verify(tagRepository).save(tag, clientSession);
		inOrder.verifyNoMoreInteractions();
	}

	@Test
	public void testDeleteExistingTag() {
		// Setup phase
		Tag tag = new Tag("1", "Work");
		when(tagRepository.findById(tag.getId(), clientSession))
		.thenReturn(tag);
		when(tagRepository.getTasksByTagId(tag.getId(), clientSession))
		.thenReturn(Collections.singletonList("1"));

		// Exercise phase
		todoService.deleteTag(tag);

		// Verify phase
		InOrder inOrder = inOrder(transactionManager, taskRepository, tagRepository);
		inOrder.verify(transactionManager).doCompositeTransaction(any());
		inOrder.verify(tagRepository).getTasksByTagId(tag.getId(), clientSession);
		inOrder.verify(taskRepository).removeTagFromTask("1", tag.getId(), clientSession);
		inOrder.verify(tagRepository).delete(tag, clientSession);
		inOrder.verifyNoMoreInteractions();
	}

	@Test
	public void testDeleteNonExistingTag() {
		// Setup phase
		Tag tag = new Tag("1", "Work");
		when(tagRepository.findById(tag.getId(), clientSession))
		.thenReturn(null);

		// Exercise and verify phases
		TagRepositoryException exception = assertThrows(TagRepositoryException.class,
				() -> todoService.deleteTag(tag));
		assertThat(exception.getMessage())
		.isEqualTo("Tag with ID " + tag.getId() + " has already been deleted");
		verify(tagRepository, never()).delete(tag, clientSession);
	}

	@Test
	public void testSuccessfulFindTasksByTagId() {
		// Setup phase
		List<String> tasks = Collections.singletonList("1");
		Tag tag = new Tag("1", "Work");
		when(tagRepository.findById("1", clientSession))
		.thenReturn(tag);
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
	public void testFindTasksByTagIdWhenTagNonExistent() {
		// Setup phase
		String tagId = "1";
		when(tagRepository.findById(tagId, clientSession))
		.thenReturn(null);

		// Exercise and verify phases
		TagRepositoryException exception = assertThrows(TagRepositoryException.class,
				() -> todoService.findTasksByTagId(tagId));
		assertThat(exception.getMessage())
		.isEqualTo("No tag with ID " + tagId);
	}

	@Test
	public void testFindTagsByTaskId() {
		// Setup phase
		List<String> tags = Collections.singletonList("1");
		Task task = new Task("1", "Start using TDD");
		when(taskRepository.findById("1", clientSession))
		.thenReturn(task);
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
	public void testFindTagsByTaskIdWhenTaskNonExistent() {
		// Setup phase
		String taskId = "1";
		when(taskRepository.findById(taskId, clientSession))
		.thenReturn(null);

		// Exercise and verify phases
		TaskRepositoryException exception = assertThrows(TaskRepositoryException.class,
				() -> todoService.findTagsByTaskId(taskId));
		assertThat(exception.getMessage())
		.isEqualTo("No task with ID " + taskId);
	}

	@Test
	public void testSuccessfulTagAdditionToTask() {
		// Setup phase
		Task task = new Task("1", "Start using TDD");
		Tag tag = new Tag("1", "Work");
		when(taskRepository.findById(task.getId(), clientSession))
		.thenReturn(task);
		when(tagRepository.findById(tag.getId(), clientSession))
		.thenReturn(tag);
		when(taskRepository.getTagsByTaskId(task.getId(), clientSession))
		.thenReturn(Collections.emptyList());

		// Exercise phase
		todoService.addTagToTask(task.getId(), tag.getId());

		// Verify phase
		InOrder inOrder = inOrder(transactionManager, taskRepository, tagRepository);
		inOrder.verify(transactionManager).doCompositeTransaction(any());
		inOrder.verify(taskRepository).addTagToTask(task.getId(), tag.getId(), clientSession);
		inOrder.verify(tagRepository).addTaskToTag(tag.getId(), task.getId(), clientSession);
		inOrder.verifyNoMoreInteractions();
	}

	@Test
	public void testSuccessfulTagAdditionToTaskWithOtherTags() {
		// Setup phase
		Task task = new Task("1", "Start using TDD");
		Tag tag = new Tag("1", "Work");
		when(taskRepository.findById(task.getId(), clientSession))
		.thenReturn(task);
		when(tagRepository.findById(tag.getId(), clientSession))
		.thenReturn(tag);
		when(taskRepository.getTagsByTaskId(task.getId(), clientSession))
		.thenReturn(Arrays.asList("2"));

		// Exercise phase
		todoService.addTagToTask(task.getId(), tag.getId());

		// Verify phase
		InOrder inOrder = inOrder(transactionManager, taskRepository, tagRepository);
		inOrder.verify(transactionManager).doCompositeTransaction(any());
		inOrder.verify(taskRepository).addTagToTask(task.getId(), tag.getId(), clientSession);
		inOrder.verify(tagRepository).addTaskToTag(tag.getId(), task.getId(), clientSession);
		inOrder.verifyNoMoreInteractions();
	}

	@Test
	public void testDuplicatedTagAdditionToTask() {
		// Setup phase
		String taskId = "1";
		String tagId = "1";
		Task task = new Task(taskId, "Start using TDD");
		Tag tag = new Tag(tagId, "Work");
		when(taskRepository.findById(taskId, clientSession))
		.thenReturn(task);
		when(tagRepository.findById(tagId, clientSession))
		.thenReturn(tag);
		when(taskRepository.getTagsByTaskId(taskId, clientSession))
		.thenReturn(Arrays.asList(tagId));

		// Exercise and verify phases
		TaskRepositoryException exception = assertThrows(TaskRepositoryException.class,
				() -> todoService.addTagToTask(taskId, tagId));
		assertThat(exception.getMessage())
		.isEqualTo("Tag with ID " + tagId +	" is already assigned to task with ID " + taskId);
		verify(taskRepository, never()).addTagToTask(taskId, tagId, clientSession);
		verify(tagRepository, never()).addTaskToTag(tagId, taskId, clientSession);
	}

	@Test
	public void testTagAdditionToTaskWhenTaskNonExistent() {
		// Setup phase
		String taskId = "1";
		String tagId = "1";
		when(taskRepository.findById(taskId, clientSession))
		.thenReturn(null);

		// Exercise and verify phases
		TaskRepositoryException exception = assertThrows(TaskRepositoryException.class,
				() -> todoService.addTagToTask(taskId, tagId));
		assertThat(exception.getMessage())
		.isEqualTo("No task with ID " + taskId);
		verify(taskRepository, never()).addTagToTask(taskId, tagId, clientSession);
		verify(tagRepository, never()).addTaskToTag(tagId, taskId, clientSession);
	}

	@Test
	public void testTagAdditionToTaskWhenTagNonExistent() {
		// Setup phase
		String taskId = "1";
		String tagId = "1";
		Task task = new Task(taskId, "Start using TDD");
		when(taskRepository.findById(taskId, clientSession))
		.thenReturn(task);
		when(tagRepository.findById(tagId, clientSession))
		.thenReturn(null);

		// Exercise and verify phases
		TagRepositoryException exception = assertThrows(TagRepositoryException.class,
				() -> todoService.addTagToTask(taskId, tagId));
		assertThat(exception.getMessage())
		.isEqualTo("No tag with ID " + tagId);
		verify(taskRepository, never()).addTagToTask(taskId, tagId, clientSession);
		verify(tagRepository, never()).addTaskToTag(tagId, taskId, clientSession);
	}

	@Test
	public void testSuccessfulTagRemovalFromTask() {
		// Setup phase
		Task task = new Task("1", "Start using TDD");
		Tag tag = new Tag("1", "Work");
		when(taskRepository.findById(task.getId(), clientSession))
		.thenReturn(task);
		when(tagRepository.findById(tag.getId(), clientSession))
		.thenReturn(tag);
		when(taskRepository.getTagsByTaskId(task.getId(), clientSession))
		.thenReturn(Arrays.asList(tag.getId()));

		// Exercise phase
		todoService.removeTagFromTask(task.getId(), tag.getId());

		// Verify phase
		InOrder inOrder = inOrder(transactionManager, taskRepository, tagRepository);
		inOrder.verify(transactionManager).doCompositeTransaction(any());
		inOrder.verify(taskRepository).removeTagFromTask(task.getId(), tag.getId(), clientSession);
		inOrder.verify(tagRepository).removeTaskFromTag(tag.getId(), task.getId(), clientSession);
		inOrder.verifyNoMoreInteractions();
	}

	@Test
	public void testTagRemovalFromTaskWhenTaskNonExistent() {
		// Setup phase
		String taskId = "1";
		String tagId = "1";
		when(taskRepository.findById(taskId, clientSession))
		.thenReturn(null);

		// Exercise and verify phases
		TaskRepositoryException exception = assertThrows(TaskRepositoryException.class,
				() -> todoService.removeTagFromTask(taskId, tagId));
		assertThat(exception.getMessage())
		.isEqualTo("No task with ID " + taskId);
		verify(taskRepository, never()).removeTagFromTask(taskId, tagId, clientSession);
		verify(tagRepository, never()).removeTaskFromTag(tagId, taskId, clientSession);
	}

	@Test
	public void testTagRemovalFromTaskWhenTagNonExistent() {
		// Setup phase
		String taskId = "1";
		String tagId = "1";
		Task task = new Task(taskId, "Start using TDD");
		when(taskRepository.findById(taskId, clientSession))
		.thenReturn(task);
		when(tagRepository.findById(tagId, clientSession))
		.thenReturn(null);

		// Exercise and verify phases
		TagRepositoryException exception = assertThrows(TagRepositoryException.class,
				() -> todoService.removeTagFromTask(taskId, tagId));
		assertThat(exception.getMessage())
		.isEqualTo("No tag with ID " + tagId);
		verify(taskRepository, never()).removeTagFromTask(taskId, tagId, clientSession);
		verify(tagRepository, never()).removeTaskFromTag(tagId, taskId, clientSession);
	}

	@Test
	public void testTagRemovalFromTaskWhenTagNotAssignedToTask() {
		// Setup phase
		String taskId = "1";
		String tagId = "1";
		Task task = new Task(taskId, "Start using TDD");
		Tag tag = new Tag(tagId, "Work");
		when(taskRepository.findById(taskId, clientSession))
		.thenReturn(task);
		when(tagRepository.findById(tagId, clientSession))
		.thenReturn(tag);
		when(taskRepository.getTagsByTaskId(taskId, clientSession))
		.thenReturn(Arrays.asList("2"));

		// Exercise and verify phases
		TaskRepositoryException exception = assertThrows(TaskRepositoryException.class,
				() -> todoService.removeTagFromTask(taskId, tagId));
		assertThat(exception.getMessage())
		.isEqualTo("No tag with ID " + tagId +  " assigned to task with ID " + taskId);
		verify(taskRepository, never()).removeTagFromTask(taskId, tagId, clientSession);
		verify(tagRepository, never()).removeTaskFromTag(tagId, taskId, clientSession);
	}

	@Test
	public void testSuccessfulTaskRemovalFromTag() {
		// Setup phase
		Task task = new Task("1", "Start using TDD");
		Tag tag = new Tag("1", "Work");
		when(taskRepository.findById(task.getId(), clientSession))
		.thenReturn(task);
		when(tagRepository.findById(tag.getId(), clientSession))
		.thenReturn(tag);
		when(tagRepository.getTasksByTagId(tag.getId(), clientSession))
		.thenReturn(Arrays.asList(task.getId()));

		// Exercise phase
		todoService.removeTaskFromTag(task.getId(), tag.getId());

		// Verify phase
		InOrder inOrder = inOrder(transactionManager, taskRepository, tagRepository);
		inOrder.verify(transactionManager).doCompositeTransaction(any());
		inOrder.verify(taskRepository).removeTagFromTask(task.getId(), tag.getId(), clientSession);
		inOrder.verify(tagRepository).removeTaskFromTag(tag.getId(), task.getId(), clientSession);
		inOrder.verifyNoMoreInteractions();
	}

	@Test
	public void testTaskRemovalFromTagWhenTaskNonExistent() {
		// Setup phase
		String taskId = "1";
		String tagId = "1";
		when(taskRepository.findById(taskId, clientSession))
		.thenReturn(null);

		// Exercise and verify phases
		TaskRepositoryException exception = assertThrows(TaskRepositoryException.class,
				() -> todoService.removeTaskFromTag(taskId, tagId));
		assertThat(exception.getMessage())
		.isEqualTo("No task with ID " + taskId);
		verify(taskRepository, never()).removeTagFromTask(taskId, tagId, clientSession);
		verify(tagRepository, never()).removeTaskFromTag(tagId, taskId, clientSession);
	}

	@Test
	public void testTaskRemovalFromTagWhenTagNonExistent() {
		// Setup phase
		String taskId = "1";
		String tagId = "1";
		Task task = new Task(taskId, "Start using TDD");
		when(taskRepository.findById(taskId, clientSession))
		.thenReturn(task);
		when(tagRepository.findById(tagId, clientSession))
		.thenReturn(null);

		// Exercise and verify phases
		TagRepositoryException exception = assertThrows(TagRepositoryException.class,
				() -> todoService.removeTaskFromTag(taskId, tagId));
		assertThat(exception.getMessage())
		.isEqualTo("No tag with ID " + tagId);
		verify(taskRepository, never()).removeTagFromTask(taskId, tagId, clientSession);
		verify(tagRepository, never()).removeTaskFromTag(tagId, taskId, clientSession);
	}

	@Test
	public void testTaskRemovalFromTagWhenTagNotAssignedToTask() {
		// Setup phase
		String taskId = "1";
		String tagId = "1";
		Task task = new Task(taskId, "Start using TDD");
		Tag tag = new Tag(tagId, "Work");
		when(taskRepository.findById(taskId, clientSession))
		.thenReturn(task);
		when(tagRepository.findById(tagId, clientSession))
		.thenReturn(tag);
		when(tagRepository.getTasksByTagId(tagId, clientSession))
		.thenReturn(Arrays.asList("2"));

		// Exercise and verify phases
		TagRepositoryException exception = assertThrows(TagRepositoryException.class,
				() -> todoService.removeTaskFromTag(taskId, tagId));
		assertThat(exception.getMessage())
		.isEqualTo("No task with ID " + taskId +  " assigned to tag with ID " + tagId);
		verify(taskRepository, never()).removeTagFromTask(taskId, tagId, clientSession);
		verify(tagRepository, never()).removeTaskFromTag(tagId, taskId, clientSession);
	}
}