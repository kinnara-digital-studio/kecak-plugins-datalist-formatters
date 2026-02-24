package com.kinnarastudio.kecakplugins.datalist.formatter;

import org.joget.apps.app.service.AppUtil;
import org.joget.apps.datalist.model.DataList;
import org.joget.apps.datalist.model.DataListColumn;
import org.joget.apps.datalist.model.DataListColumnFormatDefault;
import org.joget.commons.util.LogUtil;
import org.joget.plugin.base.PluginManager;
import org.joget.workflow.util.WorkflowUtil;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 *
 */
public class DebugDataListFormatter extends DataListColumnFormatDefault {
    public static String LABEL = "Debug Formatter";

    @Override
    public String format(DataList dataList, DataListColumn dataListColumn, Object row, Object value) {
        LogUtil.info(getClassName(), "isExcelExport ["+isExcelExport()+"]");
        return String.valueOf(value);
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
        return DebugDataListFormatter.class.getName();
    }

    @Override
    public String getPropertyOptions() {
        return "";
    }

    protected boolean isExcelExport() {
        return "2".equals(getExportCode());
    }

    protected String getExportCode() {
        HttpServletRequest request = WorkflowUtil.getHttpServletRequest();

        return Optional.ofNullable(request)
                .map(ServletRequest::getParameterMap)
                .map(Map::entrySet)
                .stream()
                .flatMap(Collection<Map.Entry<String, String[]>>::stream)
                .filter(e -> e.getKey().matches("d-\\d+-e"))
                .map(Map.Entry::getValue)
                .findFirst()
                .stream()
                .flatMap(Arrays::stream)
                .findFirst()
                .orElse("");
    }
}
