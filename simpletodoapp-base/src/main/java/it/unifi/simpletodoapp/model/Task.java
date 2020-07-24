package it.unifi.simpletodoapp.model;

import java.util.Objects;

public class Task {
	private String id;
	private String description;
	
	public Task(String id, String description) {
		this.id = id;
		this.description = description;
	}

	public String getId() {
		return id;
	}

	public String getDescription() {
		return description;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(id, description);
	}
	
	@Override
	public boolean equals(Object object) {
		if (object == this)
			return true;
		
		if (object == null || object.getClass() != this.getClass())
			return false;
		
		Task task = (Task) object;
		
		return task.getId().equals(id) && task.getDescription().equals(description);
	}
}
