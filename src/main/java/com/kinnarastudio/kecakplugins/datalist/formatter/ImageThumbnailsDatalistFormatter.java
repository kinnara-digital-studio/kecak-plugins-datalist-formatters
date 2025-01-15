package com.kinnarastudio.kecakplugins.datalist.formatter;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.service.AppService;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.datalist.model.DataList;
import org.joget.apps.datalist.model.DataListColumn;
import org.joget.apps.datalist.model.DataListColumnFormatDefault;
import org.joget.apps.form.model.Form;
import org.joget.plugin.base.PluginManager;
import org.springframework.context.ApplicationContext;

public class ImageThumbnailsDatalistFormatter extends DataListColumnFormatDefault{
    public final static String LABEL = "Image Thumbnails Formatter";

    @Override
    public String format(DataList dataList, DataListColumn column, Object row, Object value) {
        final AppDefinition appDefinition = AppUtil.getCurrentAppDefinition();
        final ApplicationContext applicationContext = AppUtil.getApplicationContext();
        final PluginManager pluginManager = (PluginManager) applicationContext.getBean("pluginManager");

        final Map<String, Object> dataModel = new HashMap<>();

        final String formDefId = getFormDefId();
        final String fieldId = getFieldId();

        final String primaryKeyValue = String.valueOf(((Map<?, ?>) row).get("id"));

        final String[] links = Optional.ofNullable(fieldId.isEmpty() ? value : ((Map<?, ?>) row).get(fieldId))
                .map(String::valueOf)
                .map(s -> s.split(";"))
                .stream()
                .flatMap(Arrays::stream)
                .filter(Predicate.not(String::isEmpty))
                .map(filename -> String.format("/web/client/app/%s/%d/form/download/%s/%s/%s.", appDefinition.getAppId(), appDefinition.getVersion(), formDefId, primaryKeyValue, filename))
                .toArray(String[]::new);

        dataModel.put("links", links);

        final int width = getWidth();
        dataModel.put("width", width);

        final int height = getHeight();
        dataModel.put("height", height);

        return pluginManager.getPluginFreeMarkerTemplate(dataModel, getClassName(), "/templates/ImageThumbnailsDataListFormetter.ftl", null);
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

    protected String getFieldId() {
        return getPropertyString("fieldId");
    }

    protected int getWidth() {
        try {
            return Integer.parseInt(getPropertyString("width"));
        } catch (NumberFormatException e) {
            return 50;
        }
    }

    protected int getHeight() {
        try {
            return Integer.parseInt(getPropertyString("height"));
        } catch (NumberFormatException e) {
            return 50;
        }
    }
}
