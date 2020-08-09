package it.unifi.simpletodoapp.repository;

import java.util.List;

import com.mongodb.client.ClientSession;

import it.unifi.simpletodoapp.model.Tag;

public interface TagRepository {

	public List<Tag> findAll(ClientSession clientSession);
	public Tag findById(String tagId, ClientSession clientSession);
	public void save(Tag tag, ClientSession clientSession);
	public void delete(Tag tag, ClientSession clientSession);
	public List<String> getTasksByTagId(String tagId, ClientSession clientSession);
	public void addTaskToTag(String tagId, String taskId, ClientSession clientSession);
	public void removeTaskFromTag(String tagId, String taskId, ClientSession clientSession);
}
