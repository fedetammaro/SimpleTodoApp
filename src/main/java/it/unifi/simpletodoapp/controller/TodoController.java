package it.unifi.simpletodoapp.controller;

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
}
