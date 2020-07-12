package it.unifi.simpletodoapp.controller;

import java.util.ArrayList;
import java.util.List;

import it.unifi.simpletodoapp.model.Tag;
import it.unifi.simpletodoapp.model.Task;
import it.unifi.simpletodoapp.service.TodoService;
import it.unifi.simpletodoapp.view.TodoView;

public class TodoController {
	private TodoService todoService;
	private TodoView todoView;
	
	public void getAllTasks() {
		todoView.showAllTasks(todoService.getAllTasks());
	}

	public void addTask(Task task) {
		Task retrievedTask = todoService.findTaskById(task.getId());
		
		if (retrievedTask == null) {
			todoService.saveTask(task);
			todoView.taskAdded(task);
		} else {
			todoView.taskError("Cannot add task with duplicated ID " + task.getId());
		}
	}

	public void deleteTask(Task task) {
		Task retrievedTask = todoService.findTaskById(task.getId());
		
		if (retrievedTask == null) {
			todoView.taskError("Task with ID " + task.getId() + " has already been removed");
		} else {
			todoService.deleteTask(task);
			todoView.taskDeleted(task);
		}
	}

	public void getAllTags() {
		todoView.showAllTags(todoService.getAllTags());
	}
	
	public void addTag(Tag tag) {
		Tag retrievedTag = todoService.findTagById(tag.getId());
		
		if (retrievedTag == null) {
			List<Tag> tagList = todoService.getAllTags();
			
			if(tagList.stream().anyMatch(t -> t.getName().equals(tag.getName()))) {
				todoView.tagError("Cannot add tag with duplicated name \"" + tag.getName() + "\"");
			} else {
				todoService.saveTag(tag);
				todoView.tagAdded(tag);
			}
		} else {
			todoView.tagError("Cannot add tag with duplicated ID " + tag.getId());
		}
	}

	public void removeTag(Tag tag) {
		Tag retrievedTag = todoService.findTagById(tag.getId());
		
		if (retrievedTag == null) {
			todoView.tagError("Tag with ID " + tag.getId() + " has already been removed");
		} else {
			todoService.deleteTag(tag);
			todoView.tagRemoved(tag);
		}
	}

	public void addTagToTask(Task task, Tag tag) {
		if (!taskExists(task) || !tagExists(tag))
			return;
		
		List<String> currentTags = todoService.findTagsByTaskId(task.getId());
		
		if (currentTags.stream().anyMatch(t -> t.equals(tag.getId()))) {
			todoView.tagError("Tag with ID " + tag.getId() + 
					" is already assigned to task with ID " + task.getId());
		} else {
			todoService.addTagToTask(task.getId(), tag.getId());
			todoView.showTaskTags(getTags(todoService.findTagsByTaskId(task.getId())));
		}
	}

	public void removeTagFromTask(Task task, Tag tag) {
		if (!taskExists(task) || !tagExists(tag))
			return;
		
		List<String> currentTags = todoService.findTagsByTaskId(task.getId());
		
		if (currentTags.stream().anyMatch(t -> t.equals(tag.getId()))) {
			todoService.removeTagFromTask(task.getId(), tag.getId());
			todoView.showTaskTags(getTags(todoService.findTagsByTaskId(task.getId())));
		} else {
			todoView.tagError("No tag with ID " + tag.getId() + 
					" assigned to task with ID " + task.getId());
		}
	}
	
	public void getTagsByTask(Task task) {
		if (!taskExists(task))
			return;
		
		List<String> tags = todoService.findTagsByTaskId(task.getId());
		todoView.showTaskTags(getTags(tags));
	}
	
	public void getTasksByTag(Tag tag) {
		if (!tagExists(tag))
			return;
		
		List<Task> tasks = todoService.findTasksByTagId(tag.getId());
		todoView.showTagTasks(tasks);
	}
	
	private boolean taskExists(Task task) {
		if (todoService.findTaskById(task.getId()) == null) {
			todoView.taskError("No task with ID " + task.getId());
			return false;
		}
		
		return true;
	}
	
	private boolean tagExists(Tag tag) {
		if (todoService.findTagById(tag.getId()) == null) {
			todoView.tagError("No tag with ID " + tag.getId());
			return false;
		}
		
		return true;
	}
	
	private List<Tag> getTags(List<String> tagIds) {
		List<Tag> tags = new ArrayList<>();
		
		for (String tagId : tagIds) {
			tags.add(todoService.findTagById(tagId));
		}
		
		return tags;
	}
}
