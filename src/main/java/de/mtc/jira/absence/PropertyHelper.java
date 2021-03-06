package de.mtc.jira.absence;

import static com.opensymphony.module.propertyset.PropertySet.BOOLEAN;
import static com.opensymphony.module.propertyset.PropertySet.DATE;
import static com.opensymphony.module.propertyset.PropertySet.DOUBLE;
import static com.opensymphony.module.propertyset.PropertySet.INT;
import static com.opensymphony.module.propertyset.PropertySet.LONG;
import static com.opensymphony.module.propertyset.PropertySet.STRING;

import java.util.Date;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.user.ApplicationUser;
import com.opensymphony.module.propertyset.PropertySet;

public class PropertyHelper {

	private PropertySet props;

	public PropertyHelper(ApplicationUser user) {
		this.props = ComponentAccessor.getUserPropertyManager().getPropertySet(user);
	}

	public Double getDouble(String key) {
		return Double.valueOf(get(key).toString());
	}

	public Object get(String key) {
		int type = props.getType(key);
		switch (type) {
		case BOOLEAN:
			return props.getBoolean(key);
		case DATE:
			return props.getDate(key);
		case DOUBLE:
			return props.getDouble(key);
		case INT:
			return props.getInt(key);
		case LONG:
			return props.getLong(key);
		case STRING:
			return props.getString(key);
		default:
			return props.getObject(key);
		}
	}

	public PropertySet getProps() {
		return props;
	}

	public boolean exists(String key) {
		return props.exists(key);
	}
	
	public void set(String key, Object o) {
		int type = props.getType(key);
		String str = o.toString();
		switch (type) {
		case BOOLEAN:
			props.setBoolean(key, Boolean.valueOf(str));
			break;
		case DATE:
			props.setDate(key, new Date(str));
			break;
		case DOUBLE:
			props.setDouble(key, Double.valueOf(str));
			break;
		case INT:
			props.setInt(key, Integer.valueOf(str));
			break;
		case LONG:
			props.setLong(key, Long.valueOf(str));
			break;
		case STRING:
			props.setString(key, str);
			break;
		default:
			props.setObject(key, o);

		}
	}
	
	public String getAnnualLeaveAsString() {
		return get(ConfigMap.get("prop.annual_leave")).toString();
	}
	
	public Double getAnnualLeaveAsDouble() {
		return Double.valueOf(getAnnualLeaveAsString());
	}
}
