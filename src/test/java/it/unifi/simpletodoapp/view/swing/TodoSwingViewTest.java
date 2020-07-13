package it.unifi.simpletodoapp.view.swing;

import org.assertj.swing.annotation.GUITest;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JPanelFixture;
import org.assertj.swing.fixture.JTabbedPaneFixture;
import org.assertj.swing.junit.runner.GUITestRunner;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(GUITestRunner.class)
public class TodoSwingViewTest extends AssertJSwingJUnitTestCase {
	private FrameFixture frameFixture;
	private TodoSwingView todoSwingView;
	private JTabbedPaneFixture tabPanel;

	@Override
	protected void onSetUp() {
		GuiActionRunner.execute(() -> {
			todoSwingView = new TodoSwingView();
			return todoSwingView;
		});
		
		frameFixture = new FrameFixture(robot(), todoSwingView);
		frameFixture.show();
		
		JPanelFixture jPanel = frameFixture.panel("contentPane");
		tabPanel = jPanel.tabbedPane("tabbedPane"); 
	}

	@Test @GUITest
	public void testTabsArePresent() {
		tabPanel.requireTabTitles("Tasks", "Tags");
	}
}
