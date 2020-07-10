package it.unifi.simpletodoapp.service;

import java.util.Collections;
import java.util.List;

import it.unifi.simpletodoapp.model.Tag;
import it.unifi.simpletodoapp.model.Task;

public class TodoService {

	public List<Task> getAllTasks() {
		return Collections.emptyList();
	}
	
	public Task findTaskById(String id) {
		return null;
	}

	public void saveTask(Task task) {
		// Currently not implemented
	}

	public void deleteTask(Task task) {
		// Currently not implemented
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

	public List<Tag> findTagsByTask(String id) {
		return Collections.emptyList();
	}

	public void removeTagFromTask(String taskId, String tagId) {
		// Currently not implemented
	}
}
