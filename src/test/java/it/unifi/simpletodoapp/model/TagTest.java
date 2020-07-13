package it.unifi.simpletodoapp.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;

import org.junit.BeforeClass;
import org.junit.Test;

public class TagTest {
	private static Tag tag;

	@BeforeClass
	public static void createTag() {
		tag = new Tag("1", "Work");
	}

	@Test
	public void testTagEqualityWithItself() {
		// Exercise and verify phases (no setup phase required)
		assertThat(tag).isEqualTo(tag);
	}

	@Test
	public void testTagEqualityWithNull() {
		// Exercise and verify phases (no setup phase required)
		assertThat(tag).isNotEqualTo(null);
	}

	@Test
	public void testTagEqualityWithOtherClass() {
		// Exercise and verify phases (no setup phase required)
		assertThat(tag).isNotEqualTo("String object");
	}

	@Test
	public void testTagEqualityWithDifferentId() {
		// Setup phase
		Tag anotherTag = new Tag("2", "Work");
		
		// Exercise and verify phases
		assertThat(tag).isNotEqualTo(anotherTag);
	}

	@Test
	public void testTagEqualityWithDifferentDescription() {
		// Setup phase
		Tag anotherTag = new Tag("1", "Important");
		
		// Exercise and verify phases
		assertThat(tag).isNotEqualTo(anotherTag);
	}

	@Test
	public void testTagEqualityWithEqualTag() {
		// Setup phase
		Tag anotherTag = new Tag("1", "Work");
		
		// Exercise and verify phases
		assertThat(tag).isEqualTo(anotherTag);	
	}

	@Test
	public void testTagHashCode() {
		// Exercise and verify phases (no setup phase required)
		assertThat(tag.hashCode()).isEqualTo(
				Objects.hash(tag.getId(), tag.getName())
				);
	}
	
	@Test
	public void testTagToString() {
		// Exercise and verify phases (no setup phase required)
		assertThat(tag.toString()).hasToString("Tag{id='1',name='Work'}");
	}
}
