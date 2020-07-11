package it.unifi.simpletodoapp.repository;

import java.util.List;

import it.unifi.simpletodoapp.model.Tag;
import it.unifi.simpletodoapp.model.Task;

public interface TagRepository {

	public List<Tag> findAll();
	public Tag findById(String tagId);
	public void save(Tag tag);
	public void delete(Tag tag);
	public List<Task> getTasksByTagId(String tagId);
	public void addTaskToTag(String tagId, String taskId);
	public void removeTaskFromTag(String tagId, String taskId);
}
