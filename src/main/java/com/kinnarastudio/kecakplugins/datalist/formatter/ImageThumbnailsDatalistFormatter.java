package com.kinnarastudio.kecakplugins.datalist.formatter;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.service.AppService;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.datalist.model.DataList;
import org.joget.apps.datalist.model.DataListColumn;
import org.joget.apps.datalist.model.DataListColumnFormatDefault;
import org.joget.apps.form.model.Form;
import org.joget.plugin.base.PluginManager;

public class ImageThumbnailsDatalistFormatter extends DataListColumnFormatDefault{
    public final static String LABEL = "Image Thumbnails Formatter";

    @Override
    public String format(DataList dataList, DataListColumn column, Object row, Object value) {
        final AppDefinition appDefinition = AppUtil.getCurrentAppDefinition();
        final String formDefId = getFormDefId();
        final String primaryKeyValue = ((Map<String, String>)row).get("id");
        return Optional.of(value)
                .map(String::valueOf)
                .map(s -> s.split(";"))
                .stream()
                .flatMap(Arrays::stream)
                .map(filename -> String.format("<img src='/client/app/%s/%d/form/download/%s/%s/%s' style='width:100px;height:100px;'/>", appDefinition.getAppId(), appDefinition.getVersion(), formDefId, primaryKeyValue, filename))
                .collect(Collectors.joining(" "));
    }

    @Override
    public String getClassName() {
        return getClass().getName();
    }

    @Override
    public String getLabel() {
       return " Image Thumbnails Datalist Formatter";
    }

    @Override
    public String getPropertyOptions() {
        return AppUtil.readPluginResource(getClass().getName(), "/properties/ImageThubnailsDatalistFormatter.json", new String[] {}, false, "/messages/ImageThubnailsDatalistFormatter");
       
    }

    @Override
    public String getDescription() {
        return getClass().getPackage().getImplementationTitle();
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

    protected String getFormDefId() {
        return getPropertyString("formDefId");
    }
}
