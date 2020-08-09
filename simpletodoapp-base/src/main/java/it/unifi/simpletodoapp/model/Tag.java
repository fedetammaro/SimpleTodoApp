package it.unifi.simpletodoapp.model;

import java.util.Objects;

public class Tag {
	private String id;
	private String name;

	public Tag(String id, String name) {
		this.id = id;
		this.name = name;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, name);
	}

	@Override
	public boolean equals(Object object) {
		if (object == this)
			return true;

		if (object == null || object.getClass() != this.getClass())
			return false;

		Tag tag = (Tag) object;
		return tag.getId().equals(id) && tag.getName().equals(name);
	}
}