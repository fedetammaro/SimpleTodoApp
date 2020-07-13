package it.unifi.simpletodoapp.view.swing;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import it.unifi.simpletodoapp.model.Tag;
import it.unifi.simpletodoapp.model.Task;
import it.unifi.simpletodoapp.view.TodoView;
import javax.swing.JTabbedPane;

public class TodoSwingView extends JFrame implements TodoView {

	private static final long serialVersionUID = -8047380292195826724L;
	private JPanel contentPane;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					TodoSwingView frame = new TodoSwingView();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public TodoSwingView() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		contentPane.setName("contentPane");
		setContentPane(contentPane);
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		contentPane.add(tabbedPane, BorderLayout.CENTER);
		tabbedPane.setName("tabbedPane");
		
		JPanel tasksPanel = new JPanel();
		tasksPanel.setName("tasks_panel");
		tabbedPane.addTab("Tasks", null, tasksPanel, null);
		
		JPanel tagsPanel = new JPanel();
		tagsPanel.setName("tags_panel");
		tabbedPane.addTab("Tags", null, tagsPanel, null);
	}

	@Override
	public void showAllTasks(List<Task> allTasks) {
		// Method not yet implemented
	}

	@Override
	public void taskAdded(Task task) {
		// Method not yet implemented
	}

	@Override
	public void taskError(String errorMessage) {
		// Method not yet implemented
	}

	@Override
	public void taskDeleted(Task task) {
		// Method not yet implemented
	}

	@Override
	public void showAllTags(List<Tag> tags) {
		// Method not yet implemented
	}

	@Override
	public void tagAdded(Tag tag) {
		// Method not yet implemented
	}

	@Override
	public void tagError(String string) {
		// Method not yet implemented
	}

	@Override
	public void tagRemoved(Tag tag) {
		// Method not yet implemented
	}

	@Override
	public void showTaskTags(List<Tag> tags) {
		// Method not yet implemented
	}

	@Override
	public void showTagTasks(List<Task> tasks) {
		// Method not yet implemented
	}

}
