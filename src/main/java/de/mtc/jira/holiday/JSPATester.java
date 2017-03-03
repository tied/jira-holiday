package de.mtc.jira.holiday;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.web.action.JiraWebActionSupport;

public class JSPATester extends JiraWebActionSupport {
	
	private static final long serialVersionUID = 1L;
	private static final Logger log = LoggerFactory.getLogger(JSPATester.class);
	
	private List<CustomField> customFields;
	
	public List<CustomField> getCustomFields() {
		return customFields;
	}
			
	@Override
	protected String doExecute() throws Exception {
		log.debug("Executing main method");
		HolidayProjectInitializor temp = new HolidayProjectInitializor();
		temp.createAllFields();
		customFields = ComponentAccessor.getCustomFieldManager().getCustomFieldObjects();
		return SUCCESS;
	}
	
	@Override
	public String doDefault() throws Exception {
		log.debug("Executing default method");
		return INPUT;
	}
}


