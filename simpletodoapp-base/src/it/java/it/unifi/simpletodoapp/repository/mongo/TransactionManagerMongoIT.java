package it.unifi.simpletodoapp.repository.mongo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;

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

import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import it.unifi.simpletodoapp.model.Tag;
import it.unifi.simpletodoapp.model.Task;

public class TransactionManagerMongoIT {
	private static final int MONGO_PORT = 27017;
	private static final String DB_NAME = "todoapp";
	private static final String TASKS_COLLECTION = "tasks";
	private static final String TAGS_COLLECTION = "tags";

	@ClassRule
	public static final MongoDBContainer mongoContainer = new MongoDBContainer()
	.withExposedPorts(MONGO_PORT);

	private TransactionManagerMongo transactionManagerMongo;

	private MongoClient mongoClient;
	private MongoDatabase mongoDatabase;
	private TaskMongoRepository taskMongoRepository;
	private TagMongoRepository tagMongoRepository;
	private MongoCollection<Document> taskCollection;
	private MongoCollection<Document> tagCollection;
	
	@BeforeClass
	public static void setupMongoLogger() {
		LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
		Logger rootLogger = loggerContext.getLogger("org.mongodb.driver");
		rootLogger.setLevel(Level.INFO);
	}

	@Before
	public void setup() {
		/* Creates the mongo client by connecting it to the mongodb instance, both
		 * repositories and collections and the transaction manager; also empties
		 * the database before each test */
		String mongoRsUrl = mongoContainer.getReplicaSetUrl();
		mongoClient = MongoClients.create(mongoRsUrl);

		taskMongoRepository = new TaskMongoRepository(mongoClient, DB_NAME, TASKS_COLLECTION);
		tagMongoRepository = new TagMongoRepository(mongoClient, DB_NAME, TAGS_COLLECTION);

		transactionManagerMongo = new TransactionManagerMongo(mongoClient, taskMongoRepository, tagMongoRepository);

		mongoDatabase = mongoClient.getDatabase(DB_NAME);

		mongoDatabase.drop();
		mongoDatabase.createCollection(TASKS_COLLECTION);
		mongoDatabase.createCollection(TAGS_COLLECTION);
		taskCollection = mongoDatabase.getCollection(TASKS_COLLECTION);
		tagCollection = mongoDatabase.getCollection(TAGS_COLLECTION);
	}
	
	@After
	public void tearDown() {
		/* Close the client connection after each test so that it can
		 * be created anew in the next test */
		mongoClient.close();
	}

	@AfterClass
	public static void stopContainer() {
		// Stops the container after all methods have been executed
		mongoContainer.stop();
	}

	@Test
	public void testTaskTransaction() {
		// Setup phase
		Task task = new Task("1", "Start using TDD");

		// Exercise phase
		transactionManagerMongo.doTaskTransaction(
				(taskMongoRepository, clientSession) -> {
					taskMongoRepository.save(task, clientSession);
					return null;
				});

		// Verify phase
		assertThat(getAllTasksFromDatabase())
		.containsExactly(task);
	}

	@Test
	public void testTaskTransactionAbortedThrowsMongoException() {
		// Setup phase
		mongoDatabase.drop();
		Task task = new Task("1", "Start using TDD");

		// Exercise and verify phases
		MongoException exception = assertThrows(MongoException.class,
				() -> transactionManagerMongo.doTaskTransaction(
						(taskMongoRepository, clientSession) -> {
							taskMongoRepository.save(task, clientSession);
							return null;
						}));
		assertThat(exception.getMessage())
		.isEqualTo("Task transaction failed, aborting");
	}

	@Test
	public void testTagTransaction() {
		// Setup phase
		Tag tag = new Tag("1", "Work");

		// Exercise phase
		transactionManagerMongo.doTagTransaction(
				(tagMongoRepository, clientSession) -> {
					tagMongoRepository.save(tag, clientSession);
					return null;
				});

		// Verify phase
		assertThat(getAllTagsFromDatabase())
		.containsExactly(tag);
	}

	@Test
	public void testTagTransactionAbortedThrowsMongoException() {
		// Setup phase
		mongoDatabase.drop();
		Tag tag = new Tag("1", "Work");

		// Exercise and verify phases
		MongoException exception = assertThrows(MongoException.class,
				() -> transactionManagerMongo.doTagTransaction(
						(tagMongoRepository, clientSession) -> {
							tagMongoRepository.save(tag, clientSession);
							return null;
						}));
		assertThat(exception.getMessage())
		.isEqualTo("Tag transaction failed, aborting");
	}

	@Test
	public void testCompositeTransaction() {
		// Setup phase
		Task task = new Task("1", "Start using TDD");
		Tag tag = new Tag("1", "Work");

		// Exercise phase
		transactionManagerMongo.doCompositeTransaction(
				(taskMongoRepository, tagMongoRepository, clientSession) -> {
					taskMongoRepository.save(task, clientSession);
					tagMongoRepository.save(tag, clientSession);

					taskMongoRepository.addTagToTask(task.getId(), tag.getId(), clientSession);
					tagMongoRepository.addTaskToTag(tag.getId(), task.getId(), clientSession);
					return null;
				});

		// Verify phase
		assertThat(getTagsAssignedToTask(task))
		.containsExactly("1");
		assertThat(getTasksAssignedToTag(tag))
		.containsExactly("1");
	}

	@Test
	public void testCompositeTransactionAbortedThrowsMongoException() {
		// Setup phase
		mongoDatabase.drop();
		Task task = new Task("1", "Start using TDD");
		Tag tag = new Tag("1", "Work");

		// Exercise and verify phases
		MongoException exception = assertThrows(MongoException.class,
				() -> transactionManagerMongo.doCompositeTransaction(
						(taskMongoRepository, tagMongoRepository, clientSession) -> {
							taskMongoRepository.save(task, clientSession);
							tagMongoRepository.save(tag, clientSession);
							return null;
						}));
		assertThat(exception.getMessage())
		.isEqualTo("Composite transaction failed, aborting");
	}

	private List<Task> getAllTasksFromDatabase() {
		// Private method to directly retrieve all tasks from the collection
		return StreamSupport
				.stream(taskCollection.find().spliterator(), false)
				.map(d -> new Task(d.getString("id"), d.getString("description")))
				.collect(Collectors.toList());
	}

	private List<Tag> getAllTagsFromDatabase() {
		// Private method to directly retrieve all tags from the collection
		return StreamSupport
				.stream(tagCollection.find().spliterator(), false)
				.map(d -> new Tag(d.getString("id"), d.getString("name")))
				.collect(Collectors.toList());
	}

	private List<String> getTagsAssignedToTask(Task task) {
		/* Private method to directly retrieve all tags assigned to a task 
		 * from the collection */
		return taskCollection
				.find(Filters.eq("id", task.getId()))
				.first()
				.getList("tags", String.class);
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
