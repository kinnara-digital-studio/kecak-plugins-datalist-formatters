/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kinnarastudio.kecakplugins.datalist.formatter;

import org.enhydra.shark.api.common.SharkConstants;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.datalist.model.DataList;
import org.joget.apps.datalist.model.DataListColumn;
import org.joget.apps.datalist.model.DataListColumnFormatDefault;
import org.joget.directory.model.User;
import org.joget.directory.model.service.ExtDirectoryManager;
import org.joget.plugin.base.PluginManager;
import org.joget.workflow.model.WorkflowActivity;
import org.joget.workflow.model.WorkflowProcess;
import org.joget.workflow.model.WorkflowProcessLink;
import org.joget.workflow.model.dao.WorkflowProcessLinkDao;
import org.joget.workflow.model.service.WorkflowManager;
import org.springframework.context.ApplicationContext;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Aristo
 * <p>
 * Get status of current assignment
 */
public class PendingAssignmentFormatter extends DataListColumnFormatDefault {

    @Override
    public String getName() {
        return getLabel() + getVersion();
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
    public String format(DataList dataList, DataListColumn column, Object row, Object value) {
        boolean onlyShowFirstAssignment = "true".equals(getPropertyString("onlyShowFirst"));
        String fieldToBeShown = getPropertyString("fieldName");
        Map<String, String> rowMap = (Map<String, String>) row;

        String primaryKey = rowMap.get(dataList.getBinder().getPrimaryKeyColumnName());

        ApplicationContext appContext = AppUtil.getApplicationContext();
        WorkflowManager wfManager = (WorkflowManager) appContext.getBean("workflowManager");
        WorkflowProcessLinkDao processLinkDao = (WorkflowProcessLinkDao) appContext.getBean("workflowProcessLinkDao");
        ExtDirectoryManager directoryManager = (ExtDirectoryManager) AppUtil.getApplicationContext().getBean("directoryManager");

        String labelCompleted = getPropertyString("labelCompleted") == null || getPropertyString("labelCompleted").isEmpty() ? "<span style=\"color:#08B70E;\">Completed</span>" : AppUtil.processHashVariable(getPropertyString("labelCompleted"), null, null, null);
        String labelWaiting = getPropertyString("labelWaiting") == null || getPropertyString("labelWaiting").isEmpty() ? "<span style=\"color:#2A0AFA;\">Waiting</span>" : AppUtil.processHashVariable(getPropertyString("labelWaiting"), null, null, null);
        String labelAborted = getPropertyString("labelAborted") == null || getPropertyString("labelAborted").isEmpty() ? "<span style=\"color:#F50505;\">Aborted</span>" : AppUtil.processHashVariable(getPropertyString("labelAborted"), null, null, null);
        final List<WorkflowProcessLink> links = Optional.of(primaryKey)
                .map(processLinkDao::getLinks)
                .stream()
                .flatMap(Collection::stream)
                .filter(Objects::nonNull)
                .filter(link -> {
                    WorkflowProcess wfProcess = wfManager.getRunningProcessById(link.getProcessId());
                    return wfProcess != null && !SharkConstants.STATE_CLOSED_ABORTED.equals(wfProcess.getState()) && !SharkConstants.STATE_CLOSED_TERMINATED.equals(wfProcess.getState());
                })

                .collect(Collectors.toList());

        if("status".equals(fieldToBeShown)) {
            return links.stream()
                    .skip(links.isEmpty() ? 0 : links.size() - 1)
                    .findFirst()
                    .map(link -> {
                        final WorkflowProcess wfParentProcess = wfManager.getRunningProcessById(link.getParentProcessId());

                        // get current state
                        final String state = Optional.ofNullable(wfParentProcess).map(WorkflowProcess::getState).orElse("");
                        // handle state
                        if (state.startsWith(SharkConstants.STATEPREFIX_CLOSED)) return labelCompleted;
                        else if (state.startsWith(SharkConstants.STATEPREFIX_OPEN)) return labelWaiting;
                        else return labelAborted;
                    })
                    .filter(s -> !s.isEmpty())
                    .orElse("");
        } else {
            return links.stream().map(link -> {
                final Stream<String> stream = wfManager.getActivityList(link.getProcessId(), 0, 1, "dateCreated", true).stream()
                        .filter(Objects::nonNull)
                        .filter(activity -> Optional.of(activity)
                                .map(WorkflowActivity::getState)
                                .map(s -> s.startsWith(SharkConstants.STATEPREFIX_OPEN))
                                .orElse(false))
                        .filter(activity -> Optional.of(activity)
                                .map(WorkflowActivity::getType)
                                .map(WorkflowActivity.TYPE_NORMAL::equals)
                                .orElse(true))
                        .map(activity -> {
                            WorkflowActivity runningActivity = wfManager.getRunningActivityInfo(activity.getId());
                            if ("pendingUsername".equals(fieldToBeShown)) {
                                return Arrays.stream(runningActivity.getAssignmentUsers())
                                        .filter(Predicate.not(String::isEmpty))
                                        .collect(Collectors.joining(","));
                            } else if ("pendingUserFullname".equals(fieldToBeShown)) {
                                return Arrays.stream(runningActivity.getAssignmentUsers())
                                        .filter(Predicate.not(String::isEmpty))
                                        .map(u -> {
                                            User user = directoryManager.getUserByUsername(u);
                                            if (user == null) {
                                                // if user not found in directory manager, show username
                                                return u;
                                            } else {
                                                return user.getFirstName() + " " + user.getLastName();
                                            }
                                        })
                                        .collect(Collectors.joining(","));
                            } else if ("activityName".equals(fieldToBeShown)) {
                                return activity.getName();
                            } else if ("activityId".equals(fieldToBeShown)) {
                                return activity.getId();
                            } else if ("processId".equalsIgnoreCase(fieldToBeShown)) {
                                return activity.getProcessId();
                            } else if ("processName".equalsIgnoreCase(fieldToBeShown)) {
                                return activity.getProcessName();
                            } else if ("processVersion".equalsIgnoreCase(fieldToBeShown)) {
                                return activity.getProcessVersion();
                            } else {
                                return "";
                            }
                        })
                        .filter(s -> !s.isEmpty())
                        .distinct();

                if (onlyShowFirstAssignment)
                    return stream.findFirst().orElse("");
                else
                    return stream.collect(Collectors.joining(","));
            })
            .distinct()
            .filter(s -> !s.isEmpty())
            .collect(Collectors.joining(","));
        }
    }

    @Override
    public String getLabel() {
        return "Pending Assignment Formatter";
    }

    @Override
    public String getClassName() {
        return this.getClass().getName();
    }

    @Override
    public String getPropertyOptions() {
        return AppUtil.readPluginResource(getClassName(), "/properties/PendingAssignmentFormatter.json", null, false, "/messages/PendingAssignmentFormatter");
    }
}
