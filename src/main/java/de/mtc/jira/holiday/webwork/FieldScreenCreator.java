package de.mtc.jira.holiday.webwork;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.screen.FieldScreen;
import com.atlassian.jira.issue.fields.screen.FieldScreenImpl;
import com.atlassian.jira.issue.fields.screen.FieldScreenLayoutItem;
import com.atlassian.jira.issue.fields.screen.FieldScreenManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenScheme;
import com.atlassian.jira.issue.fields.screen.FieldScreenSchemeItem;
import com.atlassian.jira.issue.fields.screen.FieldScreenSchemeItemImpl;
import com.atlassian.jira.issue.fields.screen.FieldScreenSchemeManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenTab;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenScheme;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeEntity;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeEntityImpl;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeImpl;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.operation.IssueOperations;
import com.atlassian.jira.issue.operation.ScreenableIssueOperation;
import com.atlassian.jira.project.Project;

import de.mtc.jira.holiday.ConfigMap;

public class FieldScreenCreator {

	private final static Logger log = LoggerFactory.getLogger(FieldScreenCreator.class);

	private final FieldScreenManager fieldScreenManager;
	private final FieldScreenSchemeManager fieldScreenSchemeManager;
	private final FieldManager fieldManager;
	private final IssueTypeScreenSchemeManager issueTypeScreenSchemeManager;
	private final ConstantsManager constantsManager;

	private final static String FS_CREATE_NAME;
	private final static String FS_CREATE_DESCRIPTION;
	private final static String FS_SCHEME_CREATE_NAME;
	private final static String FS_SCHEME_CREATE_DESCRIPTION;
	private final static String ISSUETYPE_SCREEN_SCHEME_NAME;
	private final static String ISSUETYPE_SCREEN_SCHEME_DESCRIPTION;

	static {
		FS_CREATE_NAME = ConfigMap.get("fieldscreen.create.name");
		FS_CREATE_DESCRIPTION = ConfigMap.get("fieldscreen.create.description");
		FS_SCHEME_CREATE_NAME = ConfigMap.get("fieldscreen_scheme.name");
		FS_SCHEME_CREATE_DESCRIPTION = ConfigMap.get("fieldscreen_scheme.description");
		ISSUETYPE_SCREEN_SCHEME_NAME = ConfigMap.get("issuetype.screen_scheme.name");
		ISSUETYPE_SCREEN_SCHEME_DESCRIPTION = ConfigMap.get("issuetype.screen_scheme.description");
	}

	public FieldScreenCreator() {
		fieldScreenManager = ComponentAccessor.getFieldScreenManager();
		fieldScreenSchemeManager = ComponentAccessor.getComponent(FieldScreenSchemeManager.class);
		fieldManager = ComponentAccessor.getFieldManager();
		issueTypeScreenSchemeManager = ComponentAccessor.getIssueTypeScreenSchemeManager();
		constantsManager = ComponentAccessor.getConstantsManager();
	}

	public FieldScreen doCreateAll() throws GenericEntityException {
		FieldScreen screen = createFieldScreen();
		FieldScreenScheme scheme = createFieldScreenScheme(screen);
		createIssueTypeScreenScheme(scheme);
		return screen;
	}

	private FieldScreen createFieldScreen() throws GenericEntityException {

		CustomFieldCreator customFieldCreator = new CustomFieldCreator();
		customFieldCreator.createAllFields();
		List<CustomField> customFields = customFieldCreator.getCustomFields();

		FieldScreen screen = null;
		for (FieldScreen sc : fieldScreenManager.getFieldScreens()) {
			if (FS_CREATE_NAME.equals(sc.getName())) {
				screen = sc;
				break;
			}
		}

		if (screen == null) {
			screen = new FieldScreenImpl(fieldScreenManager);
			screen.setName(FS_CREATE_NAME);
			screen.setDescription(FS_CREATE_DESCRIPTION);
			screen.store();
			screen.addTab("Tab1");
		}

		FieldScreenTab tab = screen.getTab(0);

		addFieldToScreen(tab, "summary");
		addFieldToScreen(tab, "reporter");

		for (CustomField cf : customFields) {
			addCustomFieldToScreen(tab, cf);
		}

		log.debug("Configured field screen {} with id {}", screen, screen.getId());

		return screen;
	}

	private final void addCustomFieldToScreen(FieldScreenTab tab, CustomField customField) {
		String fieldId = customField.getId();
		for (FieldScreenLayoutItem item : tab.getFieldScreenLayoutItems()) {
			if (item.getFieldId().equals(fieldId)) {
				log.info("Field {} already exists in screen", customField);
				return;
			}
		}
		log.debug("Added field {} to tab", customField.getName());
		tab.addFieldScreenLayoutItem(fieldId);
	}

