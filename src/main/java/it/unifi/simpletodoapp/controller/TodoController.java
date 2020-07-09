package it.unifi.simpletodoapp.controller;

import it.unifi.simpletodoapp.service.TodoService;
import it.unifi.simpletodoapp.view.TodoView;

public class TodoController {
	private TodoService todoService;
	private TodoView todoView;
	
	public void getAllTasks() {
		// TODO Auto-generated method stub
		todoView.showAllTasks(todoService.getAllTasks());
	}

}
