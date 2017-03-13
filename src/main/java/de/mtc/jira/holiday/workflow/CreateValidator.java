package de.mtc.jira.holiday.workflow;

import java.util.Date;
import java.util.Map;

import org.jfree.util.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.jira.issue.Issue;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.workflow.InvalidInputException;
import com.opensymphony.workflow.Validator;
import com.opensymphony.workflow.WorkflowException;

import de.mtc.jira.holiday.JiraValidationException;
import de.mtc.jira.holiday.Vacation;

@Scanned
public class CreateValidator implements Validator {

	private final static Logger log = LoggerFactory.getLogger(CreateValidator.class);

	@SuppressWarnings("rawtypes")
	@Override
	public void validate(Map transientVars, Map args, PropertySet ps) throws InvalidInputException, WorkflowException {

		try {
			Issue issue = (Issue) transientVars.get("issue");
			Log.debug("Validate Creation of issue " + issue);
			Vacation vacation = new Vacation(issue);
			Date startDate = vacation.getStartDate();
			Date endDate = vacation.getEndDate();
			if (startDate == null) {
				throw new InvalidInputException("Start Date is missing");
			}
			if (endDate == null) {
				throw new InvalidInputException("End Date is missing");
			}
			if (endDate.getTime() - startDate.getTime() < 0) {
				throw new InvalidInputException("End Date must be after start date.");
			}
			double vacationDaysSpent = vacation.getVacationDaysOfThisYear();
			double numberOfWorkingDays = vacation.getNumberOfWorkingDays();
			double annualLeave = vacation.getAnnualLeave();
			
			if(vacation.getVacationDaysOfThisYear() < vacation.getNumberOfWorkingDays()) {
				StringBuilder message = new StringBuilder();
				message.append("Nicht gen�gend Urlaubstage f�r dieses Jahr.");
				message.append("\nUrlaubstage dieses Jahr: " + vacationDaysSpent);
				message.append("\nRestliche Urlaubstage: " + (annualLeave-vacationDaysSpent));
				message.append("\nBeantragt: " + numberOfWorkingDays);
				throw new InvalidInputException(message.toString());
			}
			// check
			vacation.validate();
			vacation.getSupervisor();
			vacation.getHumanResourcesManager();
		} catch (JiraValidationException e) {
			log.error("Validation failed due to an exception: ", e);
			throw new InvalidInputException("An Exception occured while validating this issue: " + e.getMessage());
		}
	}
}