	private final void addFieldToScreen(FieldScreenTab tab, String fieldName) {
		OrderableField<?> field = fieldManager.getOrderableField(fieldName);
		if (field == null) {
			log.info("Addition of unknown field {} is ignored", fieldName);
			return;
		}
		String fieldId = field.getId();
		for (FieldScreenLayoutItem item : tab.getFieldScreenLayoutItems()) {
			if (item.getFieldId().equals(fieldId)) {
				log.info("Field {} already exists in screen", fieldId);
				return;
			}
		}
		log.debug("Added field {} to tab", fieldName);
		tab.addFieldScreenLayoutItem(fieldId);
	}

	private FieldScreenScheme createFieldScreenScheme(FieldScreen screen) {
		
		FieldScreenScheme scheme = fieldScreenSchemeManager.getFieldScreenScheme(FieldScreen.DEFAULT_SCREEN_ID);
		scheme.setName(FS_SCHEME_CREATE_NAME);
		scheme.setDescription(FS_SCHEME_CREATE_DESCRIPTION);
		scheme.store();
		
		log.debug("Created field screen scheme {}", scheme);

		List<ScreenableIssueOperation> availableOperiation = scheme.getFieldScreenSchemeItems().stream()
				.map(t -> t.getIssueOperation()).collect(Collectors.toList());
		for (ScreenableIssueOperation operation : new ScreenableIssueOperation[] {
				IssueOperations.CREATE_ISSUE_OPERATION, IssueOperations.EDIT_ISSUE_OPERATION,
				IssueOperations.VIEW_ISSUE_OPERATION }) {

			if (!availableOperiation.contains(operation)) {
				FieldScreenSchemeItem schemeItem = new FieldScreenSchemeItemImpl(fieldScreenSchemeManager,
						fieldScreenManager);
				schemeItem.setIssueOperation(operation);
				schemeItem.setFieldScreen(screen);
				scheme.addFieldScreenSchemeItem(schemeItem);
			}
		}
		return scheme;
	}

	private IssueTypeScreenScheme createIssueTypeScreenScheme(FieldScreenScheme scheme) {
		IssueTypeScreenScheme issueTypeScreenScheme = new IssueTypeScreenSchemeImpl(issueTypeScreenSchemeManager, null);
		issueTypeScreenScheme.setName(ISSUETYPE_SCREEN_SCHEME_NAME);
		issueTypeScreenScheme.setDescription(ISSUETYPE_SCREEN_SCHEME_DESCRIPTION);
		issueTypeScreenScheme.store();

		for (IssueType issueType : ComponentAccessor.getConstantsManager().getAllIssueTypeObjects()) {
			IssueTypeScreenSchemeEntity entity = new IssueTypeScreenSchemeEntityImpl(issueTypeScreenSchemeManager,
					(GenericValue) null, fieldScreenSchemeManager, constantsManager);
			entity.setIssueTypeId(issueType != null ? issueType.getId() : null);
			entity.setFieldScreenScheme(scheme);
			issueTypeScreenScheme.addEntity(entity);
		}

		String projectKey = ConfigMap.get("holiday.project.key");
		Project project = ComponentAccessor.getProjectManager().getProjectObjByKey(projectKey);
		if (project != null) {
			issueTypeScreenSchemeManager.addSchemeAssociation(project, issueTypeScreenScheme);
		}
		return issueTypeScreenScheme;
	}

	public void deleteFieldScreens() {
		List<FieldScreen> toRemove = new ArrayList<>();
		for (FieldScreen sc : fieldScreenManager.getFieldScreens()) {
			if (FS_CREATE_NAME.equals(sc.getName()) || "myName".equals(sc.getName())) {
				toRemove.add(sc);
			}
		}
		for (FieldScreen sc : toRemove) {
			log.debug("Removing Fieldscreen {} with id {}", sc.getName(), sc.getId());
			fieldScreenManager.removeFieldScreen(sc.getId());
		}
	}

	public FieldScreen getFieldScreenById(Object o) {
		Long id = null;
		if (o instanceof Number) {
			id = ((Number) o).longValue();
		} else if (o instanceof String) {
			id = Long.parseLong(o.toString());
		} else {
			throw new IllegalArgumentException();
		}
		for (FieldScreen sc : fieldScreenManager.getFieldScreens()) {
			if (sc.getId() == id) {
				return sc;
			}
		}
		return null;
	}
}