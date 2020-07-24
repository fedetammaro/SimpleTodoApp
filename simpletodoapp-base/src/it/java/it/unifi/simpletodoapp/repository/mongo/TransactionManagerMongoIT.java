package it.unifi.simpletodoapp.repository.mongo;

import static org.assertj.core.api.Assertions.*;

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
import it.unifi.simpletodoapp.model.Task;

public class TransactionManagerMongoIT {
	private static final int MONGO_PORT = 27017;
	private static final String DB_NAME = "todoapp";
	private static final String TASKS_COLLECTION = "tasks";
	private static final String TAGS_COLLECTION = "tags";

	@SuppressWarnings("rawtypes")
	@ClassRule
	public static final GenericContainer mongoContainer =
	new GenericContainer("krnbr/mongo").withExposedPorts(MONGO_PORT);
	
	private TransactionManagerMongo transactionManagerMongo;
	
	private MongoClient mongoClient;
	private TaskMongoRepository taskMongoRepository;
	private TagMongoRepository tagMongoRepository;
	private MongoCollection<Document> taskCollection;
	private MongoCollection<Document> tagCollection;

	@Before
	public void setup() {
		/* Creates the mongo client by connecting it to the mongodb instance, both
		 * repositories and collections and the transaction manager; also empties
		 * the database before each test */
		mongoClient = new MongoClient(new ServerAddress(
				mongoContainer.getContainerIpAddress(),
				mongoContainer.getMappedPort(MONGO_PORT))
				);
		taskMongoRepository = new TaskMongoRepository(mongoClient, DB_NAME, TASKS_COLLECTION);
		tagMongoRepository = new TagMongoRepository(mongoClient, DB_NAME, TAGS_COLLECTION);
		
		transactionManagerMongo = new TransactionManagerMongo(mongoClient, taskMongoRepository, tagMongoRepository);

		MongoDatabase database = mongoClient.getDatabase(DB_NAME);

		database.drop();
		taskCollection = database.getCollection(TASKS_COLLECTION);
		tagCollection = database.getCollection(TAGS_COLLECTION);
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
		transactionManagerMongo.doTaskTransaction(taskRepository -> {
			taskRepository.save(task);
			return null;
		});
		
		// Verify phase
		assertThat(getAllTasksFromDatabase()).containsExactly(task);
	}
	
	@Test
	public void testTagTransaction() {
		// Setup phase
		Tag tag = new Tag("1", "Work");
		
		// Exercise phase
		transactionManagerMongo.doTagTransaction(tagRepository -> {
			tagRepository.save(tag);
			return null;
		});

		// Verify phase
		assertThat(getAllTagsFromDatabase()).containsExactly(tag);
	}
	
	@Test
	public void testCompositeTransaction() {
		// Setup phase
		Task task = new Task("1", "Start using TDD");
		Tag tag = new Tag("1", "Work");
		
		// Exercise phase
		transactionManagerMongo.doCompositeTransaction((taskRepository, tagRepository) -> {
			taskRepository.save(task);
			tagRepository.save(tag);
			
			taskRepository.addTagToTask(task.getId(), tag.getId());
			tagRepository.addTaskToTag(tag.getId(), task.getId());
			return null;
		});

		// Verify phase
		assertThat(getTagsAssignedToTask(task)).containsExactly("1");
		assertThat(getTasksAssignedToTag(tag)).containsExactly("1");
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
