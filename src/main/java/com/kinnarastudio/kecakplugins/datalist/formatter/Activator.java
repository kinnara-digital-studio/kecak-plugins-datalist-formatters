package com.kinnarastudio.kecakplugins.datalist.formatter;

import java.util.ArrayList;
import java.util.Collection;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

public class Activator implements BundleActivator {

    protected Collection<ServiceRegistration> registrationList;

    public void start(BundleContext context) {
        registrationList = new ArrayList<ServiceRegistration>();

        //Register plugin here
        registrationList.add(context.registerService(CheckedUncheckedDataListFormatter.class.getName(), new CheckedUncheckedDataListFormatter(), null));
        registrationList.add(context.registerService(ImageThumbnailsDatalistFormatter.class.getName(), new ImageThumbnailsDatalistFormatter(), null));
    }

    public void stop(BundleContext context) {
        for (ServiceRegistration registration : registrationList) {
            registration.unregister();
        }
    }
}