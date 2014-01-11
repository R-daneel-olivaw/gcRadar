/*This file is part of gcRadar.

gcRadar is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by 
the Free Software Foundation version 3 of the License.

gcRadar is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with gcRadar.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.gcr.monitors;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.gcr.monitors.modules.in.impl.InputModule;
import com.gcr.monitors.modules.monitoring.impl.MonitoringModule;
import com.gcr.monitors.modules.monitoring.structs.MonitorStateEnum;
import com.gcr.monitors.modules.monitoring.structs.MonitorThreadYieldController;
import com.gcr.monitors.modules.notification.NotificationModuleInterface;
import com.gcr.structs.AbstractObjectRefrenceKey;

/**
 * This Object monitor runs on a worker thread and captures GC events on the
 * registered objects and logs using the log4j. The class uses the following
 * modules internally,
 * <ul>
 * <li>Individual object feed</li>
 * <li>Single worker threaded monitor</li>
 * <li>Log4j logger to log GC events</li>
 * </ul>
 * 
 * @param <I>
 *            the generic type is the type object that can be used to make the
 *            monitor type specific. However this is not advisable, If such use
 *            is not required then the monitor can also be defined as a raw
 *            type.
 * @author Manish Kumar
 * @since 0.1
 */
public class SimpleObjectLog4jMonitor<I> {

	private InputModule inMod;
	private MonitoringModule monitoringMod;
	private NotificationModuleInterface notificationMod;

	private MonitorStateEnum state = MonitorStateEnum.NEW;

	/**
	 * The constructor for creating the
	 * {@link SimpleObjectMonitor} object. The constructor will
	 * initialize the 3 internal modules,
	 * <ol>
	 * <li>Individual object feed</li>
	 * <li>Single worker threaded monitor</li>
	 * <li>Callback for notification on GC events</li>
	 * </ol>
	 * in the above order. It also annotates the monitor as running(not
	 * stopped).
	 */
	public SimpleObjectLog4jMonitor() {

		IndividualObjectFeed_Impl individualObjectFeed_Impl = new IndividualObjectFeed_Impl();
		this.inMod = individualObjectFeed_Impl;

		this.monitoringMod = new SingleThreadedMonitor_Impl(
				individualObjectFeed_Impl.getWatchList());
		this.notificationMod = new Log4jNotification_Impl(this.getClass()
				.getName());
	}

	/**
	 * The method will add the object to the monitoring list & start or restart
	 * the worker thread for the monitoring.
	 * 
	 * If the monitor declaration has been defined with a type parameter then
	 * the method will only accept objects that are of the type or a sub-type.
	 * 
	 * @param object
	 *            - The object to be monitored
	 * @param identifier
	 *            - The key that will be used as a key for the object to be
	 *            added for the monitoring purposes.
	 * @return <code>true</code> if the object was added successfully<br>
	 *         <code>false</code> if the object was not added as the identifier
	 *         used to add the object has already been used. {@link
	 *         stopMonitoring()} method.
	 */
	public <T extends I> boolean addObject(T object, String identifier) {

		if (!isMonitorReady()) {
			throw new UnsupportedOperationException(
					"Objects can not be added after the moter has been stopped");
		}

		if (inMod.addObject(object, identifier, null)) {
			MonitorStateEnum monitoringModuleStatus = monitoringMod
					.getMonitoringModuleStatus();

			if (monitoringModuleStatus == MonitorStateEnum.TERMINATED) {
				startMonitoring();
			}

			return true;
		} else {
			return false;
		}
	}

	/**
	 * The method will add the object to the monitoring list & start or restart
	 * the worker thread for the monitoring. Assigns an auto generated
	 * identifier to the object.
	 * 
	 * If the monitor declaration has been defined with a type parameter then
	 * the method will only accept objects that are of the type or a sub-type.
	 * 
	 * @param object
	 *            - The object to be monitored
	 * @return <code>true</code> if the object was added successfully<br>
	 *         <code>false</code> if the object was not added as the identifier
	 *         used to add the object has already been used. {@link
	 *         stopMonitoring()} method.
	 */
	public <T extends I> boolean addObject(T object) {
		if (!isMonitorReady()) {
			throw new UnsupportedOperationException(
					"Objects can not be added after the moter has been stopped");
		}

		if (inMod.addObject(object, null)) {
			MonitorStateEnum monitoringModuleStatus = monitoringMod
					.getMonitoringModuleStatus();

			if (monitoringModuleStatus == MonitorStateEnum.TERMINATED) {
				startMonitoring();
			}

			return true;
		} else {
			return false;
		}
	}

	/**
	 * This method will remove the object from monitoring
	 * 
	 * @param objectKey
	 *            - the identifier key used at the time of adding the object
	 * @return <code>true</code> if the object was removed sucessfully.<br>
	 *         <code>false</code> if the object was not removed.
	 * @throws NullPointerException
	 *             if objectKey is <code>null</code>
	 * @throws UnsupportedOperationException
	 *             if the monitoring has been explicitly stopped by calling the
	 *             {@link stopMonitoring()} method.
	 */
	public boolean removeObject(String objectKey) {

		if (!isMonitorReady()) {
			throw new UnsupportedOperationException(
					"Objects can not be removed after the moter has been stopped");
		}

		return inMod.removeObject(objectKey);
	}

