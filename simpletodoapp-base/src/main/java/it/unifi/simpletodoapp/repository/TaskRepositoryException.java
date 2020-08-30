package it.unifi.simpletodoapp.repository;

public class TaskRepositoryException extends RuntimeException {
	private static final long serialVersionUID = 6777400593017780486L;

	public TaskRepositoryException(String errorMessage) {
		super(errorMessage);
	}
}