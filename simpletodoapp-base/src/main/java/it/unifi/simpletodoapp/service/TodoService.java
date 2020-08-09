package it.unifi.simpletodoapp.service;

import java.util.List;

import it.unifi.simpletodoapp.model.Tag;
import it.unifi.simpletodoapp.model.Task;
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
					taskMongoRepository.save(task, clientSession);
					return null;
				});
	}

	public void deleteTask(Task task) {
		transactionManager.doCompositeTransaction(
				(taskRepository, tagRepository, clientSession) -> {
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
					tagMongoRepository.save(tag, clientSession);
					return null;
				});
	}

	public void deleteTag(Tag tag) {
		transactionManager.doCompositeTransaction(
				(taskRepository, tagRepository, clientSession) -> {
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
