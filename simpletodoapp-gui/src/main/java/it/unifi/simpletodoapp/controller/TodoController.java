package it.unifi.simpletodoapp.controller;

import java.util.ArrayList;
import java.util.List;

import it.unifi.simpletodoapp.model.Tag;
import it.unifi.simpletodoapp.model.Task;
import it.unifi.simpletodoapp.repository.TagRepositoryException;
import it.unifi.simpletodoapp.repository.TaskRepositoryException;
import it.unifi.simpletodoapp.service.TodoService;
import it.unifi.simpletodoapp.view.TodoView;

public class TodoController {
	private TodoService todoService;
	private TodoView todoView;

	public TodoController(TodoService todoService, TodoView todoView) {
		this.todoService = todoService;
		this.todoView = todoView;
	}

	public void getAllTasks() {
		todoView.showAllTasks(todoService.getAllTasks());
	}

	public void addTask(Task task) {
		try {
			todoService.saveTask(task);
			todoView.taskAdded(task);
		} catch (TaskRepositoryException exception) {
			todoView.taskError(exception.getMessage());
		}
	}

	public void deleteTask(Task task) {
		try {
			todoService.deleteTask(task);
			todoView.taskDeleted(task);
		} catch (TaskRepositoryException exception) {
			todoView.taskError(exception.getMessage());
		}
	}

	public void getAllTags() {
		todoView.showAllTags(todoService.getAllTags());
	}

	public void addTag(Tag tag) {
		try {
			todoService.saveTag(tag);
			todoView.tagAdded(tag);
		} catch (TagRepositoryException exception) {
			todoView.tagError(exception.getMessage());
		}

	}

	public void deleteTag(Tag tag) {
		try {
			todoService.deleteTag(tag);
			todoView.tagDeleted(tag);
		} catch (TagRepositoryException exception) {
			todoView.tagError(exception.getMessage());
		}
	}

	public void addTagToTask(Task task, Tag tag) {
		try {
			todoService.addTagToTask(task.getId(), tag.getId());
			todoView.tagAddedToTask(tag);
		} catch (TaskRepositoryException exception) {
			todoView.taskError(exception.getMessage());
		} catch (TagRepositoryException exception) {
			todoView.tagError(exception.getMessage());
		}
	}

	public void removeTagFromTask(Task task, Tag tag) {
		try {
			todoService.removeTagFromTask(task.getId(), tag.getId());
			todoView.tagRemovedFromTask(tag);
		} catch (TaskRepositoryException exception) {
			todoView.taskError(exception.getMessage());
		} catch (TagRepositoryException exception) {
			todoView.tagError(exception.getMessage());
		}
	}

	public void removeTaskFromTag(Tag tag, Task task) {
		try {
			todoService.removeTaskFromTag(task.getId(), tag.getId());
			todoView.taskRemovedFromTag(task);
		} catch (TaskRepositoryException exception) {
			todoView.taskError(exception.getMessage());
		} catch (TagRepositoryException exception) {
			todoView.tagError(exception.getMessage());
		}
	}

	public void getTagsByTask(Task task) {
		try {
			List<String> tags = todoService.findTagsByTaskId(task.getId());
			todoView.showTaskTags(getTags(tags));
		} catch (TaskRepositoryException exception) {
			todoView.taskError(exception.getMessage());
		}
	}

	public void getTasksByTag(Tag tag) {
		try {
			List<String> tasks = todoService.findTasksByTagId(tag.getId());
			todoView.showTagTasks(getTasks(tasks));
		} catch (TagRepositoryException exception) {
			todoView.tagError(exception.getMessage());
		}
	}

	private List<Tag> getTags(List<String> tagIds) {
		List<Tag> tags = new ArrayList<>();

		for (String tagId : tagIds) {
			tags.add(todoService.findTagById(tagId));
		}

		return tags;
	}

	private List<Task> getTasks(List<String> taskIds) {
		List<Task> tasks = new ArrayList<>();

		for (String taskId : taskIds) {
			tasks.add(todoService.findTaskById(taskId));
		}

		return tasks;
	}
}