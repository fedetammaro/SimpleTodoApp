package it.unifi.simpletodoapp.repository;

import java.util.List;

import com.mongodb.client.ClientSession;

import it.unifi.simpletodoapp.model.Task;

public interface TaskRepository {
	public List<Task> findAll(ClientSession clientSession);
	public Task findById(String taskId, ClientSession clientSession);
	public void save(Task task, ClientSession clientSession);
	public void delete(Task task, ClientSession clientSession);
	public List<String> getTagsByTaskId(String taskId, ClientSession clientSession);
	public void addTagToTask(String taskId, String tagId, ClientSession clientSession);
	public void removeTagFromTask(String taskId, String tagId, ClientSession clientSession);
}