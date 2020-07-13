package it.unifi.simpletodoapp.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;

import org.junit.BeforeClass;
import org.junit.Test;

public class TaskTest {
	private static Task task;

	@BeforeClass
	public static void createTask() {
		task = new Task("1", "Buy groceries");
	}

	@Test
	public void testTaskEqualityWithItself() {
		assertThat(task.equals(task)).isTrue();
	}

	@Test
	public void testTaskEqualityWithNull() {
		assertThat(task.equals(null)).isFalse();
	}

	@Test
	public void testTaskEqualityWithOtherClass() {
		assertThat(task.equals("String object")).isFalse();
	}

	@Test
	public void testTaskEqualityWithDifferentId() {
		Task anotherTask = new Task("2", "Buy groceries");

		assertThat(task.equals(anotherTask)).isFalse();
	}

	@Test
	public void testTaskEqualityWithDifferentDescription() {
		Task anotherTask = new Task("1", "Start using TDD");

		assertThat(task.equals(anotherTask)).isFalse();
	}

	@Test
	public void testTaskEqualityWithEqualTask() {
		Task anotherTask = new Task("1", "Buy groceries");

		assertThat(task.equals(anotherTask)).isTrue();	
	}

	@Test
	public void testTaskHashCode() {
		assertThat(task.hashCode()).isEqualTo(
				Objects.hash(task.getId(), task.getDescription())
				);
	}
	
	@Test
	public void testTaskToString() {
		// Exercise and verify phases (no setup phase required)
		assertThat(task.toString()).isEqualTo("Task{id='1',description='Buy groceries'}");
	}
}
