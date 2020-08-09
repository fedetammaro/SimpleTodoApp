package it.unifi.simpletodoapp;

import java.awt.EventQueue;
import java.util.concurrent.Callable;

import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;

import it.unifi.simpletodoapp.controller.TodoController;
import it.unifi.simpletodoapp.repository.mongo.TagMongoRepository;
import it.unifi.simpletodoapp.repository.mongo.TaskMongoRepository;
import it.unifi.simpletodoapp.repository.mongo.TransactionManagerMongo;
import it.unifi.simpletodoapp.service.TodoService;
import it.unifi.simpletodoapp.view.swing.TodoSwingView;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(mixinStandardHelpOptions = true)
public class TodoApplication implements Callable<Void> {
	@Option(names = { "--mongo-host" }, description = "MongoDB instance address")
	private String mongodbHost = "localhost";

	@Option(names = { "--mongo-port" }, description = "MongoDB instance port")
	private int mongodbPort = 27017;

	@Option(names = { "--db-name" }, description = "Database name")
	private String dbName = "todoapp";

	@Option(names = { "--db-tasksCollection" }, description = "Tasks collection name")
	private String tasksCollection = "tasks";

	@Option(names = { "--db-tagsCollection" }, description = "Tags collection name")
	private String tagsCollection = "tags";

	public static void main(String[] args) {
		new CommandLine(new TodoApplication()).execute(args);
	}

	@Override
	public Void call() throws Exception {
		EventQueue.invokeLater(() -> {
			MongoClient mongoClient = 
					new MongoClient(new ServerAddress(mongodbHost, mongodbPort));
			TaskMongoRepository taskRepository = 
					new TaskMongoRepository(mongoClient, dbName, tasksCollection);
			TagMongoRepository tagRepository =
					new TagMongoRepository(mongoClient, dbName, tagsCollection);
			TransactionManagerMongo transactionManagerMongo = 
					new TransactionManagerMongo(mongoClient, taskRepository, tagRepository);
			TodoService todoService = new TodoService(transactionManagerMongo);
			TodoSwingView todoSwingView = new TodoSwingView();
			TodoController todoController = new TodoController(todoService, todoSwingView);
			todoSwingView.setTodoController(todoController);
			todoSwingView.setVisible(true);
		});

		return null;
	}
}