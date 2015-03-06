package com.dalvandi.congen;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.rodinp.core.RodinCore;

public class Activator implements BundleActivator {

	private static BundleContext context;
	public static final String PLUGIN_ID = "com.dalvandi.congen";

	static BundleContext getContext() {
		return context;
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bundleContext) throws Exception {
		System.out.println("Plugin started...");
		Activator.context = bundleContext;
		setProbConfig();
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		Activator.context = null;
	}
	
	public static void setProbConfig() {
		RodinCore.addElementChangedListener(new ConfSettor());
	}


}
