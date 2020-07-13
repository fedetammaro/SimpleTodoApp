package it.unifi.simpletodoapp.service;

import java.util.List;

import it.unifi.simpletodoapp.model.Tag;
import it.unifi.simpletodoapp.model.Task;
import it.unifi.simpletodoapp.repository.TagRepository;
import it.unifi.simpletodoapp.repository.TaskRepository;
import it.unifi.simpletodoapp.repository.TransactionManager;

public class TodoService {
	private TransactionManager transactionManager;

	public List<Task> getAllTasks() {
		return transactionManager.doTaskTransaction(TaskRepository::findAll);
	}

	public Task findTaskById(String tagId) {
		return transactionManager.doTaskTransaction(taskRepository -> taskRepository.findById(tagId));
	}

	public void saveTask(Task task) {
		transactionManager.doTaskTransaction(taskRepository -> {
			taskRepository.save(task);
			return null;
		});
	}

	public void deleteTask(Task task) {
		transactionManager.doTaskTransaction(taskRepository -> {
			taskRepository.delete(task);
			return null;
		});
	}

	public List<Tag> getAllTags() {
		return transactionManager.doTagTransaction(TagRepository::findAll);
	}

	public Tag findTagById(String tagId) {
		return transactionManager.doTagTransaction(tagRepository -> tagRepository.findById(tagId));
	}

	public void saveTag(Tag tag) {
		transactionManager.doTagTransaction(tagRepository -> {
			tagRepository.save(tag);
			return null;
		});
	}

	public void deleteTag(Tag tag) {
		transactionManager.doTagTransaction(tagRepository -> {
			tagRepository.delete(tag);
			return null;
		});
	}

	public void addTagToTask(String taskId, String tagId) {
		transactionManager.doCompositeTransaction((taskRepository, tagRepository) -> {
			taskRepository.addTagToTask(taskId, tagId);
			tagRepository.addTaskToTag(tagId, taskId);
			return null;
		});
	}

	public List<String> findTagsByTaskId(String taskId) {
		return transactionManager.doTaskTransaction(taskRepository -> taskRepository.getTagsByTaskId(taskId));
	}

	public void removeTagFromTask(String taskId, String tagId) {
		transactionManager.doCompositeTransaction((taskRepository, tagRepository) -> {
			taskRepository.removeTagFromTask(taskId, tagId);
			tagRepository.removeTaskFromTag(tagId, taskId);
			return null;
		});
	}

	public List<String> findTasksByTagId(String tagId) {
		return transactionManager.doTagTransaction(tagRepository -> tagRepository.getTasksByTagId(tagId));
	}
}
