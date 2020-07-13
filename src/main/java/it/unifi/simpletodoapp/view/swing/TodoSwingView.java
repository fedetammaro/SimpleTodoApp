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
		
		JPanel tasks_panel = new JPanel();
		tasks_panel.setName("tasks_panel");
		tabbedPane.addTab("Tasks", null, tasks_panel, null);
		
		JPanel tags_panel = new JPanel();
		tags_panel.setName("tags_panel");
		tabbedPane.addTab("Tags", null, tags_panel, null);
	}

	@Override
	public void showAllTasks(List<Task> allTasks) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void taskAdded(Task task) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void taskError(String errorMessage) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void taskDeleted(Task task) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void showAllTags(List<Tag> tags) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void tagAdded(Tag tag) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void tagError(String string) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void tagRemoved(Tag tag) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void showTaskTags(List<Tag> tags) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void showTagTasks(List<Task> tasks) {
		// TODO Auto-generated method stub
		
	}

}