	/**
	 * Trigger the start of monitoring of the objects for GC events.
	 * 
	 * @return <code>true</code> if the monitoring has been started
	 *         successfully.<br>
	 *         <code>false</code> if monitoring could not be started.
	 */
	public boolean startMonitoring() {
		notificationMod.notifyStartMonitoring();
		state = MonitorStateEnum.RUNNING;

		return monitoringMod.startMonitoring(notificationMod);
	}

	/**
	 * Trigger the stop monitoring of the objects for GC events.
	 * 
	 * @return <code>true</code> if the monitoring has been stopped
	 *         successfully.<br>
	 *         <code>false</code> if monitoring could not be stopped.
	 */
	public boolean stopMonitoring() {
		notificationMod.notifyStopMonitoring();
		state = MonitorStateEnum.HELD;

		return monitoringMod.stopMonitoring(notificationMod);
	}

	/**
	 * Gets the pending objects.
	 * 
	 * @return the pending objects
	 */
	public Set<AbstractObjectRefrenceKey<Object>> getPendingObjects() {
		return inMod.getPendingObjects();
	}

	/**
	 * Gets the pending objects count.
	 * 
	 * @return the pending objects count
	 */
	public int getPendingObjectsCount() {
		return inMod.getPendingObjectsCount();
	}

	/**
	 * This method will hold the execution of the calling thread till the time
	 * one of the following happens,
	 * <ul>
	 * <li>All the objects in the monitor are claimed by garbage collector.</li>
	 * <li>The waiting thread is interrupted by some other thread</li>
	 * <li>The monitoring is stopped by calling the
	 * {@link ObjectTreeMonitor#startMonitoring()} method</li>
	 * </ul>
	 * 
	 * @throws InterruptedException
	 *             in case the waiting thread is interrupted
	 * @throws UnsupportedOperationException
	 *             in case the monitor is not running
	 * @since 0.4
	 */
	public void lock() throws InterruptedException {

		if (!isLockable()) {
			throw new UnsupportedOperationException(
					"Cannot lock if monitor is not running");
		}

		monitoringMod.lock();
	}

	/**
	 * This method will hold the execution of the calling thread till the time
	 * one of the following happens,
	 * <ul>
	 * <li>All the objects in the monitor are claimed by garbage collector.</li>
	 * <li>The waiting thread is interrupted by some other thread</li>
	 * <li>The timeout runs-out</li>
	 * <li>The monitoring is stopped by calling the
	 * </ul>
	 * 
	 * @param time
	 *            the time
	 * @param unit
	 *            the unit
	 * @throws InterruptedException
	 *             in case the waiting thread is interrupted
	 *             {@link ObjectTreeMonitor#startMonitoring()} method</li> </ul>
	 * @since 0.4
	 */
	public void lock(long time, TimeUnit unit) throws InterruptedException {

		if (!isLockable()) {
			throw new UnsupportedOperationException(
					"Cannot lock if monitor is not running");
		}

		monitoringMod.lock(time, unit);
	}

	private boolean isLockable() {
		// I faced a decision here as to if we should allow a thread to be
		// locked if the monitor is not running. I decided not to allow this as
		// it did not seem intuitive however I could think of instances where it
		// can be used.
		if (state != MonitorStateEnum.RUNNING) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Checks if is monitor ready.
	 * 
	 * @return true, if is monitor is ready
	 */
	private boolean isMonitorReady() {
		return (state == MonitorStateEnum.RUNNING || state == MonitorStateEnum.NEW);
	}

	/**
	 * Sets the monitor thread yield controller.
	 * 
	 * @param yeildController
	 *            the new monitor thread yield controller
	 */
	public void setMonitorThreadYieldController(
			MonitorThreadYieldController yeildController) {
		monitoringMod.setMonitorThreadYieldController(yeildController);
	}

	// --------------- INNER-CLASSES ---------------------

	private class IndividualObjectFeed_Impl extends InputModule {
		@Override
		protected List<AbstractObjectRefrenceKey<Object>> getWatchList() {
			return super.getWatchList();
		}
	}

	private class SingleThreadedMonitor_Impl extends MonitoringModule {

		protected SingleThreadedMonitor_Impl(
				List<AbstractObjectRefrenceKey<Object>> keyCollection) {
			super(keyCollection);
		}
		// Full implementation in super as functionality used as is
	}

	private class Log4jNotification_Impl implements NotificationModuleInterface {

		Logger log = null;

		public Log4jNotification_Impl(String loggerName) {
			if (loggerName.isEmpty()) {
				throw new UnsupportedOperationException("Logger Name not Found");
			}
			log = Logger.getLogger(loggerName);
			if (log == null || !(log instanceof Logger)) {
				throw new UnsupportedOperationException(loggerName
						+ " : Logger not found");
			}
		}

		@Override
		public boolean notifyStartMonitoring() {
			log.info("Monitoring start..");
			return false;
		}

		@Override
		public boolean notifyStopMonitoring() {
			log.info("Monitoring end..");
			return false;
		}

		@Override
		public boolean notifyPreGcEvent(
				AbstractObjectRefrenceKey<Object> refrenceKey) {
			log.info(refrenceKey.getObjRefrenceKey()
					+ " : refrence key object is about to be garbage collection.");
			return false;
		}

		@Override
		public boolean notifyPostGcEvent(
				AbstractObjectRefrenceKey<Object> refrenceKey) {
			log.info(refrenceKey.getObjRefrenceKey()
					+ " : refrence key object is garbage collected.");
			return false;
		}
	}

}
