package uk.ac.cam.cares.jps.agent.dashboard.json.templating;

import uk.ac.cam.cares.jps.agent.dashboard.utils.StringHelper;

/**
 * A Java representation of a JSON-like model that encapsulates and enforces information about template variable syntax
 * specific to Grafana dashboard. This is a super class that is intended to be implemented by the subclass, and only provide common syntax.
 *
 * @author qhouyee
 */
class TemplateVariable {
    private final String NAME;
    private final String DASHBOARD_DISPLAY_OPTION;
    private final boolean IS_MULTI_OPTION;
    private final boolean INCLUDE_ALL_OPTION;

    /**
     * Standard Constructor.
     *
     * @param name                   The name of the assets to create for this variable.
     * @param dashboardDisplayOption The display options for the variable on the dashboard by Grafana. 0 - Display both label and values; 1 - Display only value; 2 - Display nothing.
     * @param isMultiValue           A boolean to indicate if multiple values are allowed for the variable in Grafana.
     * @param includeAllOption       A boolean to indicate if the "All" option should be enabled in Grafana.
     */
    protected TemplateVariable(String name, Integer dashboardDisplayOption, boolean isMultiValue, boolean includeAllOption) {
        // Transform name into lower cases and remove all white spaces
        this.NAME = StringHelper.formatVariableName(name);
        this.DASHBOARD_DISPLAY_OPTION = dashboardDisplayOption.toString();
        this.IS_MULTI_OPTION = isMultiValue;
        this.INCLUDE_ALL_OPTION = includeAllOption;
    }


    /**
     * Construct the common JSON parts for variable as a StringBuilder which will continue to append specific syntax for different query types.
     *
     * @return The variable syntax as a StringBuilder.
     */
    protected StringBuilder genCommonJson() {
        StringBuilder builder = new StringBuilder();
        builder.append("{")
                // Default selection should be all
                .append("\"current\": {")
                .append("\"selected\": false,")
                .append("\"text\": [\"All\"],")
                .append("\"value\": [\"$__all\"]")
                .append("},")
                // Variable name
                .append("\"name\": \"").append(this.NAME).append("\",")
                // Include option to select all values
                .append("\"includeAll\": ").append(INCLUDE_ALL_OPTION).append(",")
                // Allow multiple value selection if true eg Value 1 and 2 can be selected but not Value 3
                .append("\"multi\":").append(this.IS_MULTI_OPTION).append(",")
                // The display option for this variable
                .append("\"hide\": ").append(this.DASHBOARD_DISPLAY_OPTION).append(",")
                .append("\"skipUrlSync\": false,");
        return builder;
    }

    /**
     * A placeholder method to construct the variable syntax required. This method must be overridden to be executed in the implemented classes.
     */
    protected String construct() {
        throw new UnsupportedOperationException("Construct() method is not supported for TemplateVariable. Please use their implementation classes instead!");
    }
}
