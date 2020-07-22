package it.unifi.simpletodoapp.repository.mongo;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.bson.Document;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;

import de.bwaldvogel.mongo.MongoServer;
import de.bwaldvogel.mongo.backend.memory.MemoryBackend;
import it.unifi.simpletodoapp.model.Tag;

public class TagMongoRepositoryTest {
	private static MongoServer mongoServer;
	private static InetSocketAddress serverAddress;
	private MongoClient mongoClient;
	private TagMongoRepository tagMongoRepository;
	private MongoCollection<Document> tagCollection;

	private static final String DB_NAME = "todoappdb";
	private static final String DB_COLLECTION = "tags";

	@BeforeClass
	public static void startServer() {
		mongoServer = new MongoServer(new MemoryBackend());
		serverAddress = mongoServer.bind();
	}

	@AfterClass
	public static void stopServer() {
		mongoServer.shutdown();
	}

	@Before
	public void setUp() {
		mongoClient = new MongoClient(new ServerAddress(serverAddress));
		mongoClient.getDatabase(DB_NAME).drop();

		tagMongoRepository = new TagMongoRepository(mongoClient, DB_NAME, DB_COLLECTION);
		tagCollection = mongoClient.getDatabase(DB_NAME)
				.getCollection(DB_COLLECTION);
	}

	@After
	public void tearDown() {
		mongoClient.close();
	}
	
	@Test
	public void testFindAllTagsWhenCollectionIsEmpty() {
		// Exercise and verify phases (no setup phase needed)
		assertThat(tagMongoRepository.findAll()).isEqualTo(Collections.emptyList());
	}
	
	@Test
	public void testFindAllTagsWhenCollectionIsNotEmpty() {
		// Setup phase
		Tag tag = new Tag("1", "Work");
		addTagToDatabase(tag, Collections.emptyList());

		// Exercise phase
		List<Tag> retrievedTags = tagMongoRepository.findAll();

		// Verify phase
		assertThat(retrievedTags).containsExactly(tag);
	}
	
	@Test
	public void testFindTagByIdWhenCollectionIsEmpty() {
		// Exercise and verify phases (no setup phase required)
		assertThat(tagMongoRepository.findById("1")).isNull();
	}

	@Test
	public void testFindTagByIdWhenCollectionIsNotEmpty() {
		// Setup phase
		Tag firstTag = new Tag("1", "Work");
		Tag secondTag = new Tag("2", "Important");
		addTagToDatabase(firstTag, Collections.emptyList());
		addTagToDatabase(secondTag, Collections.emptyList());

		// Exercise and verify phases
		assertThat(tagMongoRepository.findById("2")).isEqualTo(secondTag);
	}
	
	@Test
	public void testSaveTag() {
		// Setup phase
		Tag tag = new Tag("1", "Work");
		
		// Exercise phase
		tagMongoRepository.save(tag);
		
		// Verify phase
		assertThat(getTagsFromDatabase()).isEqualTo(Collections.singletonList(tag));
	}
	
	@Test
	public void testDeleteTag() {
		// Setup phase
		Tag tag = new Tag("1", "Work");
		addTagToDatabase(tag, Collections.emptyList());
		
		// Exercise phase
		tagMongoRepository.delete(tag);
		
		// Verify phase
		assertThat(getTagsFromDatabase()).isEmpty();
	}
	
	@Test
	public void testGetTasksWhenTaskListIsEmpty() {
		// Setup phase
		Tag tag = new Tag("1", "Work");
		addTagToDatabase(tag, Collections.emptyList());
		
		// Exercise phase
		List<String> retrievedTasks = tagMongoRepository.getTasksByTagId(tag.getId());
		
		// Verify phase
		assertThat(retrievedTasks).isEqualTo(Collections.emptyList());
	}
	
	@Test
	public void testGetTasksWhenTaskListIsNotEmpty() {
		// Setup phase
		Tag tag = new Tag("1", "Work");
		addTagToDatabase(tag, Collections.singletonList("1"));
		
		// Exercise phase
		List<String> retrievedTasks = tagMongoRepository.getTasksByTagId(tag.getId());
		
		// Verify phase
		assertThat(retrievedTasks).isEqualTo(Collections.singletonList("1"));
	}
	
	@Test
	public void testAddTaskToTag() {
		// Setup phase
		Tag tag = new Tag("1", "Work");
		addTagToDatabase(tag, Collections.emptyList());
		
		// Exercise phase
		tagMongoRepository.addTaskToTag(tag.getId(), "1");
		
		// Verify phase
		assertThat(tagMongoRepository.getTasksByTagId(tag.getId())).containsExactly("1");
	}
	
	@Test
	public void testRemoveTaskFromTag() {
		// Setup phase
		Tag tag = new Tag("1", "Work");
		addTagToDatabase(tag, Collections.singletonList("1"));
				
		// Exercise phase
		tagMongoRepository.removeTaskFromTag(tag.getId(), "1");
				
		// Verify phase
		assertThat(tagMongoRepository.getTasksByTagId(tag.getId())).isEmpty();
	}
	
	private void addTagToDatabase(Tag tag, List<String> tasks) {
		tagCollection.insertOne(new Document()
				.append("id", tag.getId())
				.append("name", tag.getName())
				.append("tasks", tasks)
				);
	}

	private List<Tag> getTagsFromDatabase() {
		return StreamSupport
				.stream(tagCollection.find().spliterator(), false)
				.map(d -> new Tag(d.getString("id"), d.getString("name")))
				.collect(Collectors.toList());
	}
}
