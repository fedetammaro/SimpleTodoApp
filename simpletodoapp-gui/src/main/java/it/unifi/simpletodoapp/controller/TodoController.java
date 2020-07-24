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
	
	public TodoController(TodoService todoService, TodoView todoView) {
		this.todoService = todoService;
		this.todoView = todoView;
	}
	
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
		if (todoService.findTaskById(task.getId()) == null) {
			todoView.taskError(noTaskErrorMessage(task));
			return;
		}
		
		if (todoService.findTagById(tag.getId()) == null) {
			todoView.taskError(noTagErrorMessage(tag));
			return;
		}

		List<String> currentTags = todoService.findTagsByTaskId(task.getId());

		if (currentTags.stream().anyMatch(t -> t.equals(tag.getId()))) {
			todoView.taskError("Tag with ID " + tag.getId() + 
					" is already assigned to task with ID " + task.getId());
		} else {
			todoService.addTagToTask(task.getId(), tag.getId());
			todoView.tagAddedToTask(tag);
		}
	}

	public void removeTagFromTask(Task task, Tag tag) {
		if (todoService.findTaskById(task.getId()) == null) {
			todoView.taskError(noTaskErrorMessage(task));
			return;
		}
		
		if (todoService.findTagById(tag.getId()) == null) {
			todoView.taskError(noTagErrorMessage(tag));
			return;
		}

		List<String> currentTags = todoService.findTagsByTaskId(task.getId());

		if (currentTags.stream().anyMatch(t -> t.equals(tag.getId()))) {
			todoService.removeTagFromTask(task.getId(), tag.getId());
			todoView.tagRemovedFromTask(tag);
		} else {
			todoView.taskError("No tag with ID " + tag.getId() + 
					" assigned to task with ID " + task.getId());
		}
	}
	
	public void removeTaskFromTag(Tag tag, Task task) {
		if (todoService.findTaskById(task.getId()) == null) {
			todoView.tagError(noTaskErrorMessage(task));
			return;
		}
		
		if (todoService.findTagById(tag.getId()) == null) {
			todoView.tagError(noTagErrorMessage(tag));
			return;
		}
		
		List<String> currentTasks = todoService.findTasksByTagId(tag.getId());
		
		if (currentTasks.stream().anyMatch(t -> t.equals(task.getId()))) {
			todoService.removeTagFromTask(task.getId(), tag.getId());
			todoView.taskRemovedFromTag(task);
		} else {
			todoView.tagError("No task with ID " + task.getId() +
					" assigned to tag with ID " + tag.getId());
		}
	}

	public void getTagsByTask(Task task) {
		if (todoService.findTaskById(task.getId()) == null) {
			todoView.taskError(noTaskErrorMessage(task));
			return;
		}

		List<String> tags = todoService.findTagsByTaskId(task.getId());
		todoView.showTaskTags(getTags(tags));
	}

	public void getTasksByTag(Tag tag) {
		if (todoService.findTagById(tag.getId()) == null) {
			todoView.tagError(noTagErrorMessage(tag));
			return;
		}

		List<String> tasks = todoService.findTasksByTagId(tag.getId());
		todoView.showTagTasks(getTasks(tasks));
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
	
	private String noTaskErrorMessage(Task task) {
		return "No task with ID " + task.getId();
	}
	
	private String noTagErrorMessage(Tag tag) {
		return "No tag with ID " + tag.getId();
	}
}
