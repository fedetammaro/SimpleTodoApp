package it.unifi.simpletodoapp.service;

import java.util.List;

import it.unifi.simpletodoapp.model.Tag;
import it.unifi.simpletodoapp.model.Task;
import it.unifi.simpletodoapp.repository.TagRepositoryException;
import it.unifi.simpletodoapp.repository.TaskRepositoryException;
import it.unifi.simpletodoapp.repository.TransactionManager;

public class TodoService {
	private TransactionManager transactionManager;

	public TodoService(TransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}

	public List<Task> getAllTasks() {
		return transactionManager.doTaskTransaction(
				(taskMongoRepository, clientSession) -> taskMongoRepository.findAll(clientSession)
				);
	}

	public Task findTaskById(String tagId) {
		return transactionManager.doTaskTransaction(
				(taskMongoRepository, clientSession) -> taskMongoRepository.findById(tagId, clientSession)
				);
	}

	public void saveTask(Task task) {
		transactionManager.doTaskTransaction(
				(taskMongoRepository, clientSession) -> {
					if (taskMongoRepository.findById(task.getId(), clientSession) != null) {
						throw new TaskRepositoryException("Cannot add task with duplicated ID " + task.getId());
					}
					
					taskMongoRepository.save(task, clientSession);
					return null;
				});
	}

	public void deleteTask(Task task) {
		// Delete the task and remove it from all the tags it was associated to
		transactionManager.doCompositeTransaction(
				(taskRepository, tagRepository, clientSession) -> {
					if (taskRepository.findById(task.getId(), clientSession) == null) {
						throw new TaskRepositoryException("Task with ID " + task.getId() + " has already been deleted");
					}
					
					taskRepository.getTagsByTaskId(task.getId(), clientSession)
					.stream()
					.forEach(tagId -> tagRepository.removeTaskFromTag(tagId, task.getId(), clientSession));

					taskRepository.delete(task, clientSession);
					return null;
				});
	}

	public List<Tag> getAllTags() {
		return transactionManager.doTagTransaction(
				(tagMongoRepository, clientSession) -> tagMongoRepository.findAll(clientSession)
				);
	}

	public Tag findTagById(String tagId) {
		return transactionManager.doTagTransaction(
				(tagMongoRepository, clientSession) -> tagMongoRepository.findById(tagId, clientSession)
				);
	}

	public void saveTag(Tag tag) {
		transactionManager.doTagTransaction(
				(tagMongoRepository, clientSession) -> {
					if (tagMongoRepository.findById(tag.getId(), clientSession) != null) {
						throw new TagRepositoryException("Cannot add tag with duplicated ID " + tag.getId());
					}
					
					List<Tag> tagList = getAllTags();

					if(tagList.stream().anyMatch(t -> t.getName().equals(tag.getName()))) {
						throw new TagRepositoryException("Cannot add tag with duplicated name \"" + tag.getName() + "\"");
					}
					
					tagMongoRepository.save(tag, clientSession);
					return null;
				});
	}

	public void deleteTag(Tag tag) {
		// Delete the tag and remove it from all the tasks it was associated to
		transactionManager.doCompositeTransaction(
				(taskRepository, tagRepository, clientSession) -> {
					if (tagRepository.findById(tag.getId(), clientSession) == null) {
						throw new TagRepositoryException("Tag with ID " + tag.getId() + " has already been deleted");
					}
					
					tagRepository.getTasksByTagId(tag.getId(), clientSession)
					.stream()
					.forEach(taskId -> taskRepository.removeTagFromTask(taskId, tag.getId(), clientSession));

					tagRepository.delete(tag, clientSession);
					return null;
				});
	}

	public void addTagToTask(String taskId, String tagId) {
		transactionManager.doCompositeTransaction(
				(taskRepository, tagRepository, clientSession) -> {
					taskRepository.addTagToTask(taskId, tagId, clientSession);
					tagRepository.addTaskToTag(tagId, taskId, clientSession);
					return null;
				});
	}

	public List<String> findTagsByTaskId(String taskId) {
		return transactionManager.doTaskTransaction(
				(taskMongoRepository, clientSession) -> taskMongoRepository.getTagsByTaskId(taskId, clientSession)
				);
	}

	public void removeTagFromTask(String taskId, String tagId) {
		transactionManager.doCompositeTransaction(
				(taskRepository, tagRepository, clientSession) -> {
					taskRepository.removeTagFromTask(taskId, tagId, clientSession);
					tagRepository.removeTaskFromTag(tagId, taskId, clientSession);
					return null;
				});
	}

	public List<String> findTasksByTagId(String tagId) {
		return transactionManager.doTagTransaction(
				(tagMongoRepository, clientSession) -> tagMongoRepository.getTasksByTagId(tagId, clientSession)
				);
	}
}