package com.kinnarastudio.kecakplugins.datalist.formatter;

import org.joget.apps.app.dao.FormDefinitionDao;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.FormDefinition;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.datalist.model.DataList;
import org.joget.apps.datalist.model.DataListColumn;
import org.joget.apps.datalist.model.DataListColumnFormatDefault;
import org.joget.apps.form.model.*;
import org.joget.apps.form.service.FileUtil;
import org.joget.apps.form.service.FormService;
import org.joget.apps.form.service.FormUtil;
import org.joget.commons.util.FileManager;
import org.joget.commons.util.LogUtil;
import org.joget.plugin.base.PluginManager;
import org.joget.plugin.base.PluginWebSupport;
import org.joget.workflow.util.WorkflowUtil;
import org.kecak.apps.exception.ApiException;
import org.springframework.context.ApplicationContext;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;
import java.util.function.Predicate;

public class ImageThumbnailsDatalistFormatter extends DataListColumnFormatDefault implements PluginWebSupport {
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

        final int width = getWidth();
        final int height = getHeight();

        dataModel.put("asLink", asLink());

        final Map<String, String>[] links = Optional.ofNullable(fieldId.isEmpty() ? value : ((Map<?, ?>) row).get(fieldId))
                .map(String::valueOf)
                .map(s -> s.split(";"))
                .stream()
                .flatMap(Arrays::stream)
                .filter(Predicate.not(String::isEmpty))
                .map(filename -> new HashMap<String, String>() {{
                    put("thumbnail", String.format("/web/json/app/%s/%d/plugin/%s/service?formDefId=%s&primaryKey=%s&filename=%s&width=%d&height=%d", appDefinition.getAppId(), appDefinition.getVersion(), getClassName(), formDefId, primaryKeyValue, filename, width, height));
                    put("fullsize", String.format("/web/client/app/%s/%d/form/download/%s/%s/%s.", appDefinition.getAppId(), appDefinition.getVersion(), formDefId, primaryKeyValue, filename));
                }})
                .toArray(Map[]::new);

        dataModel.put("links", links);

        dataModel.put("width", width);

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
        return AppUtil.readPluginResource(getClass().getName(), "/properties/ImageThumbnailsDatalistFormatter.json", new String[]{}, false, "/messages/ImageThubnailsDatalistFormatter");

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

