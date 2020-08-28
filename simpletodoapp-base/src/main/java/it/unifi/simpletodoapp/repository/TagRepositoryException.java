package it.unifi.simpletodoapp.repository;

public class TagRepositoryException extends RuntimeException{
	private static final long serialVersionUID = -55404608536003339L;

	public TagRepositoryException(String errorMessage) {
        super(errorMessage);
    }
}