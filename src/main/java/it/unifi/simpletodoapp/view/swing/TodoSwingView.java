package it.unifi.simpletodoapp.view.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import it.unifi.simpletodoapp.controller.TodoController;
import it.unifi.simpletodoapp.model.Tag;
import it.unifi.simpletodoapp.model.Task;
import it.unifi.simpletodoapp.view.TodoView;
import javax.swing.JTabbedPane;
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import java.awt.GridBagConstraints;
import javax.swing.JTextField;
import javax.swing.UIManager;

import java.awt.Insets;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JSeparator;
import javax.swing.JList;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.JComboBox;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.ActionEvent;

public class TodoSwingView extends JFrame implements TodoView {

	private static final long serialVersionUID = -8047380292195826724L;
	
	private TodoController todoController;
	
	private JPanel contentPane;
	private JTextField taskDescriptionTextField;
	private JTextField taskIdTextField;
	private JButton btnAddTask;
	private JList<TaskViewModel> tasksTaskList;
	private TaskListModel taskListModel = new TaskListModel();
	private JButton btnDeleteTask;
	private JComboBox<TagViewModel> tagsComboBox;
	private TagComboModel tagComboModel = new TagComboModel(); 
	private JButton btnAssignTag;
	private JList<Tag> assignedTagsList;
	private JButton btnRemoveTag;
	private JLabel tasksErrorLabel;
	
	static final class TaskViewModel {
		private Task task;

		public TaskViewModel(Task task) {
			this.task = task;
		}

		@Override
		public String toString() {
			return "#" + task.getId() + " - " + task.getDescription();
		}
		
		@Override
		public boolean equals(Object object) {
			TaskViewModel taskViewModel = (TaskViewModel) object;
			
			return taskViewModel.task.equals(this.task);
		}
	}

	static final class TaskListModel extends DefaultListModel<TaskViewModel> {
		private static final long serialVersionUID = 1L;

		public void addTask(Task task) {
			addElement(new TaskViewModel(task));
		}

		public void removeTask(Task task) {
			removeElement(new TaskViewModel(task));
		}
	}
	
	static final class TagViewModel {
		private Tag tag;

		public TagViewModel(Tag tag) {
			this.tag = tag;
		}

		@Override
		public String toString() {
			return "(" + tag.getId() + ") " + tag.getName();
		}
		
		@Override
		public boolean equals(Object object) {
			TagViewModel tagViewModel = (TagViewModel) object;
			
			return tagViewModel.tag.equals(this.tag);
		}
	}

	static final class TagComboModel extends DefaultComboBoxModel<TagViewModel> {
		private static final long serialVersionUID = 1L;

		public void addTag(Tag tag) {
			addElement(new TagViewModel(tag));
		}

