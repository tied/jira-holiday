package it.de.mtc.jira;

import org.junit.Test;
import org.junit.runner.RunWith;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
import de.mtc.jira.api.MyPluginComponent;
import com.atlassian.sal.api.ApplicationProperties;

import static org.junit.Assert.assertEquals;

@RunWith(AtlassianPluginsTestRunner.class)
public class MyComponentWiredTest {
	private final ApplicationProperties applicationProperties;
	private final MyPluginComponent myPluginComponent;

	public MyComponentWiredTest(ApplicationProperties applicationProperties, MyPluginComponent myPluginComponent) {
		this.applicationProperties = applicationProperties;
		this.myPluginComponent = myPluginComponent;
	}

	@Test
	public void testMyName() {
		assertEquals(0, 1);
		// assertEquals("names do not match!", "myComponent:" +
		// applicationProperties.getDisplayName(),myPluginComponent.getName());
	}
}