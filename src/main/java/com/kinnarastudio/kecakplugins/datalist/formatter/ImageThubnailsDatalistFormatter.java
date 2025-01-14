package com.kinnarastudio.kecakplugins.datalist.formatter;

import java.util.ResourceBundle;

import org.joget.apps.app.service.AppUtil;
import org.joget.apps.datalist.model.DataList;
import org.joget.apps.datalist.model.DataListColumn;
import org.joget.apps.datalist.model.DataListColumnFormatDefault;
import org.joget.plugin.base.PluginManager;

public class ImageThubnailsDatalistFormatter extends DataListColumnFormatDefault{
    public final static String LABEL = "Checked / Unchecked";
    @Override
    public String format(DataList dataList, DataListColumn column, Object row, Object value) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'format'");
    }

    @Override
    public String getClassName() {
        // TODO Auto-generated method stub
        return getClass().getName();
    }

    @Override
    public String getLabel() {
        // TODO Auto-generated method stub
       return " Image Thumbnails Datalist Formatter";
    }

    @Override
    public String getPropertyOptions() {
        // TODO Auto-generated method stub
        return AppUtil.readPluginResource(getClass().getName(), "/properties/ImageThubnailsDatalistFormatter.json", new String[] {}, false, "/messages/ImageThubnailsDatalistFormatter");
       
    }

    @Override
    public String getDescription() {
        // TODO Auto-generated method stub
        return getClass().getPackage().getImplementationTitle();
    }

    @Override
    public String getName() {
        // TODO Auto-generated method stub
        return getClassName();
    }

    @Override
    public String getVersion() {
        // TODO Auto-generated method stub
        PluginManager pluginManager = (PluginManager) AppUtil.getApplicationContext().getBean("pluginManager");
        ResourceBundle resourceBundle = pluginManager.getPluginMessageBundle(getClassName(), "/messages/BuildNumber");
        String buildNumber = resourceBundle.getString("buildNumber");
        return buildNumber;
    }

}