		public void removeTag(Tag tag) {
			removeElement(new TagViewModel(tag));
		}
	}
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					UIManager.setLookAndFeel(
							UIManager.getCrossPlatformLookAndFeelClassName());
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
		setTitle("Simple Todo Application");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 720, 500);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		contentPane.setName("contentPane");
		setContentPane(contentPane);

		JTabbedPane tabbedPane = new JTabbedPane(SwingConstants.TOP);
		contentPane.add(tabbedPane, BorderLayout.CENTER);
		tabbedPane.setName("tabbedPane");

		JPanel tasksPanel = new JPanel();
		tasksPanel.setName("tasksPanel");
		tabbedPane.addTab("Tasks", null, tasksPanel, null);
		GridBagLayout gblTasksPanel = new GridBagLayout();
		gblTasksPanel.columnWidths = new int[]{150, 200, 350, 0};
		gblTasksPanel.rowHeights = new int[]{20, 20, 20, 25, 0, 0, 0, 0};
		gblTasksPanel.columnWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		gblTasksPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
		tasksPanel.setLayout(gblTasksPanel);

		JLabel taskIdLabel = new JLabel("id");
		taskIdLabel.setName("tasksIdLabel");
		GridBagConstraints gbcTaskIdLabel = new GridBagConstraints();
		gbcTaskIdLabel.insets = new Insets(0, 0, 5, 5);
		gbcTaskIdLabel.gridx = 0;
		gbcTaskIdLabel.gridy = 1;
		tasksPanel.add(taskIdLabel, gbcTaskIdLabel);

		taskIdTextField = new JTextField();
		taskIdTextField.setName("tasksIdTextField");
		GridBagConstraints gbcTaskIdTextField = new GridBagConstraints();
		gbcTaskIdTextField.insets = new Insets(0, 0, 5, 5);
		gbcTaskIdTextField.fill = GridBagConstraints.HORIZONTAL;
		gbcTaskIdTextField.gridx = 1;
		gbcTaskIdTextField.gridy = 1;
		tasksPanel.add(taskIdTextField, gbcTaskIdTextField);
		taskIdTextField.setColumns(10);

		JLabel taskDescriptionLabel = new JLabel("description");
		taskDescriptionLabel.setName("tasksDescriptionLabel");
		GridBagConstraints gbcTaskDescriptionLabel = new GridBagConstraints();
		gbcTaskDescriptionLabel.insets = new Insets(0, 0, 5, 5);
		gbcTaskDescriptionLabel.gridx = 0;
		gbcTaskDescriptionLabel.gridy = 2;
		tasksPanel.add(taskDescriptionLabel, gbcTaskDescriptionLabel);

		taskDescriptionTextField = new JTextField();
		taskDescriptionTextField.setName("tasksDescriptionTextField");
		GridBagConstraints gbcTaskDescriptionTextField = new GridBagConstraints();
		gbcTaskDescriptionTextField.gridwidth = 2;
		gbcTaskDescriptionTextField.insets = new Insets(0, 0, 5, 0);
		gbcTaskDescriptionTextField.fill = GridBagConstraints.HORIZONTAL;
		gbcTaskDescriptionTextField.gridx = 1;
		gbcTaskDescriptionTextField.gridy = 2;
		tasksPanel.add(taskDescriptionTextField, gbcTaskDescriptionTextField);
		taskDescriptionTextField.setColumns(10);

		btnAddTask = new JButton("Add task");
		btnAddTask.setEnabled(false);
		btnAddTask.setName("btnAddTask");
		GridBagConstraints gbcBtnAddTask = new GridBagConstraints();
		gbcBtnAddTask.insets = new Insets(0, 0, 5, 0);
		gbcBtnAddTask.gridwidth = 3;
		gbcBtnAddTask.gridx = 0;
		gbcBtnAddTask.gridy = 3;
		tasksPanel.add(btnAddTask, gbcBtnAddTask);
		
		btnAddTask.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				todoController.addTask(new Task(taskIdTextField.getText(), taskDescriptionTextField.getText()));
			}
		});
		
		KeyAdapter btnAddEnabler = new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				btnAddTask.setEnabled(
						!taskIdTextField.getText().trim().isEmpty()
						&& !taskDescriptionTextField.getText().trim().isEmpty());
			}
		};
		
		taskIdTextField.addKeyListener(btnAddEnabler);
		taskDescriptionTextField.addKeyListener(btnAddEnabler);

		JSeparator tasksTabSeparator = new JSeparator();
		tasksTabSeparator.setName("tasksTabSeparator");
		GridBagConstraints gbcTasksTabSeparator = new GridBagConstraints();
		gbcTasksTabSeparator.insets = new Insets(0, 0, 5, 0);
		gbcTasksTabSeparator.gridwidth = 3;
		gbcTasksTabSeparator.gridx = 0;
		gbcTasksTabSeparator.gridy = 4;
		tasksPanel.add(tasksTabSeparator, gbcTasksTabSeparator);

		JPanel tasksLeftSubPanel = new JPanel();
		tasksLeftSubPanel.setName("tasksLeftSubPanel");
		GridBagConstraints gbcTasksLeftSubPanel = new GridBagConstraints();
		gbcTasksLeftSubPanel.gridwidth = 2;
		gbcTasksLeftSubPanel.insets = new Insets(0, 0, 5, 5);
		gbcTasksLeftSubPanel.fill = GridBagConstraints.VERTICAL;
		gbcTasksLeftSubPanel.gridx = 0;
		gbcTasksLeftSubPanel.gridy = 5;
		tasksPanel.add(tasksLeftSubPanel, gbcTasksLeftSubPanel);
		GridBagLayout gblTasksLeftSubPanel = new GridBagLayout();
		gblTasksLeftSubPanel.columnWidths = new int[]{34, 34, 34, 34, 34, 34, 34, 34, 34, 34};
		gblTasksLeftSubPanel.rowHeights = new int[]{20, 320, 0, 0};
		gblTasksLeftSubPanel.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
		gblTasksLeftSubPanel.rowWeights = new double[]{0.0, 1.0, 0.0, Double.MIN_VALUE};
		tasksLeftSubPanel.setLayout(gblTasksLeftSubPanel);

		JLabel tasksTaskListLabel = new JLabel("Todo List");
		tasksTaskListLabel.setName("tasksTaskListLabel");
		GridBagConstraints gbcTasksTaskListLabel = new GridBagConstraints();
		gbcTasksTaskListLabel.insets = new Insets(0, 0, 5, 0);
		gbcTasksTaskListLabel.gridwidth = 10;
		gbcTasksTaskListLabel.gridx = 0;
		gbcTasksTaskListLabel.gridy = 0;
		tasksLeftSubPanel.add(tasksTaskListLabel, gbcTasksTaskListLabel);

		tasksTaskList = new JList<>(taskListModel);
		tasksTaskList.setName("tasksTaskList");
		tasksTaskList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		GridBagConstraints gbcTasksTaskList = new GridBagConstraints();
		gbcTasksTaskList.insets = new Insets(0, 0, 5, 0);
		gbcTasksTaskList.gridwidth = 10;
		gbcTasksTaskList.fill = GridBagConstraints.BOTH;
		gbcTasksTaskList.gridx = 0;
		gbcTasksTaskList.gridy = 1;
		tasksLeftSubPanel.add(tasksTaskList, gbcTasksTaskList);

		tasksTaskList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				boolean isTaskSelected = tasksTaskList.getSelectedIndex() != -1;
				btnDeleteTask.setEnabled(isTaskSelected);
				tagsComboBox.setEnabled(isTaskSelected);
				btnAssignTag.setEnabled(isTaskSelected);
			}
		});
		
		btnDeleteTask = new JButton("Delete task");
		btnDeleteTask.setEnabled(false);
		btnDeleteTask.setName("btnDeleteTask");
		GridBagConstraints gbcBtnDeleteTask = new GridBagConstraints();
		gbcBtnDeleteTask.gridwidth = 10;
		gbcBtnDeleteTask.gridx = 0;
		gbcBtnDeleteTask.gridy = 2;
		tasksLeftSubPanel.add(btnDeleteTask, gbcBtnDeleteTask);
		
		btnDeleteTask.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Task task = taskListModel.get(tasksTaskList.getSelectedIndex()).task;
				todoController.deleteTask(task);
			}
		});
		
		JPanel tasksRightSubPanel = new JPanel();
		tasksRightSubPanel.setName("tasksRightSubPanel");
		GridBagConstraints gbcTasksRightSubPanel = new GridBagConstraints();
		gbcTasksRightSubPanel.insets = new Insets(0, 0, 5, 0);
		gbcTasksRightSubPanel.fill = GridBagConstraints.BOTH;
		gbcTasksRightSubPanel.gridx = 2;
		gbcTasksRightSubPanel.gridy = 5;
		tasksPanel.add(tasksRightSubPanel, gbcTasksRightSubPanel);
		GridBagLayout gblTasksRightSubPanel = new GridBagLayout();
		gblTasksRightSubPanel.columnWidths = new int[]{34, 34, 34, 34, 34, 34, 34, 34, 34, 34};
		gblTasksRightSubPanel.rowHeights = new int[]{25, 25, 290, 25, 0};
		gblTasksRightSubPanel.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
		gblTasksRightSubPanel.rowWeights = new double[]{0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
		tasksRightSubPanel.setLayout(gblTasksRightSubPanel);

		tagsComboBox = new JComboBox<>(tagComboModel);
		tagsComboBox.setEnabled(false);
		tagsComboBox.setName("tagComboBox");
		GridBagConstraints gbcTagComboBox = new GridBagConstraints();
		gbcTagComboBox.insets = new Insets(0, 0, 5, 0);
		gbcTagComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbcTagComboBox.gridwidth = 6;
		gbcTagComboBox.gridx = 2;
		gbcTagComboBox.gridy = 0;
		tasksRightSubPanel.add(tagsComboBox, gbcTagComboBox);

		btnAssignTag = new JButton("Assign tag");
		btnAssignTag.setEnabled(false);
		btnAssignTag.setName("btnAssignTag");
		btnAssignTag.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		GridBagConstraints gbcBtnAssignTag = new GridBagConstraints();
		gbcBtnAssignTag.insets = new Insets(0, 0, 5, 0);
		gbcBtnAssignTag.gridwidth = 10;
		gbcBtnAssignTag.gridx = 0;
		gbcBtnAssignTag.gridy = 1;
		tasksRightSubPanel.add(btnAssignTag, gbcBtnAssignTag);

		assignedTagsList = new JList<>();
		assignedTagsList.setName("assignedTagsList");
		assignedTagsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		GridBagConstraints gbcAssignedTagsList = new GridBagConstraints();
		gbcAssignedTagsList.insets = new Insets(0, 0, 5, 0);
		gbcAssignedTagsList.gridwidth = 10;
		gbcAssignedTagsList.fill = GridBagConstraints.BOTH;
		gbcAssignedTagsList.gridx = 0;
		gbcAssignedTagsList.gridy = 2;
		tasksRightSubPanel.add(assignedTagsList, gbcAssignedTagsList);

		btnRemoveTag = new JButton("Remove tag");
		btnRemoveTag.setEnabled(false);
		btnRemoveTag.setName("btnRemoveTag");
		GridBagConstraints gbcBtnRemoveTag = new GridBagConstraints();
		gbcBtnRemoveTag.gridwidth = 9;
		gbcBtnRemoveTag.gridx = 1;
		gbcBtnRemoveTag.gridy = 3;
		tasksRightSubPanel.add(btnRemoveTag, gbcBtnRemoveTag);
		
		tasksErrorLabel = new JLabel(" ");
		tasksErrorLabel.setName("tasksErrorLabel");
		tasksErrorLabel.setForeground(Color.RED);
		GridBagConstraints gbcTasksErrorLabel = new GridBagConstraints();
		gbcTasksErrorLabel.gridwidth = 3;
		gbcTasksErrorLabel.insets = new Insets(0, 0, 0, 5);
		gbcTasksErrorLabel.gridx = 0;
		gbcTasksErrorLabel.gridy = 6;
		tasksPanel.add(tasksErrorLabel, gbcTasksErrorLabel);

		JPanel tagsPanel = new JPanel();
		tagsPanel.setName("tagsPanel");
		tabbedPane.addTab("Tags", null, tagsPanel, null);
	}

	@Override
	public void showAllTasks(List<Task> allTasks) {
		for (Task task : allTasks)
			taskListModel.addTask(task);
	}

	@Override
	public void taskAdded(Task task) {
		taskListModel.addTask(task);
		tasksErrorLabel.setText(" ");
	}

	@Override
	public void taskError(String errorMessage) {
		tasksErrorLabel.setText(errorMessage);
	}

	@Override
	public void taskDeleted(Task task) {
		taskListModel.removeTask(task);
		tasksErrorLabel.setText(" ");
	}

	@Override
	public void showAllTags(List<Tag> allTags) {
		for (Tag tag : allTags)
			tagComboModel.addTag(tag);
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

	@Override
	public void taskAddedToTag(Task task) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void taskRemovedFromTag(Task task) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void tagAddedToTask(Tag tag) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void tagRemovedFromTask(Tag tag) {
		// TODO Auto-generated method stub
		
	}

}
