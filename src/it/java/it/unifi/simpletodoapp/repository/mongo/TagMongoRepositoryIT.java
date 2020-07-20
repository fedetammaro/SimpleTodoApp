package it.unifi.simpletodoapp.repository.mongo;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.bson.Document;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.GenericContainer;

import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

import it.unifi.simpletodoapp.model.Tag;

public class TagMongoRepositoryIT {
	@SuppressWarnings("rawtypes")
	@ClassRule
	public static final GenericContainer mongoContainer =
	new GenericContainer("mongo").withExposedPorts(27017);

	private MongoClient mongoClient;
	private TagMongoRepository tagMongoRepository;
	private MongoCollection<Document> tagCollection;

	@Before
	public void setup() {
		mongoClient = new MongoClient(new ServerAddress(
				mongoContainer.getContainerIpAddress(),
				mongoContainer.getMappedPort(27017)));
		tagMongoRepository = new TagMongoRepository(mongoClient, "todoapp", "tags");

		MongoDatabase database = mongoClient.getDatabase("todoapp");

		database.drop();
		tagCollection = database.getCollection("tags");
	}

	@After
	public void tearDown() {
		mongoClient.close();
	}
	
	@AfterClass
	public static void stopContainer() {
		mongoContainer.stop();
	}

	@Test
	public void testFindAllTags() {
		Tag firstTag = new Tag("1", "Work");
		Tag secondTag = new Tag("2", "Important");
		addTagToDatabase(firstTag, Collections.emptyList());
		addTagToDatabase(secondTag, Collections.emptyList());

		assertThat(tagMongoRepository.findAll()).containsExactly(firstTag, secondTag);
	}

	@Test
	public void testFindById() {
		Tag firstTag = new Tag("1", "Work");
		Tag secondTag = new Tag("2", "Important");
		addTagToDatabase(firstTag, Collections.emptyList());
		addTagToDatabase(secondTag, Collections.emptyList());

		assertThat(tagMongoRepository.findById(secondTag.getId())).isEqualTo(secondTag);
	}

	@Test
	public void testSave() {
		Tag tag = new Tag("1", "Work");

		tagMongoRepository.save(tag);

		assertThat(getAllTagsFromDatabase()).containsExactly(tag);
	}

	@Test
	public void testDelete() {
		Tag tag = new Tag("1", "Work");
		addTagToDatabase(tag, Collections.emptyList());
		
		tagMongoRepository.delete(tag);

		assertThat(getAllTagsFromDatabase()).isEmpty();
	}

	@Test
	public void testGetTasksByTagId() {
		Tag tag = new Tag("1", "Work");
		addTagToDatabase(tag, Arrays.asList("1", "2"));

		assertThat(tagMongoRepository.getTasksByTagId(tag.getId()))
		.containsExactly("1", "2");
	}
	
	@Test
	public void testAddTaskToTag() {
		Tag tag = new Tag("1", "Work");
		addTagToDatabase(tag, Arrays.asList("1"));
		
		tagMongoRepository.addTaskToTag(tag.getId(), "2");
		
		assertThat(getTasksAssignedToTag(tag)).containsExactly("1", "2");
	}
	
	@Test
	public void testRemoveTaskFromTag() {
		Tag tag = new Tag("1", "Work");
		addTagToDatabase(tag, Arrays.asList("1", "2"));
		
		tagMongoRepository.removeTaskFromTag(tag.getId(), "2");
		
		assertThat(getTasksAssignedToTag(tag)).containsExactly("1");
	}

	private void addTagToDatabase(Tag tag, List<String> tasks) {
		tagCollection.insertOne(
				new Document()
				.append("id", tag.getId())
				.append("name", tag.getName())
				.append("tasks", tasks));
	}

	private List<Tag> getAllTagsFromDatabase() {
		return StreamSupport
				.stream(tagCollection.find().spliterator(), false)
				.map(d -> new Tag(d.getString("id"), d.getString("name")))
				.collect(Collectors.toList());
	}
	
	private List<String> getTasksAssignedToTag(Tag tag) {
		return tagCollection
				.find(Filters.eq("id", tag.getId()))
				.first()
				.getList("tasks", String.class);
	}
}

