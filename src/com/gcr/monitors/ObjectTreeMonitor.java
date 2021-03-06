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

import com.gcr.callbacks.GcRadarCallback;
import com.gcr.monitors.modules.in.impl.TreeInputModule;
import com.gcr.monitors.modules.in.structs.annotation.GcRadarNotToInclude;
import com.gcr.monitors.modules.in.structs.annotation.GcRadarToInclude;
import com.gcr.monitors.modules.monitoring.impl.MonitoringModule;
import com.gcr.monitors.modules.monitoring.structs.MonitorStateEnum;
import com.gcr.monitors.modules.monitoring.structs.MonitorThreadYieldController;
import com.gcr.monitors.modules.notification.impl.NotificationModule;
import com.gcr.structs.AbstractObjectRefrenceKey;

/**
 * The Class ObjectTreeMonitor. This monitor should be used to add an object and
 * all it's comprising members to the GcRadar monitoring. The monitor adds all
 * qualifying fields irrespective of their access modifiers.
 * 
 * The monitor can be used in the following 2 modes by the {@code boolean}
 * parameter in the constructor.
 * <ul>
 * <li>
 * Optimistic Mode</li>
 * <li>
 * Pessimistic Mode</li>
 * </ul>
 * 
 * In the <i>Optimistic Mode</i> the monitor will add all the objects members
 * that qualify the following criteria,
 * <ul>
 * <li>Is non-Static</li>
 * <li>Has a not-null value at the time of the addition attempt</li>
 * <li>Is not a primitive type</li>
 * <li>Is not annotated using the {@link GcRadarNotToInclude} annotation.</li>
 * <li>The containing class is not annotated by the {@link GcRadarNotToInclude}
 * annotation.</li>
 * </ul>
 * 
 * In the <i>Pessimistic Mode</i> the monitor will add all the objects members
 * that qualify the following criteria,
 * <ul>
 * <li>Is non-Static</li>
 * <li>Has a not-null value at the time of the addition attempt</li>
 * <li>Is not a primitive type</li>
 * <li>Is not annotated using the {@link GcRadarNotToInclude} annotation.</li>
 * <li>The containing class is not annotated by the {@link GcRadarNotToInclude}
 * annotation.</li>
 * <li>The member is annotated by using the {@link GcRadarToInclude} annotation</li>
 * <ul>
 * 
 * @param <I>
 *            the generic type is the type object that can be used to make the
 *            monitor type specific. However this is not advisable, If such use
 *            is not required then the monitor can also be defined as a raw
 *            type.
 * @since 0.2
 * @author R.daneel.olivaw
 */
public class ObjectTreeMonitor<I> {
	private TreeInputModule_Impl treeInputMod;
	private MonitoringModule monitoringMod;
	private NotificationModule notificationMod;

	private MonitorStateEnum state = MonitorStateEnum.NEW;

	/**
	 * Instantiates a new object tree monitor.
	 * 
	 * @param isOptimistic
	 *            for choosing the operating mode of the monitor
	 */
	public ObjectTreeMonitor(boolean isOptimistic) {
		this.treeInputMod = new TreeInputModule_Impl(isOptimistic);
		this.monitoringMod = new SingleThreadedMonitor_Impl(
				((TreeInputModule_Impl) treeInputMod).getWatchList());
		this.notificationMod = new CallbackNotificationModule_Impl();
	}

	/**
	 * Adds the object along with the comprising objects and the contents of the
	 * immediate parent according to the the criteria defined by the operating
	 * mode. <br>
	 * <br>
	 * For more details please refer to {@link ObjectTreeMonitor}.
	 * 
	 * If the monitor declaration has been defined with a type parameter then
	 * the method will only accept objects that are of the type or a sub-type.
	 * 
	 * @param object
	 *            - The object to be monitored
	 * @param identifier
	 *            - The key that will be used as a key for the object to be
	 *            added for the monitoring purposes.
	 * @param callback
	 *            - {@link GcRadarCallback} is used when a GC event needs to be
	 *            reported
	 * @return <code>true</code> if the object was added successfully<br>
	 *         <code>false</code> if the object was not added as the identifier
	 *         used to add the object has already been used. {@link
	 *         stopMonitoring()} method.
	 */
	public <T extends I> boolean addObject(T object, String identifier,
			GcRadarCallback callback) {
		if (!isMonitorReady()) {
			throw new UnsupportedOperationException(
					"Objects can not be added after the moter has been stopped");
		}

		if (treeInputMod.addObject(object, identifier, callback)) {
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
	 * This operation is not supported by the monitor
	 * 
	 * @throws UnsupportedOperationException
	 */
	public <T extends I> boolean addObject(T object, GcRadarCallback callback) {
		throw new UnsupportedOperationException(
				"TreeInputModule does not support this operation. Please use addObject(I , Object , GcRadarCallback)");
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

		return treeInputMod.removeObject(objectKey);
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
	 * Gets a set of alias objects that represent one to one the objects that
	 * have not been removed from monitoring yet.
	 * 
	 * @return a set of alias objects
	 */
	public Set<AbstractObjectRefrenceKey<Object>> getPendingObjects() {
		return treeInputMod.getPendingObjects();
	}

	/**
	 * Gets the number of objects that are being monitored and are yet to be
	 * claimed by the garbage collector.
	 * 
	 * @return the pending objects count
	 */
	public int getPendingObjectsCount() {
		return treeInputMod.getPendingObjectsCount();
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

	/**
	 * Checks if monitor can perform lock.
	 * 
	 * @return true, if lockable
	 */
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

	private class TreeInputModule_Impl extends TreeInputModule {
		public TreeInputModule_Impl(boolean isOptimistic) {
			super(isOptimistic);
		}

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

	private class CallbackNotificationModule_Impl extends NotificationModule {
		// Full implementation in super as functionality used as is
	}

}
