package it.unifi.simpletodoapp.repository;

import java.util.List;

import it.unifi.simpletodoapp.model.Task;

public interface TaskRepository {

	public List<Task> findAll();
	public Task findById(String taskId);
	public void save(Task task);
	public void delete(Task task);
}
