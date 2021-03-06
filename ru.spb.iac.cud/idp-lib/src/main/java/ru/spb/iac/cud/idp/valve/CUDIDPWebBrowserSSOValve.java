package ru.spb.iac.cud.idp.valve;

import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.util.LifecycleSupport;
import org.picketlink.common.ErrorCodes;

public class CUDIDPWebBrowserSSOValve extends CUDAbstractIDPValve implements
		Lifecycle {

	// ***************Lifecycle
	/**
	 * The lifecycle event support for this component.
	 */
	protected LifecycleSupport lifecycle = new LifecycleSupport(this);

	/**
	 * Has this component been started yet?
	 */
	private boolean started = false;

	/**
	 * Add a lifecycle event listener to this component.
	 * 
	 * @param listener
	 *            The listener to add
	 */
	public void addLifecycleListener(LifecycleListener listener) {
		lifecycle.addLifecycleListener(listener);
	}

	/**
	 * Get the lifecycle listeners associated with this lifecycle. If this
	 * Lifecycle has no listeners registered, a zero-length array is returned.
	 */
	public LifecycleListener[] findLifecycleListeners() {
		return lifecycle.findLifecycleListeners();
	}

	/**
	 * Remove a lifecycle event listener from this component.
	 * 
	 * @param listener
	 *            The listener to add
	 */
	public void removeLifecycleListener(LifecycleListener listener) {
		lifecycle.removeLifecycleListener(listener);
	}

	/**
	 * Prepare for the beginning of active use of the public methods of this
	 * component. This method should be called after <code>configure()</code>,
	 * and before any of the public methods of the component are utilized.
	 * 
	 * @exception LifecycleException
	 *                if this component detects a fatal error that prevents this
	 *                component from being used
	 */
	public void start() throws LifecycleException {
		// Validate and update our current component state
		if (started)
			throw new LifecycleException(
					ErrorCodes.IDP_WEBBROWSER_VALVE_ALREADY_STARTED);
		lifecycle.fireLifecycleEvent(START_EVENT, null);
		started = true;

		startPicketLink();
	}

	/**
	 * Gracefully terminate the active use of the public methods of this
	 * component. This method should be the last one called on a given instance
	 * of this component.
	 * 
	 * @exception LifecycleException
	 *                if this component detects a fatal error that needs to be
	 *                reported
	 */
	public void stop() throws LifecycleException {
		// Validate and update our current component state
		if (!started)
			throw new LifecycleException(
					ErrorCodes.IDP_WEBBROWSER_VALVE_NOT_STARTED);
		lifecycle.fireLifecycleEvent(STOP_EVENT, null);
		started = false;
	}

	@Override
	protected String getContextPath() {
		return getContext().getServletContext().getContextPath();
	}

}
