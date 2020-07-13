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
		// Exercise and verify phases (no setup phase required)
		assertThat(task).isEqualTo(task);
	}

	@Test
	public void testTaskEqualityWithNull() {
		// Exercise and verify phases (no setup phase required)
		assertThat(task).isNotEqualTo(null);
	}

	@Test
	public void testTaskEqualityWithOtherClass() {
		// Exercise and verify phases (no setup phase required)
		assertThat(task).isNotEqualTo("String object");
	}

	@Test
	public void testTaskEqualityWithDifferentId() {
		// Setup phase
		Task anotherTask = new Task("2", "Buy groceries");
		
		// Exercise and verify phases
		assertThat(task).isNotEqualTo(anotherTask);
	}

	@Test
	public void testTaskEqualityWithDifferentDescription() {
		// Setup phase
		Task anotherTask = new Task("1", "Start using TDD");
		
		// Exercise and verify phases
		assertThat(task).isNotEqualTo(anotherTask);
	}

	@Test
	public void testTaskEqualityWithEqualTask() {
		// Setup phase
		Task anotherTask = new Task("1", "Buy groceries");
		
		// Exercise and verify phases
		assertThat(task).isEqualTo(anotherTask);	
	}

	@Test
	public void testTaskHashCode() {
		// Exercise and verify phases (no setup phase required)
		assertThat(task.hashCode()).isEqualTo(
				Objects.hash(task.getId(), task.getDescription())
				);
	}
	
	@Test
	public void testTaskToString() {
		// Exercise and verify phases (no setup phase required)
		assertThat(task.toString()).hasToString("Task{id='1',description='Buy groceries'}");
	}
}
