package com.kinnarastudio.kecakplugins.datalist.formatter;

import org.joget.apps.app.service.AppUtil;
import org.joget.apps.datalist.model.DataList;
import org.joget.apps.datalist.model.DataListColumn;
import org.joget.apps.datalist.model.DataListColumnFormatDefault;
import org.joget.plugin.base.PluginManager;

import java.util.ResourceBundle;

/**
 * Display value as check and uncheck box
 */
public class CheckedUncheckedDataListFormatter extends DataListColumnFormatDefault {
    public final static String TRUE_SYMBOL = "&check;";
    public final static String FALSE_SYMBOL = "&cross;";
    public final static String LABEL = "Checked / Unchecked";

    @Override
    public String format(DataList dataList, DataListColumn column, Object row, Object value) {
        final String strValue = String.valueOf(value);
        return (isNegated() ^ ((value instanceof Boolean && (Boolean) value)
                || "true".equalsIgnoreCase(strValue)
                || "yes".equalsIgnoreCase(strValue)
                || "y".equalsIgnoreCase(strValue))) ? getTrueSymbol() : getFalseSymbol();
    }

    @Override
    public String getName() {
        return LABEL;
    }

    @Override
    public String getVersion() {
        PluginManager pluginManager = (PluginManager) AppUtil.getApplicationContext().getBean("pluginManager");
        ResourceBundle resourceBundle = pluginManager.getPluginMessageBundle(getClassName(), "/messages/BuildNumber");
        String buildNumber = resourceBundle.getString("buildNumber");
        return buildNumber;
    }

    @Override
    public String getDescription() {
        return getClass().getPackage().getImplementationTitle();
    }

    @Override
    public String getLabel() {
        return LABEL;
    }

    @Override
    public String getClassName() {
        return getClass().getName();
    }

    @Override
    public String getPropertyOptions() {
        return AppUtil.readPluginResource(getClass().getName(), "/properties/CheckedUncheckedDataListFormatter.json", new String[]{TRUE_SYMBOL, FALSE_SYMBOL}, false, "/messages/CheckedUncheckedDataListFormatter");
    }

    protected String getTrueSymbol() {
        return ifEmpty(getPropertyString("trueSymbol"), FALSE_SYMBOL);
    }

    protected String getFalseSymbol() {
        return ifEmpty(getPropertyString("falseSymbol"), FALSE_SYMBOL);
    }

    protected String ifEmpty(String value, String failover) {
        return value == null || value.isEmpty() ? failover : value;
    }

    protected boolean isNegated() {
        return "true".equalsIgnoreCase(getPropertyString("negate"));
    }
}
