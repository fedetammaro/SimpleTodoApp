package it.unifi.simpletodoapp.view;

import java.util.List;

import it.unifi.simpletodoapp.model.Tag;
import it.unifi.simpletodoapp.model.Task;

public interface TodoView {

	public void showAllTasks(List<Task> allTasks);
	public void taskAdded(Task task);
	public void taskError(String errorMessage);
	public void taskDeleted(Task task);
	public void showAllTags(List<Tag> allTags);
	public void tagAdded(Tag tag);
	public void tagError(String string);
	public void tagRemoved(Tag tag);
	public void showTaskTags(List<Tag> tags);
	public void showTagTasks(List<Task> tasks);
	public void taskAddedToTag(Task task);
	public void taskRemovedFromTag(Task task);
	public void tagAddedToTask(Tag tag);
	public void tagRemovedFromTask(Tag tag);
}
