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
import com.mongodb.client.model.Filters;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import it.unifi.simpletodoapp.model.Tag;

public class TagMongoRepositoryIT {
	private static final int MONGO_PORT = 27017;
	private static final String DB_NAME = "todoapp";
	private static final String TAGS_COLLECTION = "tags";

	@ClassRule
	public static final MongoDBContainer mongoContainer = new MongoDBContainer()
	.withExposedPorts(MONGO_PORT);

	private MongoClient mongoClient;
	private ClientSession clientSession;
	private TagMongoRepository tagMongoRepository;
	private MongoCollection<Document> tagCollection;
	
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
		
		tagMongoRepository = new TagMongoRepository(mongoClient, DB_NAME, TAGS_COLLECTION);

		MongoDatabase database = mongoClient.getDatabase(DB_NAME);

		database.drop();
		database.createCollection(TAGS_COLLECTION);
		tagCollection = database.getCollection(TAGS_COLLECTION);
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
	public void testFindAllTags() {
		// Setup phase
		Tag firstTag = new Tag("1", "Work");
		Tag secondTag = new Tag("2", "Important");
		addTagToCollection(firstTag, Collections.emptyList());
		addTagToCollection(secondTag, Collections.emptyList());

		// Exercise and verify phases
		assertThat(tagMongoRepository.findAll(clientSession))
		.containsExactly(firstTag, secondTag);
	}

	@Test
	public void testFindById() {
		// Setup phase
		Tag firstTag = new Tag("1", "Work");
		Tag secondTag = new Tag("2", "Important");
		addTagToCollection(firstTag, Collections.emptyList());
		addTagToCollection(secondTag, Collections.emptyList());

		// Exercise and verify phases
		assertThat(tagMongoRepository.findById(secondTag.getId(), clientSession))
		.isEqualTo(secondTag);
	}

	@Test
	public void testSave() {
		// Setup phase
		Tag tag = new Tag("1", "Work");

		// Exercise phase
		tagMongoRepository.save(tag, clientSession);

		// Verify phase
		assertThat(getAllTagsFromDatabase())
		.containsExactly(tag);
	}

	@Test
	public void testDelete() {
		// Setup phase
		Tag tag = new Tag("1", "Work");
		addTagToCollection(tag, Collections.emptyList());

		// Exercise phase
		tagMongoRepository.delete(tag, clientSession);

		// Verify phase
		assertThat(getAllTagsFromDatabase())
		.isEmpty();
	}

	@Test
	public void testGetTasksByTagId() {
		// Setup phase
		Tag tag = new Tag("1", "Work");
		addTagToCollection(tag, Arrays.asList("1", "2"));

		// Exercise and verify phases
		assertThat(tagMongoRepository.getTasksByTagId(tag.getId(), clientSession))
		.containsExactly("1", "2");
	}

	@Test
	public void testAddTaskToTag() {
		// Setup phase
		Tag tag = new Tag("1", "Work");
		addTagToCollection(tag, Arrays.asList("1"));

		// Exercise phase
		tagMongoRepository.addTaskToTag(tag.getId(), "2", clientSession);

		// Verify phase
		assertThat(getTasksAssignedToTag(tag))
		.containsExactly("1", "2");
	}

	@Test
	public void testRemoveTaskFromTag() {
		// Setup phase
		Tag tag = new Tag("1", "Work");
		addTagToCollection(tag, Arrays.asList("1", "2"));

		// Exercise phase
		tagMongoRepository.removeTaskFromTag(tag.getId(), "2", clientSession);

		// Verify phase
		assertThat(getTasksAssignedToTag(tag))
		.containsExactly("1");
	}

	private void addTagToCollection(Tag tag, List<String> tasks) {
		// Private method to directly insert a tag in the collection
		tagCollection.insertOne(new Document()
				.append("id", tag.getId())
				.append("name", tag.getName())
				.append("tasks", tasks)
				);
	}

	private List<Tag> getAllTagsFromDatabase() {
		// Private method to directly retrieve all tags from the collection
		return StreamSupport
				.stream(tagCollection.find().spliterator(), false)
				.map(d -> new Tag(d.getString("id"), d.getString("name")))
				.collect(Collectors.toList());
	}

	private List<String> getTasksAssignedToTag(Tag tag) {
		/* Private method to directly retrieve all tasks assigned to a tag 
		 * from the collection */
		return tagCollection
				.find(Filters.eq("id", tag.getId()))
				.first()
				.getList("tasks", String.class);
	}
}