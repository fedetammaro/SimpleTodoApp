package it.unifi.simpletodoapp.view.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import it.unifi.simpletodoapp.controller.TodoController;
import it.unifi.simpletodoapp.model.Tag;
import it.unifi.simpletodoapp.model.Task;
import it.unifi.simpletodoapp.view.TodoView;
import javax.swing.JTabbedPane;
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import java.awt.GridBagConstraints;
import javax.swing.JTextField;

import java.awt.Insets;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JSeparator;
import javax.swing.JList;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.JComboBox;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class TodoSwingView extends JFrame implements TodoView {

	private static final long serialVersionUID = -8047380292195826724L;

	private transient TodoController todoController;

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
	private JList<TagViewModel> assignedTagsList;
	private TagListModel assignedTagsListModel = new TagListModel();
	private JButton btnRemoveTag;
	private JLabel tasksErrorLabel;
	private JTextField tagIdTextField;
	private JTextField tagNameTextField;
	private JButton btnAddTag;
	private JList<TagViewModel> tagsTagList;
	private TagListModel tagListModel = new TagListModel();
	private JButton btnDeleteTag;
	private JList<TaskViewModel> assignedTasksList;
	private TaskListModel assignedTasksListModel = new TaskListModel();
	private JButton btnRemoveTask;
	private JLabel tagsErrorLabel;

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

			return taskViewModel != null && taskViewModel.task.equals(this.task);
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

			return tagViewModel != null && tagViewModel.tag.equals(this.tag);
		}
	}

	static final class TagComboModel extends DefaultComboBoxModel<TagViewModel> {
		private static final long serialVersionUID = 1L;

		public void addTag(Tag tag) {
			addElement(new TagViewModel(tag));
		}
	}

	static final class TagListModel extends DefaultListModel<TagViewModel> {
		private static final long serialVersionUID = 1L;

		public void addTag(Tag tag) {
			addElement(new TagViewModel(tag));
		}

		public void removeTag(Tag tag) {
			removeElement(new TagViewModel(tag));
		}
	}
	
	public void setTodoController(TodoController todoController) {
		this.todoController = todoController;
	}

	/**
	 * Create the frame.
	 */
	public TodoSwingView() {
		initialSetup();

		JTabbedPane tabbedPane = createTabbedPanel();

		// Tasks panel and its components are created here
		JPanel tasksPanel = createTasksPanel(tabbedPane);
		createTaskIdLabel(tasksPanel);
		createTaskIdTextField(tasksPanel);
		createTaskDescriptionLabel(tasksPanel);
		createTaskDescriptionTextField(tasksPanel);
		createBtnAddTask(tasksPanel);
		createTasksTabSeparator(tasksPanel);
		JPanel tasksLeftSubPanel = createTasksLeftSubPanel(tasksPanel);
		createTasksTaskListLabel(tasksLeftSubPanel);
		createTasksTaskList(tasksLeftSubPanel);
		createBtnDeleteTask(tasksLeftSubPanel);
		JPanel tasksRightSubPanel = createTasksRightSubPanel(tasksPanel);
		createTagsComboBox(tasksRightSubPanel);
		createBtnAssignTag(tasksRightSubPanel);
		createAssignedTagsList(tasksRightSubPanel);
		createBtnRemoveTag(tasksRightSubPanel);
		createTasksErrorLabel(tasksPanel);

		// Tags panel and its components are created here
		JPanel tagsPanel = createTagsPanel(tabbedPane);
		createTagIdLabel(tagsPanel);
		createTagIdTextField(tagsPanel);
		createTagNameLabel(tagsPanel);
		createTagNameTextField(tagsPanel);
		createBtnAddTag(tagsPanel);
		JPanel tagsLeftSubPanel = createTagsLeftSubPanel(tagsPanel);
		createTagsTagListLabel(tagsLeftSubPanel);
		createTagsTagList(tagsLeftSubPanel);
		createBtnDeleteTag(tagsLeftSubPanel);
		JPanel tagsRightSubPanel = createTagsRightSubPanel(tagsPanel);
		createAssignedTasksListLabel(tagsRightSubPanel);
		createAssignedTasksList(tagsRightSubPanel);
		createRemoveTaskButton(tagsRightSubPanel);
		createTagsErrorLabel(tagsPanel);
	}


	@Override
	public void showAllTasks(List<Task> allTasks) {
		allTasks.stream().forEach(task -> taskListModel.addTask(task));
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
		allTags.stream().forEach(tag -> {
			tagComboModel.addTag(tag);
			tagListModel.addTag(tag);
		});
	}

	@Override
	public void tagAdded(Tag tag) {
		tagListModel.addTag(tag);
		tagsErrorLabel.setText(" ");
	}

	@Override
	public void tagError(String errorMessage) {
		tagsErrorLabel.setText(errorMessage);
	}

	@Override
	public void tagRemoved(Tag tag) {
		tagListModel.removeTag(tag);
		tagsErrorLabel.setText(" ");
	}

	@Override
	public void showTaskTags(List<Tag> tags) {
		tags.stream().forEach(tag -> assignedTagsListModel.addTag(tag));
	}

	@Override
	public void showTagTasks(List<Task> tasks) {
		tasks.stream().forEach(task -> assignedTasksListModel.addTask(task));
	}

	@Override
	public void taskRemovedFromTag(Task task) {
		assignedTasksListModel.removeTask(task);
		tagsErrorLabel.setText(" ");
	}

	@Override
	public void tagAddedToTask(Tag tag) {
		assignedTagsListModel.addTag(tag);
		tasksErrorLabel.setText(" ");
	}

	@Override
	public void tagRemovedFromTask(Tag tag) {
		assignedTagsListModel.removeTag(tag);
		tasksErrorLabel.setText(" ");
	}

	private void initialSetup() {
		setTitle("Simple Todo Application");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 720, 500);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		contentPane.setName("contentPane");
		setContentPane(contentPane);
	}

	private JTabbedPane createTabbedPanel() {
		JTabbedPane tabbedPane = new JTabbedPane(SwingConstants.TOP);
		contentPane.add(tabbedPane, BorderLayout.CENTER);
		tabbedPane.setName("tabbedPane");

		return tabbedPane;
	}

	private JPanel createTasksPanel(JTabbedPane tabbedPane) {
		JPanel tasksPanel = new JPanel();
		tasksPanel.setName("tasksPanel");
		tabbedPane.addTab("Tasks", null, tasksPanel, null);
		GridBagLayout gblTasksPanel = new GridBagLayout();
		gblTasksPanel.columnWidths = new int[]{130, 250, 20, 300, 0};
		gblTasksPanel.rowHeights = new int[]{20, 20, 20, 25, 0, 0, 20, 0};
		gblTasksPanel.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		gblTasksPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
		tasksPanel.setLayout(gblTasksPanel);

		return tasksPanel;
	}

	private void createTaskIdLabel(JPanel panel) {
		JLabel taskIdLabel = new JLabel("id");
		taskIdLabel.setName("taskIdLabel");
		GridBagConstraints gbcTaskIdLabel = new GridBagConstraints();
		gbcTaskIdLabel.insets = new Insets(0, 0, 5, 5);
		gbcTaskIdLabel.gridx = 0;
		gbcTaskIdLabel.gridy = 1;
		panel.add(taskIdLabel, gbcTaskIdLabel);
	}

	private void createTaskIdTextField(JPanel panel) {
		taskIdTextField = new JTextField();
		taskIdTextField.setName("taskIdTextField");
		GridBagConstraints gbcTaskIdTextField = new GridBagConstraints();
		gbcTaskIdTextField.insets = new Insets(0, 0, 5, 5);
		gbcTaskIdTextField.fill = GridBagConstraints.HORIZONTAL;
		gbcTaskIdTextField.gridx = 1;
		gbcTaskIdTextField.gridy = 1;
		panel.add(taskIdTextField, gbcTaskIdTextField);
		taskIdTextField.setColumns(10);
	}

	private void createTaskDescriptionLabel(JPanel panel) {
		JLabel taskDescriptionLabel = new JLabel("description");
		taskDescriptionLabel.setName("taskDescriptionLabel");
		GridBagConstraints gbcTaskDescriptionLabel = new GridBagConstraints();
		gbcTaskDescriptionLabel.insets = new Insets(0, 0, 5, 5);
		gbcTaskDescriptionLabel.gridx = 0;
		gbcTaskDescriptionLabel.gridy = 2;
		panel.add(taskDescriptionLabel, gbcTaskDescriptionLabel);
	}

	private void createTaskDescriptionTextField(JPanel panel) {
		taskDescriptionTextField = new JTextField();
		taskDescriptionTextField.setName("taskDescriptionTextField");
		GridBagConstraints gbcTaskDescriptionTextField = new GridBagConstraints();
		gbcTaskDescriptionTextField.gridwidth = 3;
		gbcTaskDescriptionTextField.insets = new Insets(0, 0, 5, 0);
		gbcTaskDescriptionTextField.fill = GridBagConstraints.HORIZONTAL;
		gbcTaskDescriptionTextField.gridx = 1;
		gbcTaskDescriptionTextField.gridy = 2;
		panel.add(taskDescriptionTextField, gbcTaskDescriptionTextField);
		taskDescriptionTextField.setColumns(10);
	}

	private void createBtnAddTask(JPanel panel) {
		btnAddTask = new JButton("Add task");
		btnAddTask.setEnabled(false);
		btnAddTask.setName("btnAddTask");
		GridBagConstraints gbcBtnAddTask = new GridBagConstraints();
		gbcBtnAddTask.insets = new Insets(0, 0, 5, 0);
		gbcBtnAddTask.gridwidth = 4;
		gbcBtnAddTask.gridx = 0;
		gbcBtnAddTask.gridy = 3;
		panel.add(btnAddTask, gbcBtnAddTask);

		btnAddTask.addActionListener(l -> todoController.addTask(
				new Task(taskIdTextField.getText(), taskDescriptionTextField.getText())
				));

		KeyAdapter btnAddTaskEnabler = new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				btnAddTask.setEnabled(
						!taskIdTextField.getText().trim().isEmpty()
						&& !taskDescriptionTextField.getText().trim().isEmpty());
			}
		};

		taskIdTextField.addKeyListener(btnAddTaskEnabler);
		taskDescriptionTextField.addKeyListener(btnAddTaskEnabler);
	}

	private void createTasksTabSeparator(JPanel panel) {
		JSeparator tasksTabSeparator = new JSeparator();
		tasksTabSeparator.setName("tasksTabSeparator");
		GridBagConstraints gbcTasksTabSeparator = new GridBagConstraints();
		gbcTasksTabSeparator.insets = new Insets(0, 0, 5, 0);
		gbcTasksTabSeparator.gridwidth = 4;
		gbcTasksTabSeparator.gridx = 0;
		gbcTasksTabSeparator.gridy = 4;
		panel.add(tasksTabSeparator, gbcTasksTabSeparator);
	}

	private JPanel createTasksLeftSubPanel(JPanel panel) {
		JPanel tasksLeftSubPanel = new JPanel();
		tasksLeftSubPanel.setName("tasksLeftSubPanel");
		GridBagConstraints gbcTasksLeftSubPanel = new GridBagConstraints();
		gbcTasksLeftSubPanel.gridwidth = 2;
		gbcTasksLeftSubPanel.insets = new Insets(0, 0, 5, 5);
		gbcTasksLeftSubPanel.fill = GridBagConstraints.VERTICAL;
		gbcTasksLeftSubPanel.gridx = 0;
		gbcTasksLeftSubPanel.gridy = 5;
		panel.add(tasksLeftSubPanel, gbcTasksLeftSubPanel);
		GridBagLayout gblTasksLeftSubPanel = new GridBagLayout();
		gblTasksLeftSubPanel.columnWidths = new int[]{37, 37, 37, 37, 37, 37, 37, 37, 37, 37};
		gblTasksLeftSubPanel.rowHeights = new int[]{20, 320, 25, 0};
		gblTasksLeftSubPanel.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
		gblTasksLeftSubPanel.rowWeights = new double[]{0.0, 1.0, 0.0, Double.MIN_VALUE};
		tasksLeftSubPanel.setLayout(gblTasksLeftSubPanel);

		return tasksLeftSubPanel;
	}

	private void createTasksTaskListLabel(JPanel panel) {
		JLabel tasksTaskListLabel = new JLabel("Todo List");
		tasksTaskListLabel.setName("tasksTaskListLabel");
		GridBagConstraints gbcTasksTaskListLabel = new GridBagConstraints();
		gbcTasksTaskListLabel.insets = new Insets(0, 0, 5, 0);
		gbcTasksTaskListLabel.gridwidth = 10;
		gbcTasksTaskListLabel.gridx = 0;
		gbcTasksTaskListLabel.gridy = 0;
		panel.add(tasksTaskListLabel, gbcTasksTaskListLabel);
	}

	private void createTasksTaskList(JPanel panel) {
		tasksTaskList = new JList<>(taskListModel);
		tasksTaskList.setName("tasksTaskList");
		tasksTaskList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		GridBagConstraints gbcTasksTaskList = new GridBagConstraints();
		gbcTasksTaskList.insets = new Insets(0, 0, 5, 0);
		gbcTasksTaskList.gridwidth = 10;
		gbcTasksTaskList.fill = GridBagConstraints.BOTH;
		gbcTasksTaskList.gridx = 0;
		gbcTasksTaskList.gridy = 1;
		panel.add(tasksTaskList, gbcTasksTaskList);

		tasksTaskList.addListSelectionListener(l -> {
			int taskIndex = tasksTaskList.getSelectedIndex();
			boolean isTaskSelected = taskIndex != -1;
			btnDeleteTask.setEnabled(isTaskSelected);
			tagsComboBox.setEnabled(isTaskSelected);
			btnAssignTag.setEnabled(isTaskSelected);

			// Prevent multiple firings
			if (!l.getValueIsAdjusting()) {
				if (isTaskSelected)
					todoController.getTagsByTask(taskListModel.get(taskIndex).task);
				else
					assignedTagsListModel.clear();
			}
		});
	}

	private void createBtnDeleteTask(JPanel panel) {
		btnDeleteTask = new JButton("Delete task");
		btnDeleteTask.setEnabled(false);
		btnDeleteTask.setName("btnDeleteTask");
		GridBagConstraints gbcBtnDeleteTask = new GridBagConstraints();
		gbcBtnDeleteTask.gridwidth = 10;
		gbcBtnDeleteTask.gridx = 0;
		gbcBtnDeleteTask.gridy = 2;
		panel.add(btnDeleteTask, gbcBtnDeleteTask);

		btnDeleteTask.addActionListener(l -> {
			Task task = taskListModel.get(tasksTaskList.getSelectedIndex()).task;
			todoController.deleteTask(task);
		});
	}

	private JPanel createTasksRightSubPanel(JPanel panel) {
		JPanel tasksRightSubPanel = new JPanel();
		tasksRightSubPanel.setName("tasksRightSubPanel");
		GridBagConstraints gbcTasksRightSubPanel = new GridBagConstraints();
		gbcTasksRightSubPanel.insets = new Insets(0, 0, 5, 0);
		gbcTasksRightSubPanel.fill = GridBagConstraints.BOTH;
		gbcTasksRightSubPanel.gridx = 3;
		gbcTasksRightSubPanel.gridy = 5;
		panel.add(tasksRightSubPanel, gbcTasksRightSubPanel);
		GridBagLayout gblTasksRightSubPanel = new GridBagLayout();
		gblTasksRightSubPanel.columnWidths = new int[]{30, 30, 30, 30, 30, 30, 30, 30, 30, 30};
		gblTasksRightSubPanel.rowHeights = new int[]{25, 25, 290, 25, 0};
		gblTasksRightSubPanel.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
		gblTasksRightSubPanel.rowWeights = new double[]{0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
		tasksRightSubPanel.setLayout(gblTasksRightSubPanel);

		return tasksRightSubPanel;
	}

	private void createTagsComboBox(JPanel panel) {
		tagsComboBox = new JComboBox<>(tagComboModel);
		tagsComboBox.setEnabled(false);
		tagsComboBox.setName("tagComboBox");
		GridBagConstraints gbcTagComboBox = new GridBagConstraints();
		gbcTagComboBox.insets = new Insets(0, 0, 5, 0);
		gbcTagComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbcTagComboBox.gridwidth = 6;
		gbcTagComboBox.gridx = 2;
		gbcTagComboBox.gridy = 0;
		panel.add(tagsComboBox, gbcTagComboBox);
	}

	private void createBtnAssignTag(JPanel panel) {
		btnAssignTag = new JButton("Assign tag");
		btnAssignTag.setEnabled(false);
		btnAssignTag.setName("btnAssignTag");
		GridBagConstraints gbcBtnAssignTag = new GridBagConstraints();
		gbcBtnAssignTag.insets = new Insets(0, 0, 5, 0);
		gbcBtnAssignTag.gridwidth = 10;
		gbcBtnAssignTag.gridx = 0;
		gbcBtnAssignTag.gridy = 1;
		panel.add(btnAssignTag, gbcBtnAssignTag);

		btnAssignTag.addActionListener(l -> {
			Task task = taskListModel.get(tasksTaskList.getSelectedIndex()).task;
			Tag tag = tagComboModel.getElementAt(tagsComboBox.getSelectedIndex()).tag;
			todoController.addTagToTask(task, tag);
		});
	}

	private void createAssignedTagsList(JPanel panel) {
		assignedTagsList = new JList<>(assignedTagsListModel);
		assignedTagsList.setName("assignedTagsList");
		assignedTagsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		GridBagConstraints gbcAssignedTagsList = new GridBagConstraints();
		gbcAssignedTagsList.insets = new Insets(0, 0, 5, 0);
		gbcAssignedTagsList.gridwidth = 10;
		gbcAssignedTagsList.fill = GridBagConstraints.BOTH;
		gbcAssignedTagsList.gridx = 0;
		gbcAssignedTagsList.gridy = 2;
		panel.add(assignedTagsList, gbcAssignedTagsList);

		assignedTagsList.addListSelectionListener(l -> btnRemoveTag.setEnabled(assignedTagsList.getSelectedIndex() != -1));
	}

	private void createBtnRemoveTag(JPanel panel) {
		btnRemoveTag = new JButton("Remove tag");
		btnRemoveTag.setEnabled(false);
		btnRemoveTag.setName("btnRemoveTag");
		GridBagConstraints gbcBtnRemoveTag = new GridBagConstraints();
		gbcBtnRemoveTag.gridwidth = 10;
		gbcBtnRemoveTag.gridx = 0;
		gbcBtnRemoveTag.gridy = 3;
		panel.add(btnRemoveTag, gbcBtnRemoveTag);

		btnRemoveTag.addActionListener(l -> {
			Task task = taskListModel.get(tasksTaskList.getSelectedIndex()).task;
			Tag tag = assignedTagsListModel.get(assignedTagsList.getSelectedIndex()).tag;
			todoController.removeTagFromTask(task, tag);
		});
	}

	private void createTasksErrorLabel(JPanel panel) {
		tasksErrorLabel = new JLabel(" ");
		tasksErrorLabel.setName("tasksErrorLabel");
		tasksErrorLabel.setForeground(Color.RED);
		GridBagConstraints gbcTasksErrorLabel = new GridBagConstraints();
		gbcTasksErrorLabel.gridwidth = 4;
		gbcTasksErrorLabel.gridx = 0;
		gbcTasksErrorLabel.gridy = 6;
		panel.add(tasksErrorLabel, gbcTasksErrorLabel);
	}

	private JPanel createTagsPanel(JTabbedPane tabbedPane) {
		JPanel tagsPanel = new JPanel();
		tagsPanel.setName("tagsPanel");
		tabbedPane.addTab("Tags", null, tagsPanel, null);
		GridBagLayout gblTagsPanel = new GridBagLayout();
		gblTagsPanel.columnWidths = new int[]{80, 200, 20, 400, 0};
		gblTagsPanel.rowHeights = new int[]{20, 20, 0, 25, 0, 20, 0};
		gblTagsPanel.columnWeights = new double[]{0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		gblTagsPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
		tagsPanel.setLayout(gblTagsPanel);

		return tagsPanel;
	}

	private void createTagIdLabel(JPanel panel) {
		JLabel tagIdLabel = new JLabel("id");
		tagIdLabel.setName("tagIdLabel");
		GridBagConstraints gbcTagIdLabel = new GridBagConstraints();
		gbcTagIdLabel.insets = new Insets(0, 0, 5, 5);
		gbcTagIdLabel.gridx = 0;
		gbcTagIdLabel.gridy = 1;
		panel.add(tagIdLabel, gbcTagIdLabel);
	}

	private void createTagIdTextField(JPanel panel) {
		tagIdTextField = new JTextField();
		tagIdTextField.setName("tagIdTextField");
		GridBagConstraints gbcTagIdTextField = new GridBagConstraints();
		gbcTagIdTextField.fill = GridBagConstraints.HORIZONTAL;
		gbcTagIdTextField.insets = new Insets(0, 0, 5, 5);
		gbcTagIdTextField.gridx = 1;
		gbcTagIdTextField.gridy = 1;
		panel.add(tagIdTextField, gbcTagIdTextField);
		tagIdTextField.setColumns(10);
	}

	private void createTagNameLabel(JPanel panel) {
		JLabel tagNameLabel = new JLabel("name");
		tagNameLabel.setName("tagNameLabel");
		GridBagConstraints gbcTagNameLabel = new GridBagConstraints();
		gbcTagNameLabel.insets = new Insets(0, 0, 5, 5);
		gbcTagNameLabel.gridx = 0;
		gbcTagNameLabel.gridy = 2;
		panel.add(tagNameLabel, gbcTagNameLabel);
	}

	private void createTagNameTextField(JPanel panel) {
		tagNameTextField = new JTextField();
		tagNameTextField.setName("tagNameTextField");
		GridBagConstraints gbcTagNameTextField = new GridBagConstraints();
		gbcTagNameTextField.insets = new Insets(0, 0, 5, 5);
		gbcTagNameTextField.fill = GridBagConstraints.HORIZONTAL;
		gbcTagNameTextField.gridx = 1;
		gbcTagNameTextField.gridy = 2;
		panel.add(tagNameTextField, gbcTagNameTextField);
		tagNameTextField.setColumns(10);
	}

	private void createBtnAddTag(JPanel panel) {
		btnAddTag = new JButton("Add tag");
		btnAddTag.setName("btnAddTag");
		btnAddTag.setEnabled(false);
		GridBagConstraints gbcBtnAddTag = new GridBagConstraints();
		gbcBtnAddTag.insets = new Insets(0, 0, 5, 5);
		gbcBtnAddTag.gridwidth = 2;
		gbcBtnAddTag.gridx = 0;
		gbcBtnAddTag.gridy = 3;
		panel.add(btnAddTag, gbcBtnAddTag);

		btnAddTag.addActionListener(l -> todoController.addTag(
				new Tag(tagIdTextField.getText(), tagNameTextField.getText())
				));

		KeyAdapter btnAddTagEnabler = new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				btnAddTag.setEnabled(
						!tagIdTextField.getText().trim().isEmpty()
						&& !tagNameTextField.getText().trim().isEmpty());
			}
		};

		tagIdTextField.addKeyListener(btnAddTagEnabler);
		tagNameTextField.addKeyListener(btnAddTagEnabler);
	}

	private JPanel createTagsLeftSubPanel(JPanel panel) {
		JPanel tagsLeftSubPanel = new JPanel();
		tagsLeftSubPanel.setName("tagsLeftSubPanel");
		GridBagConstraints gbcTagsLeftSubPanel = new GridBagConstraints();
		gbcTagsLeftSubPanel.gridwidth = 2;
		gbcTagsLeftSubPanel.insets = new Insets(0, 0, 5, 5);
		gbcTagsLeftSubPanel.fill = GridBagConstraints.BOTH;
		gbcTagsLeftSubPanel.gridx = 0;
		gbcTagsLeftSubPanel.gridy = 4;
		panel.add(tagsLeftSubPanel, gbcTagsLeftSubPanel);
		GridBagLayout gblTagsLeftSubPanel = new GridBagLayout();
		gblTagsLeftSubPanel.columnWidths = new int[]{29, 29, 29, 29, 29, 29, 29, 29, 29, 29};
		gblTagsLeftSubPanel.rowHeights = new int[]{20, 0, 25, 0};
		gblTagsLeftSubPanel.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		gblTagsLeftSubPanel.rowWeights = new double[]{0.0, 1.0, 0.0, Double.MIN_VALUE};
		tagsLeftSubPanel.setLayout(gblTagsLeftSubPanel);

		return tagsLeftSubPanel;
	}

	private void createTagsTagListLabel(JPanel panel) {
		JLabel tagsTagListLabel = new JLabel("Tag List");
		tagsTagListLabel.setName("tagsTagListLabel");
		GridBagConstraints gbcTagsTagListLabel = new GridBagConstraints();
		gbcTagsTagListLabel.insets = new Insets(0, 0, 5, 0);
		gbcTagsTagListLabel.gridwidth = 10;
		gbcTagsTagListLabel.gridx = 0;
		gbcTagsTagListLabel.gridy = 0;
		panel.add(tagsTagListLabel, gbcTagsTagListLabel);
	}

	private void createTagsTagList(JPanel panel) {
		tagsTagList = new JList<>(tagListModel);
		tagsTagList.setName("tagsTagList");
		GridBagConstraints gbcTagsTagList = new GridBagConstraints();
		gbcTagsTagList.insets = new Insets(0, 0, 5, 0);
		gbcTagsTagList.gridwidth = 10;
		gbcTagsTagList.fill = GridBagConstraints.BOTH;
		gbcTagsTagList.gridx = 0;
		gbcTagsTagList.gridy = 1;
		panel.add(tagsTagList, gbcTagsTagList);

		tagsTagList.addListSelectionListener(l -> {
			int tagIndex = tagsTagList.getSelectedIndex();
			boolean isTagSelected = tagIndex != -1;
			btnDeleteTag.setEnabled(isTagSelected);

			// Prevent multiple firings
			if (!l.getValueIsAdjusting()) {
				if (isTagSelected)
					todoController.getTasksByTag(tagListModel.get(tagIndex).tag);
				else
					assignedTasksListModel.clear();
			}
		});
	}

	private void createBtnDeleteTag(JPanel panel) {
		btnDeleteTag = new JButton("Delete tag");
		btnDeleteTag.setName("btnDeleteTag");
		btnDeleteTag.setEnabled(false);
		GridBagConstraints gbcBtnDeleteTag = new GridBagConstraints();
		gbcBtnDeleteTag.gridwidth = 10;
		gbcBtnDeleteTag.gridx = 0;
		gbcBtnDeleteTag.gridy = 2;
		panel.add(btnDeleteTag, gbcBtnDeleteTag);

		btnDeleteTag.addActionListener(l -> {
			Tag tag = tagListModel.get(tagsTagList.getSelectedIndex()).tag;
			todoController.removeTag(tag);
		});
	}

	private JPanel createTagsRightSubPanel(JPanel panel) {
		JPanel tagsRightSubPanel = new JPanel();
		tagsRightSubPanel.setName("tagsRightSubPanel");
		GridBagConstraints gbcTagsRightSubPanel = new GridBagConstraints();
		gbcTagsRightSubPanel.gridheight = 5;
		gbcTagsRightSubPanel.insets = new Insets(0, 0, 5, 0);
		gbcTagsRightSubPanel.fill = GridBagConstraints.BOTH;
		gbcTagsRightSubPanel.gridx = 3;
		gbcTagsRightSubPanel.gridy = 0;
		panel.add(tagsRightSubPanel, gbcTagsRightSubPanel);
		GridBagLayout gblTagsRightSubPanel = new GridBagLayout();
		gblTagsRightSubPanel.columnWidths = new int[]{40, 40, 40, 40, 40, 40, 40, 40, 40, 40};
		gblTagsRightSubPanel.rowHeights = new int[]{0, 0, 25, 0};
		gblTagsRightSubPanel.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
		gblTagsRightSubPanel.rowWeights = new double[]{0.0, 1.0, 0.0, Double.MIN_VALUE};
		tagsRightSubPanel.setLayout(gblTagsRightSubPanel);

		return tagsRightSubPanel;
	}

	private void createAssignedTasksListLabel(JPanel panel) {
		JLabel assignedTasksListLabel = new JLabel("Tasks with this tag");
		assignedTasksListLabel.setName("assignedTasksListLabel");
		GridBagConstraints gbcAssignedTasksListLabel = new GridBagConstraints();
		gbcAssignedTasksListLabel.insets = new Insets(0, 0, 5, 0);
		gbcAssignedTasksListLabel.gridwidth = 10;
		gbcAssignedTasksListLabel.gridx = 0;
		gbcAssignedTasksListLabel.gridy = 0;
		panel.add(assignedTasksListLabel, gbcAssignedTasksListLabel);
	}

	private void createAssignedTasksList(JPanel panel) {
		assignedTasksList = new JList<>(assignedTasksListModel);
		assignedTasksList.setName("assignedTasksList");
		GridBagConstraints gbcAssignedTaskList = new GridBagConstraints();
		gbcAssignedTaskList.insets = new Insets(0, 0, 5, 0);
		gbcAssignedTaskList.gridwidth = 10;
		gbcAssignedTaskList.fill = GridBagConstraints.BOTH;
		gbcAssignedTaskList.gridx = 0;
		gbcAssignedTaskList.gridy = 1;
		panel.add(assignedTasksList, gbcAssignedTaskList);

		assignedTasksList.addListSelectionListener(l -> btnRemoveTask.setEnabled(assignedTasksList.getSelectedIndex() != -1));
	}

	private void createRemoveTaskButton(JPanel panel) {
		btnRemoveTask = new JButton("Remove task");
		btnRemoveTask.setName("btnRemoveTask");
		btnRemoveTask.setEnabled(false);
		GridBagConstraints gbcBtnRemoveTask = new GridBagConstraints();
		gbcBtnRemoveTask.gridwidth = 10;
		gbcBtnRemoveTask.insets = new Insets(0, 0, 0, 5);
		gbcBtnRemoveTask.gridx = 0;
		gbcBtnRemoveTask.gridy = 2;
		panel.add(btnRemoveTask, gbcBtnRemoveTask);

		btnRemoveTask.addActionListener(l -> {
			Tag tag = tagListModel.get(tagsTagList.getSelectedIndex()).tag;
			Task task = assignedTasksListModel.get(assignedTasksList.getSelectedIndex()).task;
			todoController.removeTagFromTask(task, tag);
		});
	}

	private void createTagsErrorLabel(JPanel panel) {
		tagsErrorLabel = new JLabel(" ");
		tagsErrorLabel.setName("tagsErrorLabel");
		tagsErrorLabel.setForeground(Color.RED);
		GridBagConstraints gbcTagsErrorLabel = new GridBagConstraints();
		gbcTagsErrorLabel.gridwidth = 4;
		gbcTagsErrorLabel.insets = new Insets(0, 0, 0, 5);
		gbcTagsErrorLabel.gridx = 0;
		gbcTagsErrorLabel.gridy = 5;
		panel.add(tagsErrorLabel, gbcTagsErrorLabel);
	}
}
