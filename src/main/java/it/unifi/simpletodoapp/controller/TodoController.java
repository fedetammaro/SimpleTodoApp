package it.unifi.simpletodoapp.controller;

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
}
