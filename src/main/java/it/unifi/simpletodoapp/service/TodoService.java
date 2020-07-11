package it.unifi.simpletodoapp.service;

import java.util.Collections;
import java.util.List;

import it.unifi.simpletodoapp.model.Tag;
import it.unifi.simpletodoapp.model.Task;
import it.unifi.simpletodoapp.repository.TaskRepository;
import it.unifi.simpletodoapp.repository.TransactionManager;

public class TodoService {
	private TransactionManager transactionManager;

	public List<Task> getAllTasks() {
		return transactionManager.doTaskTransaction(TaskRepository::findAll);
	}
	
	public Task findTaskById(String id) {
		return transactionManager.doTaskTransaction(taskRepository -> taskRepository.findById(id));
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
		return Collections.emptyList();
	}

	public Tag findTagById(String id) {
		return null;
	}

	public void saveTag(Tag tag) {
		// Currently not implemented
	}

	public void deleteTag(Tag tag) {
		// Currently not implemented
	}

	public void addTagToTask(String taskId, String tagId) {
		// Currently not implemented
	}

	public List<Tag> findTagsByTaskId(String id) {
		return Collections.emptyList();
	}

	public void removeTagFromTask(String taskId, String tagId) {
		// Currently not implemented
	}

	public List<Task> findTasksByTagId(String id) {
		return Collections.emptyList();
	}
}
