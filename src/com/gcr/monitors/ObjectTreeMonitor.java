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

import java.lang.Thread.State;
import java.util.List;
import java.util.Set;

import com.gcr.callbacks.GcRadarCallback;
import com.gcr.monitors.modules.in.impl.TreeInputModule;
import com.gcr.monitors.modules.monitoring.impl.MonitoringModule;
import com.gcr.monitors.modules.notification.impl.NotificationModule;
import com.gcr.structs.AbstractObjectRefrenceKey;
import com.gcr.structs.annotation.GcRadarNotToInclude;
import com.gcr.structs.annotation.GcRadarToInclude;

/**
 * The Class ObjectTreeMonitor. This monitor should be used to add an object and
 * all it's comprising members to the GcRadar monitoring. The monitor also adds
 * the members first level(Immediate parent) of the object type. The monitor
 * adds all qualifying fields irrespective of their access modifiers.
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
 * @since 0.2
 * @author R.daneel.olivaw
 */
public class ObjectTreeMonitor
{
    private IndividualObjectFeed_Impl inMod;
    private MonitoringModule monitoringMod;
    private NotificationModule notificationMod;

    private boolean stopFlag = false;

    /**
     * Instantiates a new object tree monitor.
     * 
     * @param isOptimistic
     *            for choosing the operating mode of the monitor
     */
    public ObjectTreeMonitor(boolean isOptimistic)
    {
	this.inMod = new IndividualObjectFeed_Impl(isOptimistic);
	this.monitoringMod = new SingleThreadedMonitor_Impl(((IndividualObjectFeed_Impl) inMod).getWatchList());
	this.notificationMod = new CallbackNotificationModule_Impl();

	stopFlag = false;
    }

    /**
     * Adds the object along with the comprising objects and the contents of the
     * immediate parent according to the the criteria defined by the operating
     * mode. <br>
     * <br>
     * For more details please refer to {@link ObjectTreeMonitor}.
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
     *         used to add the object has already been used.
     * 
     * @throws NullPointerException
     *             if the object to be added of identifier is <code>null</code>.
     * @throws UnsupportedOperationException
     *             if the monitoring has been explicitly stopped by calling the
     *             {@link stopMonitoring()} method.
     */
    public <I> boolean addObject(I object, String identifier, GcRadarCallback callback)
    {
	if (stopFlag)
	{
	    throw new UnsupportedOperationException("Objects can not be added after the moter has been stopped");
	}

	if (inMod.addObject(object, identifier, callback))
	{
	    State monitoringStatus = monitoringMod.getStatus();

	    if (monitoringStatus == State.TERMINATED)
	    {
		startMonitoring();
	    }

	    return true;
	}
	else
	{
	    return false;
	}
    }

    /**
     * This operation is not supported by the monitor
     * 
     * @throws UnsupportedOperationException
     */
    public <I> boolean addObject(I object, GcRadarCallback callback)
    {
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
    public boolean removeObject(String objectKey)
    {
	if (stopFlag)
	{
	    throw new UnsupportedOperationException("Objects can not be removed after the moter has been stopped");
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
    public boolean startMonitoring()
    {
	notificationMod.notifyStartMonitoring();
	stopFlag = false;

	return monitoringMod.startMonitoring(notificationMod);
    }

    /**
     * Trigger the stop monitoring of the objects for GC events.
     * 
     * @return <code>true</code> if the monitoring has been stopped
     *         successfully.<br>
     *         <code>false</code> if monitoring could not be stopped.
     */
    public boolean stopMonitoring()
    {
	notificationMod.notifyStopMonitoring();
	stopFlag = true;

	return monitoringMod.stopMonitoring(notificationMod);
    }

    /**
     * Gets a set of alias objects that represent one to one the objects that
     * have not been removed from monitoring yet.
     * 
     * @return a set of alias objects
     */
    public Set<AbstractObjectRefrenceKey<Object>> getPendingObjects()
    {
	return inMod.getPendingObjects();
    }

    /**
     * Gets the number of objects that are being monitored and are yet to be
     * claimed by the garbage collector.
     * 
     * @return the pending objects count
     */
    public int getPendingObjectsCount()
    {
	return inMod.getPendingObjectsCount();
    }

    private class IndividualObjectFeed_Impl extends TreeInputModule
    {
	public IndividualObjectFeed_Impl(boolean isOptimistic)
	{
	    super(isOptimistic);
	}

	@Override
	protected List<AbstractObjectRefrenceKey<Object>> getWatchList()
	{
	    return super.getWatchList();
	}
    }

    private class SingleThreadedMonitor_Impl extends MonitoringModule
    {
	protected SingleThreadedMonitor_Impl(List<AbstractObjectRefrenceKey<Object>> keyCollection)
	{
	    super(keyCollection);
	}
	// Full implementation in super as functionality used as is
    }

    private class CallbackNotificationModule_Impl extends NotificationModule
    {
	// Full implementation in super as functionality used as is
    }

}