    @Override
    public void webService(HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws ServletException, IOException {

        try {
            final String formDefId = getParameter(servletRequest, "formDefId");
            final String primaryKeyValue = getParameter(servletRequest, "primaryKey");
            final String fileName = getParameter(servletRequest, "filename");
            final int width = optParameter(servletRequest, "width")
                    .map(Integer::parseInt)
                    .orElse(50);

            final int height = optParameter(servletRequest, "height")
                    .map(Integer::parseInt)
                    .orElse(50);

            final AppDefinition appDef = Objects.requireNonNull(AppUtil.getCurrentAppDefinition());
            final ApplicationContext appContext = Objects.requireNonNull(AppUtil.getApplicationContext());
            final FormDefinitionDao formDefinitionDao = Objects.requireNonNull((FormDefinitionDao) appContext.getBean("formDefinitionDao"));
            final FormService formService = Objects.requireNonNull((FormService) appContext.getBean("formService"));
            final FormDefinition formDef = Objects.requireNonNull(formDefinitionDao.loadById(formDefId, appDef));

            final String json = formDef.getJson();
            final Form form = Objects.requireNonNull((Form) formService.createElementFromJson(json));
            final FormLoadBinder loadBinder = Objects.requireNonNull(form.getLoadBinder());
            final String tableName = form.getPropertyString(FormUtil.PROPERTY_TABLE_NAME);

            final FormData formData = new FormData();

            final FormRowSet rows = Objects.requireNonNull(loadBinder.load(form, primaryKeyValue, formData));
            final FormRow row = rows.stream().findFirst().orElseThrow(() -> new ApiException(HttpServletResponse.SC_NOT_FOUND, "Empty row"));

            for (Object fieldId : row.keySet()) {
                String compareValue = fileName;
                if (compareValue.endsWith(FileManager.THUMBNAIL_EXT)) {
                    compareValue = compareValue.replace(FileManager.THUMBNAIL_EXT, "");
                }

                String value = row.getProperty(fieldId.toString());

                if (value.equals(compareValue)
                        || (value.contains(";")
                        && (value.startsWith(compareValue + ";")
                        || value.contains(";" + compareValue + ";")
                        || value.endsWith(";" + compareValue)))
                        || (value.contains(formDefId + "/" + primaryKeyValue + "/" + compareValue))
                        || (value.contains(FileUtil.PATH_VARIABLE + compareValue))) {
                    Element field = FormUtil.findElement(fieldId.toString(), form, formData);
                    if (field instanceof FileDownloadSecurity) {
                        FileDownloadSecurity security = (FileDownloadSecurity) field;
                        if (!security.isDownloadAllowed(servletRequest.getParameterMap())) {
                            throw new ApiException(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized user [" + WorkflowUtil.getCurrentUsername() + "]");
                        }
                    }
                }
            }

            ServletOutputStream outputStream = servletResponse.getOutputStream();
            String decodedFileName = fileName;
            try {
                decodedFileName = URLDecoder.decode(fileName, "UTF8");
            } catch (UnsupportedEncodingException ignored) {
            }

            File file = FileUtil.getFile(decodedFileName, tableName, primaryKeyValue);
            if (file.isDirectory() || !file.exists()) {
                throw new ApiException(HttpServletResponse.SC_NOT_FOUND, "File not found");
            }

            String contentType = servletRequest.getSession().getServletContext().getMimeType(decodedFileName);
            if (contentType != null) {
                servletResponse.setContentType(contentType);
            }

            // set attachment filename
            String name = URLEncoder.encode(decodedFileName, "UTF8").replaceAll("\\+", "%20");
            servletResponse.setHeader("Content-Disposition", "inline; filename=" + name + "; filename*=UTF-8''" + name);

            resizeImage(outputStream, file, height, width);
//            // send output
//            try (DataInputStream in = new DataInputStream(new FileInputStream(file))) {
//                byte[] bbuf = new byte[65536];
//                int length;
//                while ((length = in.read(bbuf)) != -1) {
//                    stream.write(bbuf, 0, length);
//                }
//            }
        } catch (ApiException e) {
            LogUtil.error(getClassName(), e, e.getMessage());
            servletResponse.sendError(e.getErrorCode(), e.getMessage());
        }
    }

    protected Optional<String> optParameter(HttpServletRequest request, String name) {
        return Optional.of(name)
                .map(request::getParameter)
                .filter(Predicate.not(String::isEmpty));
    }

    protected String getParameter(HttpServletRequest request, String name) throws ApiException {
        return optParameter(request, name)
                .orElseThrow(() -> new ApiException(HttpServletResponse.SC_BAD_REQUEST, "Parameter [" + name + "] is required"));
    }

    protected void resizeImage(OutputStream out, File inputFile, int height, int width) throws IOException {
        assert out != null;
        assert inputFile != null;

        BufferedImage originalImage = ImageIO.read(inputFile);
//        int imageWidth = originalImage.getWidth(null);
//        int imageHeight = originalImage.getHeight(null);

//        LogUtil.info(getClassName(), "image size [" + imageHeight + "] [" + imageWidth + "]");

        BufferedImage thumbImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics2D = thumbImage.createGraphics();
        graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics2D.drawImage(originalImage, 0, 0, width, height, null);

        ImageIO.write(thumbImage, "jpeg", out);
    }

    protected boolean asLink() {
        return "true".equalsIgnoreCase(getPropertyString("asLink"));
    }
}
