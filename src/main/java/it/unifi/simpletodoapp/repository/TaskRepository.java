package it.unifi.simpletodoapp.repository;

import java.util.List;

import it.unifi.simpletodoapp.model.Task;

public interface TaskRepository {

	public List<Task> findAll();
}
