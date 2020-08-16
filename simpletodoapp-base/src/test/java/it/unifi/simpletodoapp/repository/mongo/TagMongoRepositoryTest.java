package it.unifi.simpletodoapp.repository.mongo;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.bson.Document;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.MongoDBContainer;

import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import it.unifi.simpletodoapp.model.Tag;

public class TagMongoRepositoryTest {
	private static final String DB_NAME = "todoappdb";
	private static final String DB_COLLECTION = "tags";
	private static final int MONGO_PORT = 27017;

	private MongoClient mongoClient;
	private ClientSession clientSession;
	private TagMongoRepository tagMongoRepository;
	private MongoCollection<Document> tagCollection;

	@ClassRule
	public static final MongoDBContainer mongoContainer = new MongoDBContainer()
	.withExposedPorts(MONGO_PORT);
	
	@BeforeClass
	public static void setupMongoLogger() {
		LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
		Logger rootLogger = loggerContext.getLogger("org.mongodb.driver");
		rootLogger.setLevel(Level.INFO);
	}

	@Before
	public void setup() {
		/* Creates the mongo client by connecting it to the mongodb instance, the tag
		 * repository and collection; also empties the database before each test */
		String mongoRsUrl = mongoContainer.getReplicaSetUrl();
		mongoClient = MongoClients.create(mongoRsUrl);
		clientSession = mongoClient.startSession();
		
		tagMongoRepository = new TagMongoRepository(mongoClient, DB_NAME, DB_COLLECTION);

		MongoDatabase database = mongoClient.getDatabase(DB_NAME);

		database.drop();
		database.createCollection(DB_COLLECTION);
		tagCollection = database.getCollection(DB_COLLECTION);
	}

	@After
	public void tearDown() {
		/* Close the client connection after each test so that it can
		 * be created anew in the next test */
		clientSession.close();
		mongoClient.close();
	}

	@AfterClass
	public static void stopContainer() {
		// Stops the container after all methods have been executed
		mongoContainer.stop();
	}

	@Test
	public void testFindAllTagsWhenCollectionIsEmpty() {
		// Exercise and verify phases (no setup phase needed)
		assertThat(tagMongoRepository.findAll(clientSession))
		.isEqualTo(Collections.emptyList());
	}

	@Test
	public void testFindAllTagsWhenCollectionIsNotEmpty() {
		// Setup phase
		Tag tag = new Tag("1", "Work");
		addTagToDatabase(tag, Collections.emptyList());

		// Exercise phase
		List<Tag> retrievedTags = tagMongoRepository.findAll(clientSession);

		// Verify phase
		assertThat(retrievedTags)
		.containsExactly(tag);
	}

	@Test
	public void testFindTagByIdWhenCollectionIsEmpty() {
		// Exercise and verify phases (no setup phase required)
		assertThat(tagMongoRepository.findById("1", clientSession))
		.isNull();
	}

	@Test
	public void testFindTagByIdWhenCollectionIsNotEmpty() {
		// Setup phase
		Tag firstTag = new Tag("1", "Work");
		Tag secondTag = new Tag("2", "Important");
		addTagToDatabase(firstTag, Collections.emptyList());
		addTagToDatabase(secondTag, Collections.emptyList());

		// Exercise and verify phases
		assertThat(tagMongoRepository.findById("2", clientSession))
		.isEqualTo(secondTag);
	}

	@Test
	public void testSaveTag() {
		// Setup phase
		Tag tag = new Tag("1", "Work");

		// Exercise phase
		tagMongoRepository.save(tag, clientSession);

		// Verify phase
		assertThat(getTagsFromDatabase())
		.isEqualTo(Collections.singletonList(tag));
	}

	@Test
	public void testDeleteTag() {
		// Setup phase
		Tag tag = new Tag("1", "Work");
		addTagToDatabase(tag, Collections.emptyList());

		// Exercise phase
		tagMongoRepository.delete(tag, clientSession);

		// Verify phase
		assertThat(getTagsFromDatabase())
		.isEmpty();
	}

	@Test
	public void testGetTasksWhenTaskListIsEmpty() {
		// Setup phase
		Tag tag = new Tag("1", "Work");
		addTagToDatabase(tag, Collections.emptyList());

		// Exercise phase
		List<String> retrievedTasks = tagMongoRepository.getTasksByTagId(tag.getId(), clientSession);

		// Verify phase
		assertThat(retrievedTasks)
		.isEqualTo(Collections.emptyList());
	}

	@Test
	public void testGetTasksWhenTaskListIsNotEmpty() {
		// Setup phase
		Tag tag = new Tag("1", "Work");
		addTagToDatabase(tag, Collections.singletonList("1"));

		// Exercise phase
		List<String> retrievedTasks = tagMongoRepository.getTasksByTagId(tag.getId(), clientSession);

		// Verify phase
		assertThat(retrievedTasks)
		.isEqualTo(Collections.singletonList("1"));
	}

	@Test
	public void testAddTaskToTag() {
		// Setup phase
		Tag tag = new Tag("1", "Work");
		addTagToDatabase(tag, Collections.emptyList());

		// Exercise phase
		tagMongoRepository.addTaskToTag(tag.getId(), "1", clientSession);

		// Verify phase
		assertThat(tagMongoRepository.getTasksByTagId(tag.getId(), clientSession))
		.containsExactly("1");
	}

	@Test
	public void testRemoveTaskFromTag() {
		// Setup phase
		Tag tag = new Tag("1", "Work");
		addTagToDatabase(tag, Collections.singletonList("1"));

		// Exercise phase
		tagMongoRepository.removeTaskFromTag(tag.getId(), "1", clientSession);

		// Verify phase
		assertThat(tagMongoRepository.getTasksByTagId(tag.getId(), clientSession))
		.isEmpty();
	}

	private void addTagToDatabase(Tag tag, List<String> tasks) {
		// Private method to directly insert a tag in the collection
		tagCollection.insertOne(new Document()
				.append("id", tag.getId())
				.append("name", tag.getName())
				.append("tasks", tasks)
				);
	}

	private List<Tag> getTagsFromDatabase() {
		// Private method to directly retrieve all tags from the collection
		return StreamSupport
				.stream(tagCollection.find().spliterator(), false)
				.map(d -> new Tag(d.getString("id"), d.getString("name")))
				.collect(Collectors.toList());
	}
}